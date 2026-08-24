package com.carrot123.until_eternity.recipe;

import com.carrot123.until_eternity.item.ModItems;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.List;

public final class EndCraftingRecipe implements Recipe<CraftingContainer> {
    public static final int GRID_SIZE = 5;

    private final ResourceLocation id;
    private final int width;
    private final int height;
    private final List<EndCraftingIngredient> ingredients;
    private final ItemStack result;

    public EndCraftingRecipe(ResourceLocation id, int width, int height,
                             List<EndCraftingIngredient> ingredients, ItemStack result) {
        this.id = id;
        this.width = width;
        this.height = height;
        this.ingredients = List.copyOf(ingredients);
        this.result = result.copy();
    }

    public int width() { return width; }
    public int height() { return height; }
    public List<EndCraftingIngredient> endIngredients() { return ingredients; }
    public ItemStack result() { return result.copy(); }

    @Nullable
    public Match findMatch(CraftingContainer container) {
        if (container.getWidth() != GRID_SIZE || container.getHeight() != GRID_SIZE) return null;
        for (int y = 0; y <= GRID_SIZE - height; y++) {
            for (int x = 0; x <= GRID_SIZE - width; x++) {
                if (matchesAt(container, x, y, false)) return new Match(x, y, false);
                if (matchesAt(container, x, y, true)) return new Match(x, y, true);
            }
        }
        return null;
    }

    private boolean matchesAt(CraftingContainer container, int offsetX, int offsetY, boolean mirrored) {
        for (int y = 0; y < GRID_SIZE; y++) {
            for (int x = 0; x < GRID_SIZE; x++) {
                int patternX = x - offsetX;
                int patternY = y - offsetY;
                EndCraftingIngredient expected = EndCraftingIngredient.EMPTY;
                if (patternX >= 0 && patternX < width && patternY >= 0 && patternY < height) {
                    int sourceX = mirrored ? width - patternX - 1 : patternX;
                    expected = ingredients.get(sourceX + patternY * width);
                }
                ItemStack stack = container.getItem(x + y * GRID_SIZE);
                if (expected.isEmpty() ? !stack.isEmpty() : !expected.test(stack)) return false;
            }
        }
        return true;
    }

    public boolean participates(Match match, int gridIndex) {
        int x = gridIndex % GRID_SIZE - match.offsetX();
        int y = gridIndex / GRID_SIZE - match.offsetY();
        if (x < 0 || x >= width || y < 0 || y >= height) return false;
        int sourceX = match.mirrored() ? width - x - 1 : x;
        return !ingredients.get(sourceX + y * width).isEmpty();
    }

    @Override public boolean matches(CraftingContainer container, Level level) { return findMatch(container) != null; }
    @Override public ItemStack assemble(CraftingContainer container, net.minecraft.core.RegistryAccess access) { return result.copy(); }
    @Override public boolean canCraftInDimensions(int width, int height) { return width >= this.width && height >= this.height; }
    @Override public ItemStack getResultItem(net.minecraft.core.RegistryAccess access) { return result.copy(); }
    @Override public ResourceLocation getId() { return id; }
    @Override public RecipeSerializer<?> getSerializer() { return ModRecipeSerializers.END_CRAFTING.get(); }
    @Override public RecipeType<?> getType() { return ModRecipeTypes.END_CRAFTING.get(); }
    @Override public ItemStack getToastSymbol() { return new ItemStack(ModItems.END_CRAFTING_TABLE.get()); }

    @Override
    public NonNullList<Ingredient> getIngredients() {
        NonNullList<Ingredient> result = NonNullList.create();
        ingredients.forEach(value -> result.add(value.ingredient()));
        return result;
    }

    public record Match(int offsetX, int offsetY, boolean mirrored) { }
}
