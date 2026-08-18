package com.carrot123.until_eternity.block.entity;

import javax.annotation.Nullable;

import com.carrot123.until_eternity.event.ImmortalSpawnEvents;
import com.eeeab.eeeabsmobs.sever.entity.immortal.EntityImmortal;
import com.eeeab.eeeabsmobs.sever.init.EntityInit;
import com.eeeab.eeeabsmobs.sever.init.ItemInit;
import com.github.L_Ender.cataclysm.entity.effect.ScreenShake_Entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public final class ImmortalAltarBlockEntity extends BaseContainerBlockEntity {

    public static final float SHAKE_RADIUS = 20.0F;
    public static final float SHAKE_MAGNITUDE = 0.05F;

    public static final int SHAKE_DURATION = 0;
    public static final int SHAKE_FADE_DURATION =
            ImmortalAltarRitualState.DURATION_TICKS;

    private static final int OFFERING_SLOT = 0;
    private static final int SLOT_COUNT = 1;

    private static final String TAG_ACTIVATING = "Activating";
    private static final String TAG_ACTIVATION_TICKS = "ActivationTicks";

    private NonNullList<ItemStack> items =
            NonNullList.withSize(SLOT_COUNT, ItemStack.EMPTY);

    private final ImmortalAltarRitualState ritual =
            new ImmortalAltarRitualState();

    private int renderTicks;

    public ImmortalAltarBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.IMMORTAL_ALTAR.get(), pos, state);
    }

    public boolean tryInsert(Player player, ItemStack heldStack) {
        if (!(level instanceof ServerLevel serverLevel)
                || ritual.isActivating()
                || !getItem(OFFERING_SLOT).isEmpty()
                || !heldStack.is(ItemInit.IMMORTAL_BONE.get())) {
            return false;
        }

        EntityImmortal probe = createBoss(serverLevel);

        if (probe == null || !hasSpawnSpace(serverLevel, probe)) {
            return false;
        }

        ItemStack offering = heldStack.copy();
        offering.setCount(1);
        items.set(OFFERING_SLOT, offering);

        if (!player.getAbilities().instabuild) {
            heldStack.shrink(1);
        }

        startRitual(serverLevel);
        return true;
    }

    public boolean tryRestart() {
        if (!(level instanceof ServerLevel serverLevel)
                || ritual.isActivating()
                || !getItem(OFFERING_SLOT).is(ItemInit.IMMORTAL_BONE.get())) {
            return false;
        }

        EntityImmortal probe = createBoss(serverLevel);

        if (probe == null || !hasSpawnSpace(serverLevel, probe)) {
            return false;
        }

        startRitual(serverLevel);
        return true;
    }

    public boolean tryTake(Player player) {
        if (!(level instanceof ServerLevel)
                || ritual.isActivating()
                || getItem(OFFERING_SLOT).isEmpty()) {
            return false;
        }

        ItemStack offering =
                items.set(OFFERING_SLOT, ItemStack.EMPTY);

        player.getInventory().placeItemBackInInventory(offering);

        sync();
        return true;
    }

    private void startRitual(ServerLevel serverLevel) {
        if (!ritual.start()) {
            return;
        }

        ScreenShake_Entity.ScreenShake(
                serverLevel,
                Vec3.atCenterOf(worldPosition),
                SHAKE_RADIUS,
                SHAKE_MAGNITUDE,
                SHAKE_DURATION,
                SHAKE_FADE_DURATION
        );

        sync();
    }

    public static void commonTick(
            Level level,
            BlockPos pos,
            BlockState state,
            ImmortalAltarBlockEntity altar) {

        altar.renderTicks++;

        if (!altar.ritual.isActivating()) {
            return;
        }

        if (!altar.getItem(OFFERING_SLOT)
                .is(ItemInit.IMMORTAL_BONE.get())) {

            altar.ritual.stop();

            if (!level.isClientSide) {
                altar.sync();
            }

            return;
        }

        if (!altar.ritual.advanceAndShouldComplete()
                || !(level instanceof ServerLevel serverLevel)) {
            return;
        }

        altar.finishRitual(serverLevel);
    }

    private void finishRitual(ServerLevel serverLevel) {
        EntityImmortal immortal = createBoss(serverLevel);

        boolean spawned = false;

        if (immortal != null
                && hasSpawnSpace(serverLevel, immortal)) {

            immortal.getPersistentData().putBoolean(
                    ImmortalSpawnEvents.ALTAR_SUMMONED_TAG,
                    true
            );

            spawned = serverLevel.addFreshEntity(immortal);
        }

        ritual.stop();

        if (spawned) {
            items.set(OFFERING_SLOT, ItemStack.EMPTY);
        }

        sync();
    }

    @Nullable
    private EntityImmortal createBoss(ServerLevel serverLevel) {
        EntityImmortal immortal =
                EntityInit.IMMORTAL_BOSS.get().create(serverLevel);

        if (immortal == null) {
            return null;
        }

        immortal.moveTo(
                worldPosition.getX() + 0.5D,
                worldPosition.getY() + 1.0D,
                worldPosition.getZ() + 0.5D,
                serverLevel.random.nextFloat() * 360.0F,
                0.0F
        );

        return immortal;
    }

    private static boolean hasSpawnSpace(
            ServerLevel serverLevel,
            EntityImmortal immortal) {

        return serverLevel.getWorldBorder()
                .isWithinBounds(immortal.getBoundingBox())
                && serverLevel.noCollision(
                        immortal,
                        immortal.getBoundingBox()
                );
    }

    public boolean isActivating() {
        return ritual.isActivating();
    }

    public int getActivationTicks() {
        return ritual.activationTicks();
    }

    public int getRenderTicks() {
        return renderTicks;
    }

    @Override
    public int getContainerSize() {
        return SLOT_COUNT;
    }

    @Override
    public boolean isEmpty() {
        return items.get(OFFERING_SLOT).isEmpty();
    }

    @Override
    public ItemStack getItem(int slot) {
        return items.get(slot);
    }

    @Override
    public ItemStack removeItem(int slot, int amount) {
        if (ritual.isActivating()) {
            return ItemStack.EMPTY;
        }

        ItemStack removed =
                ContainerHelper.removeItem(items, slot, amount);

        if (!removed.isEmpty()) {
            sync();
        }

        return removed;
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        if (ritual.isActivating()) {
            return ItemStack.EMPTY;
        }

        return ContainerHelper.takeItem(items, slot);
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        if (!stack.isEmpty()) {
            stack.setCount(
                    Math.min(getMaxStackSize(), stack.getCount())
            );
        }

        items.set(slot, stack);
        sync();
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        return slot == OFFERING_SLOT
                && !ritual.isActivating()
                && items.get(OFFERING_SLOT).isEmpty()
                && stack.is(ItemInit.IMMORTAL_BONE.get());
    }

    @Override
    public boolean canTakeItem(
            Container target,
            int slot,
            ItemStack stack) {

        return !ritual.isActivating();
    }

    @Override
    public int getMaxStackSize() {
        return 1;
    }

    @Override
    public boolean stillValid(Player player) {
        return Container.stillValidBlockEntity(this, player);
    }

    @Override
    public void clearContent() {
        items.set(OFFERING_SLOT, ItemStack.EMPTY);
        ritual.stop();
        sync();
    }

    @Override
    protected Component getDefaultName() {
        return Component.translatable(
                "block.until_eternity.immortal_altar"
        );
    }

    @Nullable
    @Override
    protected AbstractContainerMenu createMenu(
            int id,
            Inventory inventory) {

        return null;
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);

        ContainerHelper.saveAllItems(tag, items);

        tag.putBoolean(
                TAG_ACTIVATING,
                ritual.isActivating()
        );

        tag.putInt(
                TAG_ACTIVATION_TICKS,
                ritual.activationTicks()
        );
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);

        items = NonNullList.withSize(
                SLOT_COUNT,
                ItemStack.EMPTY
        );

        ContainerHelper.loadAllItems(tag, items);

        boolean activating =
                tag.getBoolean(TAG_ACTIVATING);

        ritual.load(
                activating,
                tag.getInt(TAG_ACTIVATION_TICKS)
        );

        ItemStack offering =
                items.get(OFFERING_SLOT);

        if (activating && offering.isEmpty()) {
            ritual.stop();
            return;
        }

        if (!offering.isEmpty()) {
            if (!offering.is(ItemInit.IMMORTAL_BONE.get())) {
                items.set(
                        OFFERING_SLOT,
                        ItemStack.EMPTY
                );

                ritual.stop();
            } else {
                offering.setCount(1);
            }
        }
    }

    @Override
    public CompoundTag getUpdateTag() {
        return saveWithoutMetadata();
    }

    @Override
    public void handleUpdateTag(CompoundTag tag) {
        load(tag);
    }

    @Nullable
    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void onDataPacket(
            Connection connection,
            ClientboundBlockEntityDataPacket packet) {

        CompoundTag tag = packet.getTag();

        if (tag != null) {
            handleUpdateTag(tag);
        }
    }

    private void sync() {
        setChanged();

        if (level != null) {
            BlockState state = getBlockState();

            level.sendBlockUpdated(
                    worldPosition,
                    state,
                    state,
                    Block.UPDATE_CLIENTS
            );
        }
    }
}