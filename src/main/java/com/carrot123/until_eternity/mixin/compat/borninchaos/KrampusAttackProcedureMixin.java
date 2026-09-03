package com.carrot123.until_eternity.mixin.compat.borninchaos;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.LevelAccessor;
import net.minecraftforge.eventbus.api.Event;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(
        targets = "net.mcreator.borninchaosv.procedures.KrampusAttackProcedure",
        remap = false
)
public abstract class KrampusAttackProcedureMixin {

    @Inject(
            method = "execute(Lnet/minecraftforge/eventbus/api/Event;" +
                    "Lnet/minecraft/world/level/LevelAccessor;" +
                    "DDD" +
                    "Lnet/minecraft/world/entity/Entity;" +
                    "Lnet/minecraft/world/entity/Entity;)V",
            at = @At(
                    value = "NEW",
                    target = "net/minecraft/world/entity/item/ItemEntity"
            ),
            cancellable = true
    )
    private static void untilEternity$removeKrampusDisarm(
            Event event,
            LevelAccessor world,
            double x,
            double y,
            double z,
            Entity entity,
            Entity sourceEntity,
            CallbackInfo ci
    ) {
        ci.cancel();
    }
}