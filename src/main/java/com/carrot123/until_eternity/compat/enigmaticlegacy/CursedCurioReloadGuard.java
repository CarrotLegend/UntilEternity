package com.carrot123.until_eternity.compat.enigmaticlegacy;

public final class CursedCurioReloadGuard {

    private static final ThreadLocal<Integer> DEPTH =
            ThreadLocal.withInitial(
                    () -> 0
            );

    private CursedCurioReloadGuard() {
    }

    public static void enter() {
        DEPTH.set(
                DEPTH.get() + 1
        );
    }

    public static void exit() {
        int depth =
                DEPTH.get() - 1;

        if (depth <= 0) {
            DEPTH.remove();
            return;
        }

        DEPTH.set(depth);
    }

    public static boolean isRestoring() {
        return DEPTH.get() > 0;
    }
}