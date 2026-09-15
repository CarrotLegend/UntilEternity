package com.carrot123.until_eternity.mixin.compat.revelationfix;

import com.carrot123.until_eternity.combat.TrueChefsKnifeAbsoluteDamageContext;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@Mixin(
        targets = "com.Polarice3.Goety.common.entities.boss.Apostle",
        priority = 900,
        remap = false
)
public abstract class ApostleRevelationFixStateTrueChefsKnifeMixin {

    @Inject(
            method = "getHitCooldown()I",
            at = @At("RETURN"),
            cancellable = true,
            require = 0,
            remap = false
    )
    private void untilEternity$ignoreGoetyHitCooldown(
            CallbackInfoReturnable<Integer> cir
    ) {
        if (untilEternity$isKnifeActive()) {
            cir.setReturnValue(0);
        }
    }

    @Inject(
            method = "getTitleNumber()I",
            at = @At("RETURN"),
            cancellable = true,
            require = 0,
            remap = false
    )
    private void untilEternity$hideTitleTwelve(
            CallbackInfoReturnable<Integer> cir
    ) {
        if (untilEternity$isKnifeActive()) {
            cir.setReturnValue(0);
        }
    }

    private boolean untilEternity$isKnifeActive() {
        return TrueChefsKnifeAbsoluteDamageContext.isActiveFor(
                (LivingEntity) (Object) this
        );
    }
}
