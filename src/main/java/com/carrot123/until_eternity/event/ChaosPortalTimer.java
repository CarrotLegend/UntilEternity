package com.carrot123.until_eternity.event;

import net.minecraft.core.BlockPos;

final class ChaosPortalTimer {
    private BlockPos entrancePos;
    private int portalTime;
    private long lastContactTick = Long.MIN_VALUE;
    private long lastCountedTick = Long.MIN_VALUE;

    ChaosPortalTimer(BlockPos entrancePos) {
        this.entrancePos = entrancePos.immutable();
    }

    int touch(BlockPos currentPos, long gameTime) {
        if (lastContactTick < gameTime - 1L) {
            entrancePos = currentPos.immutable();
        }
        lastContactTick = gameTime;
        if (lastCountedTick != gameTime) {
            lastCountedTick = gameTime;
            portalTime++;
        }
        return portalTime;
    }

    boolean decayIfUntouched(long gameTime) {
        if (lastContactTick < gameTime) {
            portalTime = Math.max(0, portalTime - 4);
        }
        return portalTime == 0;
    }

    BlockPos entrancePos() {
        return entrancePos;
    }

    int portalTime() {
        return portalTime;
    }
}
