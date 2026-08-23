package com.carrot123.until_eternity.util;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextColor;

public final class RainbowTextHelper {

    public static final String RAINBOW_MARKER = "{/rainbow/}";
    private static final long CYCLE_TIME_MS = 4000L;
    private static final float CHARACTER_HUE_STEP = 0.10F;

    private RainbowTextHelper() {
    }

    public static MutableComponent parse(String text) {
        MutableComponent result = Component.empty();

        if (text == null || text.isEmpty()) {
            return result;
        }

        boolean rainbow = false;
        int rainbowCharacterIndex = 0;
        long currentTime = System.currentTimeMillis();
        float baseHue = (currentTime % CYCLE_TIME_MS) / (float) CYCLE_TIME_MS;
        int index = 0;

        while (index < text.length()) {

            if (text.startsWith(
                    RAINBOW_MARKER,
                    index
            )) {
                rainbow = !rainbow;
                index += RAINBOW_MARKER.length();
                continue;
            }

            int codePoint = text.codePointAt(index);

            String character =
                    new String(Character.toChars(codePoint));

            if (rainbow) {
                float hue = (baseHue + rainbowCharacterIndex * CHARACTER_HUE_STEP) % 1.0F;
                int rgb = hsvToRgb(hue, 1.0F, 1.0F);

                result.append(
                        Component.literal(character)
                                .withStyle(style ->
                                        style.withColor(
                                                TextColor.fromRgb(rgb)
                                        )
                                )
                );

                rainbowCharacterIndex++;
            } else {
                result.append(
                        Component.literal(character)
                );
            }

            index += Character.charCount(codePoint);
        }

        return result;
    }

    private static int hsvToRgb(
            float hue,
            float saturation,
            float value
    ) {
        hue = hue - (float) Math.floor(hue);
        float h = hue * 6.0F;
        int sector = (int) Math.floor(h);
        float fraction = h - sector;
        float p = value * (1.0F - saturation);
        float q = value * (1.0F - saturation * fraction);
        float t = value * (1.0F - saturation * (1.0F - fraction));
        float r;
        float g;
        float b;

        switch (sector % 6) {
            case 0 -> {
                r = value;
                g = t;
                b = p;
            }
            case 1 -> {
                r = q;
                g = value;
                b = p;
            }
            case 2 -> {
                r = p;
                g = value;
                b = t;
            }
            case 3 -> {
                r = p;
                g = q;
                b = value;
            }
            case 4 -> {
                r = t;
                g = p;
                b = value;
            }
            default -> {
                r = value;
                g = p;
                b = q;
            }
        }

        int red = Math.max(0, Math.min(255, Math.round(r * 255.0F)));
        int green = Math.max(0, Math.min(255, Math.round(g * 255.0F)));
        int blue = Math.max(0, Math.min(255, Math.round(b * 255.0F)));

        return (red << 16)
                | (green << 8)
                | blue;
    }
}
