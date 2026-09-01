package com.carrot123.until_eternity.mixin.client;

import com.carrot123.until_eternity.block.ModBlocks;
import com.carrot123.until_eternity.client.portal.PortalVisualTracker;
import com.eeeab.eeeabsmobs.sever.init.BlockInit;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(Gui.class)
abstract class GuiPortalOverlayMixin {

    @ModifyVariable(
            method = {
                    "renderPortalOverlay(Lnet/minecraft/client/gui/GuiGraphics;F)V",
                    "m_280379_(Lnet/minecraft/client/gui/GuiGraphics;F)V"
            },
            at = @At("STORE"),
            ordinal = 0,
            require = 1,
            remap = false
    )
    private TextureAtlasSprite untilEternity$usePortalSprite(
            TextureAtlasSprite original
    ) {
        BlockState portalState = switch (
                PortalVisualTracker.currentType()
        ) {
            case CHAOS ->
                    ModBlocks.CHAOS_PORTAL.get().defaultBlockState();

            case IMMORTAL ->
                    BlockInit.EROSION_PORTAL.get().defaultBlockState();

            case NONE ->
                    null;
        };

        if (portalState == null) {
            return original;
        }

        return Minecraft.getInstance()
                .getBlockRenderer()
                .getBlockModelShaper()
                .getParticleIcon(portalState);
    }
}