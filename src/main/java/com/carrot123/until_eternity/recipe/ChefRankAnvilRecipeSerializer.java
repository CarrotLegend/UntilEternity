package com.carrot123.until_eternity.recipe;

import javax.annotation.Nullable;

import com.google.gson.JsonObject;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeSerializer;

public final class ChefRankAnvilRecipeSerializer
        implements RecipeSerializer<ChefRankAnvilRecipe> {

    @Override
    public ChefRankAnvilRecipe fromJson(
            ResourceLocation recipeId,
            JsonObject json
    ) {
        return new ChefRankAnvilRecipe(recipeId);
    }

    @Nullable
    @Override
    public ChefRankAnvilRecipe fromNetwork(
            ResourceLocation recipeId,
            FriendlyByteBuf buffer
    ) {
        return new ChefRankAnvilRecipe(recipeId);
    }

    @Override
    public void toNetwork(
            FriendlyByteBuf buffer,
            ChefRankAnvilRecipe recipe
    ) {
    }
}