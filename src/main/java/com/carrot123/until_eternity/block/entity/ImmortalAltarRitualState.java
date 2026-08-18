package com.carrot123.until_eternity.block.entity;

final class ImmortalAltarRitualState {

    static final int DURATION_TICKS = 40;

    private boolean activating;
    private int activationTicks;

    boolean start() {
        if (activating) {
            return false;
        }

        activating = true;
        activationTicks = 0;
        return true;
    }

    boolean advanceAndShouldComplete() {
        if (!activating) {
            return false;
        }

        activationTicks++;
        return activationTicks >= DURATION_TICKS;
    }

    void stop() {
        activating = false;
        activationTicks = 0;
    }

    void load(boolean activating, int activationTicks) {
        this.activating = activating;

        if (activating) {
            this.activationTicks = Math.max(
                    0,
                    Math.min(DURATION_TICKS, activationTicks)
            );
        } else {
            this.activationTicks = 0;
        }
    }

    boolean isActivating() {
        return activating;
    }

    int activationTicks() {
        return activationTicks;
    }
}