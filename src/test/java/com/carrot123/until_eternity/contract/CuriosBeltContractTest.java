package com.carrot123.until_eternity.contract;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class CuriosBeltContractTest {
    private static JsonObject resource(String relativePath) throws IOException {
        Path path = Path.of(System.getProperty("user.dir"), relativePath);
        return JsonParser.parseString(Files.readString(path)).getAsJsonObject();
    }

    @Test
    void playerEntityAlwaysReceivesTheStandardBeltSlot() throws IOException {
        JsonObject entityData = resource(
                "src/main/resources/data/until_eternity/curios/entities/entities.json");

        assertFalse(entityData.get("replace").getAsBoolean());
        assertTrue(contains(entityData.getAsJsonArray("entities"), "minecraft:player"));
        assertTrue(contains(entityData.getAsJsonArray("slots"), "belt"));
    }

    @Test
    void beltDefinitionProvidesAMergeSafeMinimumSize() throws IOException {
        JsonObject slotData = resource(
                "src/main/resources/data/until_eternity/curios/slots/belt.json");

        assertEquals(1, slotData.get("size").getAsInt());
        assertEquals("SET", slotData.get("operation").getAsString());
        assertFalse(slotData.get("replace").getAsBoolean());
        assertEquals("curios:slot/empty_belt_slot", slotData.get("icon").getAsString());
        assertTrue(contains(slotData.getAsJsonArray("validators"), "curios:tag"));
        assertFalse(slotData.has("drop_rule"));
    }

    @Test
    void beltTagAddsTheTwoCodeBackedGoetyItemsWithoutReplacingOthers()
            throws IOException {
        JsonObject tagData = resource(
                "src/main/resources/data/curios/tags/items/belt.json");

        assertFalse(tagData.get("replace").getAsBoolean());
        JsonArray values = tagData.getAsJsonArray("values");
        assertTrue(contains(values, "goety:focus_bag"));
        assertTrue(contains(values, "goety:focus_pack"));
    }

    private static boolean contains(JsonArray values, String expected) {
        return values.asList().stream()
                .anyMatch(element -> expected.equals(element.getAsString()));
    }
}
