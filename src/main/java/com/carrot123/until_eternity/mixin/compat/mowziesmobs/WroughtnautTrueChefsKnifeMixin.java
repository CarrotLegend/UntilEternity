package com.carrot123.until_eternity.mixin.compat.mowziesmobs;

import com.carrot123.until_eternity.combat.TrueChefsKnifeAbsoluteDamageContext;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;

@Pseudo
@Mixin(
        targets = "com.bobmowzie.mowziesmobs.server.entity.wroughtnaut.EntityWroughtnaut",
        remap = false
)
public abstract class WroughtnautTrueChefsKnifeMixin {

    @ModifyExpressionValue(
            method = {
                    "hurt(Lnet/minecraft/world/damagesource/DamageSource;F)Z",
                    "m_6469_(Lnet/minecraft/world/damagesource/DamageSource;F)Z"
            },
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/damagesource/DamageSource;getEntity()Lnet/minecraft/world/entity/Entity;",
                    ordinal = 0,
                    remap = true
            ),
            remap = false,
            require = 1,
            expect = 1,
            allow = 1
    )
    private Entity untilEternity$skipWroughtnautCustomGate(
            Entity original,
            DamageSource source,
            float amount
    ) {
        return untilEternity$isKnife(source) ? null : original;
    }

    @ModifyExpressionValue(
            method = {
                    "hurt(Lnet/minecraft/world/damagesource/DamageSource;F)Z",
                    "m_6469_(Lnet/minecraft/world/damagesource/DamageSource;F)Z"
            },
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/damagesource/DamageSource;is(Lnet/minecraft/tags/TagKey;)Z",
                    ordinal = 0,
                    remap = true
            ),
            remap = false,
            require = 1,
            expect = 1,
            allow = 1
    )
    private boolean untilEternity$allowKnifeThroughWroughtnautGate(
            boolean original,
            DamageSource source,
            float amount
    ) {
        return untilEternity$isKnife(source) || original;
    }

    private boolean untilEternity$isKnife(DamageSource source) {
        return TrueChefsKnifeAbsoluteDamageContext.matches(
                (LivingEntity) (Object) this,
                source
        );
    }
}
