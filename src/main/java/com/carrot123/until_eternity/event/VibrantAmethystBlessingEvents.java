package com.carrot123.until_eternity.event;

import com.carrot123.until_eternity.item.ModItems;
import com.carrot123.until_eternity.until_eternity;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

@Mod.EventBusSubscriber(modid = until_eternity.MODID)
public final class VibrantAmethystBlessingEvents {

    private static final ResourceLocation AMETHYST_BLESSING_ID =
            new ResourceLocation("cataclysm", "blessing_of_amethyst");

    private static final int CHECK_INTERVAL_TICKS = 40;
    private static final int EFFECT_DURATION_TICKS = 200;
    private static final int EFFECT_AMPLIFIER = 0;

    private VibrantAmethystBlessingEvents() {
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END
                || !(event.player instanceof ServerPlayer player)
                || player.tickCount % CHECK_INTERVAL_TICKS != 0) {
            return;
        }

        boolean hasVibrantAmethyst = false;

        for (ItemStack stack : player.getInventory().items) {
            if (!stack.isEmpty() && stack.is(ModItems.VIBRANT_AMETHYST.get())) {
                hasVibrantAmethyst = true;
                break;
            }
        }

        if (!hasVibrantAmethyst) {
            return;
        }

        MobEffect blessing =
                ForgeRegistries.MOB_EFFECTS.getValue(AMETHYST_BLESSING_ID);

        if (blessing == null) {
            return;
        }

        player.addEffect(new MobEffectInstance(
                blessing,
                EFFECT_DURATION_TICKS,
                EFFECT_AMPLIFIER
        ));
    }
}
