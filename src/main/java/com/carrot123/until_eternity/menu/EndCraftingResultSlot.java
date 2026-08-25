package com.carrot123.until_eternity.menu;

import com.carrot123.until_eternity.recipe.EndCraftingRecipe;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.ForgeHooks;

public final class EndCraftingResultSlot extends Slot {
    private final EndCraftingTableMenu menu;
    private final Player player;
    private int removeCount;
    private EndCraftingTableMenu.ValidatedRecipe pendingRecipe;

    public EndCraftingResultSlot(EndCraftingTableMenu menu, Player player, int x, int y) {
        super(menu.resultContainer(), 0, x, y);
        this.menu = menu;
        this.player = player;
    }

    @Override public boolean mayPlace(ItemStack stack) { return false; }
    @Override
    public boolean mayPickup(Player player) {
        pendingRecipe = menu.revalidateRecipe(getItem());
        return pendingRecipe != null && super.mayPickup(player);
    }

    public void prepareTake(ItemStack expectedOutput) {
        pendingRecipe = menu.revalidateRecipe(expectedOutput);
    }

    @Override
    public ItemStack remove(int amount) {
        if (hasItem()) {
            pendingRecipe = menu.revalidateRecipe(getItem());
            removeCount += Math.min(amount, getItem().getCount());
        }
        return pendingRecipe == null ? ItemStack.EMPTY : super.remove(amount);
    }

    @Override
    protected void onQuickCraft(ItemStack stack, int amount) {
        removeCount += amount;
        checkTakeAchievements(stack);
    }

    @Override
    protected void checkTakeAchievements(ItemStack stack) {
        if (removeCount > 0) stack.onCraftedBy(player.level(), player, removeCount);
        if (container instanceof net.minecraft.world.inventory.RecipeHolder holder) holder.awardUsedRecipes(player, java.util.List.of());
        removeCount = 0;
        ForgeHooks.setCraftingPlayer(null);
    }

    @Override
    public void onTake(Player player, ItemStack stack) {
        EndCraftingTableMenu.ValidatedRecipe validated = pendingRecipe;
        pendingRecipe = null;
        if (validated == null) return;
        ForgeHooks.setCraftingPlayer(player);
        checkTakeAchievements(stack);
        EndCraftingRecipe recipe = validated.recipe();
        var remaining = recipe.getRemainingItems(menu.craftSlots());
        for (int i = 0; i < menu.craftSlots().getContainerSize(); i++) {
            if (!recipe.participates(validated.match(), i)) continue;
            ItemStack input = menu.craftSlots().getItem(i);
            if (!input.isEmpty()) menu.craftSlots().removeItem(i, 1);
            ItemStack remainder = remaining.get(i);
            if (remainder.isEmpty()) continue;
            ItemStack current = menu.craftSlots().getItem(i);
            if (current.isEmpty()) {
                menu.craftSlots().setItem(i, remainder);
            } else if (ItemStack.isSameItemSameTags(current, remainder) && current.getCount() + remainder.getCount() <= current.getMaxStackSize()) {
                current.grow(remainder.getCount());
            } else if (!player.getInventory().add(remainder)) {
                player.drop(remainder, false);
            }
        }
        menu.slotsChanged(menu.craftSlots());
        menu.triggerCraftSuccess();
        super.onTake(player, stack);
    }
}
