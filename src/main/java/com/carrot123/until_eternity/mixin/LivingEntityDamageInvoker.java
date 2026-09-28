package com.carrot123.until_eternity.mixin;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(LivingEntity.class)
public interface LivingEntityDamageInvoker {

    @Invoker("getDamageAfterArmorAbsorb")
    float untilEternity$getDamageAfterArmorAbsorb(
            DamageSource source,
            float amount
    );

    @Invoker("getDamageAfterMagicAbsorb")
    float untilEternity$getDamageAfterMagicAbsorb(
            DamageSource source,
            float amount
    );
}