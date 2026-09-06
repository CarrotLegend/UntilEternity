package com.carrot123.until_eternity.event;

import com.carrot123.until_eternity.item.BloodBottleHelper;
import com.carrot123.until_eternity.item.ModItems;
import com.carrot123.until_eternity.registry.ModPotions;
import com.carrot123.until_eternity.registry.ModTags;
import com.carrot123.until_eternity.until_eternity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraftforge.entity.PartEntity;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Mod.EventBusSubscriber(
        modid = until_eternity.MODID,
        bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class BloodSplashDaggerEvents {
    private static final Map<UUID, PendingAttack> PENDING_ATTACKS = new HashMap<>();

    private BloodSplashDaggerEvents() {
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onAttackEntity(AttackEntityEvent event) {
        Player player = event.getEntity();
        if (player.level().isClientSide
                || !player.getMainHandItem().is(ModItems.BLOOD_SPLASH_DAGGER.get())) {
            return;
        }

        PENDING_ATTACKS.remove(player.getUUID());
        LivingEntity target = resolveLivingTarget(event.getTarget());
        if (target == null || target == player) {
            return;
        }

        PENDING_ATTACKS.put(player.getUUID(), new PendingAttack(
                target.getId(),
                player.level().getGameTime(),
                target.getHealth(),
                false));
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onLivingDamage(LivingDamageEvent event) {
        float amount = event.getAmount();
        if (event.getEntity().level().isClientSide
                || !Float.isFinite(amount)
                || amount <= 0.0F
                || !(event.getSource().getEntity() instanceof Player player)
                || event.getSource().getDirectEntity() != player
                || event.getEntity() == player
                || !player.getMainHandItem().is(ModItems.BLOOD_SPLASH_DAGGER.get())) {
            return;
        }

        PendingAttack pending = PENDING_ATTACKS.get(player.getUUID());
        if (pending == null
                || pending.targetEntityId() != event.getEntity().getId()
                || pending.gameTime() != player.level().getGameTime()) {
            return;
        }

        PENDING_ATTACKS.put(player.getUUID(), pending.withDamageConfirmed());
    }

    @SubscribeEvent
    public static void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        PENDING_ATTACKS.remove(event.getEntity().getUUID());
    }

    public static void completeSuccessfulHit(Player player, LivingEntity target) {
        PendingAttack pending = PENDING_ATTACKS.remove(player.getUUID());
        if (pending == null
                || !pending.damageConfirmed()
                || pending.targetEntityId() != target.getId()
                || pending.gameTime() != player.level().getGameTime()
                || !(target.getHealth() < pending.healthBefore())
                || !player.getOffhandItem().is(Items.GLASS_BOTTLE)) {
            return;
        }

        Potion blood = bloodFor(target);
        if (blood != null) {
            BloodBottleHelper.consumeOffhandBottleAndGive(player, blood);
        }
    }

    @Nullable
    private static Potion bloodFor(LivingEntity target) {
        if (target.getMobType() == MobType.UNDEAD) {
            return ModPotions.EVIL_BLOOD.get();
        }
        if (target instanceof Animal
                || target.getType().is(ModTags.EntityTypes.PASSIVE_MOBS)) {
            return ModPotions.DIRTY_BLOOD.get();
        }
        return null;
    }

    @Nullable
    private static LivingEntity resolveLivingTarget(Entity target) {
        if (target instanceof LivingEntity living) {
            return living;
        }
        if (target instanceof PartEntity<?> part
                && part.getParent() instanceof LivingEntity living) {
            return living;
        }
        return null;
    }

    private record PendingAttack(
            int targetEntityId,
            long gameTime,
            float healthBefore,
            boolean damageConfirmed) {
        private PendingAttack withDamageConfirmed() {
            return new PendingAttack(
                    targetEntityId,
                    gameTime,
                    healthBefore,
                    true);
        }
    }
}
