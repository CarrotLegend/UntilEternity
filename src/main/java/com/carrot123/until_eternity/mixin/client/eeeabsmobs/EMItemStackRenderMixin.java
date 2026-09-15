package com.carrot123.until_eternity.mixin.client.eeeabsmobs;

import com.carrot123.until_eternity.client.render.NetherworldKatanaReplacementRenderer;
import com.eeeab.eeeabsmobs.client.render.util.EMItemStackRender;
import com.eeeab.eeeabsmobs.sever.init.ItemInit;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EMItemStackRender.class)
public abstract class EMItemStackRenderMixin {
    @Inject(
            method = "renderByItem(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemDisplayContext;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;II)V",
            at = @At("HEAD"),
            cancellable = true)
    private void untilEternity$renderNetherworldKatana(
            ItemStack stack,
            ItemDisplayContext displayContext,
            PoseStack poseStack,
            MultiBufferSource buffers,
            int packedLight,
            int packedOverlay,
            CallbackInfo callback) {
        if (!stack.is(ItemInit.THE_NETHERWORLD_KATANA.get())
                || !untilEternity$isHandContext(displayContext)) {
            return;
        }

        NetherworldKatanaReplacementRenderer.render(
                stack, poseStack, buffers, packedLight, packedOverlay);
        callback.cancel();
    }

    private static boolean untilEternity$isHandContext(
            ItemDisplayContext displayContext) {
        return displayContext == ItemDisplayContext.FIRST_PERSON_LEFT_HAND
                || displayContext == ItemDisplayContext.FIRST_PERSON_RIGHT_HAND
                || displayContext == ItemDisplayContext.THIRD_PERSON_LEFT_HAND
                || displayContext == ItemDisplayContext.THIRD_PERSON_RIGHT_HAND;
    }
}
