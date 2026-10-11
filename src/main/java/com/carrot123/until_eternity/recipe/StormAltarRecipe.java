package com.carrot123.until_eternity.recipe;

import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;

import java.util.List;

public final class StormAltarRecipe implements Recipe<SimpleContainer> {
    public static final String OVERRIDES_KEY = "StormAltarAttributeOverrides";
    public record Input(Ingredient ingredient, int count, CompoundTag nbt) {
        public boolean matches(ItemStack stack) {
            return !stack.isEmpty() && stack.getCount() >= count && ingredient.test(stack)
                    && (nbt == null || NbtUtils.compareNbt(nbt, stack.getTag(), true));
        }
        public ItemStack[] displayStacks() {
            ItemStack[] options = ingredient.getItems();
            ItemStack[] result = new ItemStack[options.length];
            for (int i = 0; i < options.length; i++) {
                result[i] = options[i].copy();
                result[i].setCount(count);
            }
            return result;
        }
    }

    private final ResourceLocation id;
    private final String mode;
    private final Input inputA;
    private final Input inputB;
    private final int duration;
    private final ItemStack result;
    private final List<String> copyNbtKeys;

    public StormAltarRecipe(ResourceLocation id, String mode, Input inputA, Input inputB,
                            int duration, ItemStack result, List<String> copyNbtKeys) {
        this.id = id;
        this.mode = mode;
        this.inputA = inputA;
        this.inputB = inputB;
        this.duration = duration;
        this.result = result;
        this.copyNbtKeys = List.copyOf(copyNbtKeys);
    }

    public Input inputA() { return inputA; }
    public Input inputB() { return inputB; }
    public int duration() { return duration; }
    public String mode() { return mode; }
    public List<String> copyNbtKeys() { return copyNbtKeys; }
    public ItemStack result() { return result.copy(); }
    public boolean acceptsFirst(ItemStack stack) { return inputA.matches(stack); }

    public ItemStack craft(ItemStack first) {
        ItemStack output = result.copy();
        if (mode.equals("upgrade") && first.hasTag()) {
            CompoundTag tag = new CompoundTag();
            for (String key : copyNbtKeys) {
                if (first.getTag().contains(key)) tag.put(key, first.getTag().get(key).copy());
            }
            if (output.hasTag()) tag.merge(output.getTag());
            output.setTag(tag);
        }
        return output;
    }

    @Override public boolean matches(SimpleContainer container, Level level) {
        return inputA.matches(container.getItem(0)) && inputB.matches(container.getItem(1));
    }
    @Override public ItemStack assemble(SimpleContainer container, net.minecraft.core.RegistryAccess access) {
        return craft(container.getItem(0));
    }
    @Override public boolean canCraftInDimensions(int width, int height) { return false; }
    @Override public ItemStack getResultItem(net.minecraft.core.RegistryAccess access) { return result(); }
    @Override public NonNullList<Ingredient> getIngredients() {
        NonNullList<Ingredient> ingredients = NonNullList.create();
        ingredients.add(inputA.ingredient());
        ingredients.add(inputB.ingredient());
        return ingredients;
    }
    @Override public ResourceLocation getId() { return id; }
    @Override public RecipeSerializer<?> getSerializer() { return ModRecipeSerializers.STORM_ALTAR.get(); }
    @Override public RecipeType<?> getType() { return ModRecipeTypes.STORM_ALTAR.get(); }
}
