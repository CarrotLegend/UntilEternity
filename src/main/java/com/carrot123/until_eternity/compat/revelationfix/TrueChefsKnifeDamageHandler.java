package com.carrot123.until_eternity.compat.revelationfix;

import com.carrot123.until_eternity.combat.AbsoluteDamageMath;
import com.carrot123.until_eternity.combat.TrueChefsKnifeAbsoluteDamageContext;
import com.mega.revelationfix.safe.DamageSourceInterface;
import com.mega.revelationfix.safe.entity.LivingEventEC;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

/** RevelationFix-backed absolute damage for the True Chef's Knife only. */
public final class TrueChefsKnifeDamageHandler {
    private TrueChefsKnifeDamageHandler() {
    }

    public static void register() {
        TrueChefsKnifeAbsoluteDamageContext.installBypassMarker(
                TrueChefsKnifeDamageHandler::markBypassAll);
        MinecraftForge.EVENT_BUS.register(TrueChefsKnifeDamageHandler.class);
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST, receiveCanceled = true)
    public static void onLivingAttack(LivingAttackEvent event) {
        if (!TrueChefsKnifeAbsoluteDamageContext.matches(
                event.getEntity(), event.getSource())) {
            return;
        }
        event.setCanceled(false);
        markBypassAll(event.getSource());
        lockUnCancelable(event);
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST, receiveCanceled = true)
    public static void onLivingHurt(LivingHurtEvent event) {
        LivingEntity target = event.getEntity();
        DamageSource source = event.getSource();
        if (!TrueChefsKnifeAbsoluteDamageContext.matches(target, source)) {
            return;
        }

        event.setCanceled(false);
        markBypassAll(source);
        float damage = TrueChefsKnifeAbsoluteDamageContext.originalDamage(target, source);
        if (Float.isFinite(damage) && damage > 0.0F
                && TrueChefsKnifeAbsoluteDamageContext.claimSplit(target, source)) {
            AbsoluteDamageMath.Split split = AbsoluteDamageMath.split(
                    damage, target.getHealth(), target.isAlive());
            if (split.directDamage() > 0.0F) {
                target.setHealth(target.getHealth() - split.directDamage());
            }
            event.setAmount(split.eventDamage());
        }
        lockUnCancelable(event);
        lockAmountUp(event);
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST, receiveCanceled = true)
    public static void onLivingDamage(LivingDamageEvent event) {
        if (!TrueChefsKnifeAbsoluteDamageContext.matches(
                event.getEntity(), event.getSource())) {
            return;
        }
        event.setCanceled(false);
        lockUnCancelable(event);
        lockAmountUp(event);
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST, receiveCanceled = true)
    public static void onLivingDeath(LivingDeathEvent event) {
        if (!TrueChefsKnifeAbsoluteDamageContext.matches(
                event.getEntity(), event.getSource())) {
            return;
        }
        event.setCanceled(false);
        lockUnCancelable(event);
    }

    private static void markBypassAll(DamageSource source) {
        ((DamageSourceInterface) (Object) source)
                .revelationfix$setBypassAll(true);
    }

    private static void lockUnCancelable(Object event) {
        ((LivingEventEC) event).revelationfix$hackedUnCancelable(true);
    }

    private static void lockAmountUp(Object event) {
        ((LivingEventEC) event).revelationfix$hackedOnlyAmountUp(true);
    }
}
