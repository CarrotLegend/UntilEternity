package com.carrot123.until_eternity.menu;

import com.carrot123.until_eternity.block.ModBlocks;
import com.carrot123.until_eternity.recipe.EndCraftingRecipe;
import com.carrot123.until_eternity.recipe.ModRecipeTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.ResultContainer;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.inventory.TransientCraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.Comparator;
import java.util.List;

public final class EndCraftingTableMenu extends AbstractContainerMenu {
    public static final int RESULT_SLOT = 0;
    public static final int INPUT_START = 1;
    public static final int INPUT_END = 26;
    public static final int PLAYER_INVENTORY_START = 26;
    public static final int PLAYER_INVENTORY_END = 53;
    public static final int HOTBAR_START = 53;
    public static final int HOTBAR_END = 62;
    public static final int PLAYER_START = PLAYER_INVENTORY_START;
    public static final int PLAYER_END = HOTBAR_END;
    public static final int INPUT_COUNT = INPUT_END - INPUT_START;
    public static final int PLAYER_SLOT_COUNT = PLAYER_END - PLAYER_START;

    private final TransientCraftingContainer craftSlots = new TransientCraftingContainer(this, 5, 5);
    private final ResultContainer resultSlots = new ResultContainer();
    private final ContainerLevelAccess access;
    private final Player player;

    public EndCraftingTableMenu(int containerId, Inventory inventory, BlockPos pos) {
        this(containerId, inventory, ContainerLevelAccess.create(inventory.player.level(), pos));
    }

    private EndCraftingTableMenu(int containerId, Inventory inventory, ContainerLevelAccess access) {
        super(ModMenuTypes.END_CRAFTING_TABLE.get(), containerId);
        this.access = access;
        this.player = inventory.player;
        addSlot(new EndCraftingResultSlot(this, inventory.player, 160, 53));
        for (int row = 0; row < 5; row++) for (int column = 0; column < 5; column++)
            addSlot(new Slot(craftSlots, column + row * 5, 30 + column * 18, 17 + row * 18));
        for (int row = 0; row < 3; row++) for (int column = 0; column < 9; column++)
            addSlot(new Slot(inventory, column + row * 9 + 9, 26 + column * 18, 120 + row * 18));
        for (int column = 0; column < 9; column++)
            addSlot(new Slot(inventory, column, 26 + column * 18, 178));
    }

    public TransientCraftingContainer craftSlots() { return craftSlots; }
    public ResultContainer resultContainer() { return resultSlots; }
    public void triggerCraftSuccess() {
        if (player.level().isClientSide) return;
        access.execute((level, pos) -> level.playSound(
                null,
                pos,
                SoundEvents.ENCHANTMENT_TABLE_USE,
                SoundSource.BLOCKS,
                1.0F,
                1.0F));
    }

    public EndCraftingTransferPlanner.Plan createTransferPlan(EndCraftingRecipe recipe, boolean maxTransfer) {
        List<ItemStack> input = slots.subList(INPUT_START, INPUT_END).stream()
                .map(slot -> slot.getItem().copy()).toList();
        List<ItemStack> inventory = slots.subList(PLAYER_START, PLAYER_END).stream()
                .map(slot -> slot.getItem().copy()).toList();
        return EndCraftingTransferPlanner.plan(recipe, input, inventory, maxTransfer);
    }

    public int recipeTransferButtonId(EndCraftingRecipe recipe, boolean maxTransfer) {
        List<EndCraftingRecipe> recipes = sortedTransferRecipes();
        for (int ordinal = 0; ordinal < recipes.size(); ordinal++) {
            if (recipes.get(ordinal).getId().equals(recipe.getId())) {
                return ordinal << 1 | (maxTransfer ? 1 : 0);
            }
        }
        return -1;
    }

