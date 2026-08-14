package com.carrot123.until_eternity.event;

import com.carrot123.until_eternity.item.ModItems;
import com.carrot123.until_eternity.item.curio.CurioEquipmentHelper;
import com.carrot123.until_eternity.until_eternity;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = until_eternity.MODID)
public final class GreaterArcaneRingEvents {
    public static final float MANA_PER_SECOND = 15.0F;

    private GreaterArcaneRingEvents() {
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END
                || !(event.player instanceof ServerPlayer player)
                || player.tickCount % 20 != 0
                || CurioEquipmentHelper.countEquipped(
                        player, ModItems.GREATER_ARCANE_RING.get()) <= 0) {
            return;
        }
        MagicData.getPlayerMagicData(player).addMana(MANA_PER_SECOND);
    }
}
