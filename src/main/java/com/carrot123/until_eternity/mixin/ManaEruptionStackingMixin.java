package com.carrot123.until_eternity.mixin;

import com.carrot123.until_eternity.effect.ManaEruptionMergeGuard;
import com.carrot123.until_eternity.effect.ManaEruptionStacking;
import com.carrot123.until_eternity.registry.ModMobEffects;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

@Mixin(LivingEntity.class)
public abstract class ManaEruptionStackingMixin {
    @Inject(
            method = "addEffect(Lnet/minecraft/world/effect/MobEffectInstance;"
                    + "Lnet/minecraft/world/entity/Entity;)Z",
            at = @At("HEAD"),
            cancellable = true,
            require = 1)
    private void untilEternity$mergeManaEruption(
            MobEffectInstance incoming,
            @Nullable Entity source,
            CallbackInfoReturnable<Boolean> cir) {
        LivingEntity self = (LivingEntity) (Object) this;
        if (!untilEternity$shouldHandle(self, incoming)) {
            return;
        }

        MobEffectInstance replacement = untilEternity$replacement(self, incoming);
        cir.setReturnValue(ManaEruptionMergeGuard.call(
                () -> self.addEffect(replacement, source)));
    }

    @Inject(
            method = "forceAddEffect(Lnet/minecraft/world/effect/MobEffectInstance;"
                    + "Lnet/minecraft/world/entity/Entity;)V",
            at = @At("HEAD"),
            cancellable = true,
            require = 1)
    private void untilEternity$forceMergeManaEruption(
            MobEffectInstance incoming,
            @Nullable Entity source,
            CallbackInfo ci) {
        LivingEntity self = (LivingEntity) (Object) this;
        if (!untilEternity$shouldHandle(self, incoming)) {
            return;
        }

        MobEffectInstance replacement = untilEternity$replacement(self, incoming);
        ManaEruptionMergeGuard.run(
                () -> self.forceAddEffect(replacement, source));
        ci.cancel();
    }

    @Unique
    private static boolean untilEternity$shouldHandle(
            LivingEntity entity,
            MobEffectInstance incoming) {
        return !entity.level().isClientSide
                && !ManaEruptionMergeGuard.isActive()
                && incoming.getEffect() == ModMobEffects.MANA_ERUPTION.get();
    }

    @Unique
    private static MobEffectInstance untilEternity$replacement(
            LivingEntity entity,
            MobEffectInstance incoming) {
        MobEffectInstance current =
                entity.getEffect(ModMobEffects.MANA_ERUPTION.get());
        int amplifier = current == null
                ? ManaEruptionStacking.clampAmplifier(incoming.getAmplifier())
                : ManaEruptionStacking.mergeAmplifier(
                        current.getAmplifier(),
                        incoming.getAmplifier());
        int duration = current == null
                ? incoming.getDuration()
                : ManaEruptionStacking.mergeDuration(
                        current.getDuration(),
                        incoming.getDuration());

        MobEffectInstance replacement = new MobEffectInstance(
                incoming.getEffect(),
                duration,
                amplifier,
                incoming.isAmbient(),
                incoming.isVisible(),
                incoming.showIcon());
        List<ItemStack> curativeItems = new ArrayList<>();
        for (ItemStack stack : incoming.getCurativeItems()) {
            curativeItems.add(stack.copy());
        }
        replacement.setCurativeItems(curativeItems);
        return replacement;
    }
}
