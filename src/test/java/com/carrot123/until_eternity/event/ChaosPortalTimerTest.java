package com.carrot123.until_eternity.event;

import net.minecraft.core.BlockPos;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ChaosPortalTimerTest {
    @Test
    void touchCountsAtMostOncePerGameTick() {
        ChaosPortalTimer timer = new ChaosPortalTimer(BlockPos.ZERO);

        assertEquals(1, timer.touch(new BlockPos(1, 2, 3), 10L));
        assertEquals(1, timer.touch(new BlockPos(1, 3, 3), 10L));
        assertEquals(2, timer.touch(new BlockPos(1, 2, 3), 11L));
    }

    @Test
    void untouchedTimeDecaysByFourUntilEmpty() {
        ChaosPortalTimer timer = new ChaosPortalTimer(BlockPos.ZERO);
        for (long gameTime = 0L; gameTime < 8L; gameTime++) {
            timer.touch(BlockPos.ZERO, gameTime);
        }

        assertFalse(timer.decayIfUntouched(8L));
        assertEquals(4, timer.portalTime());
        assertTrue(timer.decayIfUntouched(9L));
        assertEquals(0, timer.portalTime());
    }

    @Test
    void contactOnCurrentTickDoesNotDecay() {
        ChaosPortalTimer timer = new ChaosPortalTimer(BlockPos.ZERO);

        timer.touch(BlockPos.ZERO, 20L);

        assertFalse(timer.decayIfUntouched(20L));
        assertEquals(1, timer.portalTime());
    }

    @Test
    void entranceMovesOnlyAfterAContactGap() {
        BlockPos first = new BlockPos(1, 2, 3);
        BlockPos sameTick = new BlockPos(1, 3, 3);
        BlockPos afterGap = new BlockPos(8, 9, 10);
        ChaosPortalTimer timer = new ChaosPortalTimer(BlockPos.ZERO);

        timer.touch(first, 10L);
        timer.touch(sameTick, 10L);
        assertEquals(first, timer.entrancePos());

        timer.touch(afterGap, 12L);
        assertEquals(afterGap, timer.entrancePos());
    }
}
