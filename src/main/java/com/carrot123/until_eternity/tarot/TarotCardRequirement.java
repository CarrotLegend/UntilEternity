package com.carrot123.until_eternity.tarot;

public enum TarotCardRequirement {
    UPRIGHT,
    REVERSED,
    ANY;

    public boolean accepts(TarotOrientation orientation) {
        return orientation != null && (this == ANY || name().equals(orientation.name()));
    }
}
