package com.carrot123.until_eternity.recipe;

import com.carrot123.until_eternity.until_eternity;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SimpleCraftingRecipeSerializer;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModRecipeSerializers {
    public static final DeferredRegister<RecipeSerializer<?>>
            RECIPE_SERIALIZERS = DeferredRegister.create(
                    ForgeRegistries.RECIPE_SERIALIZERS,
                    until_eternity.MODID);

    public static final RegistryObject<RecipeSerializer<StaffUpgradeRecipe>>
            STAFF_UPGRADE = RECIPE_SERIALIZERS.register(
                    "staff_upgrade",
                    () -> new SimpleCraftingRecipeSerializer<>(
                            StaffUpgradeRecipe::new));
    public static final RegistryObject<RecipeSerializer<StaffAffixRerollRecipe>>
            STAFF_AFFIX_REROLL = RECIPE_SERIALIZERS.register(
                    "staff_affix_reroll",
                    () -> new SimpleCraftingRecipeSerializer<>(
                            StaffAffixRerollRecipe::new));
    public static final RegistryObject<RecipeSerializer<EndCraftingRecipe>> END_CRAFTING =
            RECIPE_SERIALIZERS.register("end_crafting", EndCraftingRecipeSerializer::new);

    private ModRecipeSerializers() {
    }

    public static void register(IEventBus eventBus) {
        RECIPE_SERIALIZERS.register(eventBus);
    }
}
