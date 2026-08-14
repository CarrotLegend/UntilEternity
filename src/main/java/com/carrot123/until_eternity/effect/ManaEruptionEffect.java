package com.carrot123.until_eternity.effect;

import com.Polarice3.Goety.init.ModAttributes;
import io.redspace.ironsspellbooks.api.registry.AttributeRegistry;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;

public final class ManaEruptionEffect extends MobEffect {
    public static final int COLOR = 0x7047FF;
    public static final double ATTRIBUTE_AMOUNT_PER_LEVEL = 2.0D;
    public static final String IRONS_SPELL_POWER_UUID =
            "1c73d95a-fafe-38e0-973e-829f64787e33";
    public static final String GOETY_SPELL_POTENCY_UUID =
            "ce524cc2-bbdf-31c0-9058-063657c75cb2";

    public ManaEruptionEffect() {
        super(MobEffectCategory.BENEFICIAL, COLOR);
        this.addAttributeModifier(
                AttributeRegistry.SPELL_POWER.get(),
                IRONS_SPELL_POWER_UUID,
                ATTRIBUTE_AMOUNT_PER_LEVEL,
                AttributeModifier.Operation.ADDITION);
        this.addAttributeModifier(
                ModAttributes.SPELL_POTENCY.get(),
                GOETY_SPELL_POTENCY_UUID,
                ATTRIBUTE_AMOUNT_PER_LEVEL,
                AttributeModifier.Operation.ADDITION);
    }
}
