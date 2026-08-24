package com.carrot123.until_eternity.recipe;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.TagParser;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class EndCraftingRecipeSerializer implements RecipeSerializer<EndCraftingRecipe> {
    @Override
    public EndCraftingRecipe fromJson(ResourceLocation id, JsonObject json) {
        Map<Character, EndCraftingIngredient> key = parseKey(GsonHelper.getAsJsonObject(json, "key"));
        String[] pattern = normalize(parsePattern(GsonHelper.getAsJsonArray(json, "pattern")));
        int width = pattern[0].length();
        int height = pattern.length;
        List<EndCraftingIngredient> ingredients = new ArrayList<>(width * height);
        Set<Character> used = new HashSet<>();
        for (String row : pattern) {
            for (int x = 0; x < width; x++) {
                char symbol = row.charAt(x);
                if (symbol == ' ') {
                    ingredients.add(EndCraftingIngredient.EMPTY);
                } else {
                    EndCraftingIngredient ingredient = key.get(symbol);
                    if (ingredient == null) throw new JsonParseException("Pattern references undefined symbol '" + symbol + "'");
                    ingredients.add(ingredient);
                    used.add(symbol);
                }
            }
        }
        for (char symbol : key.keySet()) {
            if (!used.contains(symbol)) throw new JsonParseException("Key defines unused symbol '" + symbol + "'");
        }
        return new EndCraftingRecipe(id, width, height, ingredients, parseResult(GsonHelper.getAsJsonObject(json, "result")));
    }

    private static Map<Character, EndCraftingIngredient> parseKey(JsonObject json) {
        Map<Character, EndCraftingIngredient> result = new HashMap<>();
        for (Map.Entry<String, JsonElement> entry : json.entrySet()) {
            if (entry.getKey().length() != 1 || entry.getKey().charAt(0) == ' ') {
                throw new JsonParseException("End crafting key symbols must be one non-space character");
            }
            JsonObject ingredientJson = GsonHelper.convertToJsonObject(entry.getValue(), "key ingredient").deepCopy();
            CompoundTag nbt = ingredientJson.has("nbt") ? parseNbt(ingredientJson.remove("nbt")) : null;
            result.put(entry.getKey().charAt(0), new EndCraftingIngredient(Ingredient.fromJson(ingredientJson), nbt));
        }
        return result;
    }

    private static String[] parsePattern(JsonArray array) {
        if (array.isEmpty() || array.size() > EndCraftingRecipe.GRID_SIZE) {
            throw new JsonParseException("Pattern must have 1 to 5 rows");
        }
        String[] rows = new String[array.size()];
        int width = -1;
        for (int i = 0; i < rows.length; i++) {
            rows[i] = GsonHelper.convertToString(array.get(i), "pattern row");
            if (rows[i].length() > EndCraftingRecipe.GRID_SIZE) throw new JsonParseException("Pattern rows may not exceed 5 columns");
            if (width < 0) width = rows[i].length();
            if (rows[i].length() != width) throw new JsonParseException("Pattern rows must have equal width");
        }
        return rows;
    }

    private static String[] normalize(String[] rows) {
        int top = 0, bottom = rows.length - 1, left = Integer.MAX_VALUE, right = -1;
        while (top <= bottom && rows[top].trim().isEmpty()) top++;
        while (bottom >= top && rows[bottom].trim().isEmpty()) bottom--;
        if (top > bottom) throw new JsonParseException("Pattern may not be empty");
        for (int y = top; y <= bottom; y++) {
            int first = firstNonSpace(rows[y]);
            int last = lastNonSpace(rows[y]);
            if (first >= 0) { left = Math.min(left, first); right = Math.max(right, last); }
        }
        String[] result = new String[bottom - top + 1];
        for (int y = top; y <= bottom; y++) result[y - top] = rows[y].substring(left, right + 1);
        return result;
    }

    private static int firstNonSpace(String row) { for (int i=0;i<row.length();i++) if (row.charAt(i)!=' ') return i; return -1; }
    private static int lastNonSpace(String row) { for (int i=row.length()-1;i>=0;i--) if (row.charAt(i)!=' ') return i; return -1; }

    private static ItemStack parseResult(JsonObject json) {
        ResourceLocation itemId = new ResourceLocation(GsonHelper.getAsString(json, "item"));
        Item item = BuiltInRegistries.ITEM.getOptional(itemId)
                .orElseThrow(() -> new JsonParseException("Unknown result item " + itemId));
        int count = GsonHelper.getAsInt(json, "count", 1);
        if (count < 1 || count > item.getMaxStackSize()) throw new JsonParseException("Invalid result count " + count);
        ItemStack result = new ItemStack(item, count);
        if (json.has("nbt")) result.getOrCreateTag().merge(parseNbt(json.get("nbt")));
        return result;
    }

    private static CompoundTag parseNbt(JsonElement element) {
        try {
            return TagParser.parseTag(element.toString());
        } catch (Exception exception) {
            throw new JsonParseException("Invalid NBT: " + element, exception);
        }
    }

    @Nullable
    @Override
    public EndCraftingRecipe fromNetwork(ResourceLocation id, FriendlyByteBuf buffer) {
        int width = buffer.readVarInt();
        int height = buffer.readVarInt();
        List<EndCraftingIngredient> ingredients = new ArrayList<>(width * height);
        for (int i = 0; i < width * height; i++) ingredients.add(EndCraftingIngredient.fromNetwork(buffer));
        return new EndCraftingRecipe(id, width, height, ingredients, buffer.readItem());
    }

    @Override
    public void toNetwork(FriendlyByteBuf buffer, EndCraftingRecipe recipe) {
        buffer.writeVarInt(recipe.width());
        buffer.writeVarInt(recipe.height());
        recipe.endIngredients().forEach(ingredient -> ingredient.toNetwork(buffer));
        buffer.writeItem(recipe.result());
    }
}
