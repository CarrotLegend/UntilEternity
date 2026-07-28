package com.carrot123.until_eternity.item.curio;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

class CurioAttributeProfileTest {
    private static final UUID FIRST_SLOT =
            UUID.fromString("aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee");
    private static final UUID SECOND_SLOT =
            UUID.fromString("11111111-2222-3333-4444-555555555555");

    private static final List<String> SALTS = List.of(
            "elemental_gauntlet/melee_damage",
            "elemental_gauntlet/attack_speed",
            "elemental_gauntlet/attack_knockback",
            "elemental_gauntlet/entity_reach",
            "reaper_tooth_necklace/melee_damage",
            "reaper_tooth_necklace/armor_pass",
            "sand_shark_tooth_necklace/melee_damage",
            "sand_shark_tooth_necklace/armor_pass",
            "regenerator/healing",
            "regenerator/max_health",
            "guttering_candle/max_health",
            "empowered_shield/armor",
            "empowered_shield/armor_toughness",
            "empowered_shield/knockback_resistance",
            "cosmic_aegis/armor",
            "cosmic_aegis/armor_toughness",
            "cosmic_aegis/knockback_resistance",
            "proof_of_spurner/attack_damage",
            "proof_of_spurner/attack_speed",
            "proof_of_spurner/knockback",
            "proof_of_spurner/max_health",
            "proof_of_spurner/armor",
            "proof_of_spurner/armor_toughness",
            "proof_of_spurner/damage_resistance",
            "proof_of_spurner/armor_penetration",
            "proof_of_spurner/enchantment_piercing"
    );

    @Test
    void allModifierSaltsAreUniqueWithinOneSlot() {
        Set<UUID> uuids = SALTS.stream()
                .map(salt -> CurioAttributeProfile.deriveModifierUuid(FIRST_SLOT, salt))
                .collect(Collectors.toSet());

        assertEquals(SALTS.size(), uuids.size());
    }

    @Test
    void derivationIsStable() {
        String salt = "proof_of_spurner/attack_damage";
        assertEquals(
                CurioAttributeProfile.deriveModifierUuid(FIRST_SLOT, salt),
                CurioAttributeProfile.deriveModifierUuid(FIRST_SLOT, salt)
        );
    }

    @Test
    void differentSlotsDoNotShareModifierUuids() {
        String salt = "regenerator/max_health";
        assertNotEquals(
                CurioAttributeProfile.deriveModifierUuid(FIRST_SLOT, salt),
                CurioAttributeProfile.deriveModifierUuid(SECOND_SLOT, salt)
        );
    }
}
