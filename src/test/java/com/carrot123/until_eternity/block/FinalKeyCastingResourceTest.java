package com.carrot123.until_eternity.block;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FinalKeyCastingResourceTest {
    private static final Path ROOT = Path.of(".");
    private static final Path RESOURCES = ROOT.resolve(Path.of("src", "main", "resources"));

    @Test
    void registrationsAndCreativeTabUseStableIds() throws IOException {
        String items = source("item/ModItems.java");
        String blocks = source("block/ModBlocks.java");
        String creativeTab = source("item/ModCreativeModeTabs.java");

        assertTrue(items.contains("ITEMS.register(\"final_key\""));
        assertTrue(items.contains("ITEMS.register(\"final_key_mold\""));
        assertTrue(items.contains("ITEMS.register(\"final_key_casting_fluid\""));
        assertTrue(blocks.contains("BLOCKS.register(\"final_key_mold\""));
        String normalizedItems = items.replaceAll("\\s+", " ");
        assertTrue(normalizedItems.contains(
                "FINAL_KEY = ITEMS.register(\"final_key\", () -> new Item(new Item.Properties().stacksTo(1)))"));
        assertTrue(normalizedItems.contains(
                "FINAL_KEY_CASTING_FLUID = ITEMS.register(\"final_key_casting_fluid\", () -> new Item(new Item.Properties().stacksTo(1)))"));

        int finalite = creativeTab.indexOf("output.accept(ModItems.FINALITE_INGOT.get())");
        int key = creativeTab.indexOf("output.accept(ModItems.FINAL_KEY.get())");
        int mold = creativeTab.indexOf("output.accept(ModItems.FINAL_KEY_MOLD.get())");
        int fluid = creativeTab.indexOf("output.accept(ModItems.FINAL_KEY_CASTING_FLUID.get())");
        assertTrue(finalite < key && key < mold && mold < fluid);
    }

    @Test
    void itemTexturesAndModelsAreComplete() throws IOException {
        assertGeneratedItemModel("final_key");
        assertGeneratedItemModel("final_key_casting_fluid");
        assertTexture("final_key");
        assertTexture("final_key_casting_fluid");

        JsonObject moldItem = json(asset("models/item/final_key_mold.json"));
        assertEquals("until_eternity:block/final_key_mold", moldItem.get("parent").getAsString());
        assertEquals("side", moldItem.get("gui_light").getAsString());
        JsonObject display = moldItem.getAsJsonObject("display");
        assertTransform(display, "gui",
                new double[]{30, 225, 0},
                new double[]{0, 0, 0},
                new double[]{0.625, 0.625, 0.625});
        assertTransform(display, "ground",
                new double[]{0, 0, 0},
                new double[]{0, 3, 0},
                new double[]{0.25, 0.25, 0.25});
        assertTransform(display, "fixed",
                new double[]{0, 0, 0},
                new double[]{0, 0, 0},
                new double[]{0.5, 0.5, 0.5});
        assertTransform(display, "thirdperson_righthand",
                new double[]{75, 45, 0},
                new double[]{0, 2.5, 0},
                new double[]{0.375, 0.375, 0.375});
        assertTransform(display, "firstperson_righthand",
                new double[]{0, 45, 0},
                new double[]{0, 0, 0},
                new double[]{0.4, 0.4, 0.4});
        assertTransform(display, "firstperson_lefthand",
                new double[]{0, 225, 0},
                new double[]{0, 0, 0},
                new double[]{0.4, 0.4, 0.4});
        assertFalse(Files.exists(RESOURCES.resolve("assets/model.json")));
        assertFalse(Files.exists(RESOURCES.resolve("assets/texture.png")));
        assertFalse(Files.exists(data("until_eternity/recipes/final_key.json")));
        assertFalse(Files.exists(data("until_eternity/recipes/final_key_mold.json")));
        assertFalse(Files.exists(data("until_eternity/recipes/final_key_casting_fluid.json")));
    }

    @Test
    void existingMoldGeometryIsPreservedAndWiredForFacing() throws IOException {
        JsonObject blockModel = json(asset("models/block/final_key_mold.json"));
        assertFalse(blockModel.has("parent"));
        assertFalse(blockModel.get("ambientocclusion").getAsBoolean());
        assertEquals(11, blockModel.getAsJsonArray("elements").size());
        JsonObject textures = blockModel.getAsJsonObject("textures");
        assertEquals("until_eternity:block/final_key_mold", textures.get("0").getAsString());
        assertEquals("until_eternity:block/final_key_mold", textures.get("particle").getAsString());
        assertNotNull(ImageIO.read(asset("textures/block/final_key_mold.png").toFile()));

        JsonObject variants = json(asset("blockstates/final_key_mold.json")).getAsJsonObject("variants");
        assertTrue(variants.has("facing=north"));
        assertTrue(variants.has("facing=east"));
        assertTrue(variants.has("facing=south"));
        assertTrue(variants.has("facing=west"));

        String block = source("block/FinalKeyMoldBlock.java");
        assertTrue(block.contains("extends HorizontalDirectionalBlock"));
        assertTrue(block.contains("context.getHorizontalDirection().getOpposite()"));
        assertTrue(block.contains("Block.box(1.0D, 0.0D, 2.0D, 15.0D, 2.0D, 14.0D)"));
        assertTrue(block.contains("Block.box(2.0D, 0.0D, 1.0D, 14.0D, 2.0D, 15.0D)"));
        assertTrue(block.contains("builder.add(FACING)"));
    }

    @Test
    void castingRunsOnceOnServerAndReturnsBucketOnlyInSurvival() throws IOException {
        String block = source("block/FinalKeyMoldBlock.java");
        assertTrue(block.contains("if (!heldStack.is(ModItems.FINAL_KEY_CASTING_FLUID.get()))"));
        assertTrue(block.contains("return InteractionResult.PASS"));
        assertTrue(block.contains("if (!level.isClientSide)"));
        assertTrue(block.contains("if (!player.getAbilities().instabuild)"));
        assertTrue(block.contains("player.setItemInHand(hand, new ItemStack(Items.BUCKET))"));
        assertEquals(1, occurrences(block, "new ItemEntity("));
        assertEquals(1, occurrences(block, "level.addFreshEntity(finalKey)"));
        assertTrue(block.contains("pos.getX() + 0.5D"));
        assertTrue(block.contains("pos.getY() + 0.25D"));
        assertTrue(block.contains("pos.getZ() + 0.5D"));
        assertTrue(block.contains("finalKey.setDeltaMovement(0.0D, 0.2D, 0.0D)"));
        assertTrue(block.contains("SoundEvents.BUCKET_EMPTY_LAVA"));
        assertTrue(block.contains("InteractionResult.sidedSuccess(level.isClientSide)"));
        assertFalse(block.contains("Particle"));
        assertFalse(block.contains("BlockEntity"));
    }

    @Test
    void lootToolTagsAndTranslationsArePresent() throws IOException {
        JsonObject loot = json(data("until_eternity/loot_tables/blocks/final_key_mold.json"));
        JsonArray pools = loot.getAsJsonArray("pools");
        assertEquals("until_eternity:final_key_mold",
                pools.get(0).getAsJsonObject().getAsJsonArray("entries")
                        .get(0).getAsJsonObject().get("name").getAsString());
        assertTagContains("minecraft/tags/blocks/mineable/pickaxe.json", "until_eternity:final_key_mold");
        assertTagContains("minecraft/tags/blocks/needs_iron_tool.json", "until_eternity:final_key_mold");

        JsonObject english = json(asset("lang/en_us.json"));
        JsonObject chinese = json(asset("lang/zh_cn.json"));
        assertEquals("Final Key", english.get("item.until_eternity.final_key").getAsString());
        assertEquals("Final Key Mold", english.get("block.until_eternity.final_key_mold").getAsString());
        assertEquals("Final Key Casting Fluid", english.get("item.until_eternity.final_key_casting_fluid").getAsString());
        assertEquals("最终钥匙", chinese.get("item.until_eternity.final_key").getAsString());
        assertEquals("最终钥匙模板", chinese.get("block.until_eternity.final_key_mold").getAsString());
        assertEquals("最终钥匙铸液", chinese.get("item.until_eternity.final_key_casting_fluid").getAsString());
    }

    private static void assertGeneratedItemModel(String id) throws IOException {
        JsonObject model = json(asset("models/item/" + id + ".json"));
        assertEquals("minecraft:item/generated", model.get("parent").getAsString());
        assertEquals("until_eternity:item/" + id,
                model.getAsJsonObject("textures").get("layer0").getAsString());
    }

    private static void assertTexture(String id) throws IOException {
        BufferedImage texture = ImageIO.read(asset("textures/item/" + id + ".png").toFile());
        assertNotNull(texture);
        assertEquals(16, texture.getWidth());
        assertTrue(texture.getHeight() >= texture.getWidth());
        assertEquals(0, texture.getHeight() % texture.getWidth());
        assertTrue(texture.getColorModel().hasAlpha());
    }

    private static void assertTransform(
            JsonObject display,
            String context,
            double[] rotation,
            double[] translation,
            double[] scale
    ) {
        JsonObject transform = display.getAsJsonObject(context);
        assertArray(transform.getAsJsonArray("rotation"), rotation);
        assertArray(transform.getAsJsonArray("translation"), translation);
        assertArray(transform.getAsJsonArray("scale"), scale);
    }

    private static void assertArray(JsonArray actual, double[] expected) {
        assertEquals(expected.length, actual.size());
        for (int index = 0; index < expected.length; index++) {
            assertEquals(expected[index], actual.get(index).getAsDouble());
        }
    }

    private static void assertTagContains(String relative, String id) throws IOException {
        JsonObject tag = json(data(relative));
        assertFalse(tag.get("replace").getAsBoolean());
        assertTrue(tag.getAsJsonArray("values").asList().stream()
                .anyMatch(value -> id.equals(value.getAsString())));
    }

    private static int occurrences(String text, String needle) {
        return (text.length() - text.replace(needle, "").length()) / needle.length();
    }

    private static String source(String relative) throws IOException {
        return Files.readString(ROOT.resolve(Path.of(
                "src", "main", "java", "com", "carrot123", "until_eternity")).resolve(relative));
    }

    private static JsonObject json(Path path) throws IOException {
        return JsonParser.parseString(Files.readString(path)).getAsJsonObject();
    }

    private static Path asset(String relative) {
        return RESOURCES.resolve(Path.of("assets", "until_eternity")).resolve(relative);
    }

    private static Path data(String relative) {
        return RESOURCES.resolve("data").resolve(relative);
    }
}
