package com.carrot123.until_eternity.event;

import com.carrot123.until_eternity.block.ModBlocks;
import com.carrot123.until_eternity.until_eternity;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.DeathScreen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.event.ViewportEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * 传送门屏幕效果。
 *
 * portalAnimTime 仅在「玩家主动走进传送门」时递增到 PEAK 并保持。
 * 维度切换（传送完成）后锁定为 0，直到玩家先离开传送门区域再重新进入，
 * 才会再次播放效果 —— 等价于传送冷却在视觉层的体现。
 */
@Mod.EventBusSubscriber(modid = until_eternity.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class ChaosPortalClientEvents {

    private static float portalAnimTime;
    private static float prevPortalAnimTime;

    private static final float PEAK  = 0.15F;
    /** 80 ticks = 4 秒到达峰值 */
    private static final float RISE  = 0.15F / 80.0F;
    private static final float DECAY = 0.05F;

    private static final float ALPHA_SCALE = 0.3F;
    private static final float GRAY = 0.05F;

    private static ResourceLocation lastDimension;
    private static boolean playedEnterSound;

    /**
     * 维度切换后设为 true，阻止 portalAnimTime 自动递增。
     * 需要连续 5 tick 不在传送门内才解锁 —— 防止 chunk 未加载时
     * inPortal 误判为 false 导致提前解锁。
     */
    private static boolean awaitExit;
    private static int awaitExitTicks;

    /* ── 计时 ───────────────────────────────────────────────── */

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.START) return;

        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null || mc.level == null) return;

        // 维度切换 → 传送完成 → 锁定效果，等待玩家离开传送门
        ResourceLocation dim = player.level().dimension().location();
        if (lastDimension != null && !dim.equals(lastDimension)) {
            portalAnimTime = 0.0F;
            prevPortalAnimTime = 0.0F;
            playedEnterSound = false;
            awaitExit = true;
            awaitExitTicks = 5;
        }
        lastDimension = dim;

        prevPortalAnimTime = portalAnimTime;

        boolean inPortal = mc.level.getBlockState(player.blockPosition())
                .is(ModBlocks.CHAOS_PORTAL.get())
                || mc.level.getBlockState(player.blockPosition().above())
                .is(ModBlocks.CHAOS_PORTAL.get());

        // 锁定期间：必须连续 N  tick 不在传送门内才解锁
        if (awaitExit) {
            if (!inPortal) {
                awaitExitTicks--;
                if (awaitExitTicks <= 0) {
                    awaitExit = false;
                }
            } else {
                // 仍在传送门内，重置计数器
                awaitExitTicks = 5;
            }
            return;
        }

        if (inPortal) {
            if (portalAnimTime < PEAK) {
                portalAnimTime = Math.min(PEAK, portalAnimTime + RISE);
            }
            if (!playedEnterSound) {
                playedEnterSound = true;
                onPortalEnter(mc, player);
            }
        } else {
            portalAnimTime = Math.max(0.0F, portalAnimTime - DECAY);
            if (portalAnimTime == 0.0F) {
                playedEnterSound = false;
            }
        }
    }

    private static void onPortalEnter(Minecraft mc, LocalPlayer player) {
        if (mc.screen != null
                && !mc.screen.isPauseScreen()
                && !(mc.screen instanceof DeathScreen)) {
            if (mc.screen instanceof AbstractContainerScreen) {
                player.closeContainer();
            }
            mc.setScreen(null);
        }
        mc.getSoundManager().play(
                net.minecraft.client.resources.sounds.SimpleSoundInstance.forLocalAmbience(
                        SoundEvents.PORTAL_TRIGGER,
                        player.getRandom().nextFloat() * 0.4F + 0.8F,
                        0.25F));
    }

    /* ── 灰色覆盖层 ─────────────────────────────────────────── */

    @SubscribeEvent
    public static void onRenderGuiOverlayPost(RenderGuiOverlayEvent.Post event) {
        if (portalAnimTime <= 0.0F) return;

        Minecraft mc = Minecraft.getInstance();
        int w = mc.getWindow().getGuiScaledWidth();
        int h = mc.getWindow().getGuiScaledHeight();
        float partialTick = mc.getFrameTime();
        float anim = Mth.lerp(partialTick, prevPortalAnimTime, portalAnimTime) * ALPHA_SCALE;

        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);

        Tesselator tess = Tesselator.getInstance();
        BufferBuilder buf = tess.getBuilder();
        buf.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        buf.vertex(0.0, h, -90.0).color(GRAY, GRAY, GRAY, anim).endVertex();
        buf.vertex(w, h, -90.0).color(GRAY, GRAY, GRAY, anim).endVertex();
        buf.vertex(w, 0.0, -90.0).color(GRAY, GRAY, GRAY, anim).endVertex();
        buf.vertex(0.0, 0.0, -90.0).color(GRAY, GRAY, GRAY, anim).endVertex();
        tess.end();

        RenderSystem.depthMask(true);
        RenderSystem.enableDepthTest();
        RenderSystem.disableBlend();
    }

    /* ── 反胃式扭曲 ─────────────────────────────────────────── */

    @SubscribeEvent
    public static void onComputeFov(ViewportEvent.ComputeFov event) {
        if (portalAnimTime <= 0.0F) return;
        float partialTick = Minecraft.getInstance().getFrameTime();
        float anim = Mth.lerp(partialTick, prevPortalAnimTime, portalAnimTime);
        float tick = Minecraft.getInstance().player == null ? 0
                : Minecraft.getInstance().player.tickCount + partialTick;
        event.setFOV(event.getFOV() + Math.sin(tick * 0.3) * anim * 20.0);
    }

    @SubscribeEvent
    public static void onCameraSetup(ViewportEvent.ComputeCameraAngles event) {
        if (portalAnimTime <= 0.0F) return;
        float partialTick = Minecraft.getInstance().getFrameTime();
        float anim = Mth.lerp(partialTick, prevPortalAnimTime, portalAnimTime);
        float tick = Minecraft.getInstance().player == null ? 0
                : Minecraft.getInstance().player.tickCount + partialTick;
        event.setYaw(event.getYaw() + (float)Math.sin(tick * 0.25) * anim * 8.0F);
        event.setPitch(event.getPitch() + (float)Math.cos(tick * 0.30) * anim * 4.0F);
        event.setRoll(event.getRoll() + (float)Math.sin(tick * 0.35) * anim * 3.0F);
    }
}
