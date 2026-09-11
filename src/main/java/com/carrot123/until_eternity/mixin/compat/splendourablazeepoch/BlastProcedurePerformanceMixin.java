package com.carrot123.until_eternity.mixin.compat.splendourablazeepoch;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.registries.ForgeRegistries;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 玄阴方尊 Bigtype1 / Blast Procedure 性能修复。
 *
 * 原 BlastProcedure 每 tick：
 * 1. 先扫描约 20x20x20 的区域寻找玄阴方尊，但结果实际上没有被使用；
 * 2. 再扫描近身实体并击飞；
 * 3. queueServerWork(40, discard)，每 tick 都重复注册删除任务。
 *
 * 本实现删除无用的大范围扫描，并用 tickCount 直接控制 40 tick 寿命。
 */
@Pseudo
@Mixin(targets = "net.mcreator.splendourablazeepoch.procedures.BlastProcedure", remap = false)
public abstract class BlastProcedurePerformanceMixin {

    private static final String XUANYIN_ID = "splendour_ablaze_epoch:xuanyinfangzun";
    private static final String BIGTYPE_ID = "splendour_ablaze_epoch:bigtype";
    private static final String WORDBULLET_ID = "splendour_ablaze_epoch:wordbullet";
    private static final String BIGTYPE1_ID = "splendour_ablaze_epoch:bigtype1";

    @Inject(method = "execute", at = @At("HEAD"), cancellable = true, require = 1, remap = false)
    private static void untilEternity$replaceBlastTick(LevelAccessor level,
                                                       double x,
                                                       double y,
                                                       double z,
                                                       Entity entity,
                                                       CallbackInfo ci) {
        if (entity == null) {
            ci.cancel();
            return;
        }

        if (!(level instanceof ServerLevel serverLevel)) {
            ci.cancel();
            return;
        }

        if (!entity.isAlive() || entity.tickCount >= 40) {
            entity.discard();
            ci.cancel();
            return;
        }

        AABB hitBox = new AABB(
                x - 0.5D, y, z - 0.5D,
                x + 0.5D, y + 2.0D, z + 0.5D
        );

        for (Entity target : serverLevel.getEntities(entity, hitBox, target -> target instanceof LivingEntity)) {
            if (untilEternity$isExcluded(target)) {
                continue;
            }
            target.push(0.0D, 1.0D, 0.0D);
        }

        ci.cancel();
    }

    private static boolean untilEternity$isExcluded(Entity entity) {
        String id = untilEternity$getTypeId(entity);
        return XUANYIN_ID.equals(id)
                || BIGTYPE_ID.equals(id)
                || WORDBULLET_ID.equals(id)
                || BIGTYPE1_ID.equals(id);
    }

    private static String untilEternity$getTypeId(Entity entity) {
        ResourceLocation key = ForgeRegistries.ENTITY_TYPES.getKey(entity.getType());
        return key == null ? "" : key.toString();
    }
}
