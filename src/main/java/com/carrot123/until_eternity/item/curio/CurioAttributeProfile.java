package com.carrot123.until_eternity.item.curio;

import com.carrot123.until_eternity.compat.GoetyRevelationAttributesCompat;
import com.carrot123.until_eternity.compat.PuffishAttributesCompat;
import com.carrot123.until_eternity.compat.TerraCurioCompat;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraftforge.common.ForgeMod;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

public enum CurioAttributeProfile {
    ELEMENTAL_GAUNTLET("elemental_gauntlet", List.of(
            spec(() -> PuffishAttributesCompat.resolve(PuffishAttributesCompat.MELEE_DAMAGE),
                    "melee_damage", 0.25D, AttributeModifier.Operation.MULTIPLY_TOTAL),
            spec(() -> Attributes.ATTACK_SPEED,
                    "attack_speed", 0.15D, AttributeModifier.Operation.MULTIPLY_TOTAL),
            spec(() -> Attributes.ATTACK_KNOCKBACK,
                    "attack_knockback", 1.0D, AttributeModifier.Operation.ADDITION),
            spec(() -> ForgeMod.ENTITY_REACH.get(),
                    "entity_reach", 0.1D, AttributeModifier.Operation.ADDITION)
    )),
    REAPER_TOOTH_NECKLACE("reaper_tooth_necklace", List.of(
            spec(() -> PuffishAttributesCompat.resolve(PuffishAttributesCompat.MELEE_DAMAGE),
                    "melee_damage", 0.20D, AttributeModifier.Operation.MULTIPLY_TOTAL),
            spec(TerraCurioCompat::armorPass,
                    "armor_pass", 15.0D, AttributeModifier.Operation.ADDITION)
    )),
    SAND_SHARK_TOOTH_NECKLACE("sand_shark_tooth_necklace", List.of(
            spec(() -> PuffishAttributesCompat.resolve(PuffishAttributesCompat.MELEE_DAMAGE),
                    "melee_damage", 0.10D, AttributeModifier.Operation.MULTIPLY_TOTAL),
            spec(TerraCurioCompat::armorPass,
                    "armor_pass", 10.0D, AttributeModifier.Operation.ADDITION)
    )),
    REGENERATOR("regenerator", List.of(
            spec(() -> PuffishAttributesCompat.resolve(PuffishAttributesCompat.HEALING),
                    "healing", 0.20D, AttributeModifier.Operation.MULTIPLY_TOTAL),
            spec(() -> Attributes.MAX_HEALTH,
                    "max_health", 0.15D, AttributeModifier.Operation.MULTIPLY_TOTAL)
    )),
    GUTTERING_CANDLE("guttering_candle", List.of(
            spec(() -> Attributes.MAX_HEALTH,
                    "max_health", 0.30D, AttributeModifier.Operation.MULTIPLY_TOTAL)
    )),
    EMPOWERED_SHIELD("empowered_shield", shieldSpecs(6.0D, 2.0D)),
    COSMIC_AEGIS("cosmic_aegis", shieldSpecs(8.0D, 4.0D)),
    PROOF_OF_SPURNER("proof_of_spurner", List.of(
            spec(() -> Attributes.ATTACK_DAMAGE,
                    "attack_damage", 2.0D, AttributeModifier.Operation.MULTIPLY_TOTAL),
            spec(() -> Attributes.ATTACK_SPEED,
                    "attack_speed", 0.15D, AttributeModifier.Operation.MULTIPLY_TOTAL),
            spec(() -> PuffishAttributesCompat.resolve(PuffishAttributesCompat.KNOCKBACK),
                    "knockback", 1.0D, AttributeModifier.Operation.MULTIPLY_TOTAL),
            spec(() -> Attributes.MAX_HEALTH,
                    "max_health", 100.0D, AttributeModifier.Operation.ADDITION),
            spec(() -> Attributes.ARMOR,
                    "armor", 8.0D, AttributeModifier.Operation.ADDITION),
            spec(() -> Attributes.ARMOR_TOUGHNESS,
                    "armor_toughness", 4.0D, AttributeModifier.Operation.ADDITION),
            spec(() -> GoetyRevelationAttributesCompat.resolve(
                            GoetyRevelationAttributesCompat.DAMAGE_RESISTANCE),
                    "damage_resistance", 0.70D, AttributeModifier.Operation.MULTIPLY_TOTAL),
            spec(() -> PuffishAttributesCompat.resolve(
                            PuffishAttributesCompat.ARMOR_SHRED),
                    "armor_shred", 1.0D, AttributeModifier.Operation.MULTIPLY_BASE),
            spec(() -> PuffishAttributesCompat.resolve(
                            PuffishAttributesCompat.PROTECTION_SHRED),
                    "protection_shred", 0.50D, AttributeModifier.Operation.MULTIPLY_BASE)
    ));

    private static final String MOD_ID = "until_eternity";
    private final ResourceLocation itemId;
    private final List<CurioAttributeSpec> modifierSpecs;

    CurioAttributeProfile(String itemPath, List<CurioAttributeSpec> modifierSpecs) {
        this.itemId = new ResourceLocation(MOD_ID, itemPath);
        this.modifierSpecs = List.copyOf(modifierSpecs);
    }

    public ResourceLocation itemId() {
        return itemId;
    }

    public List<CurioAttributeSpec> modifierSpecs() {
        return modifierSpecs;
    }

    int expectedModifierCount() {
        return modifierSpecs.size();
    }

    private static CurioAttributeSpec spec(
            java.util.function.Supplier<? extends net.minecraft.world.entity.ai.attributes.Attribute> attribute,
            String key,
            double amount,
            AttributeModifier.Operation operation
    ) {
        return CurioAttributeSpec.of(attribute, key, amount, operation);
    }

    private static List<CurioAttributeSpec> shieldSpecs(
            double armor,
            double toughness
    ) {
        return List.of(
                spec(() -> Attributes.ARMOR,
                        "armor", armor, AttributeModifier.Operation.ADDITION),
                spec(() -> Attributes.ARMOR_TOUGHNESS,
                        "armor_toughness", toughness, AttributeModifier.Operation.ADDITION),
                spec(() -> Attributes.KNOCKBACK_RESISTANCE,
                        "knockback_resistance", 1.0D, AttributeModifier.Operation.ADDITION)
        );
    }
}
