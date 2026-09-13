package com.carrot123.until_eternity.compat.revelationfix;

public final class HaloAttackCheckGuard {

    private static final ThreadLocal<Integer> DEPTH = new ThreadLocal<>();

    private HaloAttackCheckGuard() {
    }

    public static void enter() {
        Integer depth = DEPTH.get();
        DEPTH.set(depth == null ? 1 : depth + 1);
    }

    public static void exit() {
        Integer depth = DEPTH.get();

        if (depth == null || depth <= 1) {
            DEPTH.remove();
        } else {
            DEPTH.set(depth - 1);
        }
    }

    public static boolean isActive() {
        Integer depth = DEPTH.get();
        return depth != null && depth > 0;
    }
}
