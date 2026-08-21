package com.carrot123.until_eternity.compat.goety;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GoetyFocusCastContextTest {
    @Test
    void nestedTrackedCastersRestoreOuterContext() {
        UUID outer = UUID.fromString(
                "cd3a5b58-9795-43dc-87c4-2c89aec3e4c7");
        UUID inner = UUID.fromString(
                "b588a8b1-c4b0-4774-803d-b6d91d136128");

        GoetyFocusCastContext.withTrackedCaster(outer, () -> {
            assertEquals(outer, GoetyFocusCastContext
                    .currentCasterUuid().orElseThrow());
            GoetyFocusCastContext.withTrackedCaster(inner, () -> {
                assertEquals(inner, GoetyFocusCastContext
                        .currentCasterUuid().orElseThrow());
                return null;
            });
            assertEquals(outer, GoetyFocusCastContext
                    .currentCasterUuid().orElseThrow());
            return null;
        });
        assertTrue(GoetyFocusCastContext.currentCasterUuid().isEmpty());
    }

    @Test
    void exceptionAlwaysClearsContext() {
        UUID caster = UUID.fromString(
                "059a2bdd-89a3-4590-bf24-72c222d0d4ce");
        assertThrows(IllegalStateException.class,
                () -> GoetyFocusCastContext.withTrackedCaster(caster, () -> {
                    throw new IllegalStateException("test");
                }));
        assertTrue(GoetyFocusCastContext.currentCasterUuid().isEmpty());
    }
}
