package com.carrot123.until_eternity.compat.jei;

import com.carrot123.until_eternity.compat.ironsspellbooks.StaffUpgradeHelper;
import com.carrot123.until_eternity.until_eternity;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.List;

@JeiPlugin
public final class UntilEternityJeiPlugin implements IModPlugin {
    public static final RecipeType<StaffAffixRerollJeiRecipe> REROLL_TYPE =
            RecipeType.create(until_eternity.MODID, "staff_affix_reroll",
                    StaffAffixRerollJeiRecipe.class);
    private static final ResourceLocation UID = new ResourceLocation(
            until_eternity.MODID, "staff_affix_reroll_plugin");

    @Override
    public ResourceLocation getPluginUid() {
        return UID;
    }

    @Override
    public void registerCategories(
            IRecipeCategoryRegistration registration
    ) {
        registration.addRecipeCategories(new StaffAffixRerollCategory(
                registration.getJeiHelpers().getGuiHelper()));
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        List<ItemStack> staffs = BuiltInRegistries.ITEM.stream()
                .map(ItemStack::new)
                .filter(StaffUpgradeHelper::isUpgradeableStaff)
                .toList();
        if (!staffs.isEmpty()) {
            registration.addRecipes(REROLL_TYPE,
                    List.of(new StaffAffixRerollJeiRecipe(staffs)));
        }
    }
}
