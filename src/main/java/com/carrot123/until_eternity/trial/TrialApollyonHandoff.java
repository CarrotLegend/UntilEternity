package com.carrot123.until_eternity.trial;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.phys.Vec3;

import java.util.UUID;
import java.util.Set;

/** One strictly paired summoning entity and eventual Apollyon for a trial run. */
final class TrialApollyonHandoff {
    enum Stage { SUMMONING, CANDIDATE }

    final UUID summonId;
    final UUID runId;
    final BlockPos monument;
    final int wave;
    final Vec3 expectedPos;
    Stage stage;
    UUID candidateId;
    long candidateSince;

    TrialApollyonHandoff(UUID summonId, UUID runId, BlockPos monument, int wave, Vec3 expectedPos) {
        this.summonId = summonId;
        this.runId = runId;
        this.monument = monument.immutable();
        this.wave = wave;
        this.expectedPos = expectedPos;
        this.stage = Stage.SUMMONING;
    }

    boolean matchesSource(UUID sourceId, UUID currentRun, BlockPos currentMonument, int currentWave) {
        return stage == Stage.SUMMONING && summonId.equals(sourceId) && runId.equals(currentRun)
                && monument.equals(currentMonument) && wave == currentWave;
    }

    boolean offer(UUID sourceId, UUID currentRun, BlockPos currentMonument, int currentWave,
                  UUID proposedId, Vec3 proposedPos, boolean isApollyon, int summonAge, long gameTime) {
        if (!matchesSource(sourceId, currentRun, currentMonument, currentWave)
                || !isApollyon || summonAge != 450 || proposedId.equals(summonId)
                || proposedPos.distanceToSqr(expectedPos) > 1.0D) return false;
        candidateId = proposedId;
        candidateSince = gameTime;
        stage = Stage.CANDIDATE;
        return true;
    }

    boolean isCandidate(UUID id) { return stage == Stage.CANDIDATE && id.equals(candidateId); }

    Set<UUID> cleanupIds() {
        return candidateId == null ? Set.of(summonId) : Set.of(summonId, candidateId);
    }

    CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        tag.putString("Stage", stage.name());
        tag.putUUID("Summon", summonId);
        tag.putUUID("Run", runId);
        tag.putLong("Monument", monument.asLong());
        tag.putInt("Wave", wave);
        tag.putDouble("X", expectedPos.x);
        tag.putDouble("Y", expectedPos.y);
        tag.putDouble("Z", expectedPos.z);
        if (candidateId != null) tag.putUUID("Candidate", candidateId);
        tag.putLong("CandidateSince", candidateSince);
        return tag;
    }

    static TrialApollyonHandoff load(CompoundTag tag) {
        if (!tag.hasUUID("Summon") || !tag.hasUUID("Run")) return null;
        TrialApollyonHandoff handoff = new TrialApollyonHandoff(tag.getUUID("Summon"), tag.getUUID("Run"),
                BlockPos.of(tag.getLong("Monument")), tag.getInt("Wave"),
                new Vec3(tag.getDouble("X"), tag.getDouble("Y"), tag.getDouble("Z")));
        if ("CANDIDATE".equals(tag.getString("Stage")) && tag.hasUUID("Candidate")) {
            handoff.stage = Stage.CANDIDATE;
            handoff.candidateId = tag.getUUID("Candidate");
            handoff.candidateSince = tag.getLong("CandidateSince");
        }
        return handoff;
    }
}
