package com.carrot123.until_eternity.recipe;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.HexFormat;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EndCraftingResourceTest {
    private static final Path ROOT = Path.of(".");
    private static final Path JAVA = ROOT.resolve("src/main/java/com/carrot123/until_eternity");
    private static final Path RES = ROOT.resolve("src/main/resources");

    @Test
    void registrationsMenuAndJeiUseTheFixedContracts() throws Exception {
        assertTrue(source("block/ModBlocks.java").contains("BLOCKS.register(\"end_crafting_table\""));
        assertTrue(source("item/ModItems.java").contains("ITEMS.register(\"end_crafting_table\""));
        assertTrue(source("recipe/ModRecipeTypes.java").contains("TYPES.register(\"end_crafting\""));
        assertTrue(source("recipe/ModRecipeSerializers.java").contains("register(\"end_crafting\""));
        String menu = source("menu/EndCraftingTableMenu.java");
        assertTrue(menu.contains("RESULT_SLOT = 0"));
        assertTrue(menu.contains("INPUT_START = 1"));
        assertTrue(menu.contains("INPUT_END = 26"));
        assertTrue(menu.contains("PLAYER_START = 26"));
        assertTrue(menu.contains("PLAYER_END = 62"));
        String jei = source("compat/jei/EndCraftingRecipeTransferHandler.java");
        assertTrue(jei.contains("1, 25, 26, 36"));
        assertTrue(jei.contains("requiredNbt()"));
        assertEquals(25, count(source("compat/jei/EndCraftingRecipeCategory.java"), "builder.addInputSlot") * 25);
    }

    @Test
    void blockAssetsRecipeLootTagsAndTranslationsAreComplete() throws Exception {
        JsonObject state = json(asset("blockstates/end_crafting_table.json"));
        assertEquals(4, state.getAsJsonObject("variants").size());
        JsonObject model = json(asset("models/block/end_crafting_table.json"));
        assertEquals("until_eternity:block/end_crafting_table_front",
                model.getAsJsonObject("textures").get("north").getAsString());
        assertTrue(Files.exists(data("until_eternity/loot_tables/blocks/end_crafting_table.json")));
        assertTrue(Files.readString(data("minecraft/tags/blocks/mineable/axe.json"))
                .contains("until_eternity:end_crafting_table"));
        JsonObject recipe = json(data("until_eternity/recipes/end_crafting_table.json"));
        assertEquals("minecraft:crafting_shaped", recipe.get("type").getAsString());
        assertFalse(Files.exists(data("until_eternity/recipes/end_crafting_example.json")));
        assertTrue(Files.exists(ROOT.resolve("src/test/resources/fixtures/end_crafting/full_5x5_nbt.json")));
        assertEquals("End Crafting Table", json(asset("lang/en_us.json"))
                .get("block.until_eternity.end_crafting_table").getAsString());
        assertEquals("终末合成台", json(asset("lang/zh_cn.json"))
                .get("block.until_eternity.end_crafting_table").getAsString());
    }

    @Test
    void migratedTexturesKeepTheirExactBytesAndArgbShape() throws Exception {
        assertTexture("bottom", "D75C1FF6EC1E36A0540F2F25D156817265A1AA75187D584ED14876E663581C33");
        assertTexture("front", "D435B91A21A88F3AC3166DF3D5DC3644E49B1CE692F4C6693E3A5B486D21FE38");
        assertTexture("side", "CE9EDF40488905C8BE867E0486C658D185C5C65652A9D871980CFE471C00B3EB");
        assertTexture("top", "D42857AF0CC8139EBA1ED4D7547FE7843CB9FDAB765C666FFF4EDCE17A8CF60D");
        for (String old : new String[]{"front.png", "side.png", "top.png", "bottom.png"})
            assertFalse(Files.exists(RES.resolve("assets").resolve(old)));
    }

    private static void assertTexture(String name, String hash) throws Exception {
        Path path = asset("textures/block/end_crafting_table_" + name + ".png");
        var image = ImageIO.read(path.toFile());
        assertEquals(16, image.getWidth());
        assertEquals(16, image.getHeight());
        assertTrue(image.getColorModel().hasAlpha());
        assertEquals(hash, HexFormat.of().withUpperCase().formatHex(
                MessageDigest.getInstance("SHA-256").digest(Files.readAllBytes(path))));
    }

    private static int count(String text, String needle) { return (text.length()-text.replace(needle,"").length())/needle.length(); }
    private static String source(String relative) throws Exception { return Files.readString(JAVA.resolve(relative)); }
    private static JsonObject json(Path path) throws Exception { return JsonParser.parseString(Files.readString(path)).getAsJsonObject(); }
    private static Path asset(String relative) { return RES.resolve("assets/until_eternity").resolve(relative); }
    private static Path data(String relative) { return RES.resolve("data").resolve(relative); }
}
