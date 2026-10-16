package com.carrot123.until_eternity.trial;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/** Durable provenance for trial Apollyon; entity persistent data is only a compatibility hint. */
final class TrialApollyonOwnership extends SavedData {
    private static final String NAME = "until_eternity_trial_apollyon";
    private static final ResourceLocation APOSTLE_ID = new ResourceLocation("goety", "apostle");
    private final Map<UUID, Entry> entries = new HashMap<>();

    static TrialApollyonOwnership get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(TrialApollyonOwnership::load,
                TrialApollyonOwnership::new, NAME);
    }

    static final class Entry {
        final UUID run;
        final BlockPos monument;
        Entity live;
        boolean cleanup;

        Entry(UUID run, BlockPos monument, Entity live, boolean cleanup) {
            this.run = run;
            this.monument = monument.immutable();
            this.live = live;
            this.cleanup = cleanup;
        }

        boolean matches(UUID expectedRun, BlockPos expectedMonument, boolean sameEntity) {
            return !cleanup && run.equals(expectedRun) && monument.equals(expectedMonument)
                    && sameEntity;
        }
    }

    boolean register(Entity entity, UUID run, BlockPos monument) {
        if (entries.containsKey(entity.getUUID())) return false;
        entries.put(entity.getUUID(), new Entry(run, monument, entity, false));
        setDirty();
        return true;
    }

    void rememberOld(UUID id, UUID run, BlockPos monument) {
        if (!entries.containsKey(id)) {
            entries.put(id, new Entry(run, monument, null, true));
            setDirty();
        }
    }

    Entry active(Entity entity) {
        Entry entry = entries.get(entity.getUUID());
        return entry != null && !entry.cleanup && entry.live == entity ? entry : null;
    }

    boolean knows(UUID id) { return entries.containsKey(id); }

    boolean pendingCleanup(UUID id) {
        Entry entry = entries.get(id);
        return entry != null && entry.cleanup;
    }

    boolean shouldCancelJoin(Entity entity) {
        Entry entry = entries.get(entity.getUUID());
        return entry != null && (entry.cleanup || entry.live != entity)
                && isApostle(entity);
    }

    void forget(UUID id) {
        if (entries.remove(id) != null) setDirty();
    }

    void cleanup(ServerLevel level, UUID id) {
        Entry entry = entries.get(id);
        if (entry == null) return;
        Entity entity = level.getEntity(id);
        if (entity != null && entity.level() == level && isApostle(entity)) {
            entity.discard();
            forget(id);
        } else {
            entry.live = null;
            entry.cleanup = true;
            setDirty();
        }
    }

    private static boolean isApostle(Entity entity) {
        return APOSTLE_ID.equals(BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType()));
    }

    static TrialApollyonOwnership load(CompoundTag root) {
        TrialApollyonOwnership data = new TrialApollyonOwnership();
        for (Tag raw : root.getList("Entries", Tag.TAG_COMPOUND)) {
            CompoundTag tag = (CompoundTag) raw;
            if (tag.hasUUID("Entity") && tag.hasUUID("Run") && tag.contains("Monument", Tag.TAG_LONG))
                // An unfinished run is never resumed after a server restart.
                data.entries.put(tag.getUUID("Entity"),
                        new Entry(tag.getUUID("Run"), BlockPos.of(tag.getLong("Monument")), null, true));
        }
        return data;
    }

    @Override public CompoundTag save(CompoundTag root) {
        ListTag list = new ListTag();
        entries.forEach((id, entry) -> {
            CompoundTag tag = new CompoundTag();
            tag.putUUID("Entity", id);
            tag.putUUID("Run", entry.run);
            tag.putLong("Monument", entry.monument.asLong());
            list.add(tag);
        });
        root.put("Entries", list);
        return root;
    }
}
