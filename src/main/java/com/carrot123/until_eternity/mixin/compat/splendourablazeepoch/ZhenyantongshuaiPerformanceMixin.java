package com.carrot123.until_eternity.mixin.compat.splendourablazeepoch;

import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * 真焰统帅性能修复。
 *
 * 该 Boss 没有苍曜星/玄阴方尊那样明显的无限实体倍增问题，
 * 主要尖峰来自：
 * - 某技能一次发送 100 个粒子；
 * - 约 40 tick 后触发 3~7 强度、MOB 模式的地形爆炸。
 *
 * 本 Mixin：
 * - 粒子数 100 -> 24；
 * - 保留爆炸伤害/击退感，但改为 ExplosionInteraction.NONE，
 *   不再破坏方块，从而避免大量方块更新、掉落物和同步开销。
 */
@Pseudo
@Mixin(targets = "net.mcreator.splendourablazeepoch.procedures.ZhenyantongshuaiZaiShiTiKeGengXinShiProcedure", remap = false)
public abstract class ZhenyantongshuaiPerformanceMixin {

    @ModifyConstant(
            method = "execute",
            constant = @Constant(intValue = 100),
            require = 1,
            remap = false
    )
    private static int untilEternity$reduceHeavySkillParticles(int original) {
        return 24;
    }

    /**
     * 原 Procedure 40 tick 延迟后的合成 lambda。
     * 该方法名在当前用户提供的 1.0.0 JAR 中就是 lambda$execute$7。
     */
    @Inject(
            method = "lambda$execute$7",
            at = @At("HEAD"),
            cancellable = true,
            require = 1,
            remap = false
    )
    private static void untilEternity$replaceTerrainExplosion(Entity entity,
                                                               LevelAccessor level,
                                                               double x,
                                                               double y,
                                                               double z,
                                                               CallbackInfo ci) {
        if (entity == null) {
            ci.cancel();
            return;
        }

        untilEternity$setAnimation(entity, "empty");

        if (level instanceof Level realLevel && !realLevel.isClientSide()) {
            float strength = Mth.nextInt(RandomSource.create(), 3, 7);
            realLevel.explode(
                    null,
                    x,
                    y,
                    z,
                    strength,
                    Level.ExplosionInteraction.NONE
            );
        }

        entity.push(0.0D, -3.0D, 0.0D);
        untilEternity$setAnimation(entity, "3");

        ci.cancel();
    }

    /**
     * 为了让 Core Mod 编译时完全不需要把原模组作为 compile dependency，
     * 这里不直接 import ZhenyantongshuaiEntity，而是只在这个低频技能节点反射调用 setAnimation。
     */
    private static void untilEternity$setAnimation(Entity entity, String animation) {
        try {
            Method method = entity.getClass().getMethod("setAnimation", String.class);
            method.invoke(entity, animation);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException ignored) {
            // 如果未来原模组改名了动画 API，性能修复仍不应因此导致崩溃。
        }
    }
}
