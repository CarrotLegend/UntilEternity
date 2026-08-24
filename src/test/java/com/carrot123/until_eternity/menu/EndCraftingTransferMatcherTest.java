package com.carrot123.until_eternity.menu;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EndCraftingTransferMatcherTest {
    @Test
    void overlappingCandidatesReserveTheStrictGroup() {
        var match = EndCraftingTransferMatcher.match(
                new int[]{1, 1}, new int[]{64, 64}, new int[]{-1, -1},
                new boolean[][]{{true, true}, {true, false}}, 1);
        assertTrue(match.success());
        assertArrayEquals(new int[]{1, 0}, match.assignments());
    }

    @Test
    void repeatedSlotsConsumeRealCounts() {
        boolean[][] accepts = new boolean[25][1];
        for (boolean[] cell : accepts) cell[0] = true;
        var enough = EndCraftingTransferMatcher.match(
                new int[]{25}, new int[]{64}, new int[25], accepts, 1);
        assertTrue(enough.success());
        var shortOne = EndCraftingTransferMatcher.match(
                new int[]{24}, new int[]{64}, new int[25], accepts, 1);
        assertFalse(shortOne.success());
        assertEquals(1, shortOne.missingCells().size());
    }

    @Test
    void maxSetsRespectCountsAndStackLimits() {
        boolean[][] accepts = {{true}};
        assertTrue(EndCraftingTransferMatcher.match(
                new int[]{10}, new int[]{64}, new int[]{0}, accepts, 10).success());
        assertFalse(EndCraftingTransferMatcher.match(
                new int[]{10}, new int[]{1}, new int[]{0}, accepts, 10).success());
    }

    @Test
    void currentGroupIsPreferredWhenBothAreLegal() {
        var match = EndCraftingTransferMatcher.match(
                new int[]{1, 1}, new int[]{64, 64}, new int[]{1},
                new boolean[][]{{true, true}}, 1);
        assertTrue(match.success());
        assertArrayEquals(new int[]{1}, match.assignments());
    }
}
