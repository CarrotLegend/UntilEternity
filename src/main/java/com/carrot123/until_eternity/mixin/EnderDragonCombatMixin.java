package com.carrot123.until_eternity.mixin;

import net.minecraft.tags.DamageTypeTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.projectile.Projectile;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(EnderDragon.class)
public abstract class EnderDragonCombatMixin {
    @Redirect(
            method = "hurt(Lnet/minecraft/world/entity/boss/EnderDragonPart;Lnet/minecraft/world/damagesource/DamageSource;F)Z",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/damagesource/DamageSource;is(Lnet/minecraft/tags/TagKey;)Z"
            ),
            require = 1
    )
    private boolean untilEternity$allowProjectileDamage(DamageSource source, TagKey<DamageType> tag) {
        if (tag.equals(DamageTypeTags.ALWAYS_HURTS_ENDER_DRAGONS)
                && (source.is(DamageTypeTags.IS_PROJECTILE)
                || source.getDirectEntity() instanceof Projectile)) {
            return true;
        }
        return source.is(tag);
    }

    @Redirect(
            method = "knockBack(Ljava/util/List;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/Entity;push(DDD)V"
            ),
            require = 1
    )
    private void untilEternity$preventDragonKnockback(Entity entity, double x, double y, double z) {
    }
}
