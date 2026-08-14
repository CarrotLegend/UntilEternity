package com.carrot123.until_eternity.item.curio;

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
import static org.junit.jupiter.api.Assertions.assertTrue;

class HorrorHuntResourceTest {
    private static final Path ROOT = Path.of(".");
    private static final Path RESOURCES = ROOT.resolve(
            Path.of("src", "main", "resources"));

    @Test
    void registrationBackTagAndCreativeTabArePresent() throws IOException {
        String items = source("item/ModItems.java");
        String tab = source("item/ModCreativeModeTabs.java");
        String combat = source("event/HorrorHuntCombatEvents.java");

        assertTrue(items.contains("ITEMS.register(\"horror_hunt\""));
        assertTrue(tab.contains("ModItems.HORROR_HUNT.get()"));
        assertTrue(combat.contains("LivingDamageEvent"));
        assertTrue(combat.contains("EventPriority.LOWEST"));
        assertTrue(combat.contains("OwnedDamageSource"));
        assertTrue(combat.contains("Projectile"));
        assertTrue(combat.contains("OwnableEntity"));
        assertTrue(combat.contains("Player.PERSISTED_NBT_TAG"));
        assertFalse(combat.contains(".hurt("));

        JsonObject tag = json(RESOURCES.resolve(Path.of(
                "data", "curios", "tags", "items", "back.json")));
        assertEquals("until_eternity:horror_hunt",
                tag.getAsJsonArray("values").get(0).getAsString());
    }

    @Test
    void modelTranslationsAndTextureAreComplete() throws IOException {
        JsonObject model = json(RESOURCES.resolve(Path.of(
                "assets", "until_eternity", "models", "item",
                "horror_hunt.json")));
        assertEquals("until_eternity:item/horror_hunt",
                model.getAsJsonObject("textures")
                        .get("layer0").getAsString());

        JsonObject english = json(RESOURCES.resolve(Path.of(
                "assets", "until_eternity", "lang", "en_us.json")));
        JsonObject chinese = json(RESOURCES.resolve(Path.of(
                "assets", "until_eternity", "lang", "zh_cn.json")));
        assertEquals("Horrific Hunt",
                english.get("item.until_eternity.horror_hunt")
                        .getAsString());
        assertEquals("恐怖狩猎",
                chinese.get("item.until_eternity.horror_hunt")
                        .getAsString());

        Path texture = RESOURCES.resolve(Path.of(
                "assets", "until_eternity", "textures", "item",
                "horror_hunt.png"));
        BufferedImage image = ImageIO.read(texture.toFile());
        assertTrue(image.getWidth() >= 16);
        assertEquals(image.getWidth(), image.getHeight());
        assertEquals(0, image.getWidth() & image.getWidth() - 1);
        assertTrue(image.getColorModel().hasAlpha());
        assertFalse(Files.exists(RESOURCES.resolve(Path.of(
                "data", "until_eternity", "recipes",
                "horror_hunt.json"))));
    }

    @Test
    void tooltipAndHudUseSharedClientOnlyNameRenderer() throws IOException {
        String events = source(
                "client/tooltip/HorrorHuntTooltipEvents.java");
        String component = source(
                "client/tooltip/ClientHorrorHuntTooltipComponent.java");
        String renderer = source(
                "client/tooltip/HorrorHuntNameRenderer.java");
        String style = source(
                "client/tooltip/HorrorHuntNameStyle.java");
        String hudMixin = source(
                "mixin/client/HorrorHuntSelectedItemNameMixin.java");
        String item = source("item/curio/HorrorHuntItem.java");

        assertTrue(events.contains("RenderTooltipEvent.GatherComponents"));
        assertTrue(events.contains(
                "RegisterClientTooltipComponentFactoriesEvent"));
        assertTrue(events.contains("value = Dist.CLIENT"));
        assertTrue(events.contains("EventPriority.LOWEST"));
        assertTrue(events.contains(
                "event.getItemStack().is(ModItems.HORROR_HUNT.get())"));
        assertTrue(events.contains(
                "event.getTooltipElements().set(0, Either.right("));
        assertTrue(item.contains("Rarity.EPIC"));
        assertTrue(style.contains("MAIN_COLOR = 0xFF820016"));
        assertTrue(style.contains("OUTLINE_COLOR = 0xFF000000"));
        assertTrue(style.contains(
                "GHOST_OUTLINE_COLOR = 0xC0430008"));
        assertTrue(renderer.contains("style.withColor((TextColor) null)"));
        assertTrue(renderer.contains("renderGhostOutlineOnly("));
        assertTrue(renderer.contains("renderMainOutlinedText("));
        assertTrue(renderer.contains("font.drawInBatch8xOutline("));
        assertTrue(renderer.contains(
                "mainColor,\n                outlineColor,"));
        int ghostMethod = renderer.indexOf(
                "private static void renderGhostOutlineOnly(");
        int mainMethod = renderer.indexOf(
                "private static void renderMainOutlinedText(");
        int ghostGlyphMethod = renderer.indexOf(
                "private static void drawGhostGlyph(");
        assertTrue(ghostMethod >= 0 && mainMethod > ghostMethod);
        assertTrue(ghostGlyphMethod > mainMethod);
        String ghostBody = renderer.substring(ghostMethod, mainMethod);
        String mainBody = renderer.substring(mainMethod, ghostGlyphMethod);
        assertTrue(ghostBody.contains("drawGhostGlyph("));
        assertFalse(ghostBody.contains("drawInBatch8xOutline("));
        assertTrue(mainBody.contains("drawInBatch8xOutline("));
        assertFalse(mainBody.contains("drawGhostGlyph("));
        assertFalse(renderer.contains("RenderSystem.setShaderColor"));
        assertFalse(renderer.contains("0xFF60000D"));
        assertTrue(style.contains("FRAME_MILLIS = 35"));
        assertTrue(style.contains("MIN_SCALE = 1.12F"));
        assertTrue(style.contains("MAX_SCALE = 1.26F"));
        assertTrue(component.contains("HorrorHuntNameRenderer.render("));
        assertTrue(hudMixin.contains("HorrorHuntNameRenderer.render("));
        assertTrue(hudMixin.contains(
                "lastToolHighlight.is(ModItems.HORROR_HUNT.get())"));
        assertFalse(component.contains("0xFF820016"));
        assertFalse(hudMixin.contains("0xFF820016"));

        JsonObject mixins = json(RESOURCES.resolve(
                "until_eternity.mixins.json"));
        assertTrue(mixins.getAsJsonArray("client").asList().stream()
                .anyMatch(element -> element.getAsString().equals(
                        "client.HorrorHuntSelectedItemNameMixin")));
        assertFalse(mixins.getAsJsonArray("mixins").asList().stream()
                .anyMatch(element -> element.getAsString().equals(
                        "client.HorrorHuntSelectedItemNameMixin")));
    }

    private static String source(String relative) throws IOException {
        return Files.readString(ROOT.resolve(Path.of(
                "src", "main", "java", "com", "carrot123",
                "until_eternity")).resolve(relative));
    }

    private static JsonObject json(Path path) throws IOException {
        return JsonParser.parseString(Files.readString(path))
                .getAsJsonObject();
    }
}
