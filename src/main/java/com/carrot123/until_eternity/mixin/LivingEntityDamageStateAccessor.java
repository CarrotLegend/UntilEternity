package com.carrot123.until_eternity.mixin;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/** Field-only bridge for the vanilla damage state mirrored by direct knife hits. */
@Mixin(LivingEntity.class)
public interface LivingEntityDamageStateAccessor {
    @Accessor("lastHurt")
    void untilEternity$setLastHurt(float amount);

    @Accessor("lastHurtByPlayerTime")
    void untilEternity$setLastHurtByPlayerTime(int ticks);

    @Accessor("lastDamageSource")
    void untilEternity$setLastDamageSource(DamageSource source);

    @Accessor("lastDamageStamp")
    void untilEternity$setLastDamageStamp(long gameTime);
}
