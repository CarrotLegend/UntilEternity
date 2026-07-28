package com.carrot123.until_eternity.item.curio;

import com.carrot123.until_eternity.compat.GoetyRevelationAttributesCompat;
import com.carrot123.until_eternity.compat.PuffishAttributesCompat;
import com.carrot123.until_eternity.compat.TerraCurioCompat;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraftforge.common.ForgeMod;
import top.theillusivec4.curios.api.SlotContext;

import javax.annotation.Nullable;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

public enum CurioAttributeProfile {
    ELEMENTAL_GAUNTLET("hands") {
        @Override
        void addModifiers(ImmutableMultimap.Builder<Attribute, AttributeModifier> builder, UUID slotUuid) {
            add(builder, PuffishAttributesCompat.resolve(PuffishAttributesCompat.MELEE_DAMAGE),
                    slotUuid, "elemental_gauntlet/melee_damage", 0.25D,
                    AttributeModifier.Operation.MULTIPLY_TOTAL);
            add(builder, Attributes.ATTACK_SPEED, slotUuid,
                    "elemental_gauntlet/attack_speed", 0.15D,
                    AttributeModifier.Operation.MULTIPLY_TOTAL);
            add(builder, Attributes.ATTACK_KNOCKBACK, slotUuid,
                    "elemental_gauntlet/attack_knockback", 1.0D,
                    AttributeModifier.Operation.ADDITION);
            add(builder, ForgeMod.ENTITY_REACH.get(), slotUuid,
                    "elemental_gauntlet/entity_reach", 0.1D,
                    AttributeModifier.Operation.ADDITION);
        }
    },
    REAPER_TOOTH_NECKLACE("necklace") {
        @Override
        void addModifiers(ImmutableMultimap.Builder<Attribute, AttributeModifier> builder, UUID slotUuid) {
            add(builder, PuffishAttributesCompat.resolve(PuffishAttributesCompat.MELEE_DAMAGE),
                    slotUuid, "reaper_tooth_necklace/melee_damage", 0.20D,
                    AttributeModifier.Operation.MULTIPLY_TOTAL);
            add(builder, TerraCurioCompat.armorPass(), slotUuid,
                    "reaper_tooth_necklace/armor_pass", 15.0D,
                    AttributeModifier.Operation.ADDITION);
        }
    },
    SAND_SHARK_TOOTH_NECKLACE("necklace") {
        @Override
        void addModifiers(ImmutableMultimap.Builder<Attribute, AttributeModifier> builder, UUID slotUuid) {
            add(builder, PuffishAttributesCompat.resolve(PuffishAttributesCompat.MELEE_DAMAGE),
                    slotUuid, "sand_shark_tooth_necklace/melee_damage", 0.10D,
                    AttributeModifier.Operation.MULTIPLY_TOTAL);
            add(builder, TerraCurioCompat.armorPass(), slotUuid,
                    "sand_shark_tooth_necklace/armor_pass", 10.0D,
                    AttributeModifier.Operation.ADDITION);
        }
    },
    REGENERATOR("necklace") {
        @Override
        void addModifiers(ImmutableMultimap.Builder<Attribute, AttributeModifier> builder, UUID slotUuid) {
            add(builder, PuffishAttributesCompat.resolve(PuffishAttributesCompat.HEALING),
                    slotUuid, "regenerator/healing", 0.20D,
                    AttributeModifier.Operation.MULTIPLY_TOTAL);
            add(builder, Attributes.MAX_HEALTH, slotUuid,
                    "regenerator/max_health", 0.15D,
                    AttributeModifier.Operation.MULTIPLY_TOTAL);
        }
    },
    GUTTERING_CANDLE("necklace") {
        @Override
        void addModifiers(ImmutableMultimap.Builder<Attribute, AttributeModifier> builder, UUID slotUuid) {
            add(builder, Attributes.MAX_HEALTH, slotUuid,
                    "guttering_candle/max_health", 0.30D,
                    AttributeModifier.Operation.MULTIPLY_TOTAL);
        }
    },
    EMPOWERED_SHIELD("back") {
        @Override
        void addModifiers(ImmutableMultimap.Builder<Attribute, AttributeModifier> builder, UUID slotUuid) {
            addShieldModifiers(builder, slotUuid, "empowered_shield", 6.0D, 2.0D);
        }
    },
    COSMIC_AEGIS("back") {
        @Override
        void addModifiers(ImmutableMultimap.Builder<Attribute, AttributeModifier> builder, UUID slotUuid) {
            addShieldModifiers(builder, slotUuid, "cosmic_aegis", 8.0D, 4.0D);
        }
    },
    PROOF_OF_SPURNER("back") {
        @Override
        void addModifiers(ImmutableMultimap.Builder<Attribute, AttributeModifier> builder, UUID slotUuid) {
            add(builder, Attributes.ATTACK_DAMAGE, slotUuid,
                    "proof_of_spurner/attack_damage", 2.0D,
                    AttributeModifier.Operation.MULTIPLY_TOTAL);
            add(builder, Attributes.ATTACK_SPEED, slotUuid,
                    "proof_of_spurner/attack_speed", 0.15D,
                    AttributeModifier.Operation.MULTIPLY_TOTAL);
            add(builder, PuffishAttributesCompat.resolve(PuffishAttributesCompat.KNOCKBACK),
                    slotUuid, "proof_of_spurner/knockback", 1.0D,
                    AttributeModifier.Operation.MULTIPLY_TOTAL);
            add(builder, Attributes.MAX_HEALTH, slotUuid,
                    "proof_of_spurner/max_health", 100.0D,
                    AttributeModifier.Operation.ADDITION);
            add(builder, Attributes.ARMOR, slotUuid,
                    "proof_of_spurner/armor", 8.0D,
                    AttributeModifier.Operation.ADDITION);
            add(builder, Attributes.ARMOR_TOUGHNESS, slotUuid,
                    "proof_of_spurner/armor_toughness", 4.0D,
                    AttributeModifier.Operation.ADDITION);
            add(builder, GoetyRevelationAttributesCompat.resolve(
                            GoetyRevelationAttributesCompat.DAMAGE_RESISTANCE),
                    slotUuid, "proof_of_spurner/damage_resistance", 0.70D,
                    AttributeModifier.Operation.MULTIPLY_TOTAL);
            add(builder, GoetyRevelationAttributesCompat.resolve(
                            GoetyRevelationAttributesCompat.ARMOR_PENETRATION),
                    slotUuid, "proof_of_spurner/armor_penetration", 1.0D,
                    AttributeModifier.Operation.MULTIPLY_TOTAL);
            add(builder, GoetyRevelationAttributesCompat.resolve(
                            GoetyRevelationAttributesCompat.ENCHANTMENT_PIERCING),
                    slotUuid, "proof_of_spurner/enchantment_piercing", 0.50D,
                    AttributeModifier.Operation.MULTIPLY_TOTAL);
        }
    };

