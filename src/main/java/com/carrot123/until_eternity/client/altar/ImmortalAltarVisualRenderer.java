package com.carrot123.until_eternity.client.altar;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;

final class ImmortalAltarVisualRenderer {
    private static final ResourceLocation ALTAR_TEXTURE = new ResourceLocation(
            "until_eternity",
            "textures/block/immortal_altar.png");

    private ImmortalAltarVisualRenderer() {
    }

    static void render(
            ImmortalAltarModel model,
            PoseStack poseStack,
            MultiBufferSource bufferSource,
            int packedLight,
            int packedOverlay) {
        poseStack.pushPose();
        poseStack.translate(0.5F, 1.5F, 0.5F);
        poseStack.scale(1.0F, -1.0F, -1.0F);
        model.renderToBuffer(
                poseStack,
                bufferSource.getBuffer(RenderType.entityCutoutNoCull(ALTAR_TEXTURE)),
                packedLight,
                packedOverlay == 0 ? OverlayTexture.NO_OVERLAY : packedOverlay,
                1.0F,
                1.0F,
                1.0F,
                1.0F);
        poseStack.popPose();
    }
}
