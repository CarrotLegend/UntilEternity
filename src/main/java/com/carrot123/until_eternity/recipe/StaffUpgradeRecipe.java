package com.carrot123.until_eternity.recipe;

import com.carrot123.until_eternity.compat.ironsspellbooks.StaffUpgradeHelper;
import io.redspace.ironsspellbooks.registries.ItemRegistry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

public final class StaffUpgradeRecipe extends CustomRecipe {
    public StaffUpgradeRecipe(
            ResourceLocation id,
            CraftingBookCategory category
    ) {
        super(id, category);
    }

    @Override
    public boolean matches(CraftingContainer container, Level level) {
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

        ItemStack result = match.staff().copy();
        result.setCount(1);
        result.getOrCreateTag().putInt(
                StaffUpgradeHelper.LEVEL_TAG,
                match.targetLevel());
        return result;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width * height >= 3;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipeSerializers.STAFF_UPGRADE.get();
    }

    private static UpgradeMatch findMatch(CraftingContainer container) {
        int nonEmptyStacks = 0;
        int staffSlot = -1;
        ItemStack staff = ItemStack.EMPTY;

        for (int slot = 0; slot < container.getContainerSize(); slot++) {
            ItemStack stack = container.getItem(slot);
            if (stack.isEmpty()) {
                continue;
            }
            nonEmptyStacks++;
            if (StaffUpgradeHelper.isUpgradeableStaff(stack)) {
                if (staffSlot >= 0) {
                    return null;
                }
                staffSlot = slot;
                staff = stack;
            }
        }

        if (nonEmptyStacks != 3 || staffSlot < 0) {
            return null;
        }

        int targetLevel = StaffUpgradeHelper.getNextLevel(staff);
        if (targetLevel == 0) {
            return null;
        }

        Item expectedInk = expectedInk(targetLevel);
        Item expectedMaterial = expectedMaterial(targetLevel);
        boolean foundInk = false;
        boolean foundMaterial = false;

        for (int slot = 0; slot < container.getContainerSize(); slot++) {
            if (slot == staffSlot) {
                continue;
            }
            ItemStack stack = container.getItem(slot);
            if (stack.isEmpty()) {
                continue;
            }
            if (!foundInk && stack.is(expectedInk)) {
                foundInk = true;
            } else if (!foundMaterial && stack.is(expectedMaterial)) {
                foundMaterial = true;
            } else {
                return null;
            }
        }

        return foundInk && foundMaterial
                ? new UpgradeMatch(staff, targetLevel)
                : null;
    }

    private static Item expectedInk(int targetLevel) {
        return switch (targetLevel) {
            case 1 -> ItemRegistry.INK_COMMON.get();
            case 2 -> ItemRegistry.INK_UNCOMMON.get();
            case 3 -> ItemRegistry.INK_RARE.get();
            case 4 -> ItemRegistry.INK_EPIC.get();
            case 5 -> ItemRegistry.INK_LEGENDARY.get();
            default -> throw new IllegalArgumentException(
                    "Unsupported staff upgrade level: " + targetLevel);
        };
    }

    private static Item expectedMaterial(int targetLevel) {
        return switch (targetLevel) {
            case 1 -> Items.COAL;
            case 2 -> Items.LAPIS_LAZULI;
            case 3 -> Items.IRON_INGOT;
            case 4 -> Items.DIAMOND;
            case 5 -> Items.NETHERITE_INGOT;
            default -> throw new IllegalArgumentException(
                    "Unsupported staff upgrade level: " + targetLevel);
        };
    }

    private record UpgradeMatch(ItemStack staff, int targetLevel) {
    }
}
