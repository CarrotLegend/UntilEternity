package com.carrot123.until_eternity.recipe;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NumericTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

import javax.annotation.Nullable;
import java.util.Arrays;

public final class EndCraftingIngredient {
    public static final EndCraftingIngredient EMPTY = new EndCraftingIngredient(Ingredient.EMPTY, null);

    private final Ingredient ingredient;
    @Nullable
    private final CompoundTag requiredNbt;

    public EndCraftingIngredient(Ingredient ingredient, @Nullable CompoundTag requiredNbt) {
        this.ingredient = ingredient;
        this.requiredNbt = requiredNbt == null ? null : requiredNbt.copy();
    }

    public Ingredient ingredient() {
        return ingredient;
    }

    @Nullable
    public CompoundTag requiredNbt() {
        return requiredNbt == null ? null : requiredNbt.copy();
    }

    public boolean isEmpty() {
        return ingredient.isEmpty();
    }

    public boolean test(ItemStack stack) {
        if (!ingredient.test(stack)) {
            return false;
        }
        return requiredNbt == null || stack.hasTag() && containsSubset(requiredNbt, stack.getTag());
    }

    public ItemStack[] displayStacks() {
        return Arrays.stream(ingredient.getItems()).map(stack -> {
            ItemStack result = stack.copy();
            if (requiredNbt != null) {
                result.getOrCreateTag().merge(requiredNbt.copy());
            }
            return result;
        }).toArray(ItemStack[]::new);
    }

    public void toNetwork(FriendlyByteBuf buffer) {
        ingredient.toNetwork(buffer);
        buffer.writeBoolean(requiredNbt != null);
        if (requiredNbt != null) {
            buffer.writeNbt(requiredNbt);
        }
    }

    public static EndCraftingIngredient fromNetwork(FriendlyByteBuf buffer) {
        Ingredient ingredient = Ingredient.fromNetwork(buffer);
        return new EndCraftingIngredient(ingredient, buffer.readBoolean() ? buffer.readNbt() : null);
    }

    public static boolean containsSubset(Tag expected, Tag actual) {
        if (expected instanceof CompoundTag expectedCompound) {
            if (!(actual instanceof CompoundTag actualCompound)) {
                return false;
            }
            for (String key : expectedCompound.getAllKeys()) {
                Tag expectedChild = expectedCompound.get(key);
                Tag actualChild = actualCompound.get(key);
                if (expectedChild == null || actualChild == null || !containsSubset(expectedChild, actualChild)) {
                    return false;
                }
            }
            return true;
        }
        if (expected instanceof NumericTag expectedNumber && actual instanceof NumericTag actualNumber) {
            return isIntegral(expected) && isIntegral(actual)
                    ? expectedNumber.getAsLong() == actualNumber.getAsLong()
                    : Double.compare(expectedNumber.getAsDouble(), actualNumber.getAsDouble()) == 0;
        }
        return expected.equals(actual);
    }

    private static boolean isIntegral(Tag tag) {
        return tag.getId() >= Tag.TAG_BYTE && tag.getId() <= Tag.TAG_LONG;
    }
}
