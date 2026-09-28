package com.carrot123.until_eternity.combat;

import com.carrot123.until_eternity.network.ModNetworking;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;

public final class ArrowForcedDamageHelper {

    private ArrowForcedDamageHelper() {
    }

    public static boolean hurt(
            Entity target,
            DamageSource source,
            float amount
    ) {
        boolean normalResult =
                target.hurt(
                        source,
                        amount
                );

        if (normalResult) {
            return true;
        }

        if (target.level().isClientSide) {
            return false;
        }

        if (!(target instanceof LivingEntity living)) {
            return false;
        }

        if (!living.isAlive()
                || living.isDeadOrDying()
                || amount <= 0.0F
                || !Float.isFinite(amount)) {
            return false;
        }

        if (living instanceof Player player
                && player.getAbilities().invulnerable) {
            return false;
        }

        if (living.isInvulnerableTo(source)) {
            return false;
        }

        if (living.isDamageSourceBlocked(source)) {
            return false;
        }

        if (source.is(DamageTypeTags.IS_FIRE)
                && living.hasEffect(
                        MobEffects.FIRE_RESISTANCE
                )) {
            return false;
        }

        LivingAttackEvent attackEvent =
                new LivingAttackEvent(
                        living,
                        source,
                        amount
                );

        MinecraftForge.EVENT_BUS.post(
                attackEvent
        );

        if (attackEvent.isCanceled()) {
            return false;
        }

        LivingHurtEvent hurtEvent =
                new LivingHurtEvent(
                        living,
                        attackEvent.getSource(),
                        attackEvent.getAmount()
                );

        MinecraftForge.EVENT_BUS.post(
                hurtEvent
        );

        if (hurtEvent.isCanceled()) {
            return false;
        }

        float damage =
                hurtEvent.getAmount();

        if (!Float.isFinite(damage)
                || damage <= 0.0F) {
            return true;
        }

        damage =
                VanillaDamageHelper
                        .getDamageAfterArmorAbsorb(
                                living,
                                source,
                                damage
                        );

        damage =
                VanillaDamageHelper
                        .getDamageAfterMagicAbsorb(
                                living,
                                source,
                                damage
                        );

        if (!Float.isFinite(damage)
                || damage <= 0.0F) {
            return true;
        }

        LivingDamageEvent damageEvent =
                new LivingDamageEvent(
                        living,
                        source,
                        damage
                );

        MinecraftForge.EVENT_BUS.post(
                damageEvent
        );

        if (damageEvent.isCanceled()) {
            return false;
        }

        damage =
                damageEvent.getAmount();

        if (!Float.isFinite(damage)
                || damage <= 0.0F) {
            return true;
        }

        float absorption =
                living.getAbsorptionAmount();

        float healthDamage =
                Math.max(
                        damage - absorption,
                        0.0F
                );

        float absorbed =
                damage - healthDamage;

        if (absorbed > 0.0F) {
            living.setAbsorptionAmount(
                    Math.max(
                            0.0F,
                            absorption - absorbed
                    )
            );
        }

        Entity attacker =
                source.getEntity();

        if (attacker instanceof Player player) {
            living.setLastHurtByPlayer(
                    player
            );
        } else if (attacker instanceof LivingEntity livingAttacker) {
            living.setLastHurtByMob(
                    livingAttacker
            );
        }

        if (healthDamage > 0.0F) {
            float oldHealth =
                    living.getHealth();

            float newHealth =
                    Math.max(
                            0.0F,
                            oldHealth - healthDamage
                    );

            living.getCombatTracker()
                    .recordDamage(
                            source,
                            healthDamage
                    );

            living.setHealth(
                    newHealth
            );

            living.gameEvent(
                    GameEvent.ENTITY_DAMAGE
            );

            if (newHealth <= 0.0F
                    && oldHealth > 0.0F) {
                living.die(
                        source
                );
            }
        }

        ModNetworking.sendHurtAnimation(
                living
        );

        return true;
    }
}