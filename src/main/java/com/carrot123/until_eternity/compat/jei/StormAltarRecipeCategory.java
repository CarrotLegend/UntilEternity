package com.carrot123.until_eternity.compat.jei;

import com.carrot123.until_eternity.item.ModItems;
import com.carrot123.until_eternity.recipe.StormAltarRecipe;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.network.chat.Component;

import java.util.List;

public final class StormAltarRecipeCategory implements IRecipeCategory<StormAltarRecipe> {
    private final IDrawable background;
    private final IDrawable icon;

    public StormAltarRecipeCategory(IGuiHelper helper) {
        background = helper.createBlankDrawable(108, 40);
        icon = helper.createDrawableItemLike(ModItems.STORM_ALTAR.get());
    }
    @Override public RecipeType<StormAltarRecipe> getRecipeType() { return UntilEternityJeiPlugin.STORM_ALTAR_TYPE; }
    @Override public Component getTitle() { return Component.translatable("jei.until_eternity.storm_altar"); }
    @Override public IDrawable getBackground() { return background; }
    @Override public IDrawable getIcon() { return icon; }
    @Override public void setRecipe(IRecipeLayoutBuilder builder, StormAltarRecipe recipe, IFocusGroup focuses) {
        builder.addInputSlot(4, 11).setStandardSlotBackground().addItemStacks(List.of(recipe.inputA().displayStacks()));
        builder.addInputSlot(35, 11).setStandardSlotBackground().addItemStacks(List.of(recipe.inputB().displayStacks()));
        builder.addOutputSlot(85, 11).setOutputSlotBackground().addItemStack(recipe.result());
    }
}