    private static final String SALT_PREFIX = "until_eternity:";
    private final String slot;

    CurioAttributeProfile(String slot) {
        this.slot = slot;
    }

    public Multimap<Attribute, AttributeModifier> getModifiers(
            SlotContext slotContext,
            UUID slotUuid
    ) {
        if (!slot.equals(slotContext.identifier())) {
            return ImmutableMultimap.of();
        }
        ImmutableMultimap.Builder<Attribute, AttributeModifier> builder =
                ImmutableMultimap.builder();
        addModifiers(builder, slotUuid);
        return builder.build();
    }

    abstract void addModifiers(
            ImmutableMultimap.Builder<Attribute, AttributeModifier> builder,
            UUID slotUuid
    );

    static UUID deriveModifierUuid(UUID slotUuid, String itemAndAttribute) {
        String source = slotUuid + "|" + SALT_PREFIX + itemAndAttribute;
        return UUID.nameUUIDFromBytes(source.getBytes(StandardCharsets.UTF_8));
    }

    private static void addShieldModifiers(
            ImmutableMultimap.Builder<Attribute, AttributeModifier> builder,
            UUID slotUuid,
            String itemId,
            double armor,
            double toughness
    ) {
        add(builder, Attributes.ARMOR, slotUuid, itemId + "/armor",
                armor, AttributeModifier.Operation.ADDITION);
        add(builder, Attributes.ARMOR_TOUGHNESS, slotUuid,
                itemId + "/armor_toughness", toughness,
                AttributeModifier.Operation.ADDITION);
        add(builder, Attributes.KNOCKBACK_RESISTANCE, slotUuid,
                itemId + "/knockback_resistance", 1.0D,
                AttributeModifier.Operation.ADDITION);
    }

    private static void add(
            ImmutableMultimap.Builder<Attribute, AttributeModifier> builder,
            @Nullable Attribute attribute,
            UUID slotUuid,
            String itemAndAttribute,
            double amount,
            AttributeModifier.Operation operation
    ) {
        if (attribute == null) {
            return;
        }
        String salt = SALT_PREFIX + itemAndAttribute;
        builder.put(attribute, new AttributeModifier(
                deriveModifierUuid(slotUuid, itemAndAttribute),
                salt,
                amount,
                operation
        ));
    }
}
