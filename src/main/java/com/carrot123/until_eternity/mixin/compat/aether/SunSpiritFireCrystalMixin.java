package com.carrot123.until_eternity.mixin.compat.aether;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.BlockHitResult;

@Pseudo
@Mixin(
        targets = "com.aetherteam.aether.entity.projectile.crystal.FireCrystal",
        remap = false
)
public abstract class SunSpiritFireCrystalMixin {

    @Unique
    private static final String UNTIL_ETERNITY_RADIAL_FIREBALL =
            "until_eternity:sun_spirit_radial_fireball";

    @Inject(
            method = {
                    "onHitBlock(Lnet/minecraft/world/phys/BlockHitResult;)V",
                    "m_8060_(Lnet/minecraft/world/phys/BlockHitResult;)V"
            },
            at = @At("HEAD"),
            cancellable = true,
            remap = false,
            require = 1
    )
    private void untilEternity$discardRadialFireballOnWall(
            BlockHitResult result,
            CallbackInfo ci
    ) {
        Entity self =
                (Entity) (Object) this;

        if (!self.getPersistentData()
                .getBoolean(
                        UNTIL_ETERNITY_RADIAL_FIREBALL
                )) {
            return;
        }

        self.discard();

        ci.cancel();
    }
}