package com.carrot123.until_eternity.event;

import net.minecraft.core.BlockPos;

final class DivinePortalTimer {
    private BlockPos entrance;
    private int portalTime;
    private long lastTouched = Long.MIN_VALUE;
    private long lastCounted = Long.MIN_VALUE;

    DivinePortalTimer(BlockPos pos) {
        entrance = pos.immutable();
    }

    int touch(BlockPos pos, long gameTime) {
        if (lastTouched < gameTime - 1L) entrance = pos.immutable();
        lastTouched = gameTime;
        if (lastCounted != gameTime) {
            lastCounted = gameTime;
            portalTime++;
        }
        return portalTime;
    }

    boolean decayIfUntouched(long gameTime) {
        if (lastTouched < gameTime) portalTime = Math.max(0, portalTime - 4);
        return portalTime == 0;
    }

    BlockPos entrance() { return entrance; }
}
