package com.carrot123.until_eternity.recipe;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.TagParser;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public final class StormAltarRecipeSerializer implements RecipeSerializer<StormAltarRecipe> {
    @Override
    public StormAltarRecipe fromJson(ResourceLocation id, JsonObject json) {
        String mode = GsonHelper.getAsString(json, "mode", "fusion");
        if (!mode.equals("fusion") && !mode.equals("upgrade")) throw new JsonParseException("Invalid storm altar mode: " + mode);
        int duration = GsonHelper.getAsInt(json, "duration", 60);
        if (duration < 1) throw new JsonParseException("Storm altar duration must be positive");
        StormAltarRecipe.Input a = readInput(GsonHelper.getAsJsonObject(json, "input_a"));
        StormAltarRecipe.Input b = readInput(GsonHelper.getAsJsonObject(json, "input_b"));
        JsonObject output = GsonHelper.getAsJsonObject(json, "result");
        ResourceLocation itemId = new ResourceLocation(GsonHelper.getAsString(output, "item"));
        if (!ForgeRegistries.ITEMS.containsKey(itemId)) throw new JsonParseException("Unknown storm altar result: " + itemId);
        int count = GsonHelper.getAsInt(output, "count", 1);
        if (count < 1) throw new JsonParseException("Storm altar output count must be positive");
        ItemStack result = new ItemStack(ForgeRegistries.ITEMS.getValue(itemId), count);
        if (output.has("nbt")) result.setTag(parseNbt(GsonHelper.getAsString(output, "nbt")));
        if (output.has("attribute_overrides")) {
            ListTag overrides = new ListTag();
            for (var element : GsonHelper.getAsJsonArray(output, "attribute_overrides")) {
                JsonObject modifier = element.getAsJsonObject();
                ResourceLocation attribute = new ResourceLocation(GsonHelper.getAsString(modifier, "attribute"));
                if (!ForgeRegistries.ATTRIBUTES.containsKey(attribute)) throw new JsonParseException("Unknown attribute: " + attribute);
                String operation = GsonHelper.getAsString(modifier, "operation");
                if (!operation.equals("multiply_base") && !operation.equals("multiply_total") && !operation.equals("addition"))
                    throw new JsonParseException("Invalid attribute operation: " + operation);
                CompoundTag tag = new CompoundTag();
                tag.putString("Attribute", attribute.toString());
                tag.putDouble("Amount", GsonHelper.getAsDouble(modifier, "amount"));
                tag.putString("Operation", operation);
                overrides.add(tag);
            }
            result.getOrCreateTag().put(StormAltarRecipe.OVERRIDES_KEY, overrides);
        }
        List<String> copyKeys = new ArrayList<>();
        if (json.has("copy_nbt_keys")) {
            for (var element : GsonHelper.getAsJsonArray(json, "copy_nbt_keys")) copyKeys.add(element.getAsString());
        }
        return new StormAltarRecipe(id, mode, a, b, duration, result, copyKeys);
    }

    private static StormAltarRecipe.Input readInput(JsonObject json) {
        int count = GsonHelper.getAsInt(json, "count", 1);
        if (count < 1) throw new JsonParseException("Storm altar input count must be positive");
        JsonObject ingredientJson = json.deepCopy();
        ingredientJson.remove("count");
        ingredientJson.remove("nbt");
        Ingredient ingredient = Ingredient.fromJson(ingredientJson);
        CompoundTag nbt = json.has("nbt") ? parseNbt(GsonHelper.getAsString(json, "nbt")) : null;
        return new StormAltarRecipe.Input(ingredient, count, nbt);
    }

    private static CompoundTag parseNbt(String value) {
        try { return TagParser.parseTag(value); }
        catch (com.mojang.brigadier.exceptions.CommandSyntaxException e) { throw new JsonParseException("Invalid storm altar NBT", e); }
    }

    @Nullable @Override
    public StormAltarRecipe fromNetwork(ResourceLocation id, FriendlyByteBuf buffer) {
        String mode = buffer.readUtf();
        StormAltarRecipe.Input a = readInput(buffer);
        StormAltarRecipe.Input b = readInput(buffer);
        int duration = buffer.readVarInt();
        ItemStack result = buffer.readItem();
        List<String> keys = buffer.readList(FriendlyByteBuf::readUtf);
        return new StormAltarRecipe(id, mode, a, b, duration, result, keys);
    }

    @Override
    public void toNetwork(FriendlyByteBuf buffer, StormAltarRecipe recipe) {
        buffer.writeUtf(recipe.mode());
        writeInput(buffer, recipe.inputA());
        writeInput(buffer, recipe.inputB());
        buffer.writeVarInt(recipe.duration());
        buffer.writeItem(recipe.result());
        buffer.writeCollection(recipe.copyNbtKeys(), FriendlyByteBuf::writeUtf);
    }

    private static StormAltarRecipe.Input readInput(FriendlyByteBuf buffer) {
        Ingredient ingredient = Ingredient.fromNetwork(buffer);
        int count = buffer.readVarInt();
        CompoundTag nbt = buffer.readNbt();
        return new StormAltarRecipe.Input(ingredient, count, nbt);
    }

    private static void writeInput(FriendlyByteBuf buffer, StormAltarRecipe.Input input) {
        input.ingredient().toNetwork(buffer);
        buffer.writeVarInt(input.count());
        buffer.writeNbt(input.nbt());
    }
}
