package com.carrot123.until_eternity.compat.goety;

import com.carrot123.until_eternity.until_eternity;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(
        modid = until_eternity.MODID,
        bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class GoetyFocusDamageTrackingEvents {
    private GoetyFocusDamageTrackingEvents() {
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onEntityJoin(EntityJoinLevelEvent event) {
        if (event.getLevel().isClientSide()) {
            return;
        }
        GoetyFocusCastContext.currentCasterUuid().ifPresent(
                casterUuid -> GoetyFocusDamageMarker.mark(
                        event.getEntity(), casterUuid));
    }
}
