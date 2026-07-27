package com.carrot123.until_eternity.compat.legendarymonsters;

import com.mojang.logging.LogUtils;
import net.miauczel.legendary_monsters.effect.ModEffects;
import net.miauczel.legendary_monsters.entity.AnimatedMonster.Projectile.SoulStrike;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;
import org.slf4j.Logger;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

public final class SoulGreatSwordCompat {
    public static final ResourceLocation SOUL_GREAT_SWORD_ID = legendaryMonstersId("soul_great_sword");
    private static final ResourceLocation BLEEDING_ID = legendaryMonstersId("bleeding");
    private static final ResourceLocation SOUL_RAGE_ID = legendaryMonstersId("soul_rage");
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final AtomicBoolean WARNED_MISSING_BLEEDING = new AtomicBoolean();
    private static final AtomicBoolean WARNED_MISSING_SOUL_RAGE = new AtomicBoolean();

    private SoulGreatSwordCompat() {
    }

    private static ResourceLocation legendaryMonstersId(String path) {
        return Objects.requireNonNull(ResourceLocation.tryBuild("legendary_monsters", path));
    }

    public static boolean isSoulGreatSword(ItemStack stack) {
        return SOUL_GREAT_SWORD_ID.equals(ForgeRegistries.ITEMS.getKey(stack.getItem()));
    }

    public static void applyBleeding(LivingEntity target) {
        MobEffect bleeding = resolveBleeding();
        if (bleeding == null) {
            return;
        }

        MobEffectInstance instance = new MobEffectInstance(bleeding, 70, 0);
        if (target.canBeAffected(instance)) {
            target.addEffect(instance);
        }
    }

    public static void useSoulRage(ServerLevel level, Player player, ItemStack stack) {
        MobEffect soulRage = resolveSoulRage();
        if (soulRage == null) {
            return;
        }

        player.addEffect(new MobEffectInstance(soulRage, 200, 0, false, true, true));
        player.getCooldowns().addCooldown(stack.getItem(), 280);
        level.playSound(null, player.blockPosition(), SoundEvents.WITHER_SHOOT,
                SoundSource.NEUTRAL, 1.0F, 1.0F);
        spawnSoulStrikes(level, player);

        for (int i = 1; i < 35; i++) {
            double angle = Math.PI * 2.0D * i / 35.0D;
            double x = player.getX() + Math.cos(angle);
            double y = player.getY() + 0.5D;
            double z = player.getZ() + Math.sin(angle);
            level.sendParticles(ParticleTypes.SOUL, x, y, z, 1, 0.0D, 0.05D, 0.0D, 0.0D);
            level.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, x, y, z, 1,
                    0.0D, 0.05D, 0.0D, 0.0D);
        }
    }

    private static void spawnSoulStrikes(ServerLevel level, Player player) {
        double bodyYaw = Math.toRadians(player.yBodyRot);
        double spawnX = player.getX() + Math.cos(bodyYaw) * 1.5D;
        double spawnZ = player.getZ() + Math.sin(bodyYaw) * 1.5D;

        for (int i = 0; i < 9; i++) {
            SoulStrike soulStrike = new SoulStrike(level, player, false);
            soulStrike.setDamage(9.0F);
            soulStrike.shootFromRotation(player, 0.0F, 40.0F * i, 0.0F, 0.45F, 0.0F);
            soulStrike.setPos(spawnX, player.getY() + 0.3D, spawnZ);
            level.addFreshEntity(soulStrike);
        }
    }

    private static MobEffect resolveBleeding() {
        MobEffect effect = null;
        try {
            effect = ModEffects.BLEEDING.get();
        } catch (RuntimeException | LinkageError ignored) {
            // Fall through to the registry lookup for a safe compatibility failure.
        }
        if (effect == null) {
            effect = ForgeRegistries.MOB_EFFECTS.getValue(BLEEDING_ID);
        }
        if (effect == null && WARNED_MISSING_BLEEDING.compareAndSet(false, true)) {
            LOGGER.warn("Missing mob effect {}; Soul Great Sword critical bleeding is disabled", BLEEDING_ID);
        }
        return effect;
    }

    private static MobEffect resolveSoulRage() {
        MobEffect effect = null;
        try {
            effect = ModEffects.SOUL_RAGE.get();
        } catch (RuntimeException | LinkageError ignored) {
            // Fall through to the registry lookup for a safe compatibility failure.
        }
        if (effect == null) {
            effect = ForgeRegistries.MOB_EFFECTS.getValue(SOUL_RAGE_ID);
        }
        if (effect == null && WARNED_MISSING_SOUL_RAGE.compareAndSet(false, true)) {
            LOGGER.warn("Missing mob effect {}; Soul Great Sword right-click ability is disabled", SOUL_RAGE_ID);
        }
        return effect;
    }
}
