package com.carrot123.until_eternity.block;

import com.eeeab.eeeabsmobs.sever.entity.immortal.EntityImmortal;
import com.eeeab.eeeabsmobs.sever.init.EntityInit;
import com.eeeab.eeeabsmobs.sever.init.ItemInit;
import com.github.L_Ender.cataclysm.entity.effect.ScreenShake_Entity;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.HexFormat;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ImmortalAltarResourceTest {
    private static final Path ROOT = Path.of(".");
    private static final Path JAVA = ROOT.resolve(Path.of(
            "src", "main", "java", "com", "carrot123", "until_eternity"));
    private static final Path RESOURCES = ROOT.resolve(Path.of("src", "main", "resources"));

    @Test
    void registryCreativeAndBlockEntityWiringAreComplete() throws IOException {
        String blocks = source("block/ModBlocks.java");
        String items = source("item/ModItems.java");
        String blockEntities = source("block/entity/ModBlockEntities.java");
        String main = source("until_eternity.java");
        String creative = source("item/ModCreativeModeTabs.java");

        assertTrue(blocks.contains("BLOCKS.register(\"immortal_altar\""));
        assertTrue(items.contains("ITEMS.register(\"immortal_altar\""));
        assertTrue(items.contains("new ImmortalAltarBlockItem(ModBlocks.IMMORTAL_ALTAR.get()"));
        assertTrue(blockEntities.contains("BLOCK_ENTITIES.register(\"immortal_altar\""));
        assertTrue(blockEntities.contains("ModBlocks.IMMORTAL_ALTAR.get()"));
        assertTrue(main.contains("ModBlockEntities.register(modEventBus)"));
        int template = creative.indexOf("output.accept(ModItems.UNIVERSAL_SMITHING_TEMPLATE.get())");
        int altar = creative.indexOf("output.accept(ModItems.IMMORTAL_ALTAR.get())");
        assertTrue(template >= 0 && template < altar);
    }

    @Test
    void dependenciesArePinnedToRequestedBuilds() throws IOException {
        String build = Files.readString(ROOT.resolve("build.gradle"));
        String mods = Files.readString(RESOURCES.resolve("META-INF/mods.toml"));
        assertTrue(build.contains("lendercataclysm-551586:7908487"));
        assertTrue(build.contains("lionfish-api-1001614:5922047"));
        assertTrue(mods.contains("modId=\"cataclysm\""));
        assertTrue(mods.contains("versionRange=\"[3.27,3.28)\""));
        assertTrue(mods.contains("modId=\"lionfishapi\""));
        assertTrue(mods.contains("versionRange=\"[2.4,)\""));
        assertFalse(build.contains("7934870"));
    }

    @Test
    void customModelTextureAndRenderersAreFullyWired() throws Exception {
        JsonObject blockModel = json(asset("models/block/immortal_altar.json"));
        JsonObject itemModel = json(asset("models/item/immortal_altar.json"));
        assertEquals("minecraft:block/block",
                blockModel.get("parent").getAsString());
        assertEquals("until_eternity:block/immortal_altar",
                blockModel.getAsJsonObject("textures")
                        .get("particle").getAsString());
        assertEquals("minecraft:builtin/entity",
                itemModel.get("parent").getAsString());
        assertEquals("until_eternity:block/immortal_altar",
                itemModel.getAsJsonObject("textures")
                        .get("particle").getAsString());
        assertItemDisplayTransforms(itemModel.getAsJsonObject("display"));

        Path texture = asset("textures/block/immortal_altar.png");
        BufferedImage image = ImageIO.read(texture.toFile());
        assertNotNull(image);
        assertEquals(64, image.getWidth());
        assertEquals(64, image.getHeight());
        assertTrue(image.getColorModel().hasAlpha());
        assertEquals(
                "B4AAB91E51BEE1C8D0CDC622F4BFFBA9E9C12E599C1F44C47BB87F0CE25B31DF",
                HexFormat.of().withUpperCase().formatHex(
                        MessageDigest.getInstance("SHA-256")
                                .digest(Files.readAllBytes(texture))));
        assertFalse(Files.exists(RESOURCES.resolve("assets/texture.png")));
        assertFalse(Files.exists(RESOURCES.resolve("assets/CustomModel.java")));

        String model = source("client/altar/ImmortalAltarModel.java");
        String events = source("client/altar/ImmortalAltarClientEvents.java");
        String blockRenderer = source(
                "client/altar/ImmortalAltarBlockEntityRenderer.java");
        String itemRenderer = source(
                "client/altar/ImmortalAltarItemRenderer.java");
        assertEquals(20, count(model, ".addBox("));
        assertEquals(2, count(model, "main.addOrReplaceChild("));
        assertTrue(model.contains("LayerDefinition.create(mesh, 64, 64)"));
        assertTrue(model.contains("RenderType::entityCutoutNoCull"));
        assertTrue(events.contains("RegisterLayerDefinitions"));
        assertTrue(events.contains("ImmortalAltarModel::createBodyLayer"));
        assertTrue(blockRenderer.contains(
                "context.bakeLayer(ImmortalAltarModel.LAYER_LOCATION)"));
        assertTrue(itemRenderer.contains(
                "entityModelSet.bakeLayer(ImmortalAltarModel.LAYER_LOCATION)"));

        String visual = source("client/altar/ImmortalAltarVisualRenderer.java");
        assertTrue(visual.contains("textures/block/immortal_altar.png"));
        assertTrue(visual.contains("ImmortalAltarModel model"));
        assertTrue(visual.contains("model.renderToBuffer("));
        assertTrue(blockRenderer.contains("ImmortalAltarVisualRenderer.render("));
        assertTrue(itemRenderer.contains("ImmortalAltarVisualRenderer.render("));
        assertFalse(model.contains("cataclysm"));
        assertFalse(visual.contains("cataclysm"));
        assertFalse(visual.contains("Altar_of_Fire_Model"));
        assertFalse(visual.contains("altar_of_fire.png"));
    }

    @Test
    void gameplayContractUsesExactItemBossDormancyAndShake() throws Exception {
        assertNotNull(ItemInit.class.getDeclaredField("IMMORTAL_BONE"));
        assertNotNull(EntityInit.class.getDeclaredField("IMMORTAL_BOSS"));
        assertTrue(EntityInit.class.getDeclaredField("IMMORTAL_BOSS")
                .getGenericType().getTypeName().contains(EntityImmortal.class.getName()));
        assertNotNull(EntityImmortal.class.getMethod("setInitSpawn"));
        assertNotNull(ScreenShake_Entity.class.getMethod(
                "ScreenShake", Level.class, Vec3.class,
                float.class, float.class, int.class, int.class));

        String entity = source("block/entity/ImmortalAltarBlockEntity.java");
        assertTrue(entity.contains("SHAKE_RADIUS = 20.0F"));
        assertTrue(entity.contains("SHAKE_MAGNITUDE = 0.05F"));
        assertTrue(entity.contains("SHAKE_DURATION = 0"));
        assertTrue(entity.contains(
                "SHAKE_FADE_DURATION =\n            ImmortalAltarRitualState.DURATION_TICKS"));
        assertTrue(entity.contains("ScreenShake_Entity.ScreenShake("));
        int marker = entity.indexOf(
                "ImmortalSpawnEvents.ALTAR_SUMMONED_TAG");
        int addEntity = entity.indexOf("serverLevel.addFreshEntity(immortal)");
        assertTrue(marker >= 0 && marker < addEntity);
        assertTrue(entity.contains("serverLevel.noCollision("));
        assertTrue(entity.contains("immortal.getBoundingBox()"));
        assertFalse(entity.contains("setActive("));
        assertFalse(entity.contains("setInvulnerable("));
        assertFalse(entity.contains("playAnimation("));
    }

    @Test
    void altarUsesOnePersistedOfferingAndRendererReadsIt() throws IOException {
        String block = source("block/ImmortalAltarBlock.java");
        String entity = source("block/entity/ImmortalAltarBlockEntity.java");
        String renderer = source(
                "client/altar/ImmortalAltarBlockEntityRenderer.java");

        assertTrue(entity.contains("extends BaseContainerBlockEntity"));
        assertTrue(entity.contains("SLOT_COUNT = 1"));
        assertTrue(entity.contains("getMaxStackSize()"));
        assertTrue(entity.contains("ContainerHelper.saveAllItems(tag, items)"));
        assertTrue(entity.contains("ContainerHelper.loadAllItems(tag, items)"));
        assertTrue(entity.contains("stack.is(ItemInit.IMMORTAL_BONE.get())"));
        assertTrue(entity.contains("activating && offering.isEmpty()"));
        assertFalse(entity.contains("RefundBone"));
        assertTrue(block.contains("ImmortalAltarBlockEntity::commonTick"));
        assertTrue(block.contains("Containers.dropContents(level, pos, altar)"));
        assertTrue(renderer.contains("ItemStack offering = altar.getItem(0)"));
        assertTrue(renderer.contains("altar.getRenderTicks() + partialTick"));
        assertFalse(renderer.contains("new ItemStack(ItemInit.IMMORTAL_BONE"));
        assertFalse(entity.contains("new ItemEntity("));
    }

    @Test
    void resourcesLootTagsAndTranslationsAreComplete() throws IOException {
        JsonObject blockstate = json(asset("blockstates/immortal_altar.json"));
        assertEquals("until_eternity:block/immortal_altar",
                blockstate.getAsJsonObject("variants").getAsJsonObject("")
                        .get("model").getAsString());
        JsonObject loot = json(data("until_eternity/loot_tables/blocks/immortal_altar.json"));
        assertEquals("until_eternity:immortal_altar",
                loot.getAsJsonArray("pools").get(0).getAsJsonObject()
                        .getAsJsonArray("entries").get(0).getAsJsonObject()
                        .get("name").getAsString());
        assertTagContains("minecraft/tags/blocks/mineable/pickaxe.json");
        assertTagContains("minecraft/tags/blocks/needs_iron_tool.json");
        assertEquals("Immortal Altar", json(asset("lang/en_us.json"))
                .get("block.until_eternity.immortal_altar").getAsString());
        assertEquals("不朽者祭坛", json(asset("lang/zh_cn.json"))
                .get("block.until_eternity.immortal_altar").getAsString());
        assertFalse(Files.exists(data("until_eternity/recipes/immortal_altar.json")));
    }

    @Test
    void clientTypesStayOutOfServerGameplayClasses() throws IOException {
        String block = source("block/ImmortalAltarBlock.java");
        String entity = source("block/entity/ImmortalAltarBlockEntity.java");
        String clientEvents = source("client/altar/ImmortalAltarClientEvents.java");
        assertFalse(block.contains("net.minecraft.client"));
        assertFalse(entity.contains("net.minecraft.client"));
        assertFalse(entity.contains("com.github.L_Ender.cataclysm.client"));
        assertTrue(clientEvents.contains("value = Dist.CLIENT"));
    }

    private static void assertTagContains(String relative) throws IOException {
        assertTrue(json(data(relative)).getAsJsonArray("values").asList().stream()
                .anyMatch(value -> "until_eternity:immortal_altar".equals(value.getAsString())));
    }

    private static String source(String relative) throws IOException {
        return Files.readString(JAVA.resolve(relative));
    }

    private static JsonObject json(Path path) throws IOException {
        return JsonParser.parseString(Files.readString(path)).getAsJsonObject();
    }

    private static void assertItemDisplayTransforms(JsonObject display) {
        assertVector(display, "gui", "rotation", 30.0D, 45.0D, 0.0D);
        assertVector(display, "gui", "scale", 0.5D, 0.5D, 0.5D);
        assertVector(display, "ground", "translation", 0.0D, 3.0D, 0.0D);
        assertVector(display, "ground", "scale", 0.25D, 0.25D, 0.25D);
        assertVector(display, "head", "rotation", 0.0D, 180.0D, 0.0D);
        assertVector(display, "fixed", "scale", 0.5D, 0.5D, 0.5D);
        assertVector(display, "thirdperson_righthand", "rotation",
                75.0D, 315.0D, 0.0D);
        assertVector(display, "thirdperson_righthand", "translation",
                -2.0D, 2.5D, 0.0D);
        assertVector(display, "firstperson_righthand", "rotation",
                0.0D, 315.0D, 0.0D);
        assertVector(display, "firstperson_righthand", "scale",
                0.375D, 0.375D, 0.375D);
    }

    private static void assertVector(
            JsonObject display,
            String context,
            String key,
            double x,
            double y,
            double z) {
        var vector = display.getAsJsonObject(context).getAsJsonArray(key);
        assertEquals(x, vector.get(0).getAsDouble());
        assertEquals(y, vector.get(1).getAsDouble());
        assertEquals(z, vector.get(2).getAsDouble());
    }

    private static int count(String text, String needle) {
        return (text.length() - text.replace(needle, "").length())
                / needle.length();
    }

    private static Path asset(String relative) {
        return RESOURCES.resolve(Path.of("assets", "until_eternity")).resolve(relative);
    }

    private static Path data(String relative) {
        return RESOURCES.resolve("data").resolve(relative);
    }
}
