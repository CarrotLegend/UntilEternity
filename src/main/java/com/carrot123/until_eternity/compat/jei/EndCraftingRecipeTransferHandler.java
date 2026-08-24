package com.carrot123.until_eternity.compat.jei;

import com.carrot123.until_eternity.menu.EndCraftingTableMenu;
import com.carrot123.until_eternity.menu.EndCraftingTransferPlanner;
import com.carrot123.until_eternity.menu.ModMenuTypes;
import com.carrot123.until_eternity.recipe.EndCraftingRecipe;
import mezz.jei.api.gui.ingredient.IRecipeSlotView;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.transfer.IRecipeTransferError;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandler;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandlerHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;

import java.util.List;
import java.util.Optional;

public final class EndCraftingRecipeTransferHandler implements IRecipeTransferHandler<EndCraftingTableMenu, EndCraftingRecipe> {
    private final IRecipeTransferHandlerHelper helper;

    public EndCraftingRecipeTransferHandler(IRecipeTransferHandlerHelper helper) {
        this.helper = helper;
    }

    @Override public Class<? extends EndCraftingTableMenu> getContainerClass() { return EndCraftingTableMenu.class; }
    @Override public Optional<MenuType<EndCraftingTableMenu>> getMenuType() { return Optional.of(ModMenuTypes.END_CRAFTING_TABLE.get()); }
    @Override public RecipeType<EndCraftingRecipe> getRecipeType() { return UntilEternityJeiPlugin.END_CRAFTING_TYPE; }

    @Override
    public IRecipeTransferError transferRecipe(EndCraftingTableMenu menu, EndCraftingRecipe recipe,
                                               IRecipeSlotsView slotsView, Player player,
                                               boolean maxTransfer, boolean doTransfer) {
        EndCraftingTransferPlanner.Plan plan = menu.createTransferPlan(recipe, maxTransfer);
        if (plan.error() == EndCraftingTransferPlanner.Error.MISSING_ITEMS) {
            List<IRecipeSlotView> inputs = slotsView.getSlotViews(RecipeIngredientRole.INPUT);
            List<IRecipeSlotView> missing = plan.missingGridSlots().stream()
                    .filter(index -> index >= 0 && index < inputs.size())
                    .map(inputs::get)
                    .toList();
            return helper.createUserErrorForMissingSlots(
                    Component.translatable("jei.tooltip.error.recipe.transfer.missing"), missing);
        }
        if (plan.error() == EndCraftingTransferPlanner.Error.INVENTORY_FULL) {
            return helper.createUserErrorWithTooltip(Component.translatable(
                    "jei.tooltip.error.recipe.transfer.inventory.full"));
        }
        if (!doTransfer) return null;

        int buttonId = menu.recipeTransferButtonId(recipe, maxTransfer);
        Minecraft minecraft = Minecraft.getInstance();
        if (buttonId < 0 || minecraft.gameMode == null) return helper.createInternalError();
        minecraft.gameMode.handleInventoryButtonClick(menu.containerId, buttonId);
        return null;
    }
}
