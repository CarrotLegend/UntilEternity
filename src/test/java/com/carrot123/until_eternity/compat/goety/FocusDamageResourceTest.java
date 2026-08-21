package com.carrot123.until_eternity.compat.goety;

import com.Polarice3.Goety.api.magic.ISpell;
import com.Polarice3.Goety.common.items.magic.DarkWand;
import com.Polarice3.Goety.common.magic.SpellStat;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FocusDamageResourceTest {
    private static final Path ROOT = Path.of(".");
    private static final Path JAVA = ROOT.resolve(Path.of(
            "src", "main", "java", "com", "carrot123",
            "until_eternity"));
    private static final Path RESOURCES = ROOT.resolve(Path.of(
            "src", "main", "resources"));

    @Test
    void attributeIsRegisteredSyncedAndAttachedOnlyAsFocusDamage()
            throws IOException {
        String attributes = source("registry/ModAttributes.java");
        String main = source("until_eternity.java");
        assertTrue(attributes.contains(
                "ATTRIBUTES.register(\"focus_damage\""));
        assertTrue(attributes.contains("1.0D,\n                    0.0D,"));
        assertTrue(attributes.contains("32767.0D).setSyncable(true)"));
        assertTrue(attributes.contains(
                "event.add(EntityType.PLAYER, FOCUS_DAMAGE.get())"));
        assertTrue(main.contains("ModAttributes.register(modEventBus)"));

        JsonObject en = json("assets/until_eternity/lang/en_us.json");
        JsonObject zh = json("assets/until_eternity/lang/zh_cn.json");
        assertEquals("Focus Damage", en.get(
                "attribute.name.until_eternity.focus_damage").getAsString());
        assertEquals("聚晶伤害", zh.get(
                "attribute.name.until_eternity.focus_damage").getAsString());
        assertFalse(allProductionText().contains("witchcraft_damage"));
    }

    @Test
    void handlerUsesTheFinalAttributeValueAsItsOnlyMultiplier()
            throws IOException {
        String math = source("compat/goety/FocusDamageMath.java");
        String event = source("event/FocusDamageEvents.java");
        assertTrue(math.contains("originalDamage * multiplier"));
        assertFalse(math.contains("1.0D +"));
        assertEquals(1, count(event, "event.setAmount("));
        assertTrue(event.contains("ModAttributes.FOCUS_DAMAGE.get()"));
    }

    @Test
    void goetyPublicSpellEntryPointsMatchThePinnedJar() throws Exception {
        assertNotNull(DarkWand.class.getMethod(
                "onUseTick", net.minecraft.world.level.Level.class,
                LivingEntity.class, ItemStack.class, int.class));
        assertNotNull(DarkWand.class.getMethod(
                "releaseUsing", ItemStack.class,
                net.minecraft.world.level.Level.class,
                LivingEntity.class, int.class));
        assertNotNull(DarkWand.class.getMethod(
                "MagicResults", ItemStack.class,
                net.minecraft.world.level.Level.class,
                LivingEntity.class, ISpell.class));
        assertNotNull(ISpell.class.getMethod(
                "startSpell", ServerLevel.class, LivingEntity.class,
                ItemStack.class, SpellStat.class));
        assertNotNull(ISpell.class.getMethod(
                "useSpell", ServerLevel.class, LivingEntity.class,
                ItemStack.class, int.class, SpellStat.class));
        assertNotNull(ISpell.class.getMethod(
                "stopSpell", ServerLevel.class, LivingEntity.class,
                ItemStack.class, ItemStack.class, int.class,
                SpellStat.class));
        assertNotNull(ISpell.class.getMethod(
                "SpellResult", ServerLevel.class, LivingEntity.class,
                ItemStack.class, SpellStat.class));
    }

    @Test
    void mixinsTrackPublicWandCallsAndOnlyTickMarkedEntities()
            throws IOException {
        String wand = source(
                "mixin/compat/goety/DarkWandFocusCastMixin.java");
        String tick = source(
                "mixin/compat/goety/ServerLevelFocusEntityTickMixin.java");
        String config = Files.readString(
                RESOURCES.resolve("until_eternity.mixins.json"));

        assertEquals(3, count(wand, "@WrapMethod("));
        assertEquals(3, count(wand, "require = 1"));
        for (String method : new String[]{
                "onUseTick(", "releaseUsing(", "MagicResults("}) {
            assertTrue(wand.contains(method), method);
        }
        assertTrue(wand.contains("GoetyFocusCastContext.withPlayerCast"));
        assertTrue(tick.contains("tickNonPassenger"));
        assertTrue(tick.contains("Entity;tick()V"));
        assertTrue(tick.contains("GoetyFocusDamageMarker.getCasterUuid"));
        assertTrue(config.contains("compat.goety.DarkWandFocusCastMixin"));
        assertTrue(config.contains(
                "compat.goety.ServerLevelFocusEntityTickMixin"));
    }

    @Test
    void trackingRejectsLivingSummonsAndDamageIsAppliedOnce()
            throws IOException {
        String marker = source(
                "compat/goety/GoetyFocusDamageMarker.java");
        String resolver = source(
                "compat/goety/GoetyFocusDamageResolver.java");
        String event = source("event/FocusDamageEvents.java");

        assertTrue(marker.contains("!(entity instanceof LivingEntity)"));
        assertFalse(resolver.contains("OwnableEntity"));
        assertTrue(resolver.contains("isForeignLivingEntity"));
        assertTrue(resolver.contains("source instanceof OwnedDamageSource"));
        assertEquals(1, count(event, "event.setAmount("));
        assertFalse(event.contains(".hurt("));
        assertFalse(event.contains("DamageTypeTags.IS_MAGIC"));
        assertFalse(event.contains("irons_spellbooks"));
    }

    @Test
    void persistentMarkerAndTooltipRemainServerClientIsolated()
            throws IOException {
        String marker = source(
                "compat/goety/GoetyFocusDamageMarker.java");
        String tracking = source(
                "compat/goety/GoetyFocusDamageTrackingEvents.java");
        String tooltip = source(
                "client/tooltip/FocusDamageTooltipEvents.java");

        assertTrue(marker.contains(
                "until_eternity:focus_spell_damage"));
        assertTrue(marker.contains("until_eternity:focus_caster"));
        assertTrue(marker.contains("putUUID("));
        assertTrue(tracking.contains("EntityJoinLevelEvent"));
        assertTrue(tracking.contains("event.getLevel().isClientSide()"));
        assertTrue(tooltip.contains("value = Dist.CLIENT"));
        assertFalse(marker.contains("net.minecraft.client"));
        assertFalse(tracking.contains("net.minecraft.client"));
    }

    private static String source(String relative) throws IOException {
        return Files.readString(JAVA.resolve(relative));
    }

    private static JsonObject json(String relative) throws IOException {
        return JsonParser.parseString(Files.readString(
                RESOURCES.resolve(relative))).getAsJsonObject();
    }

    private static int count(String text, String needle) {
        return (text.length() - text.replace(needle, "").length())
                / needle.length();
    }

    private static String allProductionText() throws IOException {
        StringBuilder result = new StringBuilder();
        try (var paths = Files.walk(ROOT.resolve("src/main"))) {
            for (Path path : paths.filter(Files::isRegularFile).toList()) {
                String name = path.toString();
                if (name.endsWith(".java") || name.endsWith(".json")
                        || name.endsWith(".toml")
                        || name.endsWith(".properties")) {
                    result.append(Files.readString(path));
                }
            }
        }
        return result.toString();
    }
}
