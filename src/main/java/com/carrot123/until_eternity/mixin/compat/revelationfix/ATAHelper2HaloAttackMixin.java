package com.carrot123.until_eternity.mixin.compat.revelationfix;

import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import com.carrot123.until_eternity.compat.revelationfix.HaloAttackCheckGuard;

@Pseudo
@Mixin(
        targets = "com.mega.revelationfix.util.entity.ATAHelper2",
        remap = false)
public abstract class ATAHelper2HaloAttackMixin {
    @Inject(
            method = "hasOdamane(Lnet/minecraft/world/entity/Entity;)Z",
            at = @At("HEAD"),
            cancellable = true,
            require = 1,
            remap = false)
    private static void untilEternity$ignoreHaloOnlyForAttackCheck(
            Entity entity,
            CallbackInfoReturnable<Boolean> cir
    ) {
        if (HaloAttackCheckGuard.isActive()) {
            cir.setReturnValue(false);
        }
    }
}
