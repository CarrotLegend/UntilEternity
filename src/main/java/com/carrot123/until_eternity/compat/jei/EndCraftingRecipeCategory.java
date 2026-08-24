package com.carrot123.until_eternity.compat.jei;

import com.carrot123.until_eternity.item.ModItems;
import com.carrot123.until_eternity.recipe.EndCraftingIngredient;
import com.carrot123.until_eternity.recipe.EndCraftingRecipe;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.network.chat.Component;

public final class EndCraftingRecipeCategory implements IRecipeCategory<EndCraftingRecipe> {
    private final IDrawable background;
    private final IDrawable icon;

    public EndCraftingRecipeCategory(IGuiHelper helper) {
        background = helper.createBlankDrawable(144, 92);
        icon = helper.createDrawableItemLike(ModItems.END_CRAFTING_TABLE.get());
    }

    @Override public RecipeType<EndCraftingRecipe> getRecipeType() { return UntilEternityJeiPlugin.END_CRAFTING_TYPE; }
    @Override public Component getTitle() { return Component.translatable("jei.until_eternity.end_crafting"); }
    @Override public IDrawable getBackground() { return background; }
    @Override public IDrawable getIcon() { return icon; }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, EndCraftingRecipe recipe, IFocusGroup focuses) {
        for (int gridY = 0; gridY < 5; gridY++) {
            for (int gridX = 0; gridX < 5; gridX++) {
                var slot = builder.addInputSlot(1 + gridX * 18, 1 + gridY * 18).setStandardSlotBackground();
                EndCraftingIngredient ingredient = recipe.displayIngredientAt(gridX + gridY * 5);
                if (!ingredient.isEmpty()) slot.addItemStacks(java.util.List.of(ingredient.displayStacks()));
            }
        }
        builder.addOutputSlot(119, 37).setOutputSlotBackground()
                .addItemStack(recipe.result());
    }
}
