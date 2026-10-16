package com.carrot123.until_eternity.trial;

import com.carrot123.until_eternity.until_eternity;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.fml.common.Mod;

import java.util.HashSet;
import java.util.Set;

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
        CompoundTag tag = entity.getPersistentData();
        if (!tag.hasUUID(TrialMonumentBlockEntity.MOB_RUN) || !tag.contains(TrialMonumentBlockEntity.MOB_MONUMENT)) return;
        BlockPos pos = BlockPos.of(tag.getLong(TrialMonumentBlockEntity.MOB_MONUMENT));
        if (level.getBlockEntity(pos) instanceof TrialMonumentBlockEntity monument)
            monument.onMobDeath(entity.getUUID(), tag.getUUID(TrialMonumentBlockEntity.MOB_RUN));
    }

    @SubscribeEvent public static void onEntityLoad(EntityJoinLevelEvent event) {
        if (!(event.getLevel() instanceof ServerLevel level) || !level.dimension().equals(TrialRealm.KEY)) return;
        Entity entity = event.getEntity();
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
