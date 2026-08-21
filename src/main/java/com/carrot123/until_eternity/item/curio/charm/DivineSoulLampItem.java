package com.carrot123.until_eternity.item.curio.charm;

import com.carrot123.until_eternity.compat.GoetyRevelationAttributesCompat;
import com.carrot123.until_eternity.item.curio.BaseModCurioItem;
import com.carrot123.until_eternity.item.curio.CurioAttributeSpec;
import com.carrot123.until_eternity.registry.ModAttributes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.Rarity;

import java.util.List;

public final class DivineSoulLampItem extends BaseModCurioItem {
    static final double FOCUS_DAMAGE_AMOUNT = 0.66D;
    private static final ResourceLocation ITEM_ID =
            new ResourceLocation("until_eternity", "divine_soul_lamp");

    public DivineSoulLampItem() {
        super(
                new Properties().rarity(Rarity.COMMON),
                ITEM_ID,
                List.of(
                        CurioAttributeSpec.of(
                                () -> GoetyRevelationAttributesCompat.resolve(
                                        GoetyRevelationAttributesCompat.SOUL_DECREASE_REDUCTION),
                                "soul_reflux",
                                0.66D,
                                AttributeModifier.Operation.MULTIPLY_BASE),
                        CurioAttributeSpec.of(
                                () -> GoetyRevelationAttributesCompat.resolve(
                                        GoetyRevelationAttributesCompat.SOUL_INCREASE_EFFICIENCY),
                                "soul_affinity",
                                0.66D,
                                AttributeModifier.Operation.MULTIPLY_BASE),
                        CurioAttributeSpec.of(
                                () -> GoetyRevelationAttributesCompat.resolve(
                                        GoetyRevelationAttributesCompat.SPELL_POWER),
                                "spell_power_flat",
                                6.0D,
                                AttributeModifier.Operation.ADDITION),
                        CurioAttributeSpec.of(
                                () -> GoetyRevelationAttributesCompat.resolve(
                                        GoetyRevelationAttributesCompat.SPELL_POWER),
                                "spell_power_percent",
                                0.66D,
                                AttributeModifier.Operation.MULTIPLY_TOTAL),
                        CurioAttributeSpec.of(
                                () -> GoetyRevelationAttributesCompat.resolve(
                                        GoetyRevelationAttributesCompat.SPELL_POWER_MULTIPLIER),
                                "spell_power_multiplier",
                                0.66D,
                                AttributeModifier.Operation.ADDITION),
                        CurioAttributeSpec.of(
                                ModAttributes.FOCUS_DAMAGE,
                                "divine_soul_lamp_focus_damage",
                                FOCUS_DAMAGE_AMOUNT,
                                AttributeModifier.Operation.ADDITION)
                )
        );
    }
}
