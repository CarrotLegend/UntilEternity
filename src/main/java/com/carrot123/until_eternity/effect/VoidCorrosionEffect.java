package com.carrot123.until_eternity.effect;

import com.carrot123.until_eternity.compat.TargetDummyCompat;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;

public final class VoidCorrosionEffect extends MobEffect {

    public static final int COLOR = 0x4B0F6F;

    public VoidCorrosionEffect() {
        super(MobEffectCategory.HARMFUL, COLOR);
    }

    @Override
    public boolean isDurationEffectTick(
            int duration,
            int amplifier
    ) {
        return duration % 20 == 0;
    }

    @Override
    public void applyEffectTick(
            LivingEntity target,
            int amplifier
    ) {
        if (target.level().isClientSide) {
            return;
        }

        if (TargetDummyCompat.isTargetDummy(target)) {
            return;
        }

        float amount =
                VoidCorrosionDamageLogic.periodicDamage(
                        target.getMaxHealth(),
                        target.getHealth()
                );

        if (amount <= 0.0F) {
            return;
        }

        DamageSource source =
                target.damageSources().fellOutOfWorld();

        VoidCorrosionDamageContext.hurt(
                target,
                source,
                amount
        );
    }
}