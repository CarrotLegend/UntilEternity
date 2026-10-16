package com.carrot123.until_eternity.trial;

import com.carrot123.until_eternity.until_eternity;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.projectile.EvokerFangs;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.fml.common.Mod;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Mod.EventBusSubscriber(modid = until_eternity.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class TrialRunEvents {
    private static final Set<BlockPos> ACTIVE = new HashSet<>();
    private TrialRunEvents() {}

    public static boolean reserve(ServerLevel level, BlockPos pos) {
        if (!level.dimension().equals(TrialRealm.KEY)) return false;
        for (BlockPos active : ACTIVE) if (active.distSqr(pos) < 96 * 96) return false;
        ACTIVE.add(pos.immutable());
        return true;
    }
    public static void release(ServerLevel level, BlockPos pos) { ACTIVE.remove(pos); }

    @SubscribeEvent(priority = EventPriority.LOWEST) public static void onDeath(LivingDeathEvent event) {
        if (event.isCanceled()) return;
        Entity entity = event.getEntity();
        if (!(entity.level() instanceof ServerLevel level) || !level.dimension().equals(TrialRealm.KEY)) return;
        TrialApollyonOwnership ownership = TrialApollyonOwnership.get(level);
        TrialApollyonOwnership.Entry directBoss = ownership.active(entity);
        if (directBoss != null) {
            if (level.getBlockEntity(directBoss.monument) instanceof TrialMonumentBlockEntity monument
                    && monument.ownsApollyon(level, entity))
                monument.onMobDeath(entity.getUUID(), directBoss.run);
            return;
        }
        if (ownership.knows(entity.getUUID())) return;
        CompoundTag tag = entity.getPersistentData();
        if (!tag.hasUUID(TrialMonumentBlockEntity.MOB_RUN) || !tag.contains(TrialMonumentBlockEntity.MOB_MONUMENT)) return;
        BlockPos pos = BlockPos.of(tag.getLong(TrialMonumentBlockEntity.MOB_MONUMENT));
        if (level.getBlockEntity(pos) instanceof TrialMonumentBlockEntity monument)
            monument.onMobDeath(entity.getUUID(), tag.getUUID(TrialMonumentBlockEntity.MOB_RUN));
    }

    @SubscribeEvent(priority = EventPriority.LOWEST) public static void onHurt(LivingHurtEvent event) {
        if (event.isCanceled() || !(event.getEntity().level() instanceof ServerLevel level)
                || !level.dimension().equals(TrialRealm.KEY)) return;
        Entity attacker = event.getSource().getEntity();
        if (!(attacker instanceof Mob) && event.getSource().getDirectEntity() instanceof Projectile projectile)
            attacker = projectile.getOwner();
        if (!(attacker instanceof Mob) && event.getSource().getDirectEntity() instanceof EvokerFangs fangs)
            attacker = fangs.getOwner();
        if (!(attacker instanceof Mob mob) || mob.level() != level) return;
        TrialApollyonOwnership ownership = TrialApollyonOwnership.get(level);
        TrialApollyonOwnership.Entry directBoss = ownership.active(mob);
        if (directBoss != null) {
            if (level.getBlockEntity(directBoss.monument) instanceof TrialMonumentBlockEntity monument
                    && monument.ownsApollyon(level, mob))
                event.setAmount((float)(event.getAmount() * monument.difficulty().damageMultiplier()));
            return;
        }
        if (ownership.knows(mob.getUUID())) return;
        CompoundTag tag = mob.getPersistentData();
        if (!tag.hasUUID(TrialMonumentBlockEntity.MOB_RUN)
                || !tag.contains(TrialMonumentBlockEntity.MOB_MONUMENT)) return;
        BlockPos pos = BlockPos.of(tag.getLong(TrialMonumentBlockEntity.MOB_MONUMENT));
        if (!level.hasChunkAt(pos)) return;
        if (level.getBlockEntity(pos) instanceof TrialMonumentBlockEntity monument
                && monument.tracksDamageSource(mob.getUUID(), tag.getUUID(TrialMonumentBlockEntity.MOB_RUN)))
            event.setAmount((float)(event.getAmount() * monument.difficulty().damageMultiplier()));
    }

    @SubscribeEvent(priority = EventPriority.LOWEST) public static void onEntityLoad(EntityJoinLevelEvent event) {
        if (event.isCanceled() || !(event.getLevel() instanceof ServerLevel level)
                || !level.dimension().equals(TrialRealm.KEY)) return;
        Entity entity = event.getEntity();
        TrialApollyonOwnership ownership = TrialApollyonOwnership.get(level);
        if (ownership.shouldCancelJoin(entity)) {
            event.setCanceled(true);
            // A duplicate UUID must not erase the live boss's authoritative ownership.
            if (ownership.pendingCleanup(entity.getUUID())) ownership.forget(entity.getUUID());
            return;
        }
        if (ownership.active(entity) != null || ownership.knows(entity.getUUID())) return;
        CompoundTag tag = entity.getPersistentData();
        if (!tag.hasUUID(TrialMonumentBlockEntity.MOB_RUN)) return;
        BlockPos pos = BlockPos.of(tag.getLong(TrialMonumentBlockEntity.MOB_MONUMENT));
        if (!(level.getBlockEntity(pos) instanceof TrialMonumentBlockEntity monument)
                || !monument.hasRun(tag.getUUID(TrialMonumentBlockEntity.MOB_RUN))) {
            until_eternity.LOGGER.info("Discarding stale trial mob on join: uuid={} type={} pos={} monument={} run={}",
                    entity.getUUID(), entity.getType(), entity.blockPosition(), pos,
                    tag.getUUID(TrialMonumentBlockEntity.MOB_RUN));
            event.setCanceled(true);
        }
    }

    @SubscribeEvent public static void onLevelTick(TickEvent.LevelTickEvent event) {
        if (event.phase != TickEvent.Phase.END || !(event.level instanceof ServerLevel level)
                || !level.dimension().equals(TrialRealm.KEY)) return;
        if (level.isRaining() || level.isThundering()) level.setWeatherParameters(6000, 0, false, false);
    }
    @SubscribeEvent public static void onStop(ServerStoppedEvent event) { ACTIVE.clear(); }
}