    @Override
    public boolean clickMenuButton(Player player, int buttonId) {
        if (buttonId < 0) return false;
        int ordinal = buttonId >>> 1;
        List<EndCraftingRecipe> recipes = sortedTransferRecipes();
        if (ordinal >= recipes.size()) return false;
        if (player.level().isClientSide) return true;

        EndCraftingTransferPlanner.Plan plan = createTransferPlan(recipes.get(ordinal), (buttonId & 1) != 0);
        if (!plan.success()) {
            player.displayClientMessage(net.minecraft.network.chat.Component.translatable(
                    plan.error() == EndCraftingTransferPlanner.Error.INVENTORY_FULL
                            ? "jei.tooltip.error.recipe.transfer.inventory.full"
                            : "jei.tooltip.error.recipe.transfer.missing"), true);
            return false;
        }

        for (int index = INPUT_START; index < INPUT_END; index++) {
            slots.get(index).setByPlayer(ItemStack.EMPTY);
        }
        for (int index = PLAYER_START; index < PLAYER_END; index++) {
            slots.get(index).setByPlayer(plan.playerInventory().get(index - PLAYER_START).copy());
        }
        for (int index = INPUT_START; index < INPUT_END; index++) {
            slots.get(index).setByPlayer(plan.input().get(index - INPUT_START).copy());
        }
        broadcastChanges();
        return true;
    }

    private List<EndCraftingRecipe> sortedTransferRecipes() {
        return player.level().getRecipeManager().getAllRecipesFor(ModRecipeTypes.END_CRAFTING.get()).stream()
                .sorted(Comparator.comparing(recipe -> recipe.getId().toString()))
                .toList();
    }

    @Override
    public void slotsChanged(Container container) {
        super.slotsChanged(container);
        if (!player.level().isClientSide && container == craftSlots) updateResult();
    }

    private void updateResult() {
        ValidatedRecipe validated = findRecipe();
        ItemStack output = ItemStack.EMPTY;
        if (validated != null) {
            output = validated.recipe().assemble(craftSlots, player.level().registryAccess());
            resultSlots.setRecipeUsed(validated.recipe());
        } else {
            resultSlots.setRecipeUsed(null);
        }
        resultSlots.setItem(0, output);
        if (player instanceof ServerPlayer serverPlayer) {
            serverPlayer.connection.send(new net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket(
                    containerId, incrementStateId(), RESULT_SLOT, output));
        }
    }

    @Nullable
    private ValidatedRecipe findRecipe() {
        Level level = player.level();
        return level.getRecipeManager().getRecipeFor(ModRecipeTypes.END_CRAFTING.get(), craftSlots, level)
                .map(holder -> {
                    EndCraftingRecipe.Match match = holder.findMatch(craftSlots);
                    return match == null ? null : new ValidatedRecipe(holder, match);
                }).orElse(null);
    }

    @Nullable
    public ValidatedRecipe revalidateRecipe(ItemStack expectedOutput) {
        ValidatedRecipe validated = findRecipe();
        if (validated == null) return null;
        ItemStack expected = validated.recipe().assemble(craftSlots, player.level().registryAccess());
        return ItemStack.isSameItemSameTags(expected, expectedOutput)
                && expected.getCount() == expectedOutput.getCount() ? validated : null;
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(access, player, ModBlocks.END_CRAFTING_TABLE.get());
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = slots.get(index);
        if (!slot.hasItem()) return ItemStack.EMPTY;
        ItemStack source = slot.getItem();
        ItemStack copy = source.copy();
        if (index == RESULT_SLOT) {
            ((EndCraftingResultSlot) slot).prepareTake(copy);
            if (!moveItemStackTo(source, PLAYER_START, PLAYER_END, true)) return ItemStack.EMPTY;
            slot.onQuickCraft(source, copy);
        } else if (index >= INPUT_START && index < INPUT_END) {
            if (!moveItemStackTo(source, PLAYER_START, PLAYER_END, false)) return ItemStack.EMPTY;
        } else if (!moveItemStackTo(source, INPUT_START, INPUT_END, false)) {
            if (index < HOTBAR_START) {
                if (!moveItemStackTo(source, HOTBAR_START, HOTBAR_END, false)) return ItemStack.EMPTY;
            } else if (!moveItemStackTo(source, PLAYER_INVENTORY_START, PLAYER_INVENTORY_END, false)) return ItemStack.EMPTY;
        }
        if (source.isEmpty()) slot.setByPlayer(ItemStack.EMPTY); else slot.setChanged();
        if (source.getCount() == copy.getCount()) return ItemStack.EMPTY;
        slot.onTake(player, source);
        return copy;
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        access.execute((level, pos) -> clearContainer(player, craftSlots));
    }

    public record ValidatedRecipe(EndCraftingRecipe recipe, EndCraftingRecipe.Match match) { }
}
