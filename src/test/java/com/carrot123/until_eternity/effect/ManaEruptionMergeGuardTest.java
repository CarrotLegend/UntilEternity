package com.carrot123.until_eternity.effect;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ManaEruptionMergeGuardTest {
    @Test
    void normalReturnCleansTheOutermostScope() {
        assertFalse(ManaEruptionMergeGuard.isActive());
        assertEquals("ok", ManaEruptionMergeGuard.call(() -> {
            assertTrue(ManaEruptionMergeGuard.isActive());
            assertEquals(1, ManaEruptionMergeGuard.depth());
            return "ok";
        }));
        assertFalse(ManaEruptionMergeGuard.isActive());
        assertEquals(0, ManaEruptionMergeGuard.depth());
    }

    @Test
    void nestedCallsRestoreTheOuterDepth() {
        ManaEruptionMergeGuard.run(() -> {
            assertEquals(1, ManaEruptionMergeGuard.depth());
            ManaEruptionMergeGuard.run(() ->
                    assertEquals(2, ManaEruptionMergeGuard.depth()));
            assertEquals(1, ManaEruptionMergeGuard.depth());
        });
        assertEquals(0, ManaEruptionMergeGuard.depth());
    }

    @Test
    void exceptionPathStillRemovesTheThreadLocalValue() {
        assertThrows(IllegalStateException.class, () ->
                ManaEruptionMergeGuard.run(() -> {
                    throw new IllegalStateException("expected");
                }));
        assertFalse(ManaEruptionMergeGuard.isActive());
        assertEquals(0, ManaEruptionMergeGuard.depth());
    }
}
