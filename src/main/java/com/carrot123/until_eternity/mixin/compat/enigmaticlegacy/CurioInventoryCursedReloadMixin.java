package com.carrot123.until_eternity.mixin.compat.enigmaticlegacy;

import com.carrot123.until_eternity.compat.enigmaticlegacy.CursedCurioReloadGuard;
import net.minecraft.nbt.Tag;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(
        targets =
                "top.theillusivec4.curios.common.capability.CurioInventoryCapability$CurioInventoryWrapper",
        remap = false
)
public abstract class CurioInventoryCursedReloadMixin {

    @Inject(
            method = {
                    "readTag",
                    "readTag(Lnet/minecraft/nbt/Tag;)V"
            },
            at = @At(
                    value = "HEAD",
                    remap = false
            ),
            remap = false,
            require = 1
    )
    private void untilEternity$beginCursedCurioRestore(
            Tag tag,
            CallbackInfo ci
    ) {
        CursedCurioReloadGuard.enter();
    }

    @Inject(
            method = {
                    "readTag",
                    "readTag(Lnet/minecraft/nbt/Tag;)V"
            },
            at = @At(
                    value = "RETURN",
                    remap = false
            ),
            remap = false,
            require = 1
    )
    private void untilEternity$endCursedCurioRestore(
            Tag tag,
            CallbackInfo ci
    ) {
        CursedCurioReloadGuard.exit();
    }
}