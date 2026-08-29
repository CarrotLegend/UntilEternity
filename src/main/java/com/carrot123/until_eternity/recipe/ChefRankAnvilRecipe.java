package com.carrot123.until_eternity.recipe;

import com.carrot123.until_eternity.compat.eternalcareer.ChefRank;
import com.carrot123.until_eternity.compat.eternalcareer.ChefRankHelper;

import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;

public final class ChefRankAnvilRecipe implements Recipe<Container> {

    public static final int LEVEL_COST = 1;
    public static final int MATERIAL_COST = 1;

    private final ResourceLocation id;

    public ChefRankAnvilRecipe(ResourceLocation id) {
        this.id = id;
    }

    @Override
    public boolean matches(Container container, Level level) {
        if (container.getContainerSize() < 2) {
            return false;
        }

        ItemStack armor = container.getItem(0);
        ItemStack badge = container.getItem(1);

        return ChefRankHelper.canUpgrade(armor, badge);
    }

    @Override
    public ItemStack assemble(
            Container container,
            RegistryAccess registryAccess
    ) {
        if (container.getContainerSize() < 2) {
            return ItemStack.EMPTY;
        }

        ItemStack armor = container.getItem(0);
        ItemStack badge = container.getItem(1);

        if (!ChefRankHelper.canUpgrade(armor, badge)) {
            return ItemStack.EMPTY;
        }

        ChefRank targetRank =
                ChefRankHelper.getRankForBadge(badge);

        if (targetRank == ChefRank.NONE) {
            return ItemStack.EMPTY;
        }

        ItemStack result = armor.copy();
        result.setCount(1);

        ChefRankHelper.setRank(
                result,
                targetRank
        );

        return result;
    }

    public int getLevelCost() {
        return LEVEL_COST;
    }

    public int getMaterialCost() {
        return MATERIAL_COST;
    }

    @Override
    public boolean canCraftInDimensions(
            int width,
            int height
    ) {
        return true;
    }

    @Override
    public ItemStack getResultItem(
            RegistryAccess registryAccess
    ) {
        return ItemStack.EMPTY;
    }

    @Override
    public ResourceLocation getId() {
        return id;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipeSerializers.CHEF_RANK_ANVIL.get();
    }

    @Override
    public RecipeType<?> getType() {
        return ModRecipeTypes.CHEF_RANK_ANVIL.get();
    }

    @Override
    public boolean isSpecial() {
        return true;
    }
}