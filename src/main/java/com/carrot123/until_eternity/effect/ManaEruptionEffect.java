package com.carrot123.until_eternity.effect;

import com.carrot123.until_eternity.registry.ModAttributes;
import io.redspace.ironsspellbooks.api.registry.AttributeRegistry;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;

public final class ManaEruptionEffect extends MobEffect {
    public static final int COLOR = 0x7047FF;
    public static final double ATTRIBUTE_AMOUNT_PER_LEVEL = 1.0D;
    public static final double FOCUS_DAMAGE_AMOUNT_PER_LEVEL = 0.10D;
    public static final String IRONS_SPELL_POWER_UUID =
            "1c73d95a-fafe-38e0-973e-829f64787e33";
    public static final String FOCUS_DAMAGE_UUID =
            "d0b0a8a2-03be-38ee-b6f0-225d3e6ba086";

    public ManaEruptionEffect() {
        super(MobEffectCategory.BENEFICIAL, COLOR);
        this.addAttributeModifier(
                AttributeRegistry.SPELL_POWER.get(),
                IRONS_SPELL_POWER_UUID,
                ATTRIBUTE_AMOUNT_PER_LEVEL,
                AttributeModifier.Operation.ADDITION);
        this.addAttributeModifier(
                ModAttributes.FOCUS_DAMAGE.get(),
                FOCUS_DAMAGE_UUID,
                FOCUS_DAMAGE_AMOUNT_PER_LEVEL,
                AttributeModifier.Operation.MULTIPLY_BASE);
    }
}
