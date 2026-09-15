package com.carrot123.until_eternity.mixin.client.eeeabsmobs;

import java.util.function.Consumer;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.carrot123.until_eternity.client.render.NetherworldKatanaReplacementRenderer;
import com.eeeab.eeeabsmobs.sever.item.ItemNetherworldKatana;

import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;

@Mixin(value = ItemNetherworldKatana.class, remap = false)
public abstract class ItemNetherworldKatanaMixin {

    @Inject(
            method = "initializeClient",
            at = @At("HEAD"),
            cancellable = true,
            remap = false
    )
    private void untilEternity$replaceNetherworldKatanaRenderer(
            Consumer<IClientItemExtensions> consumer,
            CallbackInfo ci
    ) {
        consumer.accept(new IClientItemExtensions() {
            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                return NetherworldKatanaReplacementRenderer.getInstance();
            }
        });

        ci.cancel();
    }
}