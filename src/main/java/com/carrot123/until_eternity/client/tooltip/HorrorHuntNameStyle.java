package com.carrot123.until_eternity.client.tooltip;

final class HorrorHuntNameStyle {
    static final int TEXT_HEIGHT = 9;
    static final int COMPONENT_HEIGHT = 16;
    static final float MIN_SCALE = 1.12F;
    static final float MAX_SCALE = 1.26F;
    static final int FRAME_MILLIS = 35;

    static final int MAIN_COLOR = 0xFF820016;
    static final int OUTLINE_COLOR = 0xFF000000;
    static final int GHOST_OUTLINE_COLOR = 0xC0430008;

    static final int[][] OUTLINE_OFFSETS = {
            {-1, -1}, {0, -1}, {1, -1},
            {-1, 0},           {1, 0},
            {-1, 1},  {0, 1}, {1, 1}
    };

    private HorrorHuntNameStyle() {
    }

    static int applyFade(int color, int fadeAlpha) {
        int clampedFade = Math.max(0, Math.min(255, fadeAlpha));
        int baseAlpha = color >>> 24;
        int combinedAlpha = (baseAlpha * clampedFade + 127) / 255;
        return (color & 0x00FFFFFF) | (combinedAlpha << 24);
    }
}
