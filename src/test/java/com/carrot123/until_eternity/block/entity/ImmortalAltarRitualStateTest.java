package com.carrot123.until_eternity.block.entity;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ImmortalAltarRitualStateTest {
    @Test
    void completesExactlyOnFortiethTick() {
        ImmortalAltarRitualState state = new ImmortalAltarRitualState();
        assertTrue(state.start());
        for (int tick = 1; tick < 40; tick++) {
            assertFalse(state.advanceAndShouldComplete(),
                    "completed at tick " + tick);
        }
        assertTrue(state.advanceAndShouldComplete());
        assertEquals(40, state.activationTicks());
        state.stop();
        assertFalse(state.isActivating());
        assertEquals(0, state.activationTicks());
    }

    @Test
    void duplicateStartCannotResetProgress() {
        ImmortalAltarRitualState state = new ImmortalAltarRitualState();
        assertTrue(state.start());
        for (int tick = 0; tick < 17; tick++) {
            state.advanceAndShouldComplete();
        }
        assertFalse(state.start());
        assertEquals(17, state.activationTicks());
    }

    @Test
    void persistedProgressResumesAndInactiveDataIsNormalized() {
        ImmortalAltarRitualState state = new ImmortalAltarRitualState();
        state.load(true, 39);
        assertTrue(state.advanceAndShouldComplete());

        state.load(false, 25);
        assertFalse(state.isActivating());
        assertEquals(0, state.activationTicks());
    }
}
