package com.carrot123.until_eternity.recipe;

import com.carrot123.until_eternity.compat.ironsspellbooks.StaffUpgradeHelper;
import com.carrot123.until_eternity.item.ModItems;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

public final class StaffAffixRerollRecipe extends CustomRecipe {
    public StaffAffixRerollRecipe(
            ResourceLocation id,
            CraftingBookCategory category
    ) {
        super(id, category);
    }

    @Override
    public boolean matches(CraftingContainer container, Level level) {
        return findStaff(container) != null;
    }

    @Override
    public ItemStack assemble(
            CraftingContainer container,
            RegistryAccess registryAccess
    ) {
        ItemStack staff = findStaff(container);
        if (staff == null) {
            return ItemStack.EMPTY;
        }
        ItemStack result = staff.copy();
        result.setCount(1);
        return result;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width * height >= 2;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipeSerializers.STAFF_AFFIX_REROLL.get();
    }

    private static ItemStack findStaff(CraftingContainer container) {
        ItemStack staff = null;
        boolean fragment = false;
        int nonEmpty = 0;

        for (int slot = 0; slot < container.getContainerSize(); slot++) {
            ItemStack stack = container.getItem(slot);
            if (stack.isEmpty()) {
                continue;
            }
            nonEmpty++;
            if (StaffUpgradeHelper.isUpgradeableStaff(stack)) {
                if (staff != null) {
                    return null;
                }
                staff = stack;
            } else if (stack.is(ModItems.SPAWNER_FRAGMENT.get())) {
                if (fragment) {
                    return null;
                }
                fragment = true;
            } else {
                return null;
            }
        }
        return nonEmpty == 2 && staff != null && fragment ? staff : null;
    }
}
