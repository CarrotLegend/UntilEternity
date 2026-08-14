package com.carrot123.until_eternity.effect;

import java.util.function.Supplier;

public final class ManaEruptionMergeGuard {
    private static final ThreadLocal<Integer> DEPTH =
            ThreadLocal.withInitial(() -> 0);

    private ManaEruptionMergeGuard() {
    }

    public static boolean isActive() {
        return DEPTH.get() > 0;
    }

    public static <T> T call(Supplier<T> action) {
        push();
        try {
            return action.get();
        } finally {
            pop();
        }
    }

    public static void run(Runnable action) {
        push();
        try {
            action.run();
        } finally {
            pop();
        }
    }

    static int depth() {
        return DEPTH.get();
    }

    private static void push() {
        DEPTH.set(DEPTH.get() + 1);
    }

    private static void pop() {
        int depth = DEPTH.get() - 1;
        if (depth <= 0) {
            DEPTH.remove();
        } else {
            DEPTH.set(depth);
        }
    }
}
