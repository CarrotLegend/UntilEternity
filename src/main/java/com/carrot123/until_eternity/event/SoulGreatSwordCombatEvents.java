package com.carrot123.until_eternity.event;

import com.carrot123.until_eternity.compat.legendarymonsters.BleedingChance;
import com.carrot123.until_eternity.compat.legendarymonsters.SoulGreatSwordCompat;
import com.carrot123.until_eternity.until_eternity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.player.CriticalHitEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Mod.EventBusSubscriber(modid = until_eternity.MODID)
public final class SoulGreatSwordCombatEvents {
    private static final Map<UUID, PendingCritical> PENDING_CRITICALS = new HashMap<>();

    private SoulGreatSwordCombatEvents() {
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onCriticalHit(CriticalHitEvent event) {
        Player player = event.getEntity();
        if (player.level().isClientSide) {
            return;
        }

        boolean finalCritical = event.getResult() == Event.Result.ALLOW
                || event.getResult() == Event.Result.DEFAULT && event.isVanillaCritical();
        if (!finalCritical || !SoulGreatSwordCompat.isSoulGreatSword(player.getMainHandItem())) {
            PENDING_CRITICALS.remove(player.getUUID());
            return;
        }

        Entity target = event.getTarget();
        if (target instanceof LivingEntity livingTarget) {
            PENDING_CRITICALS.put(player.getUUID(),
                    new PendingCritical(livingTarget.getId(), player.level().getGameTime()));
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onLivingDamage(LivingDamageEvent event) {
        if (event.getEntity().level().isClientSide || event.getAmount() <= 0.0F) {
            return;
        }
        if (!(event.getSource().getEntity() instanceof Player player)
                || event.getSource().getDirectEntity() != player) {
            return;
        }

        PendingCritical pending = PENDING_CRITICALS.remove(player.getUUID());
        LivingEntity target = event.getEntity();
        if (pending == null
                || pending.targetEntityId() != target.getId()
                || pending.gameTime() != player.level().getGameTime()
                || !target.isAlive()
                || !SoulGreatSwordCompat.isSoulGreatSword(player.getMainHandItem())) {
            return;
        }

        if (BleedingChance.shouldApply(player.getRandom().nextFloat())) {
            SoulGreatSwordCompat.applyBleeding(target);
        }
    }

    private record PendingCritical(int targetEntityId, long gameTime) {
    }
}
