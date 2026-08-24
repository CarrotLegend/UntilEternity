package com.carrot123.until_eternity.client.endcrafting;

import com.carrot123.until_eternity.block.entity.EndCraftingTableBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import org.joml.Matrix4f;

public final class EndCraftingTableBlockEntityRenderer
        implements BlockEntityRenderer<EndCraftingTableBlockEntity> {
    static final float SHELL_EXPANSION = 0.002F;

    public EndCraftingTableBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(EndCraftingTableBlockEntity altar, float partialTick, PoseStack poseStack,
                       MultiBufferSource buffers, int packedLight, int packedOverlay) {
        Matrix4f pose = poseStack.last().pose();
        VertexConsumer vertices = buffers.getBuffer(EndCraftingTableGlintRenderType.GLINT);
        float min = -SHELL_EXPANSION;
        float max = 1.0F + SHELL_EXPANSION;

        face(vertices, pose, min, max, min, max, min, min,  max, max, min,  min, max, min); // north
        face(vertices, pose, max, max, max, min, max, max,  min, min, max,  max, min, max); // south
        face(vertices, pose, min, max, max, min, max, min,  min, min, min,  min, min, max); // west
        face(vertices, pose, max, max, min, max, max, max,  max, min, max,  max, min, min); // east
        face(vertices, pose, min, max, max, max, max, max,  max, max, min,  min, max, min); // up
        face(vertices, pose, min, min, min, max, min, min,  max, min, max,  min, min, max); // down
    }

    private static void face(VertexConsumer vertices, Matrix4f pose,
                             float x0, float y0, float z0, float x1, float y1, float z1,
                             float x2, float y2, float z2, float x3, float y3, float z3) {
        vertex(vertices, pose, x0, y0, z0, 0.0F, 0.0F);
        vertex(vertices, pose, x1, y1, z1, 1.0F, 0.0F);
        vertex(vertices, pose, x2, y2, z2, 1.0F, 1.0F);
        vertex(vertices, pose, x3, y3, z3, 0.0F, 1.0F);
    }

    private static void vertex(VertexConsumer vertices, Matrix4f pose,
                               float x, float y, float z, float u, float v) {
        vertices.vertex(pose, x, y, z).uv(u, v).endVertex();
    }
}
