package com.carrot123.until_eternity.trial;

import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class TrialApollyonHandoffTest {
    private final UUID source = UUID.randomUUID();
    private final UUID run = UUID.randomUUID();
    private final UUID boss = UUID.randomUUID();
    private final BlockPos monument = new BlockPos(1, 65, 2);
    private final Vec3 position = new Vec3(10.5, 65, 10.5);

    @Test void acceptsOnlyTheTrueBossFromTheExactSummonAndRun() {
        TrialApollyonHandoff handoff = new TrialApollyonHandoff(source, run, monument, 12, position);
        assertEquals(java.util.Set.of(source), handoff.cleanupIds());
        assertFalse(handoff.offer(UUID.randomUUID(), run, monument, 12, boss, position, true, 450, 100));
        assertFalse(handoff.offer(source, UUID.randomUUID(), monument, 12, boss, position, true, 450, 100));
        assertFalse(handoff.offer(source, run, monument.offset(1, 0, 0), 12, boss, position, true, 450, 100));
        assertFalse(handoff.offer(source, run, monument, 11, boss, position, true, 450, 100));
        assertFalse(handoff.offer(source, run, monument, 12, boss, position, false, 450, 100));
        assertFalse(handoff.offer(source, run, monument, 12, boss, position, true, 449, 100));
        assertFalse(handoff.offer(source, run, monument, 12, boss, position.add(2, 0, 0), true, 450, 100));
        assertFalse(handoff.isCandidate(boss));
        assertTrue(handoff.offer(source, run, monument, 12, boss, position, true, 450, 100));
        assertTrue(handoff.isCandidate(boss));
        assertEquals(java.util.Set.of(source, boss), handoff.cleanupIds());
        assertFalse(handoff.offer(source, run, monument, 12, UUID.randomUUID(), position, true, 450, 101));
    }

    @Test void candidateStateSurvivesSaveAndCannotBeOfferedAgain() {
        TrialApollyonHandoff handoff = new TrialApollyonHandoff(source, run, monument, 12, position);
        assertTrue(handoff.offer(source, run, monument, 12, boss, position, true, 450, 100));
        TrialApollyonHandoff restored = TrialApollyonHandoff.load(handoff.save());
        assertNotNull(restored);
        assertEquals(TrialApollyonHandoff.Stage.CANDIDATE, restored.stage);
        assertEquals(100, restored.candidateSince);
        assertTrue(restored.isCandidate(boss));
        assertFalse(restored.matchesSource(source, run, monument, 12));
    }
}
