package com.carrot123.until_eternity.compat.goetyrevelation;

import com.Polarice3.Goety.common.items.ModItems;
import com.carrot123.until_eternity.item.curio.CurioModifierId;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GoetyFocusCurioAttributesTest {
    private static final Path JAVA = Path.of(
            "src", "main", "java", "com", "carrot123",
            "until_eternity");
    private static final Path REVELATION_JAR = Path.of(
            "libs", "GoetyRevelation-2.3.2.jar");
    private static final Path RESOURCES = Path.of(
            "src", "main", "resources");

    @Test
    void externalCuriosUseExactIdsSlotsAmountsAndOperations()
            throws IOException {
        String source = Files.readString(JAVA.resolve(
                "compat/goetyrevelation/GoetyCurioAttributeCompat.java"));

        assertTrue(source.contains("new ResourceLocation(\"goety\", \"dark_robe\")"));
        assertTrue(source.contains("new ResourceLocation(\"goety\", \"grand_robe\")"));
        assertTrue(source.contains("new ResourceLocation(\"goety_revelation\", \"gold_feather\")"));
        assertFalse(source.contains("dark_robe_fancy"));
        assertTrue(source.contains("private static final String BODY_SLOT = \"body\""));
        assertTrue(source.contains("private static final String CHARM_SLOT = \"charm\""));
        assertTrue(source.contains("private static final String BRACELET_SLOT = \"bracelet\""));
        assertTrue(source.contains("event.getSlotContext().cosmetic()"));

        assertEquals(0.15D, GoetyCurioAttributeCompat.DARK_ROBE_FOCUS_DAMAGE);
        assertEquals(0.25D, GoetyCurioAttributeCompat.GRAND_ROBE_FOCUS_DAMAGE);
        assertEquals(0.10D, GoetyCurioAttributeCompat.GOLD_FEATHER_FOCUS_DAMAGE);
        assertEquals(0.10D, GoetyCurioAttributeCompat.GOLD_FEATHER_RANGED_DAMAGE);
        assertEquals(0.30D, GoetyCurioAttributeCompat.QUIETUS_STAR_FOCUS_DAMAGE);
        assertTrue(source.contains("event.getItemStack().getCount() == 1"));
        assertTrue(source.contains("\"quietus_star_focus_damage\""));
        assertTrue(source.contains("PuffishAttributes.RANGED_DAMAGE"));
        assertTrue(source.contains("AttributeModifier.Operation.MULTIPLY_BASE"));
        assertTrue(source.contains("AttributeModifier.Operation.ADDITION"));
    }

    @Test
    void allEightModifierKeysDeriveDistinctStableUuids() {
        UUID slotUuid = UUID.fromString(
                "12345678-1234-5678-9abc-def012345678");
        List<String> keys = List.of(
                "dark_robe_focus_damage",
                "grand_robe_focus_damage",
                "gold_feather_focus_damage",
                "gold_feather_ranged_damage",
                "quietus_star_focus_damage",
                "void_ring_focus_damage",
                "dark_cage_focus_damage",
                "divine_soul_lamp_focus_damage");

        assertEquals(keys.size(), keys.stream()
                .map(key -> CurioModifierId.create(slotUuid, key))
                .distinct()
                .count());
        keys.forEach(key -> assertEquals(
                CurioModifierId.create(slotUuid, key),
                CurioModifierId.create(slotUuid, key)));
    }

    @Test
    void quietusStarUsesExistingBraceletSlotAndScopedStackMixin()
            throws IOException {
        String bracelet = Files.readString(RESOURCES.resolve(Path.of(
                "data", "curios", "tags", "items", "bracelet.json")));
        String mixin = source(
                "mixin/compat/goetyrevelation/QuietusStarPropertiesMixin.java");
        String mixinConfig = Files.readString(
                RESOURCES.resolve("until_eternity.mixins.json"));

        assertTrue(bracelet.contains("goety_revelation:quietus_star"));
        assertTrue(bracelet.contains("\"required\": false"));
        assertTrue(mixin.contains("@Pseudo"));
        assertTrue(mixin.contains(
                "com.mega.revelationfix.common.init.GRItems"));
        assertTrue(mixin.contains(
                "method = \"lambda$init$0()Lnet/minecraft/world/item/Item;\""));
        assertTrue(mixin.contains("properties.stacksTo(1)"));
        assertTrue(mixin.contains("require = 1"));
        assertFalse(mixin.contains("@Mixin(Item.class)"));
        assertTrue(mixinConfig.contains(
                "compat.goetyrevelation.QuietusStarPropertiesMixin"));
    }

    @Test
    void internalCuriosAppendFocusWithoutRemovingCurrentModifiers()
            throws IOException {
        String voidRing = source("item/curio/VoidRingItem.java");
        assertContainsAll(voidRing,
                "spell_power", "spell_power_multiplier",
                "void_ring_focus_damage", "0.25D");

        String darkCage = source("item/curio/DarkCageItem.java");
        assertContainsAll(darkCage,
                "body_slots", "spell_power",
                "dark_cage_focus_damage", "0.13D");

        String lamp = source(
                "item/curio/charm/DivineSoulLampItem.java");
        assertContainsAll(lamp,
                "soul_reflux", "soul_affinity", "spell_power_flat",
                "spell_power_percent", "spell_power_multiplier",
                "divine_soul_lamp_focus_damage", "0.66D");
    }

    @Test
    void pinnedGoetyAndEmbeddedRevelationFixProvideTargetItems()
            throws Exception {
        assertNotNull(ModItems.class.getField("DARK_ROBE"));
        assertNotNull(ModItems.class.getField("DARK_ROBE_FANCY"));
        assertNotNull(ModItems.class.getField("GRAND_ROBE"));

        try (ZipFile outer = new ZipFile(REVELATION_JAR.toFile())) {
            ZipEntry nestedEntry = outer.getEntry(
                    "META-INF/jarjar/[Forge]RevelationFix-1.20.1-4.2.jar");
            assertNotNull(nestedEntry);
            byte[] nestedJar = outer.getInputStream(nestedEntry).readAllBytes();

            boolean goldFeatherClass = false;
            boolean registryEntry = false;
            boolean charmTag = false;
            try (ZipInputStream nested = new ZipInputStream(
                    new ByteArrayInputStream(nestedJar))) {
                ZipEntry entry;
                while ((entry = nested.getNextEntry()) != null) {
                    ByteArrayOutputStream data = new ByteArrayOutputStream();
                    nested.transferTo(data);
                    if (entry.getName().equals(
                            "com/mega/revelationfix/common/item/curios/GoldFeatherItem.class")) {
                        goldFeatherClass = true;
                    } else if (entry.getName().equals(
                            "com/mega/revelationfix/common/init/GRItems.class")) {
                        registryEntry = new String(data.toByteArray(),
                                StandardCharsets.ISO_8859_1)
                                .contains("gold_feather");
                    } else if (entry.getName().equals(
                            "data/curios/tags/items/charm.json")) {
                        charmTag = new String(data.toByteArray(),
                                StandardCharsets.UTF_8)
                                .contains("goety_revelation:gold_feather");
                    }
                }
            }
            assertTrue(goldFeatherClass);
            assertTrue(registryEntry);
            assertTrue(charmTag);
        }
    }

    private static String source(String relative) throws IOException {
        return Files.readString(JAVA.resolve(relative));
    }

    private static void assertContainsAll(
            String source, String... needles) {
        for (String needle : needles) {
            assertTrue(source.contains(needle), needle);
        }
    }
}
