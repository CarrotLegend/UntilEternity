package com.carrot123.until_eternity.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.ServerLevelAccessor;

@Mixin(Monster.class)
public abstract class MonsterSpawnerLightMixin {

    @Inject(
            method = {
                    "checkMonsterSpawnRules",
                    "m_219013_"
            },
            at = @At("HEAD"),
            cancellable = true,
            remap = false
    )
    private static void untilEternity$ignoreSpawnerLight(
            EntityType<? extends Monster> entityType,
            ServerLevelAccessor level,
            MobSpawnType spawnType,
            BlockPos pos,
            RandomSource random,
            CallbackInfoReturnable<Boolean> cir
    ) {
        if (spawnType != MobSpawnType.SPAWNER) {
            return;
        }

        cir.setReturnValue(
                level.getDifficulty() != Difficulty.PEACEFUL
                        && Mob.checkMobSpawnRules(
                                entityType,
                                level,
                                spawnType,
                                pos,
                                random
                        )
        );
    }
}