
package com.carrot123.until_eternity.recipe;

import com.carrot123.until_eternity.compat.goetyrevelation.VenomousWhipUpgradeHelper;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.ForgeRegistries;

public final class VenomousWhipUpgradeRecipe extends CustomRecipe {

    public VenomousWhipUpgradeRecipe(
            ResourceLocation id,
            CraftingBookCategory category
    ) {
        super(id, category);
    }

    @Override
    public boolean matches(
            CraftingContainer container,
            Level level
    ) {
        return findMatch(container) != null;
    }

    @Override
    public ItemStack assemble(
            CraftingContainer container,
            RegistryAccess registryAccess
    ) {
        UpgradeMatch match = findMatch(container);

        if (match == null) {
            return ItemStack.EMPTY;
        }

        ItemStack result = match.whip().copy();
        result.setCount(1);

        VenomousWhipUpgradeHelper.setLevel(
                result,
                match.targetLevel()
        );

        return result;
    }

    @Override
    public boolean canCraftInDimensions(
            int width,
            int height
    ) {
        return width * height >= 2;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipeSerializers.VENOMOUS_WHIP_UPGRADE.get();
    }

    private static UpgradeMatch findMatch(
            CraftingContainer container
    ) {
        ItemStack whip = ItemStack.EMPTY;
        ItemStack material = ItemStack.EMPTY;
        int nonEmpty = 0;

        for (int slot = 0;
             slot < container.getContainerSize();
             slot++) {

            ItemStack stack = container.getItem(slot);

            if (stack.isEmpty()) {
                continue;
            }

            nonEmpty++;

            if (nonEmpty > 2) {
                return null;
            }

            if (VenomousWhipUpgradeHelper.isWhip(stack)) {
                if (!whip.isEmpty()) {
                    return null;
                }

                whip = stack;
            } else {
                if (!material.isEmpty()) {
                    return null;
                }

                material = stack;
            }
        }

        if (nonEmpty != 2
                || whip.isEmpty()
                || material.isEmpty()) {
            return null;
        }

        int targetLevel =
                VenomousWhipUpgradeHelper.getNextLevel(whip);

        if (targetLevel == 0) {
            return null;
        }

        ResourceLocation required =
                VenomousWhipUpgradeHelper.getRequiredMaterial(
                        targetLevel
                );

        if (required == null) {
            return null;
        }

        ResourceLocation supplied =
                ForgeRegistries.ITEMS.getKey(
                        material.getItem()
                );

        if (!required.equals(supplied)) {
            return null;
        }

        return new UpgradeMatch(whip, targetLevel);
    }

    private record UpgradeMatch(
            ItemStack whip,
            int targetLevel
    ) {
    }
}
