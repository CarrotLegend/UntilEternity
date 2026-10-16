package com.carrot123.until_eternity.trial;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class TrialApollyonOwnershipTest {
    @Test void ownershipRequiresRunMonumentAndLiveObjectIdentity() {
        UUID run = UUID.randomUUID();
        BlockPos monument = new BlockPos(0, 65, 3);
        TrialApollyonOwnership.Entry entry = new TrialApollyonOwnership.Entry(run, monument, null, false);
        assertTrue(entry.matches(run, monument, true));
        assertFalse(entry.matches(UUID.randomUUID(), monument, true));
        assertFalse(entry.matches(run, monument.above(), true));
        assertFalse(entry.matches(run, monument, false));
        entry.cleanup = true;
        assertFalse(entry.matches(run, monument, true));
    }

    @Test void savedOwnershipAlwaysRestoresAsCleanupOnly() {
        UUID id = UUID.randomUUID();
        UUID run = UUID.randomUUID();
        BlockPos monument = new BlockPos(2, 65, 4);
        CompoundTag record = new CompoundTag();
        record.putUUID("Entity", id);
        record.putUUID("Run", run);
        record.putLong("Monument", monument.asLong());
        ListTag records = new ListTag();
        records.add(record);
        CompoundTag root = new CompoundTag();
        root.put("Entries", records);
        TrialApollyonOwnership restored = TrialApollyonOwnership.load(root);
        assertTrue(restored.knows(id));
        assertTrue(restored.pendingCleanup(id));
        assertEquals(root, restored.save(new CompoundTag()));
        restored.forget(id);
        assertFalse(restored.knows(id));
    }
}
