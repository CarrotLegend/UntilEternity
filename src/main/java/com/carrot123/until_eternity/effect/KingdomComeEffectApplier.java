package com.carrot123.until_eternity.effect;

import com.carrot123.until_eternity.mixin.LivingEntityEffectAccess;
import com.carrot123.until_eternity.registry.ModMobEffects;
import java.util.Map;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

public final class KingdomComeEffectApplier {
    public static final int DURATION_TICKS = 200;

    private KingdomComeEffectApplier() {
    }

    public static void forceApply(LivingEntity target, Player owner) {
        if (target.level().isClientSide) {
            return;
        }
        MobEffect effect = ModMobEffects.KINGDOM_COME.get();
        MobEffectInstance replacement = new MobEffectInstance(effect, DURATION_TICKS, 0);
        KingdomComeOwnerTracker.remember(target, owner);
        LivingEntityEffectAccess access = (LivingEntityEffectAccess) target;
        Map<MobEffect, MobEffectInstance> effects = access.untilEternity$getActiveEffects();
        MobEffectInstance previous = effects.put(effect, replacement);
        if (previous == null) {
            access.untilEternity$onEffectAdded(replacement, owner);
        } else {
            access.untilEternity$onEffectUpdated(replacement, true, owner);
        }
    }
}
