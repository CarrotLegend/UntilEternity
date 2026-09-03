package com.carrot123.until_eternity.event;

import com.carrot123.until_eternity.item.ModItems;
import com.carrot123.until_eternity.until_eternity;

import net.minecraft.server.level.ServerPlayer;

import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import top.theillusivec4.curios.api.CuriosApi;
@Mod.EventBusSubscriber(
        modid = until_eternity.MODID,
        bus = Mod.EventBusSubscriber.Bus.FORGE
)
public final class ProofOfSpurnerCombatEvents {

    private static final float DAMAGE_MULTIPLIER = 3.4F;

    private ProofOfSpurnerCombatEvents() {
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onLivingDamage(LivingDamageEvent event) {
        float amount = event.getAmount();

        if (event.getEntity().level().isClientSide
                || amount <= 0.0F
                || !Float.isFinite(amount)) {
            return;
        }

        ServerPlayer attacker =
                HorrorHuntCombatEvents.resolvePlayerAttacker(
                        event.getSource()
                );

        if (attacker == null
                || attacker == event.getEntity()) {
            return;
        }

        boolean equipped =
                CuriosApi.getCuriosInventory(attacker)
                        .map(handler ->
                                handler.isEquipped(
                                        ModItems.PROOF_OF_SPURNER.get()
                                )
                        )
                        .orElse(false);

        if (!equipped) {
            return;
        }

        float amplifiedDamage =
                amount * DAMAGE_MULTIPLIER;

        if (!Float.isFinite(amplifiedDamage)) {
            return;
        }

        event.setAmount(amplifiedDamage);
    }
}