package com.carrot123.until_eternity.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeMap;

public final class KingdomComeEffect extends MobEffect {
    public KingdomComeEffect() {
        super(MobEffectCategory.HARMFUL, 0xD4B76A);
    }

    @Override
    public void removeAttributeModifiers(LivingEntity target, AttributeMap attributes, int amplifier) {
        super.removeAttributeModifiers(target, attributes, amplifier);
        if (!target.hasEffect(this)) {
            KingdomComeOwnerTracker.clear(target);
        }
    }
}
