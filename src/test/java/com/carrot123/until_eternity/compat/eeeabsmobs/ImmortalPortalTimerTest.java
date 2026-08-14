package com.carrot123.until_eternity.compat.eeeabsmobs;

import net.minecraft.core.BlockPos;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ImmortalPortalTimerTest {
    @Test
    void multiplePortalBlocksOnlyCountOncePerTick() {
        ImmortalPortalTimer timer =
                new ImmortalPortalTimer(new BlockPos(1, 64, 1));

        assertEquals(1, timer.touch(new BlockPos(1, 64, 1), 20L));
        assertEquals(1, timer.touch(new BlockPos(2, 64, 1), 20L));
        assertEquals(2, timer.touch(new BlockPos(2, 65, 1), 21L));
        assertEquals(new BlockPos(1, 64, 1), timer.entrancePos());
    }

    @Test
    void leavingPortalDecaysByFourAndEventuallyClears() {
        ImmortalPortalTimer timer =
                new ImmortalPortalTimer(BlockPos.ZERO);
        for (long tick = 1; tick <= 9; tick++) {
            timer.touch(BlockPos.ZERO, tick);
        }

        assertFalse(timer.decayIfUntouched(10L));
        assertEquals(5, timer.portalTime());
        assertFalse(timer.decayIfUntouched(11L));
        assertEquals(1, timer.portalTime());
        assertTrue(timer.decayIfUntouched(12L));
        assertEquals(0, timer.portalTime());
    }

    @Test
    void aNewContactStreakUpdatesTheEntrancePosition() {
        ImmortalPortalTimer timer =
                new ImmortalPortalTimer(BlockPos.ZERO);
        timer.touch(BlockPos.ZERO, 1L);
        timer.decayIfUntouched(2L);

        BlockPos newPortal = new BlockPos(100, 64, -50);
        timer.touch(newPortal, 4L);
        assertEquals(newPortal, timer.entrancePos());
    }

}
