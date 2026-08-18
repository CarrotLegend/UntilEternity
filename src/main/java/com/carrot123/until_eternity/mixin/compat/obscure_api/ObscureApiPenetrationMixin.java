package com.carrot123.until_eternity.mixin.compat.obscure_api;

import com.obscuria.obscureapi.registry.ObscureAPIAttributes;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = ObscureAPIAttributes.class, remap = false)
public abstract class ObscureApiPenetrationMixin {
    @Inject(
            method = "getPenetration(Lnet/minecraft/world/entity/LivingEntity;)F",
            at = @At("HEAD"),
            cancellable = true,
            require = 1,
            remap = false)
    private static void untilEternity$disableObscurePenetration(
            LivingEntity entity,
            CallbackInfoReturnable<Float> callback) {
        callback.setReturnValue(0.0F);
    }
}
