package com.carrot123.until_eternity.tarot;

/** Runtime state owned by one TarotSetManager.PlayerState. */
final class TarotForesightCharge {
    static final long RECHARGE_TICKS = 300L;

    private boolean active;
    private boolean ready;
    private long nextReadyTick;

    void activate(long now) {
        active = true;
        ready = false;
        nextReadyTick = now + RECHARGE_TICKS;
    }

    void clear() {
        active = false;
        ready = false;
        nextReadyTick = 0L;
    }

    void tick(long now) {
        if (active && !ready && now >= nextReadyTick) {
            ready = true;
        }
    }

    boolean consume(long now) {
        if (!active || !ready) {
            return false;
        }
        ready = false;
        nextReadyTick = now + RECHARGE_TICKS;
        return true;
    }

    boolean isReady() {
        return ready;
    }
}
