package com.carrot123.until_eternity.combat;

import com.carrot123.until_eternity.mixin.LivingEntityDamageStateAccessor;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraftforge.common.ForgeHooks;

/** Applies the True Chef's Knife primary hit without entering the victim's hurt method. */
public final class TrueChefsKnifeDirectDamage {
    private static final int HURT_DURATION_TICKS = 10;
    private static final int INVULNERABLE_TIME_TICKS = 20;
    private static final int PLAYER_KILL_CREDIT_TICKS = 100;

    private TrueChefsKnifeDirectDamage() {
    }

    public static boolean apply(
            Player attacker,
            LivingEntity victim,
            DamageSource source,
            float amount) {
        if (attacker.level().isClientSide
                || victim.isRemoved()
                || !victim.isAlive()
                || !Float.isFinite(amount)
                || amount <= 0.0F) {
            return false;
        }

        float healthBefore = victim.getHealth();
        ForgeHooks.onLivingAttack(victim, source, amount);
        float afterHurtHook = preserveIncrease(
                amount,
                ForgeHooks.onLivingHurt(victim, source, amount));
        float finalDamage = preserveIncrease(
                afterHurtHook,
                ForgeHooks.onLivingDamage(victim, source, afterHurtHook));

        if (victim.isRemoved() || !victim.isAlive()) {
            return true;
        }

        if (victim.isSleeping()) {
            victim.stopSleeping();
        }
        victim.setNoActionTime(0);
        victim.walkAnimation.setSpeed(1.5F);
        victim.setLastHurtByMob(attacker);
        victim.setLastHurtByPlayer(attacker);

        LivingEntityDamageStateAccessor state = (LivingEntityDamageStateAccessor) victim;
        state.untilEternity$setLastHurt(finalDamage);
        state.untilEternity$setLastHurtByPlayerTime(PLAYER_KILL_CREDIT_TICKS);
        state.untilEternity$setLastDamageSource(source);
        state.untilEternity$setLastDamageStamp(victim.level().getGameTime());

        victim.invulnerableTime = INVULNERABLE_TIME_TICKS;
        victim.hurtDuration = HURT_DURATION_TICKS;
        victim.hurtTime = victim.hurtDuration;
        victim.hurtMarked = true;
        victim.getCombatTracker().recordDamage(source, finalDamage);

        float requiredHealth = Math.max(0.0F, healthBefore - finalDamage);
        if (victim.getHealth() > requiredHealth) {
            victim.setHealth(requiredHealth);
        }

        victim.gameEvent(GameEvent.ENTITY_DAMAGE);
        victim.level().broadcastDamageEvent(victim, source);
        triggerAdvancements(attacker, victim, source, finalDamage);

        if (victim.getHealth() <= 0.0F && !victim.isRemoved()) {
            victim.die(source);
        }
        return true;
    }

    static float preserveIncrease(float previous, float returned) {
        return Float.isFinite(returned) ? Math.max(previous, returned) : previous;
    }

    private static void triggerAdvancements(
            Player attacker,
            LivingEntity victim,
            DamageSource source,
            float finalDamage) {
        if (victim instanceof ServerPlayer serverVictim) {
            CriteriaTriggers.ENTITY_HURT_PLAYER.trigger(
                    serverVictim, source, finalDamage, finalDamage, false);
        }
        if (attacker instanceof ServerPlayer serverAttacker) {
            CriteriaTriggers.PLAYER_HURT_ENTITY.trigger(
                    serverAttacker, victim, source, finalDamage, finalDamage, false);
        }
    }
}
