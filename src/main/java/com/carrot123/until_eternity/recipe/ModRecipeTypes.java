package com.carrot123.until_eternity.recipe;

import com.carrot123.until_eternity.until_eternity;

import net.minecraft.world.item.crafting.RecipeType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModRecipeTypes {
    private static final DeferredRegister<RecipeType<?>> TYPES =
            DeferredRegister.create(ForgeRegistries.RECIPE_TYPES, until_eternity.MODID);

    public static final RegistryObject<RecipeType<EndCraftingRecipe>> END_CRAFTING =
        TYPES.register(
                "end_crafting",
                () -> new RecipeType<EndCraftingRecipe>() {
                    @Override
                    public String toString() {
                        return until_eternity.MODID + ":end_crafting";
                    }
                });

    private ModRecipeTypes() { }
    public static void register(IEventBus bus) { TYPES.register(bus); }
}
