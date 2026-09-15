package com.carrot123.until_eternity.client.model;

import com.carrot123.until_eternity.until_eternity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;

/** Blockbench-authored replacement for EEEAB's Netherworld Katana model. */
public final class NetherworldKatanaReplacementModel<T extends Entity>
        extends EntityModel<T> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
            new ResourceLocation(until_eternity.MODID, "netherworld_katana_remaster"),
            "main");

    private final ModelPart root;
    private final ModelPart bladeRoot;
    private final ModelPart tsubaRoot;
    private final ModelPart handleRoot;
    private final ModelPart skeHead;

    public NetherworldKatanaReplacementModel(ModelPart root) {
        this.root = root.getChild("root");
        this.bladeRoot = this.root.getChild("blade_root");
        this.tsubaRoot = this.root.getChild("tsuba_root");
        this.handleRoot = this.root.getChild("handle_root");
        this.skeHead = this.root.getChild("ske_head");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition root = partdefinition.addOrReplaceChild("root", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition bladeRoot = root.addOrReplaceChild("blade_root", CubeListBuilder.create().texOffs(0, 6).addBox(-2.1F, 2.0F, 0.0F, 4.0F, 58.0F, 0.0F, new CubeDeformation(0.0F))
                .texOffs(8, 2).addBox(0.95F, 2.0F, -0.53F, 1.0F, 61.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(8, 2).addBox(0.95F, 63.0F, -0.53F, 1.0F, 20.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(0.2F, 6.0F, 0.0F));

        bladeRoot.addOrReplaceChild("blade_plane_4_r1", CubeListBuilder.create().texOffs(27, 40).addBox(-1.165F, -0.01F, 0.0F, 1.0F, 2.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.0F, 80.9F, 0.0F, 0.0F, 0.0F, -0.5411F));
        bladeRoot.addOrReplaceChild("blade_plane_3_r1", CubeListBuilder.create().texOffs(23, 35).addBox(-1.1046F, 0.0F, 0.0F, 2.0F, 1.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.85F, 80.25F, 0.0F, 0.0F, 0.0F, -0.2356F));
        bladeRoot.addOrReplaceChild("blade_plane_2_r1", CubeListBuilder.create().texOffs(23, 36).addBox(-1.15F, 0.0F, 0.0F, 2.0F, 6.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.05F, 74.55F, 0.0F, 0.0F, 0.0F, -0.1484F));
        bladeRoot.addOrReplaceChild("blade_plane_1_r1", CubeListBuilder.create().texOffs(23, 42).addBox(-1.15F, 0.0F, 0.0F, 3.0F, 5.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.65F, 69.8F, 0.0F, 0.0F, 0.0F, -0.1047F));
        bladeRoot.addOrReplaceChild("blade_plane_r1", CubeListBuilder.create().texOffs(23, 47).addBox(-1.15F, 0.0F, 0.0F, 3.0F, 10.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.9F, 59.9F, 0.0F, 0.0F, 0.0F, -0.0175F));

        PartDefinition tsubaRoot = root.addOrReplaceChild("tsuba_root", CubeListBuilder.create(), PartPose.offset(0.0F, 6.0F, 0.0F));
        tsubaRoot.addOrReplaceChild("habaki_r1", CubeListBuilder.create().texOffs(16, 57).addBox(-2.75F, -2.25F, -1.4F, 5.0F, 4.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.3F, 0.75F, 0.0F, 0.0F, 0.0F, 0.0F));

        PartDefinition handleRoot = root.addOrReplaceChild("handle_root", CubeListBuilder.create().texOffs(52, 0).addBox(-1.45F, -24.5F, -1.55F, 3.0F, 24.0F, 3.0F, new CubeDeformation(0.0F))
                .texOffs(32, 56).addBox(-1.95F, -28.2F, -2.05F, 4.0F, 4.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(52, 62).addBox(-2.55F, -29.0F, -0.45F, 5.0F, 1.0F, 1.0F, new CubeDeformation(0.0001F))
                .texOffs(52, 62).addBox(-2.55F, -33.0F, -0.45F, 5.0F, 1.0F, 1.0F, new CubeDeformation(0.0001F))
                .texOffs(48, 58).addBox(1.45F, -33.0F, -0.45F, 1.0F, 5.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(48, 58).addBox(-2.65F, -33.0F, -0.45F, 1.0F, 5.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

        handleRoot.addOrReplaceChild("ito_wrap_8_r1", CubeListBuilder.create().texOffs(48, 27).addBox(-1.9F, -0.5F, -2.1F, 4.0F, 1.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(48, 27).addBox(-1.9F, 5.5F, -2.1F, 4.0F, 1.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(48, 27).addBox(-1.9F, 11.5F, -2.1F, 4.0F, 1.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(48, 27).addBox(-1.9F, 17.5F, -2.1F, 4.0F, 1.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -23.5F, 0.0F, 0.0F, 0.7854F, 0.0F));

        handleRoot.addOrReplaceChild("ito_wrap_7_r1", CubeListBuilder.create().texOffs(48, 27).addBox(-1.9F, -0.5F, -2.1F, 4.0F, 1.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(48, 27).addBox(-1.9F, 5.5F, -2.1F, 4.0F, 1.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(48, 27).addBox(-1.9F, 11.5F, -2.1F, 4.0F, 1.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(48, 27).addBox(-1.9F, 17.5F, -2.1F, 4.0F, 1.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -20.5F, 0.0F, 0.0F, -0.7854F, 0.0F));

        root.addOrReplaceChild("ske_head", CubeListBuilder.create().texOffs(36, 32).addBox(-4.0F, -1.0F, -3.0F, 8.0F, 5.0F, 6.0F, new CubeDeformation(0.0F))
                .texOffs(44, 43).addBox(-3.0F, 4.0F, -2.0F, 6.0F, 2.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

        return LayerDefinition.create(meshdefinition, 64, 64);
    }

    @Override
    public void setupAnim(T entity, float limbSwing, float limbSwingAmount,
                          float ageInTicks, float netHeadYaw, float headPitch) {
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer,
                               int packedLight, int packedOverlay, float red,
                               float green, float blue, float alpha) {
        root.render(poseStack, vertexConsumer, packedLight, packedOverlay,
                red, green, blue, alpha);
    }
}
