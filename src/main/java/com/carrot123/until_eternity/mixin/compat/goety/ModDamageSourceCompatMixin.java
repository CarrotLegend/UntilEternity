package com.carrot123.until_eternity.mixin.compat.goety;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
@Mixin(
        targets = "com.Polarice3.Goety.utils.ModDamageSource",
        remap = false
)
public abstract class ModDamageSourceCompatMixin {
    @Shadow
    public static DamageSource getDamageSource(
            Level level,
            ResourceKey<DamageType> type,
            EntityType<?>... toIgnore
    ) {
        throw new AssertionError();
    }
    public static DamageSource getDamageSource(
            Level level,
            ResourceKey<DamageType> type
    ) {
        return getDamageSource(
                level,
                type,
                new EntityType<?>[0]
        );
    }
}