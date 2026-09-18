package com.carrot123.until_eternity.recipe;

import org.jetbrains.annotations.Nullable;

import com.google.gson.JsonObject;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.common.crafting.CraftingHelper;

public final class NbtStonecuttingRecipeSerializer
        implements net.minecraft.world.item.crafting.RecipeSerializer<NbtStonecuttingRecipe> {

    @Override
    public NbtStonecuttingRecipe fromJson(
            ResourceLocation recipeId,
            JsonObject json
    ) {

        String group =
                GsonHelper.getAsString(
                        json,
                        "group",
                        ""
                );

        Ingredient ingredient =
                Ingredient.fromJson(
                        GsonHelper.getNonNull(
                                json,
                                "ingredient"
                        )
                );

        JsonObject resultJson =
                GsonHelper.getAsJsonObject(
                        json,
                        "result"
                );

        ItemStack result =
                CraftingHelper.getItemStack(
                        resultJson,
                        true
                );

        if (result.isEmpty()) {
            throw new IllegalArgumentException(
                    "NBT stonecutting recipe "
                            + recipeId
                            + " has an empty result"
            );
        }

        return new NbtStonecuttingRecipe(
                recipeId,
                group,
                ingredient,
                result
        );
    }

    @Nullable
    @Override
    public NbtStonecuttingRecipe fromNetwork(
            ResourceLocation recipeId,
            FriendlyByteBuf buffer
    ) {

        String group =
                buffer.readUtf();

        Ingredient ingredient =
                Ingredient.fromNetwork(buffer);

        ItemStack result =
                buffer.readItem();

        return new NbtStonecuttingRecipe(
                recipeId,
                group,
                ingredient,
                result
        );
    }

    @Override
    public void toNetwork(
            FriendlyByteBuf buffer,
            NbtStonecuttingRecipe recipe
    ) {

        buffer.writeUtf(
                recipe.getGroup()
        );

        recipe.getIngredients()
                .get(0)
                .toNetwork(buffer);

        buffer.writeItem(
                recipe.getResultItem(
                        net.minecraft.core.RegistryAccess.EMPTY
                )
        );
    }
}