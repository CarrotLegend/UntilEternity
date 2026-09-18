package com.carrot123.until_eternity.compat.legendarymonsters;

import com.carrot123.until_eternity.until_eternity;

import net.miauczel.legendary_monsters.entity.AnimatedMonster.IAnimatedBoss.PossessedPaladin.PossessedPaladinEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(
        modid = until_eternity.MODID,
        bus = Mod.EventBusSubscriber.Bus.FORGE
)
public final class PossessedPaladinActivationFix {

    private static final String INITIALIZED_KEY =
            "UntilEternityPossessedPaladinInitialized";

    private static final String AWAKENED_KEY =
            "UntilEternityPossessedPaladinAwakened";

    private PossessedPaladinActivationFix() {
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onEntityJoin(EntityJoinLevelEvent event) {

        if (event.getLevel().isClientSide()) {
            return;
        }

        if (!(event.getEntity()
                instanceof PossessedPaladinEntity paladin)) {
            return;
        }

        CompoundTag data =
                paladin.getPersistentData();

        if (!data.contains(
                INITIALIZED_KEY,
                Tag.TAG_BYTE
        )) {

            boolean awakened;

            if (event.loadedFromDisk()) {
                awakened =
                        paladin.getIsAwakened()
                                || !paladin.isSleep();
            } else {

                awakened = false;
            }

            data.putBoolean(
                    INITIALIZED_KEY,
                    true
            );

            data.putBoolean(
                    AWAKENED_KEY,
                    awakened
            );
        }

        restoreState(paladin);
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onLivingTick(
            LivingEvent.LivingTickEvent event
    ) {

        if (!(event.getEntity()
                instanceof PossessedPaladinEntity paladin)) {
            return;
        }

        if (paladin.level().isClientSide()) {
            return;
        }

        CompoundTag data =
                paladin.getPersistentData();

        if (!data.contains(
                INITIALIZED_KEY,
                Tag.TAG_BYTE
        )) {

            data.putBoolean(
                    INITIALIZED_KEY,
                    true
            );

            data.putBoolean(
                    AWAKENED_KEY,
                    false
            );
        }

        if (paladin.getIsAwakened()) {

            if (!data.getBoolean(AWAKENED_KEY)) {
                data.putBoolean(
                        AWAKENED_KEY,
                        true
                );
            }

            return;
        }

        if (data.getBoolean(AWAKENED_KEY)) {

            paladin.setAwakened(true);

            return;
        }

        if (!paladin.isSleep()) {
            paladin.setSleep(true);
        }

        if (paladin.isAlive()
                && paladin.getHealth()
                < paladin.getMaxHealth()) {

            paladin.setHealth(
                    paladin.getMaxHealth()
            );
        }

        paladin.fallDistance = 0.0F;
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onLivingAttack(
            LivingAttackEvent event
    ) {

        if (!(event.getEntity()
                instanceof PossessedPaladinEntity paladin)) {
            return;
        }

        if (paladin.level().isClientSide()) {
            return;
        }

        if (!isActivated(paladin)) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onLivingHurt(
            LivingHurtEvent event
    ) {

        if (!(event.getEntity()
                instanceof PossessedPaladinEntity paladin)) {
            return;
        }

        if (paladin.level().isClientSide()) {
            return;
        }

        if (!isActivated(paladin)) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onLivingDamage(
            LivingDamageEvent event
    ) {

        if (!(event.getEntity()
                instanceof PossessedPaladinEntity paladin)) {
            return;
        }

        if (paladin.level().isClientSide()) {
            return;
        }

        if (!isActivated(paladin)) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onLivingDeath(
            LivingDeathEvent event
    ) {

        if (!(event.getEntity()
                instanceof PossessedPaladinEntity paladin)) {
            return;
        }

        if (paladin.level().isClientSide()) {
            return;
        }

        if (isActivated(paladin)) {
            return;
        }

        event.setCanceled(true);

        paladin.setHealth(
                paladin.getMaxHealth()
        );

        paladin.setAwakened(false);

        if (!paladin.isSleep()) {
            paladin.setSleep(true);
        }
    }

    private static boolean isActivated(
            PossessedPaladinEntity paladin
    ) {

        CompoundTag data =
                paladin.getPersistentData();

        if (paladin.getIsAwakened()) {

            data.putBoolean(
                    INITIALIZED_KEY,
                    true
            );

            data.putBoolean(
                    AWAKENED_KEY,
                    true
            );

            return true;
        }

        return data.getBoolean(
                AWAKENED_KEY
        );
    }

    private static void restoreState(
            PossessedPaladinEntity paladin
    ) {

        CompoundTag data =
                paladin.getPersistentData();

        boolean awakened =
                data.getBoolean(AWAKENED_KEY);

        if (awakened) {

            paladin.setAwakened(true);

            if (paladin.isSleep()) {
                paladin.setSleep(false);
            }

        } else {

            paladin.setAwakened(false);

            if (!paladin.isSleep()) {
                paladin.setSleep(true);
            }

            paladin.setHealth(
                    paladin.getMaxHealth()
            );
        }
    }
}