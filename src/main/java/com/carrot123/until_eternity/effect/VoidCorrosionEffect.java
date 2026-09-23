package com.carrot123.until_eternity.effect;

import com.carrot123.until_eternity.registry.ModDamageTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeMap;

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
        if (!(target.level() instanceof ServerLevel serverLevel)) {
            return;
        }

        float amount =
                VoidCorrosionDamageLogic.periodicDamage(
                        target.getMaxHealth()
                );

        if (amount <= 0.0F) {
            return;
        }

        ServerPlayer attacker = VoidCorrosionSourceTracker.resolve(target);
        DamageSource source = ModDamageTypes.bypassAll(serverLevel, attacker);

        VoidCorrosionDamageContext.hurt(
                target,
                source,
                amount
        );
    }

    @Override
    public void removeAttributeModifiers(
            LivingEntity target,
            AttributeMap attributes,
            int amplifier
    ) {
        super.removeAttributeModifiers(target, attributes, amplifier);
        if (!target.hasEffect(this)) {
            VoidCorrosionSourceTracker.clear(target);
        }
    }
}
