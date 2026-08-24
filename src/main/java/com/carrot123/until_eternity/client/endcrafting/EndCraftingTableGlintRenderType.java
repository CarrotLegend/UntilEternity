package com.carrot123.until_eternity.client.endcrafting;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.ItemRenderer;

/** Client-only vanilla glint state adapted for an inflated world-space shell. */
final class EndCraftingTableGlintRenderType extends RenderType {
    static final RenderType GLINT = create(
            "until_eternity_end_crafting_table_glint",
            DefaultVertexFormat.POSITION_TEX,
            VertexFormat.Mode.QUADS,
            256,
            false,
            false,
            CompositeState.builder()
                    .setShaderState(RENDERTYPE_GLINT_DIRECT_SHADER)
                    .setTextureState(new RenderStateShard.TextureStateShard(
                            ItemRenderer.ENCHANTED_GLINT_ITEM, true, false))
                    .setTransparencyState(GLINT_TRANSPARENCY)
                    .setDepthTestState(LEQUAL_DEPTH_TEST)
                    .setCullState(NO_CULL)
                    .setTexturingState(GLINT_TEXTURING)
                    .setWriteMaskState(COLOR_WRITE)
                    .setLayeringState(POLYGON_OFFSET_LAYERING)
                    .createCompositeState(false));

    private EndCraftingTableGlintRenderType(String name, VertexFormat format, VertexFormat.Mode mode,
                                             int bufferSize, boolean affectsCrumbling, boolean sortOnUpload,
                                             Runnable setupState, Runnable clearState) {
        super(name, format, mode, bufferSize, affectsCrumbling, sortOnUpload, setupState, clearState);
    }
}
