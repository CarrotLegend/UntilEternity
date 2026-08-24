package com.carrot123.until_eternity.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

public final class CookingFrenzyEffect extends MobEffect {
    public static final int COLOR = 0xE65A24;
    public static final double ATTACK_SPEED_AMOUNT_PER_LEVEL = 0.02D;
    public static final String ATTACK_SPEED_UUID = "26c9568e-4df4-3fa9-bb50-63d92d8e9029";

    public CookingFrenzyEffect() {
        super(MobEffectCategory.BENEFICIAL, COLOR);
        this.addAttributeModifier(
                Attributes.ATTACK_SPEED,
                ATTACK_SPEED_UUID,
                ATTACK_SPEED_AMOUNT_PER_LEVEL,
                AttributeModifier.Operation.MULTIPLY_BASE);
    }
}
