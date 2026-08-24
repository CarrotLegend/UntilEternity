package com.carrot123.until_eternity.compat.jei;

import com.carrot123.until_eternity.compat.ironsspellbooks.StaffUpgradeHelper;
import com.carrot123.until_eternity.until_eternity;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeTransferRegistration;
import com.carrot123.until_eternity.recipe.EndCraftingRecipe;
import com.carrot123.until_eternity.recipe.ModRecipeTypes;
import com.carrot123.until_eternity.item.ModItems;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.List;

@JeiPlugin
public final class UntilEternityJeiPlugin implements IModPlugin {
    public static final RecipeType<StaffAffixRerollJeiRecipe> REROLL_TYPE =
            RecipeType.create(until_eternity.MODID, "staff_affix_reroll",
                    StaffAffixRerollJeiRecipe.class);
    public static final RecipeType<EndCraftingRecipe> END_CRAFTING_TYPE =
            RecipeType.create(until_eternity.MODID, "end_crafting", EndCraftingRecipe.class);
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
        registration.addRecipeCategories(
                new StaffAffixRerollCategory(registration.getJeiHelpers().getGuiHelper()),
                new EndCraftingRecipeCategory(registration.getJeiHelpers().getGuiHelper()));
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
        if (Minecraft.getInstance().level != null) {
            registration.addRecipes(END_CRAFTING_TYPE,
                    Minecraft.getInstance().level.getRecipeManager().getAllRecipesFor(ModRecipeTypes.END_CRAFTING.get()));
        }
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        registration.addRecipeCatalyst(ModItems.END_CRAFTING_TABLE.get(), END_CRAFTING_TYPE);
    }

    @Override
    public void registerRecipeTransferHandlers(IRecipeTransferRegistration registration) {
        registration.addRecipeTransferHandler(
                new EndCraftingRecipeTransferHandler(registration.getTransferHelper()), END_CRAFTING_TYPE);
    }
}
