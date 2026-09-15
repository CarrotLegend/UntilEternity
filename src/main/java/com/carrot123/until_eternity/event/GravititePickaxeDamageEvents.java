package com.carrot123.until_eternity.event;

import com.carrot123.until_eternity.combat.GravititePickaxeAttackContext;
import com.carrot123.until_eternity.until_eternity;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = until_eternity.MODID)
public final class GravititePickaxeDamageEvents {
    private GravititePickaxeDamageEvents() {
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onLivingDamage(LivingDamageEvent event) {
        // Slider's own gate and vanilla reductions have already run. Never re-enter hurt().
        if (!event.getEntity().level().isClientSide && event.getAmount() > 0.0F
                && GravititePickaxeAttackContext.claim(event.getEntity(), event.getSource())) {
            event.setAmount(event.getAmount() * 2.5F);
        }
    }
}
