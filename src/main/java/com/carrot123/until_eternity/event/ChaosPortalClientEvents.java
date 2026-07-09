package com.carrot123.until_eternity.event;

import com.carrot123.until_eternity.block.ModBlocks;
import com.carrot123.until_eternity.until_eternity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.DeathScreen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Client-side portal effects: close GUI on entry, play PORTAL_TRIGGER sound.
 * Does NOT use the vanilla portal overlay (which is purple) — relies on
 * gray chaos_particle particles from ChaosPortalBlock.animateTick for visuals.
 */
@Mod.EventBusSubscriber(modid = until_eternity.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class ChaosPortalClientEvents {

    private static boolean wasInPortal;

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.START) return;
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null) return;

        BlockPos feetPos = player.blockPosition();
        BlockState feetState = player.level().getBlockState(feetPos);
        boolean inPortal = feetState.is(ModBlocks.CHAOS_PORTAL.get());
        if (!inPortal) {
            inPortal = player.level().getBlockState(feetPos.above()).is(ModBlocks.CHAOS_PORTAL.get());
        }

        if (inPortal && !wasInPortal) {
            // Just entered the portal
            if (mc.screen != null && !mc.screen.isPauseScreen()
                    && !(mc.screen instanceof DeathScreen)) {
                if (mc.screen instanceof AbstractContainerScreen) {
                    player.closeContainer();
                }
                mc.setScreen(null);
            }
            mc.getSoundManager().play(
                    SimpleSoundInstance.forLocalAmbience(
                            SoundEvents.PORTAL_TRIGGER,
                            player.getRandom().nextFloat() * 0.4F + 0.8F,
                            0.25F));
        }

        wasInPortal = inPortal;
    }
}
