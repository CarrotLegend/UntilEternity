package com.carrot123.until_eternity.block.entity;

import com.carrot123.until_eternity.recipe.ModRecipeTypes;
import com.carrot123.until_eternity.recipe.StormAltarRecipe;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.Containers;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public final class StormAltarBlockEntity extends BlockEntity {
    private ItemStack inputA = ItemStack.EMPTY;
    private ItemStack inputB = ItemStack.EMPTY;
    private ItemStack output = ItemStack.EMPTY;
    private UUID owner;
    private net.minecraft.resources.ResourceLocation recipeId;
    private int progress;
    private int duration;
    private int outputTicks;
    private boolean invalidRecipe;

    public StormAltarBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.STORM_ALTAR.get(), pos, state);
    }

    public ItemStack inputA() { return inputA; }
    public ItemStack inputB() { return inputB; }
    public ItemStack output() { return output; }
    public int progress() { return progress; }
    public int duration() { return duration; }
    public int outputTicks() { return outputTicks; }
    public boolean isFusing() { return recipeId != null && !invalidRecipe && !inputB.isEmpty(); }
    @Override public net.minecraft.world.phys.AABB getRenderBoundingBox() {
        return new net.minecraft.world.phys.AABB(worldPosition).inflate(1.0, 1.0, 1.0);
    }

    public InteractionResult interact(Player player, InteractionHand hand) {
        if (!(level instanceof ServerLevel server)) return InteractionResult.PASS;
        ItemStack held = player.getItemInHand(hand);
        if (!output.isEmpty()) {
            if (!isOwner(player)) return message(player, "owner_only");
            if (outputTicks < 20) return message(player, "settling");
            if (!deliver(player)) return message(player, "inventory_full");
            return message(player, "collected");
        }
        if (player.isShiftKeyDown()) {
            if (inputA.isEmpty() && inputB.isEmpty()) return InteractionResult.PASS;
            if (!isOwner(player)) return message(player, "owner_only");
            if (isFusing()) return message(player, "locked");
            giveOrDrop(player, inputA); inputA = ItemStack.EMPTY;
            giveOrDrop(player, inputB); inputB = ItemStack.EMPTY;
            owner = null; recipeId = null; progress = 0; duration = 0; invalidRecipe = false;
            sync();
            return message(player, "retrieved");
        }
        if (isFusing()) return message(player, "locked");
        if (held.isEmpty()) return InteractionResult.PASS;
        List<StormAltarRecipe> recipes = server.getRecipeManager().getAllRecipesFor(ModRecipeTypes.STORM_ALTAR.get());
        if (inputA.isEmpty()) {
            Optional<StormAltarRecipe> found = recipes.stream().filter(recipe -> recipe.acceptsFirst(held)).findFirst();
            if (found.isEmpty()) return message(player, "wrong_material");
            int count = found.get().inputA().count();
            inputA = consume(held, count);
            owner = player.getUUID();
            sync();
            return message(player, "inserted_a");
        }
        if (!isOwner(player)) return message(player, "owner_only");
        if (!inputB.isEmpty()) return message(player, "invalid_recipe");
        Optional<StormAltarRecipe> found = recipes.stream()
                .filter(recipe -> recipe.inputA().matches(inputA) && recipe.inputB().matches(held)).findFirst();
        if (found.isEmpty()) return message(player, "wrong_material");
        StormAltarRecipe recipe = found.get();
        inputB = consume(held, recipe.inputB().count());
        recipeId = recipe.getId();
        duration = recipe.duration();
        progress = 0;
        invalidRecipe = false;
        sync();
        return message(player, "started");
    }

    private boolean isOwner(Player player) { return owner != null && owner.equals(player.getUUID()); }
    private static ItemStack consume(ItemStack held, int count) {
        ItemStack taken = held.copy(); taken.setCount(count); held.shrink(count); return taken;
    }
    private static void giveOrDrop(Player player, ItemStack stack) {
        if (stack.isEmpty()) return;
        ItemStack remaining = stack.copy();
        player.getInventory().add(remaining);
        if (!remaining.isEmpty()) player.drop(remaining, false);
    }
    private InteractionResult message(Player player, String key) {
        player.displayClientMessage(Component.translatable("message.until_eternity.storm_altar." + key), true);
        return InteractionResult.CONSUME;
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, StormAltarBlockEntity altar) {
        if (!(level instanceof ServerLevel server)) return;
        if (altar.recipeId != null && !altar.invalidRecipe) {
            Optional<StormAltarRecipe> found = server.getRecipeManager()
                    .byKey(altar.recipeId).filter(recipe -> recipe instanceof StormAltarRecipe)
                    .map(recipe -> (StormAltarRecipe) recipe);
            if (found.isEmpty() || !found.get().matches(new SimpleContainer(altar.inputA, altar.inputB), server)) {
                altar.invalidRecipe = true;
                altar.recipeId = null;
                altar.sync();
                return;
            }
            altar.progress++;
            if (altar.progress >= altar.duration) {
                altar.output = found.get().craft(altar.inputA);
                altar.inputA = ItemStack.EMPTY;
                altar.inputB = ItemStack.EMPTY;
                altar.recipeId = null;
                altar.progress = 0;
                altar.outputTicks = 0;
                altar.sync();
                server.playSound(null, pos, SoundEvents.TRIDENT_THUNDER, SoundSource.BLOCKS, 0.7f, 1.3f);
                server.sendParticles(ParticleTypes.ELECTRIC_SPARK, pos.getX() + 0.5, pos.getY() + 1.2,
                        pos.getZ() + 0.5, 12, 0.25, 0.2, 0.25, 0.06);
            } else if (altar.progress % 5 == 0) altar.sync();
        } else if (!altar.output.isEmpty()) {
            altar.outputTicks++;
            if (altar.outputTicks == 20) altar.sync();
            if (altar.outputTicks >= 20 && altar.owner != null && altar.outputTicks % 10 == 0) {
                ServerPlayer player = server.getServer().getPlayerList().getPlayer(altar.owner);
                if (player != null && player.level() == server && player.distanceToSqr(pos.getX() + 0.5,
                        pos.getY() + 0.5, pos.getZ() + 0.5) <= 36) altar.deliver(player);
            }
        }
    }

    private boolean deliver(Player player) {
        ItemStack remainder = output.copy();
        if (!canFit(player, remainder)) return false;
        player.getInventory().add(remainder);
        if (!remainder.isEmpty()) return false;
        output = ItemStack.EMPTY;
        owner = null;
        outputTicks = 0;
        sync();
        return true;
    }

    private static boolean canFit(Player player, ItemStack stack) {
        int available = 0;
        for (int i = 0; i < player.getInventory().items.size(); i++) {
            ItemStack slot = player.getInventory().items.get(i);
            if (slot.isEmpty()) available += stack.getMaxStackSize();
            else if (ItemStack.isSameItemSameTags(slot, stack)) available += Math.max(0, slot.getMaxStackSize() - slot.getCount());
            if (available >= stack.getCount()) return true;
        }
        return false;
    }

    public void dropContents() {
        if (level == null || level.isClientSide) return;
        for (ItemStack stack : new ItemStack[] {inputA, inputB, output}) {
            if (!stack.isEmpty()) Containers.dropItemStack(level, worldPosition.getX() + 0.5,
                    worldPosition.getY() + 0.5, worldPosition.getZ() + 0.5, stack);
        }
        inputA = ItemStack.EMPTY; inputB = ItemStack.EMPTY; output = ItemStack.EMPTY;
        recipeId = null; owner = null; setChanged();
    }

    private void sync() {
        setChanged();
        if (level != null) level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
    }

    @Override protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put("InputA", inputA.save(new CompoundTag()));
        tag.put("InputB", inputB.save(new CompoundTag()));
        tag.put("Output", output.save(new CompoundTag()));
        if (owner != null) tag.putUUID("Owner", owner);
        if (recipeId != null) tag.putString("Recipe", recipeId.toString());
        tag.putInt("Progress", progress); tag.putInt("Duration", duration);
        tag.putInt("OutputTicks", outputTicks); tag.putBoolean("InvalidRecipe", invalidRecipe);
    }

    @Override public void load(CompoundTag tag) {
        super.load(tag);
        inputA = ItemStack.of(tag.getCompound("InputA"));
        inputB = ItemStack.of(tag.getCompound("InputB"));
        output = ItemStack.of(tag.getCompound("Output"));
        owner = tag.hasUUID("Owner") ? tag.getUUID("Owner") : null;
        recipeId = tag.contains("Recipe") ? net.minecraft.resources.ResourceLocation.tryParse(tag.getString("Recipe")) : null;
        progress = tag.getInt("Progress"); duration = tag.getInt("Duration");
        outputTicks = tag.getInt("OutputTicks"); invalidRecipe = tag.getBoolean("InvalidRecipe");
    }

    @Override public CompoundTag getUpdateTag() { return saveWithoutMetadata(); }
    @Nullable @Override public ClientboundBlockEntityDataPacket getUpdatePacket() { return ClientboundBlockEntityDataPacket.create(this); }
    @Override public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket packet) {
        if (packet.getTag() != null) load(packet.getTag());
    }
}
