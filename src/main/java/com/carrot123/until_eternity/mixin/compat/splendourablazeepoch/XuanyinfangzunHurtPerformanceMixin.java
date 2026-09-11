package com.carrot123.until_eternity.mixin.compat.splendourablazeepoch;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

/**
 * 玄阴方尊受击时 Wordbullet 生成频率修复。
 *
 * 原过程有两层 1..10 > 4 判断：
 * 第一层是整个反击逻辑，第二层才是 Wordbullet 生成。
 * 这里只修改第二个常量 4 -> 8，因此不会破坏主要反击技能；
 * Wordbullet 条件从 60% 降到 20%，综合概率约从 36% 降到 12%。
 */
@Pseudo
@Mixin(targets = "net.mcreator.splendourablazeepoch.procedures.Xuanyinfangzun_1Procedure", remap = false)
public abstract class XuanyinfangzunHurtPerformanceMixin {

    @ModifyConstant(
            method = "execute",
            constant = @Constant(intValue = 4, ordinal = 1),
            require = 1,
            remap = false
    )
    private static int untilEternity$reduceWordbulletSpawnChance(int original) {
        return 8;
    }
}
