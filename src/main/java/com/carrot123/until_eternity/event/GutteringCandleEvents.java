package com.carrot123.until_eternity.event;

import com.carrot123.until_eternity.item.ModItems;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import top.theillusivec4.curios.api.CuriosApi;

@Mod.EventBusSubscriber(
        modid = "until_eternity",
        bus = Mod.EventBusSubscriber.Bus.FORGE
)
public final class GutteringCandleEvents {

    private static final float ABSORPTION_RATE = 0.20F;
    private static final float MAX_ABSORPTION_PER_ATTACK = 100.0F;
    private static final float MAX_TOTAL_ABSORPTION = 2000.0F;

    private GutteringCandleEvents() {
    }

    @SubscribeEvent
    public static void onLivingDamage(LivingDamageEvent event) {
        if (event.getEntity().level().isClientSide()) {
            return;
        }

        if (!(event.getSource().getEntity() instanceof Player player)) {
            return;
        }

        if (event.getEntity() == player) {
            return;
        }

        if (!hasGutteringCandle(player)) {
            return;
        }

        float damage = event.getAmount();

        if (damage <= 0.0F) {
            return;
        }

        float gainedAbsorption = Math.min(
                damage * ABSORPTION_RATE,
                MAX_ABSORPTION_PER_ATTACK
        );

        if (gainedAbsorption <= 0.0F) {
            return;
        }

        float currentAbsorption =
                player.getAbsorptionAmount();

        if (currentAbsorption >= MAX_TOTAL_ABSORPTION) {
            return;
        }

        player.setAbsorptionAmount(
                Math.min(
                        MAX_TOTAL_ABSORPTION,
                        currentAbsorption + gainedAbsorption
                )
        );
    }

    @SubscribeEvent
    public static void onTooltip(ItemTooltipEvent event) {
        if (!event.getItemStack().is(
                ModItems.GUTTERING_CANDLE.get()
        )) {
            return;
        }

        event.getToolTip().add(
                Component.translatable(
                        "tooltip.until_eternity.guttering_candle.absorption"
                ).withStyle(ChatFormatting.GRAY)
        );
    }

    private static boolean hasGutteringCandle(
            Player player
    ) {
        return CuriosApi
                .getCuriosInventory(player)
                .map(handler ->
                        handler.isEquipped(
                                ModItems.GUTTERING_CANDLE.get()
                        )
                )
                .orElse(false);
    }
}