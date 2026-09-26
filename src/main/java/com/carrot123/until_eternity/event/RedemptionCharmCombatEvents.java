package com.carrot123.until_eternity.event;

import com.carrot123.until_eternity.effect.KingdomComeEffectApplier;
import com.carrot123.until_eternity.effect.KingdomComeOwnerTracker;
import com.carrot123.until_eternity.item.ModItems;
import com.carrot123.until_eternity.until_eternity;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = until_eternity.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class RedemptionCharmCombatEvents {
    private RedemptionCharmCombatEvents() {
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onLivingHurt(LivingHurtEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer victim)
                || !(event.getAmount() > 0.0F)
                || !Float.isFinite(event.getAmount())) {
            return;
        }
        LivingEntity attacker = PlayerDamageAttackerResolver.resolveLiving(event.getSource());
        if (attacker != null && KingdomComeOwnerTracker.isOwnedBy(attacker, victim)) {
            event.setAmount(event.getAmount() * 0.80F);
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onLivingDamage(LivingDamageEvent event) {
        LivingEntity target = event.getEntity();
        if (target.level().isClientSide
                || !(event.getAmount() > 0.0F)
                || !Float.isFinite(event.getAmount())) {
            return;
        }
        ServerPlayer attacker = PlayerDamageAttackerResolver.resolve(event.getSource());
        if (attacker != null && attacker != target
                && RedemptionCharmDamageLogic.hasActiveCharm(
                        attacker, ModItems.KABBALAH_TREE.get())) {
            KingdomComeEffectApplier.forceApply(target, attacker);
        }
    }
}
