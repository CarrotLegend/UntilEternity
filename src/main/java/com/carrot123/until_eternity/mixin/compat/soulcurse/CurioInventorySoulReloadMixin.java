
package com.carrot123.until_eternity.mixin.compat.soulcurse;

import com.carrot123.until_eternity.compat.soulcurse.SoulCurioReloadGuard;
import net.minecraft.nbt.Tag;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(
        targets = "top.theillusivec4.curios.common.capability.CurioInventoryCapability$CurioInventoryWrapper",
        remap = false
)
public abstract class CurioInventorySoulReloadMixin {

    @Inject(
            method = {
                    "readTag",
                    "readTag(Lnet/minecraft/nbt/Tag;)V"
            },
            at = @At(value = "HEAD", remap = false),
            remap = false,
            require = 1
    )
    private void untilEternity$beginSoulCurioRestore(
            Tag tag,
            CallbackInfo ci
    ) {
        SoulCurioReloadGuard.enter();
    }

    @Inject(
            method = {
                    "readTag",
                    "readTag(Lnet/minecraft/nbt/Tag;)V"
            },
            at = @At(value = "RETURN", remap = false),
            remap = false,
            require = 1
    )
    private void untilEternity$endSoulCurioRestore(
            Tag tag,
            CallbackInfo ci
    ) {
        SoulCurioReloadGuard.exit();
    }
}
