package com.carrot123.until_eternity.mixin.compat.splendourablazeepoch;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.registries.ForgeRegistries;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 紫虚仙君 Alchemypage 性能修复。
 *
 * 原实现每 tick 扫描附近实体；命中后每 tick 都根据目标血量
 * queueServerWork(delay, () -> page.setHealth(0))。
 * 这会不断往服务器延时任务队列中重复塞入同一类删除任务。
 *
 * 本实现：
 * - 不再使用 queueServerWork；
 * - 第一次命中时只记录一个“到期 tick”；
 * - 最长存在 200 tick，避免页面实体长期残留；
 * - 近身扫描改为每 2 tick 一次；
 * - 原有点燃、凋零、放火效果保留。
 */
@Pseudo
@Mixin(targets = "net.mcreator.splendourablazeepoch.procedures.AlchemypageZaiShiTiKeGengXinShiProcedure", remap = false)
public abstract class AlchemypageTickPerformanceMixin {

    private static final String EXPIRE_TAG = "until_eternity_zixu_page_expire_tick";
    private static final int HARD_MAX_LIFETIME = 200;

    @Inject(method = "execute", at = @At("HEAD"), cancellable = true, require = 1, remap = false)
    private static void untilEternity$replaceAlchemypageTick(LevelAccessor level,
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

        CompoundTag persistentData = entity.getPersistentData();
        int expireAt = persistentData.getInt(EXPIRE_TAG);

        if (!entity.isAlive()
                || entity.tickCount >= HARD_MAX_LIFETIME
                || (expireAt > 0 && entity.tickCount >= expireAt)) {
            entity.discard();
            ci.cancel();
            return;
        }

        // 原来是每 tick；改成 10 次/秒即可保持接触效果，同时把扫描开销减半。
        if ((entity.tickCount & 1) != 0) {
            ci.cancel();
            return;
        }

        AABB hitBox = new AABB(
                x - 1.0D, y - 1.0D, z - 1.0D,
                x + 1.0D, y + 1.0D, z + 1.0D
        );

        for (Entity target : serverLevel.getEntities(entity, hitBox, target -> target.isAlive())) {
            if (untilEternity$isExcluded(target)) {
                continue;
            }

            target.setSecondsOnFire(1);

            if (target instanceof LivingEntity living) {
                // 与原 Procedure 相同：10 tick，等级 8。
                living.addEffect(new MobEffectInstance(MobEffects.WITHER, 10, 8, false, false));
            }

            // 原代码每次接触都会 setBlock(FIRE)。只在空气位置放置，避免无意义的重复方块更新。
            BlockPos firePos = BlockPos.containing(x, y, z);
            if (serverLevel.getBlockState(firePos).isAir()) {
                serverLevel.setBlock(firePos, Blocks.FIRE.defaultBlockState(), 3);
            }

            // 只在第一次有效命中时设置到期时间，之后不再注册任何延时任务。
            if (!persistentData.contains(EXPIRE_TAG)) {
                int delay = 20;
                if (target instanceof LivingEntity living) {
                    delay = Mth.clamp(Math.round(living.getHealth() * 10.0F), 20, HARD_MAX_LIFETIME);
                }
                persistentData.putInt(EXPIRE_TAG, entity.tickCount + delay);
            }
        }

        ci.cancel();
    }

    private static boolean untilEternity$isExcluded(Entity entity) {
        String id = untilEternity$getTypeId(entity);
        return "minecraft:area_effect_cloud".equals(id)
                || "splendour_ablaze_epoch:darkworm".equals(id)
                || "splendour_ablaze_epoch:pagegnat".equals(id)
                || "splendour_ablaze_epoch:pagewraith".equals(id);
    }

    private static String untilEternity$getTypeId(Entity entity) {
        ResourceLocation key = ForgeRegistries.ENTITY_TYPES.getKey(entity.getType());
        return key == null ? "" : key.toString();
    }
}
