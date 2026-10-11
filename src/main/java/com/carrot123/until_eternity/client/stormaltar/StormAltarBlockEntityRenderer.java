package com.carrot123.until_eternity.client.stormaltar;

import com.carrot123.until_eternity.block.entity.StormAltarBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public final class StormAltarBlockEntityRenderer implements BlockEntityRenderer<StormAltarBlockEntity> {
    public StormAltarBlockEntityRenderer(BlockEntityRendererProvider.Context context) {}

    @Override
    public void render(StormAltarBlockEntity altar, float partialTick, PoseStack pose,
                       MultiBufferSource buffers, int light, int overlay) {
        float time = altar.getLevel() == null ? 0 : altar.getLevel().getGameTime() + partialTick;
        if (!altar.output().isEmpty()) {
            renderItem(altar.output(), pose, buffers, light, overlay,
                    0, 1.22 + 0.05 * Mth.sin(time * 0.12f), 0, time * 2.5f);
            return;
        }
        if (altar.isFusing()) {
            float fraction = Mth.clamp(
                    (altar.visualProgress() + partialTick) / Math.max(1f, altar.duration()), 0, 1);
            float phase = Math.max(0, (fraction - 0.12f) / 0.88f);
            float angle = phase * phase * 1200;
            double radius = 0.34 * (1 - phase * phase * (3 - 2 * phase));
            double height = 1.19 + phase * 0.16;
            renderItem(altar.inputA(), pose, buffers, light, overlay,
                    Math.cos(Math.toRadians(angle)) * radius,
                    height,
                    Math.sin(Math.toRadians(angle)) * radius,
                    angle);
            renderItem(altar.inputB(), pose, buffers, light, overlay,
                    -Math.cos(Math.toRadians(angle)) * radius,
                    height,
                    -Math.sin(Math.toRadians(angle)) * radius,
                    angle + 180);
        } else {
            if (!altar.inputA().isEmpty()) {
                renderItem(altar.inputA(), pose, buffers, light, overlay,
                        0, 1.17 + 0.04 * Mth.sin(time * 0.1f), 0, time * 2);
            }
            if (!altar.inputB().isEmpty()) {
                renderItem(altar.inputB(), pose, buffers, light, overlay,
                        0.25, 1.17 + 0.04 * Mth.sin(time * 0.1f + 3), 0, -time * 2);
            }
        }
    }

    private static void renderItem(ItemStack stack, PoseStack pose,
                                   MultiBufferSource buffers, int light, int overlay,
                                   double x, double y, double z, float angle) {
        if (stack.isEmpty()) return;
        pose.pushPose();
        pose.translate(0.5 + x, y, 0.5 + z);
        pose.mulPose(Axis.YP.rotationDegrees(angle));
        pose.scale(0.48f, 0.48f, 0.48f);
        Minecraft.getInstance().getItemRenderer().renderStatic(
                stack, ItemDisplayContext.GROUND, light, overlay,
                pose, buffers, null, 0);
        pose.popPose();
    }
}
