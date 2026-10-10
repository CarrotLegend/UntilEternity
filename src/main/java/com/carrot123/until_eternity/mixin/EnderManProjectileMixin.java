package com.carrot123.until_eternity.mixin;

import net.minecraft.tags.DamageTypeTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.monster.EnderMan;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(EnderMan.class)
public abstract class EnderManProjectileMixin {

    @Redirect(
            method = "hurt(Lnet/minecraft/world/damagesource/DamageSource;F)Z",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/damagesource/DamageSource;is(Lnet/minecraft/tags/TagKey;)Z",
                    remap = false
            ),
            remap = false,
            require = 0
    )
    private boolean untilEternity$allowProjectileDamageDev(
            DamageSource source,
            TagKey<DamageType> tag
    ) {
        if (tag.equals(DamageTypeTags.IS_PROJECTILE)) {
            return false;
        }

        return source.is(tag);
    }

    @Redirect(
            method = "m_6469_(Lnet/minecraft/world/damagesource/DamageSource;F)Z",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/damagesource/DamageSource;m_269533_(Lnet/minecraft/tags/TagKey;)Z",
                    remap = false
            ),
            remap = false,
            require = 0
    )
    private boolean untilEternity$allowProjectileDamageSrg(
            DamageSource source,
            TagKey<DamageType> tag
    ) {
        if (tag.equals(DamageTypeTags.IS_PROJECTILE)) {
            return false;
        }

        return source.is(tag);
    }
}