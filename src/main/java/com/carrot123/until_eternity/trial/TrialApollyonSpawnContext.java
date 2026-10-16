package com.carrot123.until_eternity.trial;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;

/** Server-thread provenance for the synchronous summon-to-Apollyon insertion. */
public final class TrialApollyonSpawnContext {
    public static final ResourceLocation SUMMON_ID = new ResourceLocation("goety_revelation", "summon_apollyon");
    private static final ThreadLocal<Entity> SOURCE = new ThreadLocal<>();

    private TrialApollyonSpawnContext() {}

    public static boolean isSummonApollyon(Entity entity) {
        return SUMMON_ID.equals(BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType()))
                && entity.getClass().getName().equals("z1gned.goetyrevelation.entitiy.SummonApollyon");
    }

    public static Entity source() { return SOURCE.get(); }

    public static void duringTick(Entity entity, Runnable original) {
        if (!isSummonApollyon(entity) || !(entity.level() instanceof ServerLevel level)
                || !level.dimension().equals(TrialRealm.KEY)) {
            original.run();
            return;
        }
        CompoundTag tag = entity.getPersistentData();
        if (!tag.hasUUID(TrialMonumentBlockEntity.MOB_RUN)
                || !tag.contains(TrialMonumentBlockEntity.MOB_MONUMENT)) {
            original.run();
            return;
        }
        BlockPos pos = BlockPos.of(tag.getLong(TrialMonumentBlockEntity.MOB_MONUMENT));
        if (!(level.getBlockEntity(pos) instanceof TrialMonumentBlockEntity monument)
                || !monument.awaitingApollyon(entity)) {
            original.run();
            return;
        }
        Entity previous = SOURCE.get();
        SOURCE.set(entity);
        try { original.run(); }
        finally {
            if (previous == null) SOURCE.remove();
            else SOURCE.set(previous);
        }
    }
}
