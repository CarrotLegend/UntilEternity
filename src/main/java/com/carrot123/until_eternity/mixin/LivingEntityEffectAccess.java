package com.carrot123.until_eternity.mixin;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import javax.annotation.Nullable;
import java.util.Map;

@Mixin(LivingEntity.class)
public interface LivingEntityEffectAccess {
    @Accessor("activeEffects")
    Map<MobEffect, MobEffectInstance> untilEternity$getActiveEffects();

    @Invoker("onEffectAdded")
    void untilEternity$onEffectAdded(
            MobEffectInstance effect,
            @Nullable Entity source);

    @Invoker("onEffectUpdated")
    void untilEternity$onEffectUpdated(
            MobEffectInstance effect,
            boolean reapply,
            @Nullable Entity source);
}
