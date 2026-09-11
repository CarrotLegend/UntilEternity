package com.carrot123.until_eternity.mixin.compat.splendourablazeepoch;

import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.registries.ForgeRegistries;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Comparator;
import java.util.List;

/**
 * 玄阴方尊 Wordbullet 核心性能修复。
 *
 * 原实现每一个 Wordbullet、每一个 tick 都执行一次命令：
 *   execute as @e[type=...:wordbullet] ... facing entity @e[type=...:xuanyinfangzun] ...
 * 这会形成近似 O(n^2) 的实体选择器开销。
 * 同时原实现每 tick 都 queueServerWork(600, discard)，
 * 一个弹丸一生会向延时任务队列塞入数百个冗余任务。
 *
 * 本 Mixin 完全替换该 tick Procedure：
 * - 只处理当前这一枚 Wordbullet；
 * - 直接寻找附近最近的玄阴方尊，不执行命令；
 * - 用 tickCount 管理寿命，不创建延时任务；
 * - 碰撞只爆炸一次并立刻删除；
 * - 爆炸不破坏方块，避免额外方块更新/掉落物卡顿。
 */
@Pseudo
@Mixin(targets = "net.mcreator.splendourablazeepoch.procedures.WordbulletZaiShiTiKeGengXinShiProcedure", remap = false)
public abstract class WordbulletTickPerformanceMixin {

    private static final String XUANYIN_ID = "splendour_ablaze_epoch:xuanyinfangzun";
    private static final int MAX_LIFETIME = 200; // 10 秒。原版延迟是 600 tick，但原逻辑会重复排队。
    private static final double BOSS_SEARCH_RADIUS = 96.0D;

    @Inject(method = "execute", at = @At("HEAD"), cancellable = true, require = 1, remap = false)
    private static void untilEternity$replaceWordbulletTick(LevelAccessor level,
                                                           double x,
                                                           double y,
                                                           double z,
                                                           Entity entity,
                                                           CallbackInfo ci) {
        if (entity == null) {
            ci.cancel();
            return;
        }

        // 所有有效逻辑只在服务端执行，客户端位置由实体同步。
        if (!(level instanceof ServerLevel serverLevel)) {
            ci.cancel();
            return;
        }

        if (!entity.isAlive() || entity.tickCount >= MAX_LIFETIME) {
            entity.discard();
            ci.cancel();
            return;
        }

        Entity boss = untilEternity$findNearestEntityOfType(
                serverLevel,
                entity,
                XUANYIN_ID,
                BOSS_SEARCH_RADIUS
        );

        if (boss != null && boss.isAlive()) {
            untilEternity$moveRelativeToBoss(serverLevel, entity, boss);
        }

        // 原源码使用零体积 AABB。这里给极小容差，避免高速/浮点位置下漏判。
        AABB hitBox = entity.getBoundingBox().inflate(0.05D);
        List<Entity> hits = serverLevel.getEntities(
                entity,
                hitBox,
                target -> target.isAlive() && !untilEternity$isType(target, XUANYIN_ID)
        );

        if (!hits.isEmpty()) {
            serverLevel.explode(
                    null,
                    entity.getX(),
                    entity.getY(),
                    entity.getZ(),
                    4.0F,
                    Level.ExplosionInteraction.NONE
            );
            entity.discard();
        }

        ci.cancel();
    }

    private static void untilEternity$moveRelativeToBoss(ServerLevel level, Entity bullet, Entity boss) {
        Vec3 toBoss = boss.getEyePosition().subtract(bullet.position());
        if (toBoss.lengthSqr() < 1.0E-8D) {
            return;
        }

        Vec3 forward = toBoss.normalize();
        Vec3 right = new Vec3(forward.z, 0.0D, -forward.x);
        if (right.lengthSqr() < 1.0E-8D) {
            right = new Vec3(1.0D, 0.0D, 0.0D);
        } else {
            right = right.normalize();
        }

        RandomSource random = level.getRandom();

        // 对应原命令中的：^0..1  ^-0.01..-0.15  ^-2..2
        double localX = random.nextDouble();
        double localY = -(0.01D + random.nextDouble() * 0.14D);
        double localZ = -2.0D + random.nextDouble() * 4.0D;

        Vec3 offset = right.scale(localX)
                .add(0.0D, localY, 0.0D)
                .add(forward.scale(localZ));

        bullet.lookAt(EntityAnchorArgument.Anchor.EYES, boss.getEyePosition());
        bullet.setDeltaMovement(Vec3.ZERO);
        bullet.setPos(
                bullet.getX() + offset.x,
                bullet.getY() + offset.y,
                bullet.getZ() + offset.z
        );
    }

    private static Entity untilEternity$findNearestEntityOfType(ServerLevel level,
                                                                 Entity origin,
                                                                 String typeId,
                                                                 double radius) {
        AABB searchBox = origin.getBoundingBox().inflate(radius);
        return level.getEntities(
                        origin,
                        searchBox,
                        candidate -> candidate.isAlive() && untilEternity$isType(candidate, typeId)
                )
                .stream()
                .min(Comparator.comparingDouble(candidate -> origin.distanceToSqr(candidate)))
                .orElse(null);
    }

    private static boolean untilEternity$isType(Entity entity, String expectedId) {
        ResourceLocation key = ForgeRegistries.ENTITY_TYPES.getKey(entity.getType());
        return key != null && expectedId.equals(key.toString());
    }
}
