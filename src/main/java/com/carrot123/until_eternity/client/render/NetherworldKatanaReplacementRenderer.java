package com.carrot123.until_eternity.client.render;

import com.carrot123.until_eternity.client.model.NetherworldKatanaReplacementModel;
import com.carrot123.until_eternity.until_eternity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.joml.Quaternionf;

public final class NetherworldKatanaReplacementRenderer
        extends BlockEntityWithoutLevelRenderer {

    public static final ResourceLocation TEXTURE = new ResourceLocation(
            until_eternity.MODID,
            "textures/entity/netherworld_katana_remaster.png"
    );

    private static final NetherworldKatanaReplacementRenderer INSTANCE =
            new NetherworldKatanaReplacementRenderer();

    private static NetherworldKatanaReplacementModel<Entity> model;

    private NetherworldKatanaReplacementRenderer() {
        super(null, null);
    }

    public static NetherworldKatanaReplacementRenderer getInstance() {
        return INSTANCE;
    }

    @Override
    public void renderByItem(
            ItemStack stack,
            ItemDisplayContext displayContext,
            PoseStack poseStack,
            MultiBufferSource buffers,
            int packedLight,
            int packedOverlay
    ) {
        poseStack.pushPose();

        try {
            poseStack.translate(0.5F, 0.5F, 0.5F);
            poseStack.scale(0.5F, -0.5F, -0.5F);

            poseStack.translate(
                    -0.5F / 16.0F,
                    22.0F / 16.0F,
                    1.0F / 16.0F
            );

            poseStack.mulPose(
                    new Quaternionf().rotationZYX(
                            -3.1416F,
                            0.0F,
                            -1.5708F
                    )
            );

            poseStack.mulPose(
                    Axis.ZP.rotationDegrees(180.0F)
            );

            poseStack.mulPose(
                    Axis.YP.rotationDegrees(270.0F)
            );

            VertexConsumer consumer =
                    ItemRenderer.getArmorFoilBuffer(
                            buffers,
                            RenderType.armorCutoutNoCull(TEXTURE),
                            false,
                            stack.hasFoil()
                    );

            getModel().renderToBuffer(
                    poseStack,
                    consumer,
                    packedLight,
                    packedOverlay,
                    1.0F,
                    1.0F,
                    1.0F,
                    1.0F
            );
        } finally {
            poseStack.popPose();
        }
    }

    public static void invalidateModel() {
        model = null;
    }

    private static NetherworldKatanaReplacementModel<Entity> getModel() {
        if (model == null) {
            model = new NetherworldKatanaReplacementModel<>(
                    Minecraft.getInstance()
                            .getEntityModels()
                            .bakeLayer(
                                    NetherworldKatanaReplacementModel.LAYER_LOCATION
                            )
            );
        }

        return model;
    }
}

