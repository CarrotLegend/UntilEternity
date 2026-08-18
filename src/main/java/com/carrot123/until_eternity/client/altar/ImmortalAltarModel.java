package com.carrot123.until_eternity.client.altar;

import com.carrot123.until_eternity.until_eternity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

final class ImmortalAltarModel extends Model {
    static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
            new ResourceLocation(until_eternity.MODID, "immortal_altar"),
            "main");

    private final ModelPart root;

    ImmortalAltarModel(ModelPart root) {
        super(RenderType::entityCutoutNoCull);
        this.root = root.getChild("bb_main");
    }

    static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        PartDefinition main = root.addOrReplaceChild(
                "bb_main",
                CubeListBuilder.create()
                        .texOffs(8, 46).addBox(-7.0F, -2.0F, -7.0F,
                                14.0F, 2.0F, 14.0F, new CubeDeformation(0.0F))
                        .texOffs(16, 33).addBox(-6.0F, -3.0F, -6.0F,
                                12.0F, 1.0F, 12.0F, new CubeDeformation(0.0F))
                        .texOffs(0, 37).addBox(4.0F, -0.999F, -8.0F,
                                4.0F, 1.0F, 4.0F, new CubeDeformation(0.0F))
                        .texOffs(0, 37).addBox(-8.0F, -0.999F, -8.0F,
                                4.0F, 1.0F, 4.0F, new CubeDeformation(0.0F))
                        .texOffs(0, 37).addBox(-8.0F, -0.999F, 4.0F,
                                4.0F, 1.0F, 4.0F, new CubeDeformation(0.0F))
                        .texOffs(0, 37).addBox(4.0F, -0.999F, 4.0F,
                                4.0F, 1.0F, 4.0F, new CubeDeformation(0.0F))
                        .texOffs(8, 46).addBox(-7.0F, -7.0F, -7.0F,
                                14.0F, 4.0F, 14.0F, new CubeDeformation(0.0F))
                        .texOffs(0, 42).addBox(-2.0F, -9.0F, -8.0F,
                                4.0F, 4.0F, 4.0F, new CubeDeformation(0.0F))
                        .texOffs(0, 42).addBox(-2.0F, -9.0F, 4.0F,
                                4.0F, 4.0F, 4.0F, new CubeDeformation(0.0F))
                        .texOffs(0, 42).addBox(-8.0F, -9.0F, -2.0F,
                                4.0F, 4.0F, 4.0F, new CubeDeformation(0.0F))
                        .texOffs(0, 42).addBox(4.0F, -9.0F, -2.0F,
                                4.0F, 4.0F, 4.0F, new CubeDeformation(0.0F))
                        .texOffs(0, 50).addBox(6.0F, -3.999F, -3.0F,
                                2.0F, 4.0F, 6.0F, new CubeDeformation(0.0F))
                        .texOffs(0, 50).addBox(-8.0F, -3.999F, -3.0F,
                                2.0F, 4.0F, 6.0F, new CubeDeformation(0.0F))
                        .texOffs(0, 25).addBox(5.0F, -11.0F, 5.0F,
                                3.0F, 9.0F, 3.0F, new CubeDeformation(0.0F))
                        .texOffs(0, 25).addBox(-8.0F, -11.0F, 5.0F,
                                3.0F, 9.0F, 3.0F, new CubeDeformation(0.0F))
                        .texOffs(0, 25).addBox(-8.0F, -11.0F, -8.0F,
                                3.0F, 9.0F, 3.0F, new CubeDeformation(0.0F))
                        .texOffs(0, 25).addBox(5.0F, -11.0F, -8.0F,
                                3.0F, 9.0F, 3.0F, new CubeDeformation(0.0F))
                        .texOffs(24, 22).addBox(-5.0F, -8.0F, -5.0F,
                                10.0F, 1.0F, 10.0F, new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, 24.0F, 0.0F));

        main.addOrReplaceChild(
                "cube_r1",
                CubeListBuilder.create().texOffs(0, 50).addBox(
                        -2.0F, -0.999F, -3.0F,
                        2.0F, 4.0F, 6.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(
                        0.0F, -3.0F, -8.0F, 0.0F, 1.5708F, 0.0F));
        main.addOrReplaceChild(
                "cube_r2",
                CubeListBuilder.create().texOffs(0, 50).addBox(
                        -2.0F, -0.999F, -3.0F,
                        2.0F, 4.0F, 6.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(
                        0.0F, -3.0F, 6.0F, 0.0F, 1.5708F, 0.0F));

        return LayerDefinition.create(mesh, 64, 64);
    }

    @Override
    public void renderToBuffer(
            PoseStack poseStack,
            VertexConsumer vertexConsumer,
            int packedLight,
            int packedOverlay,
            float red,
            float green,
            float blue,
            float alpha) {
        root.render(poseStack, vertexConsumer, packedLight, packedOverlay,
                red, green, blue, alpha);
    }
}
