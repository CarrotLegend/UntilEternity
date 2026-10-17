package com.carrot123.until_eternity.trial;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TrialPortalTravelTest {
    private static final Vec3 ENTRY = new Vec3(0.5, 65, 0.5);

    @Test void walkingAndSprintingCrossingTheDiskAllowTwoContactTicks() {
        assertTrue(consecutiveContactTicks(0.10) >= 2);
        assertTrue(consecutiveContactTicks(0.28) >= 2);
    }

    @Test void safePreferredPositionWinsWithoutSearching() {
        List<Vec3> checked = new ArrayList<>();
        assertEquals(ENTRY, TrialPortalTravel.findNearby(ENTRY, candidate -> {
            checked.add(candidate);
            return true;
        }));
        assertEquals(List.of(ENTRY), checked);
    }

    @Test void sameHeightSearchChoosesNearestAndHasDeterministicTies() {
        List<Vec3> checked = new ArrayList<>();
        Vec3 selected = TrialPortalTravel.findNearby(ENTRY, candidate -> {
            checked.add(candidate);
            return candidate.equals(new Vec3(1.5, 65, 0.5))
                    || candidate.equals(new Vec3(2.5, 65, 0.5));
        });
        assertEquals(new Vec3(1.5, 65, 0.5), selected);
        assertEquals(ENTRY, checked.get(0));
        assertTrue(checked.stream().skip(1).allMatch(candidate -> candidate.y == 65));

        List<Vec3> firstRun = new ArrayList<>();
        List<Vec3> secondRun = new ArrayList<>();
        TrialPortalTravel.findNearby(ENTRY, candidate -> { firstRun.add(candidate); return false; });
        TrialPortalTravel.findNearby(ENTRY, candidate -> { secondRun.add(candidate); return false; });
        assertEquals(firstRun, secondRun);
    }

    @Test void verticalSearchStartsOnlyAfterSameHeightAndStaysWithinBounds() {
        List<Vec3> checked = new ArrayList<>();
        Vec3 selected = TrialPortalTravel.findNearby(ENTRY, candidate -> {
            checked.add(candidate);
            return candidate.equals(new Vec3(0.5, 66, 0.5));
        });
        assertEquals(new Vec3(0.5, 66, 0.5), selected);
        assertTrue(checked.subList(0, checked.size() - 1).stream()
                .allMatch(candidate -> candidate.y == 65));

        List<Vec3> exhausted = new ArrayList<>();
        assertNull(TrialPortalTravel.findNearby(ENTRY, candidate -> {
            exhausted.add(candidate);
            return candidate.equals(new Vec3(13.5, 65, 0.5))
                    || candidate.equals(new Vec3(0.5, 70, 0.5));
        }));
        assertTrue(exhausted.size() < 4000);
        assertTrue(exhausted.stream().allMatch(candidate ->
                Math.pow(candidate.x - ENTRY.x, 2) + Math.pow(candidate.z - ENTRY.z, 2) <= 144
                        && Math.abs(candidate.y - ENTRY.y) <= 4));
    }

    @Test void recordedPositionIsCheckedExactlyBeforeBlockCenters() {
        Vec3 recorded = new Vec3(0.25, 65, 0.75);
        List<Vec3> checked = new ArrayList<>();
        Vec3 selected = TrialPortalTravel.findNearby(recorded, candidate -> {
            checked.add(candidate);
            return candidate.equals(new Vec3(0.5, 65, 0.5));
        });
        assertEquals(new Vec3(0.5, 65, 0.5), selected);
        assertEquals(List.of(recorded, selected), checked);
    }

    private static int consecutiveContactTicks(double blocksPerTick) {
        BlockPos anchor = new BlockPos(0, 65, 0);
        int consecutive = 0;
        int maximum = 0;
        for (int tick = 0; tick < 25; tick++) {
            double z = -0.6 + tick * blocksPerTick;
            AABB player = new AABB(1.7, 65, z - 0.3, 2.3, 66.8, z + 0.3);
            if (TrialPortalGeometry.intersects(anchor, Direction.Axis.X, player)) {
                consecutive++;
                maximum = Math.max(maximum, consecutive);
            } else {
                consecutive = 0;
            }
        }
        return maximum;
    }
}
