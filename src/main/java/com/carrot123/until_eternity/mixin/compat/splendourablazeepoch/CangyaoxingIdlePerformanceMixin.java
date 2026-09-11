package com.carrot123.until_eternity.mixin.compat.splendourablazeepoch;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

/**
 * 苍曜星性能修复。
 *
 * 原模组 Cangyaoxing_idelProcedure 中弹幕分支是 random >= 198，
 * 而 random 的范围为 0..300，导致该分支约 34.2% 的 tick 都会触发。
 * 此处把阈值 198 改为 300，使原来的 >= 判断等价于 random == 300，
 * 将触发率恢复为约 1/301。
 * 同时把该分支最后一个“10 次弹幕循环”改为 4 次，避免拥挤场景中一次技能仍瞬间生成过多实体。
 */
@Pseudo
@Mixin(targets = "net.mcreator.splendourablazeepoch.procedures.Cangyaoxing_idelProcedure", remap = false)
public abstract class CangyaoxingIdlePerformanceMixin {

    @ModifyConstant(
            method = "execute",
            constant = @Constant(doubleValue = 198.0D),
            require = 1,
            remap = false
    )
    private static double untilEternity$fixProjectileSpamThreshold(double original) {
        return 300.0D;
    }

    /**
     * execute() 中一共有 5 个 int 常量 10；ordinal=4 是 random>=198 分支里
     * for (i = 0; i < 10; i++) 的循环上限。
     */
    @ModifyConstant(
            method = "execute",
            constant = @Constant(intValue = 10, ordinal = 4),
            require = 1,
            remap = false
    )
    private static int untilEternity$reduceProjectileCountPerTarget(int original) {
        return 4;
    }
}

