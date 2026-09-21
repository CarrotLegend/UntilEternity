package com.carrot123.until_eternity.mixin.compat.enigmaticaddons;

import com.aizistral.enigmaticlegacy.items.generic.ItemBaseCurio;
import com.carrot123.until_eternity.compat.enigmaticaddons.RedemptionRecognitionCompat;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import top.theillusivec4.curios.api.SlotContext;

@Pseudo
@Mixin(
        targets = "com.aizistral.enigmaticlegacy.items.BerserkEmblem",
        priority = 500,
        remap = false
)
public abstract class BerserkEmblemRecognitionMixin
        extends ItemBaseCurio {

    @Inject(
            method = "canEquip",
            at = @At("RETURN"),
            cancellable = true,
            remap = false
    )
    private void untilEternity$allowRecognizedRelicEquip(
            SlotContext context,
            ItemStack stack,
            CallbackInfoReturnable<Boolean> cir
    ) {
        if (cir.getReturnValue()) {
            return;
        }

        if (!super.canEquip(context, stack)) {
            return;
        }

        if (!(context.entity() instanceof Player player)) {
            return;
        }

        if (RedemptionRecognitionCompat.canUseRecognizedRelic(player)) {
            cir.setReturnValue(true);
        }
    }
}