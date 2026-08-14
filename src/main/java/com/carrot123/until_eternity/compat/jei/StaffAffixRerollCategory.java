package com.carrot123.until_eternity.compat.jei;

import com.carrot123.until_eternity.item.ModItems;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.network.chat.Component;

public final class StaffAffixRerollCategory
        implements IRecipeCategory<StaffAffixRerollJeiRecipe> {
    private final IDrawable background;
    private final IDrawable icon;

    public StaffAffixRerollCategory(IGuiHelper guiHelper) {
        this.background = guiHelper.createBlankDrawable(116, 36);
        this.icon = guiHelper.createDrawableItemLike(
                ModItems.SPAWNER_FRAGMENT.get());
    }

    @Override
    public RecipeType<StaffAffixRerollJeiRecipe> getRecipeType() {
        return UntilEternityJeiPlugin.REROLL_TYPE;
    }

    @Override
    public Component getTitle() {
        return Component.translatable(
                "jei.until_eternity.staff_affix_reroll");
    }

    @Override
    public IDrawable getBackground() {
        return background;
    }

    @Override
    public IDrawable getIcon() {
        return icon;
    }

    @Override
    public void setRecipe(
            IRecipeLayoutBuilder builder,
            StaffAffixRerollJeiRecipe recipe,
            IFocusGroup focuses
    ) {
        builder.addInputSlot(1, 10)
                .setStandardSlotBackground()
                .addItemStacks(recipe.staffs());
        builder.addInputSlot(27, 10)
                .setStandardSlotBackground()
                .addItemLike(ModItems.SPAWNER_FRAGMENT.get());
        builder.addOutputSlot(91, 10)
                .setOutputSlotBackground()
                .addItemStacks(recipe.staffs())
                .addTooltipCallback((view, tooltip) -> tooltip.add(
                        Component.translatable(
                                "jei.until_eternity.staff_affix_reroll.random")));
        builder.setShapeless();
    }
}
