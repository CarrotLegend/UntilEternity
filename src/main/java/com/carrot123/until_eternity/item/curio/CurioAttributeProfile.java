package com.carrot123.until_eternity.item.curio;

import com.carrot123.until_eternity.compat.GoetyRevelationAttributesCompat;
import com.carrot123.until_eternity.compat.PuffishAttributesCompat;
import com.carrot123.until_eternity.compat.TerraCurioCompat;
import com.carrot123.until_eternity.registry.ModAttributes;
import io.redspace.ironsspellbooks.api.registry.AttributeRegistry;
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
    VOID_GRIP("void_grip", List.of(
            spec(() -> PuffishAttributesCompat.resolve(PuffishAttributesCompat.MELEE_DAMAGE),
                    "melee_damage", 0.45D, AttributeModifier.Operation.MULTIPLY_TOTAL),
            spec(() -> Attributes.ATTACK_SPEED,
                    "attack_speed", 0.25D, AttributeModifier.Operation.MULTIPLY_TOTAL),
            spec(() -> PuffishAttributesCompat.resolve(PuffishAttributesCompat.KNOCKBACK),
                    "knockback", 1.0D, AttributeModifier.Operation.MULTIPLY_TOTAL),
            spec(() -> ForgeMod.ENTITY_REACH.get(),
                    "entity_reach", 2.0D, AttributeModifier.Operation.ADDITION)
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
    REDEMPTION_STAR("redemption_star", List.of(
            spec(() -> Attributes.LUCK,
                    "redemption_star/luck", 20.0D, AttributeModifier.Operation.ADDITION)
    )),
    UNSTABLE_HALO("unstable_halo", List.of(
            spec(() -> Attributes.LUCK,
                    "unstable_halo/luck", 10.0D, AttributeModifier.Operation.ADDITION),
            spec(ModAttributes.ALL_DAMAGE::get,
                    "unstable_halo/all_damage", 0.10D, AttributeModifier.Operation.MULTIPLY_BASE)
    )),
    KABBALAH_TREE("kabbalah_tree", List.of(
            spec(() -> Attributes.MAX_HEALTH,
                    "kabbalah_tree/max_health", 1.0D, AttributeModifier.Operation.MULTIPLY_TOTAL),
            spec(() -> PuffishAttributesCompat.resolve(PuffishAttributesCompat.RESISTANCE),
                    "kabbalah_tree/resistance", -0.10D, AttributeModifier.Operation.MULTIPLY_TOTAL),
            spec(() -> Attributes.LUCK,
                    "kabbalah_tree/luck", 10.0D, AttributeModifier.Operation.ADDITION),
            spec(() -> Attributes.ARMOR,
                    "kabbalah_tree/armor", 6.0D, AttributeModifier.Operation.ADDITION),
            spec(() -> Attributes.ATTACK_SPEED,
                    "kabbalah_tree/attack_speed", 0.10D, AttributeModifier.Operation.MULTIPLY_TOTAL),
            spec(() -> Attributes.MOVEMENT_SPEED,
                    "kabbalah_tree/movement_speed", -0.10D, AttributeModifier.Operation.MULTIPLY_TOTAL)
    )),
    ANCIENT_SHIELD("ancient_shield", List.of(
        spec(() -> Attributes.MAX_HEALTH,
                "max_health",
                20.0D,
                AttributeModifier.Operation.ADDITION),
        spec(() -> PuffishAttributesCompat.resolve(
                        PuffishAttributesCompat.RESISTANCE),
                "resistance",
                0.15D,
                AttributeModifier.Operation.MULTIPLY_TOTAL)
        )),
    PERMAFROST_CRYSTAL("permafrost_crystal", List.of(
            spec(() -> Attributes.ARMOR,
                    "permafrost_crystal/armor", 4.0D, AttributeModifier.Operation.ADDITION),
            spec(() -> PuffishAttributesCompat.resolve(PuffishAttributesCompat.RESISTANCE),
                    "permafrost_crystal/resistance", 0.15D,
                    AttributeModifier.Operation.MULTIPLY_TOTAL),
            spec(AttributeRegistry.SPELL_POWER::get,
                    "permafrost_crystal/spell_power", 0.25D,
                    AttributeModifier.Operation.MULTIPLY_BASE)
    )),
    EMPOWERED_SHIELD("empowered_shield", shieldSpecs(6.0D, 2.0D)),
    COSMIC_AEGIS("cosmic_aegis", shieldSpecs(8.0D, 4.0D)),
    PROOF_OF_SPURNER("proof_of_spurner", List.of(
        spec(ModAttributes.ALL_DAMAGE::get,
                "all_damage", 2.4D, AttributeModifier.Operation.MULTIPLY_BASE),
        spec(() -> Attributes.MAX_HEALTH,
                "max_health", 1200.0D, AttributeModifier.Operation.ADDITION),
        spec(() -> Attributes.ARMOR,
                "armor", 12.0D, AttributeModifier.Operation.ADDITION),
        spec(() -> Attributes.ARMOR_TOUGHNESS,
                "armor_toughness", 6.0D, AttributeModifier.Operation.ADDITION),
        spec(() -> Attributes.ATTACK_SPEED,
                "attack_speed", 0.15D, AttributeModifier.Operation.MULTIPLY_TOTAL),
        spec(() -> PuffishAttributesCompat.resolve(
                        PuffishAttributesCompat.KNOCKBACK),
                "knockback", 1.0D, AttributeModifier.Operation.MULTIPLY_TOTAL),
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
