package com.carrot123.until_eternity.client.altar;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public final class ImmortalAltarItemRenderer extends BlockEntityWithoutLevelRenderer {
    private final ImmortalAltarModel model;

    public ImmortalAltarItemRenderer(
            BlockEntityRenderDispatcher blockEntityRenderDispatcher,
            EntityModelSet entityModelSet) {
        super(blockEntityRenderDispatcher, entityModelSet);
        this.model = new ImmortalAltarModel(
                entityModelSet.bakeLayer(ImmortalAltarModel.LAYER_LOCATION));
    }

    @Override
    public void renderByItem(
            ItemStack stack,
            ItemDisplayContext displayContext,
            PoseStack poseStack,
            MultiBufferSource bufferSource,
            int packedLight,
            int packedOverlay) {
        ImmortalAltarVisualRenderer.render(
                model, poseStack, bufferSource, packedLight, packedOverlay);
    }
}
