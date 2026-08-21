package com.carrot123.until_eternity.item.curio;

import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

class AllCurioModifierIdTest {
    private static final UUID FIRST_SLOT =
            UUID.fromString("12345678-1234-5678-9abc-def012345678");
    private static final UUID SECOND_SLOT =
            UUID.fromString("87654321-4321-8765-cba9-876543210fed");
    private static final Map<String, List<String>> MODIFIER_KEYS =
            createModifierKeys();

    @Test
    void allStaticItemsHaveUniqueKeysWithinOneItem() {
        assertEquals(23, MODIFIER_KEYS.size());
        assertEquals(52, MODIFIER_KEYS.values().stream()
                .mapToInt(List::size)
                .sum());
        MODIFIER_KEYS.forEach((item, keys) ->
                assertEquals(
                        keys.size(),
                        keys.stream()
                                .map(key -> CurioModifierId.create(
                                        FIRST_SLOT, key))
                                .distinct()
                                .count(),
                        item));
    }

    @Test
    void twoSpellPowerModifiersOnDivineLampDoNotCollide() {
        assertNotEquals(
                CurioModifierId.create(FIRST_SLOT, "spell_power_flat"),
                CurioModifierId.create(FIRST_SLOT, "spell_power_percent"));
    }

    @Test
    void everyModifierChangesUuidWhenEquippedInAnotherSlot() {
        MODIFIER_KEYS.forEach((item, keys) -> keys.forEach(key ->
                assertNotEquals(
                        CurioModifierId.create(FIRST_SLOT, key),
                        CurioModifierId.create(SECOND_SLOT, key),
                        item + "/" + key)));
    }

    private static Map<String, List<String>> createModifierKeys() {
        Map<String, List<String>> result = new LinkedHashMap<>();
        result.put("elemental_gauntlet", List.of(
                "melee_damage", "attack_speed", "attack_knockback",
                "entity_reach"));
        result.put("reaper_tooth_necklace", List.of(
                "melee_damage", "armor_pass"));
        result.put("sand_shark_tooth_necklace", List.of(
                "melee_damage", "armor_pass"));
        result.put("regenerator", List.of("healing", "max_health"));
        result.put("guttering_candle", List.of("max_health"));
        result.put("empowered_shield", List.of(
                "armor", "armor_toughness", "knockback_resistance"));
        result.put("cosmic_aegis", List.of(
                "armor", "armor_toughness", "knockback_resistance"));
        result.put("proof_of_spurner", List.of(
                "attack_damage", "attack_speed", "knockback", "max_health",
                "armor", "armor_toughness", "damage_resistance",
                "armor_shred", "protection_shred"));
        result.put("dark_cage", List.of(
                "body_slots", "spell_power", "dark_cage_focus_damage"));
        result.put("mithril_gloves", List.of("magic_ring_slots"));
        result.put("pewter_gloves", List.of("warped_ring_slots"));
        result.put("divine_soul_lamp", List.of(
                "soul_reflux", "soul_affinity", "spell_power_flat",
                "spell_power_percent", "spell_power_multiplier",
                "divine_soul_lamp_focus_damage"));
        result.put("ring_of_warped_magic", List.of("spell_power"));
        result.put("advanced_ring_of_warped_magic", List.of(
                "spell_power_multiplier"));
        result.put("ring_of_soul_craving", List.of(
                "soul_increase_efficiency"));
        result.put("ring_of_purity", List.of("soul_decrease_reduction"));
        result.put("ring_of_warped_chanting", List.of("cast_duration"));
        result.put("ring_of_warped_cooling", List.of("spell_cooldown"));
        result.put("void_ring", List.of(
                "spell_power", "spell_power_multiplier",
                "void_ring_focus_damage"));
        result.put("empowered_ring", List.of(
                "empowered_ring/spell_power"));
        result.put("advanced_empowered_ring", List.of(
                "advanced_empowered_ring/spell_power"));
        result.put("aetherlight_ring", List.of(
                "aetherlight_ring/spell_power",
                "aetherlight_ring/max_mana",
                "aetherlight_ring/mana_regen"));
        result.put("resonance_armor", List.of(
                "resonance_armor/cooldown_reduction"));
        return Map.copyOf(result);
    }
}
