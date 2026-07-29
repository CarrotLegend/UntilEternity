package com.carrot123.until_eternity.item.curio;

import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StaticCurioMigrationTest {
    private static final Path JAVA = Path.of(
            "src", "main", "java", "com", "carrot123",
            "until_eternity");
    private static final Path CURIO_ITEMS = Path.of(
            "src", "main", "resources", "data", "curios", "tags", "items");

    private static final Set<String> STATIC_CURIOS = Set.of(
            "until_eternity:elemental_gauntlet",
            "until_eternity:reaper_tooth_necklace",
            "until_eternity:sand_shark_tooth_necklace",
            "until_eternity:regenerator",
            "until_eternity:guttering_candle",
            "until_eternity:empowered_shield",
            "until_eternity:cosmic_aegis",
            "until_eternity:proof_of_spurner",
            "until_eternity:dark_cage",
            "until_eternity:mithril_gloves",
            "until_eternity:pewter_gloves",
            "until_eternity:divine_soul_lamp",
            "until_eternity:ring_of_warped_magic",
            "until_eternity:advanced_ring_of_warped_magic",
            "until_eternity:ring_of_soul_craving",
            "until_eternity:ring_of_purity",
            "until_eternity:ring_of_warped_chanting",
            "until_eternity:ring_of_warped_cooling",
            "until_eternity:void_ring",
            "until_eternity:empowered_ring",
            "until_eternity:advanced_empowered_ring",
            "until_eternity:aetherlight_ring",
            "until_eternity:resonance_armor"
    );

    @Test
    void everyStaticCurioIsCoveredByDataTags() throws IOException {
        Set<String> tagged = new HashSet<>();
        try (var files = Files.list(CURIO_ITEMS)) {
            for (Path file : files.filter(path ->
                    path.toString().endsWith(".json")).toList()) {
                var values = JsonParser.parseString(
                                Files.readString(file))
                        .getAsJsonObject()
                        .getAsJsonArray("values");
                values.forEach(value -> tagged.add(value.getAsString()));
            }
        }
        tagged.remove("until_eternity:dying_fury");

        assertEquals(23, STATIC_CURIOS.size());
        assertEquals(STATIC_CURIOS, tagged);
    }

    @Test
    void itemAttributeImplementationsDoNotReadEquippedSlotContext()
            throws IOException {
        List<String> implementations = List.of(
                "item/curio/BaseModCurioItem.java",
                "item/curio/IronAttributeCurioItem.java",
                "item/curio/CurioAttributeProfile.java",
                "item/curio/AttributeCurioItem.java",
                "item/curio/ImmuneCurioItem.java",
                "item/curio/LifeCapItem.java",
                "item/curio/DarkCageItem.java",
                "item/curio/MithrilGlovesItem.java",
                "item/curio/PewterGlovesItem.java",
                "item/curio/WarpedRingItem.java",
                "item/curio/VoidRingItem.java",
                "item/curio/charm/DivineSoulLampItem.java"
        );

        for (String relative : implementations) {
            String source = Files.readString(JAVA.resolve(relative));
            assertFalse(source.contains("slotContext.identifier()"), relative);
            assertFalse(source.contains("slotContext.index()"), relative);
            if (relative.endsWith("BaseModCurioItem.java")) {
                assertTrue(source.contains("slotContext.cosmetic()"), relative);
            } else {
                assertFalse(source.contains("slotContext.cosmetic()"), relative);
            }
            assertFalse(source.contains("CuriosApi.getSlotUuid"), relative);
        }
    }

    @Test
    void allRegisteredCurioClassesShareOneImplementation()
            throws IOException {
        String base = Files.readString(
                JAVA.resolve("item/curio/BaseModCurioItem.java"));
        assertTrue(base.contains(
                "extends Item implements ICurioItem"));

        try (var files = Files.walk(JAVA.resolve("item"))) {
            long directImplementations = files
                    .filter(path -> path.toString().endsWith(".java"))
                    .map(path -> {
                        try {
                            return Files.readString(path);
                        } catch (IOException exception) {
                            throw new RuntimeException(exception);
                        }
                    })
                    .filter(source -> source.contains("implements ICurioItem"))
                    .count();
            assertEquals(1L, directImplementations);
        }
    }

    @Test
    void baseBuildsImmutablePerSlotModifiersAndRejectsDuplicateKeys()
            throws IOException {
        String source = Files.readString(
                JAVA.resolve("item/curio/BaseModCurioItem.java"));

        assertTrue(source.contains("validateUniqueKeys(this.modifierSpecs)"));
        assertTrue(source.contains(
                "throw new IllegalArgumentException("));
        assertTrue(source.contains(
                "createModifierUuid(slotUuid, spec.modifierKey())"));
        assertTrue(source.contains("return builder.build()"));
        assertFalse(source.contains("staticModifiers"));
        assertFalse(source.contains("handler.isEquipped(this)"));
    }

    @Test
    void removedSlotUuidHelpersAndPerItemAttributeCompatDoNotRemain() {
        assertFalse(Files.exists(JAVA.resolve(
                "item/curio/VoidRingModifierIds.java")));
        assertFalse(Files.exists(JAVA.resolve(
                "compat/goetyrevelation/DivineSoulLampAttributeCompat.java")));
        assertFalse(Files.exists(JAVA.resolve(
                "item/curio/ModCurioSlots.java")));
    }

    @Test
    void proofSpecialAbilityUsesAnyEquippedFunctionalCurioSlot()
            throws IOException {
        String events = Files.readString(
                JAVA.resolve("event/CurioEventHandler.java"));
        assertTrue(events.contains(
                "handler.isEquipped(ModItems.PROOF_OF_SPURNER.get())"));
        assertFalse(events.contains(
                "proof_of_spurner\""));
    }
}
