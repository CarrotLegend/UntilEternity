package com.carrot123.until_eternity.mixin.compat.summoningrituals;

import java.util.function.Function;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Unit;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.monster.warden.Warden;
import net.minecraft.world.entity.monster.warden.WardenAi;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

@Pseudo
@Mixin(
        targets = "com.almostreliable.summoningrituals.recipe.component.RecipeOutputs$MobOutput",
        remap = false
)
public abstract class SummoningRitualsWardenSpawnMixin {

    @WrapOperation(
            method = "spawn(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/core/BlockPos;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lextensions/net/minecraft/world/entity/Entity/EntityExt;spawn(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/level/Level;Lnet/minecraft/world/phys/Vec3;Ljava/util/function/Function;)V",
                    remap = false
            ),
            remap = false
    )
    private void untilEternity$initializeWarden(
            Entity entity,
            Level level,
            Vec3 position,
            Function<Entity, Entity> dataWriter,
            Operation<Void> original
    ) {
        original.call(entity, level, position, dataWriter);

        if (entity.getType() != EntityType.WARDEN
                || !(entity instanceof Warden warden)
                || !warden.isAddedToWorld()) {
            return;
        }

        warden.getBrain().setMemoryWithExpiry(
                MemoryModuleType.DIG_COOLDOWN,
                Unit.INSTANCE,
                WardenAi.DIGGING_COOLDOWN
        );
        warden.setPose(Pose.EMERGING);
        warden.getBrain().setMemoryWithExpiry(
                MemoryModuleType.IS_EMERGING,
                Unit.INSTANCE,
                WardenAi.EMERGE_DURATION
        );
        warden.playSound(SoundEvents.WARDEN_AGITATED, 5.0F, 1.0F);
    }
}
