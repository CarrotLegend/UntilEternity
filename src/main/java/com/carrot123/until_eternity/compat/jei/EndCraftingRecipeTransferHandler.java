package com.carrot123.until_eternity.compat.jei;

import com.carrot123.until_eternity.menu.EndCraftingTableMenu;
import com.carrot123.until_eternity.menu.ModMenuTypes;
import com.carrot123.until_eternity.recipe.EndCraftingIngredient;
import com.carrot123.until_eternity.recipe.EndCraftingRecipe;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.transfer.IRecipeTransferError;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandler;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandlerHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;

import java.util.Optional;

public final class EndCraftingRecipeTransferHandler implements IRecipeTransferHandler<EndCraftingTableMenu, EndCraftingRecipe> {
    private final IRecipeTransferHandlerHelper helper;
    private final IRecipeTransferHandler<EndCraftingTableMenu, EndCraftingRecipe> delegate;

    public EndCraftingRecipeTransferHandler(IRecipeTransferHandlerHelper helper) {
        this.helper = helper;
        this.delegate = helper.createUnregisteredRecipeTransferHandler(helper.createBasicRecipeTransferInfo(
                EndCraftingTableMenu.class, ModMenuTypes.END_CRAFTING_TABLE.get(),
                UntilEternityJeiPlugin.END_CRAFTING_TYPE, 1, 25, 26, 36));
    }

    @Override public Class<? extends EndCraftingTableMenu> getContainerClass() { return EndCraftingTableMenu.class; }
    @Override public Optional<MenuType<EndCraftingTableMenu>> getMenuType() { return Optional.of(ModMenuTypes.END_CRAFTING_TABLE.get()); }
    @Override public RecipeType<EndCraftingRecipe> getRecipeType() { return UntilEternityJeiPlugin.END_CRAFTING_TYPE; }

    @Override
    public IRecipeTransferError transferRecipe(EndCraftingTableMenu menu, EndCraftingRecipe recipe,
                                               IRecipeSlotsView slotsView, Player player,
                                               boolean maxTransfer, boolean doTransfer) {
        for (EndCraftingIngredient ingredient : recipe.endIngredients()) {
            if (ingredient.isEmpty() || ingredient.requiredNbt() == null) continue;
            for (int slotIndex = 1; slotIndex < EndCraftingTableMenu.PLAYER_END; slotIndex++) {
                ItemStack stack = menu.getSlot(slotIndex).getItem();
                if (!stack.isEmpty() && ingredient.ingredient().test(stack) && !ingredient.test(stack)) {
                    return helper.createUserErrorWithTooltip(Component.translatable(
                            "jei.until_eternity.end_crafting.nbt_transfer_conflict"));
                }
            }
        }
        return delegate.transferRecipe(menu, recipe, slotsView, player, maxTransfer, doTransfer);
    }
}
