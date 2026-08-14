package com.carrot123.until_eternity.compat;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ScopedValueStackTest {
    @Test
    void supportsNestingAndAlwaysCleansUp() {
        ScopedValueStack<String> context = new ScopedValueStack<>();
        assertEquals("none", context.current("none"));

        String result = context.withValue("outer", () -> {
            assertEquals("outer", context.current("none"));
            return context.withValue("inner", () -> {
                assertEquals("inner", context.current("none"));
                return "ok";
            });
        });
        assertEquals("ok", result);
        assertEquals("none", context.current("none"));

        assertThrows(IllegalStateException.class, () ->
                context.withValue("failure", () -> {
                    throw new IllegalStateException("expected");
                }));
        assertEquals("none", context.current("none"));
    }

    @Test
    void cleansUpAfterBothBooleanReturnValues() {
        ScopedValueStack<String> context = new ScopedValueStack<>();

        assertTrue(context.withValue("true", () -> {
            assertEquals("true", context.current("none"));
            return true;
        }));
        assertEquals("none", context.current("none"));

        assertFalse(context.withValue("false", () -> {
            assertEquals("false", context.current("none"));
            return false;
        }));
        assertEquals("none", context.current("none"));
    }
}
