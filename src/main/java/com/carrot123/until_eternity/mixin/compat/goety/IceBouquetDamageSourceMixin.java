package com.carrot123.until_eternity.mixin.compat.goety;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.Polarice3.Goety.common.entities.projectiles.IceBouquet;
import com.Polarice3.Goety.utils.ModDamageSource;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;

@Mixin(value = ModDamageSource.class, remap = false)
public abstract class IceBouquetDamageSourceMixin {
    @Inject(
            method =
                    "iceBouquet(" +
                    "Lnet/minecraft/world/entity/Entity;" +
                    "Lnet/minecraft/world/entity/Entity;" +
                    ")Lnet/minecraft/world/damagesource/DamageSource;",
            at = @At("HEAD"),
            cancellable = true,
            remap = false,
            require = 1
    )
    private static void untilEternity$restoreTrapDamageSource(
            Entity source,
            Entity indirectEntity,
            CallbackInfoReturnable<DamageSource> cir
    ) {
        if (!(source instanceof IceBouquet iceBouquet)) {
            return;
        }

        if (!iceBouquet.getPersistentData().getBoolean(
                "until_eternity:ice_bouquet_trap"
        )) {
            return;
        }

        cir.setReturnValue(
                ModDamageSource.getDamageSource(
                        source.level(),
                        ModDamageSource.ICE_BOUQUET
                )
        );
    }
}