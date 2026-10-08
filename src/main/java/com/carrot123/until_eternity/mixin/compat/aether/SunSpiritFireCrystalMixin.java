package com.carrot123.until_eternity.mixin.compat.aether;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(
        targets = "com.aetherteam.aether.entity.projectile.crystal.FireCrystal",
        remap = false
)
public abstract class SunSpiritFireCrystalMixin {

    @Unique
    private static final String UNTIL_ETERNITY_RADIAL_FIREBALL =
            "until_eternity:sun_spirit_radial_fireball";

    @ModifyConstant(
            method = "m_5790_(Lnet/minecraft/world/phys/EntityHitResult;)V",
            constant = @Constant(floatValue = 15.0F),
            remap = false,
            require = 1
    )
    private float untilEternity$changeBaseDamage(
            float original
    ) {
        return 6.0F;
    }

    @Inject(
            method = "m_8060_(Lnet/minecraft/world/phys/BlockHitResult;)V",
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