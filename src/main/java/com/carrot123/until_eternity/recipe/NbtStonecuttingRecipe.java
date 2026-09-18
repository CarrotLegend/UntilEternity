package com.carrot123.until_eternity.recipe;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.StonecutterRecipe;

public class NbtStonecuttingRecipe extends StonecutterRecipe {

    public NbtStonecuttingRecipe(
            ResourceLocation id,
            String group,
            Ingredient ingredient,
            ItemStack result
    ) {
        super(
                id,
                group,
                ingredient,
                result
        );
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipeSerializers.NBT_STONECUTTING.get();
    }
}