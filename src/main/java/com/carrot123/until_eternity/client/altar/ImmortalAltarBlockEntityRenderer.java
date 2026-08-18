package com.carrot123.until_eternity.client.altar;

import com.carrot123.until_eternity.block.entity.ImmortalAltarBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public final class ImmortalAltarBlockEntityRenderer
        implements BlockEntityRenderer<ImmortalAltarBlockEntity> {
    private final ImmortalAltarModel model;

    public ImmortalAltarBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
        this.model = new ImmortalAltarModel(
                context.bakeLayer(ImmortalAltarModel.LAYER_LOCATION));
    }

    @Override
    public void render(
            ImmortalAltarBlockEntity altar,
            float partialTick,
            PoseStack poseStack,
            MultiBufferSource bufferSource,
            int packedLight,
            int packedOverlay) {
        ImmortalAltarVisualRenderer.render(
                model, poseStack, bufferSource, packedLight, packedOverlay);
        ItemStack offering = altar.getItem(0);
        if (offering.isEmpty()) {
            return;
        }

        poseStack.pushPose();
        poseStack.translate(0.5D, 1.0D, 0.5D);
        poseStack.mulPose(Axis.YP.rotationDegrees(
                (altar.getRenderTicks() + partialTick) % 360.0F));
        Minecraft.getInstance().getItemRenderer().renderStatic(
                offering,
                ItemDisplayContext.GROUND,
                packedLight,
                OverlayTexture.NO_OVERLAY,
                poseStack,
                bufferSource,
                altar.getLevel(),
                0);
        poseStack.popPose();
    }
}
