package com.carrot123.until_eternity.effect;

import com.carrot123.until_eternity.mixin.LivingEntityEffectAccess;
import com.carrot123.until_eternity.registry.ModMobEffects;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

import java.util.Map;

public final class VoidCorrosionEffectApplier {
    public static final int DURATION_TICKS = 100;
    public static final int AMPLIFIER = 0;

    private VoidCorrosionEffectApplier() {
    }

    public static void forceApply(
            LivingEntity target,
            Player source
    ) {
        if (target.level().isClientSide) {
            return;
        }

        MobEffect effect = ModMobEffects.VOID_CORROSION.get();
        MobEffectInstance replacement = new MobEffectInstance(
                effect, DURATION_TICKS, AMPLIFIER);
        VoidCorrosionSourceTracker.remember(target, source);
        LivingEntityEffectAccess access = (LivingEntityEffectAccess) target;
        Map<MobEffect, MobEffectInstance> activeEffects =
                access.untilEternity$getActiveEffects();
        MobEffectInstance previous = activeEffects.put(effect, replacement);
        if (previous == null) {
            access.untilEternity$onEffectAdded(replacement, source);
        } else {
            access.untilEternity$onEffectUpdated(replacement, true, source);
        }
    }
}
