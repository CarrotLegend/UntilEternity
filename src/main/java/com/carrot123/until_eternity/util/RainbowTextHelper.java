package com.carrot123.until_eternity.util;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.util.FormattedCharSequence;

public final class RainbowTextHelper {

    public static final String MARKER =
            "{/rainbow/}";

    private static final int[] MARKER_CODE_POINTS =
            MARKER.codePoints().toArray();

    private static final long CYCLE_TIME_MS =
            3000L;

    private static final float CHARACTER_HUE_OFFSET =
            0.08F;

    private RainbowTextHelper() {
    }

    public static FormattedCharSequence transform(
            FormattedCharSequence source
    ) {
        if (source == null) {
            return FormattedCharSequence.EMPTY;
        }

        if (!containsMarker(source)) {
            return source;
        }

        List<StyledCodePoint> input =
                new ArrayList<>();

        source.accept(
                (index, style, codePoint) -> {
                    input.add(
                            new StyledCodePoint(
                                    style,
                                    codePoint
                            )
                    );

                    return true;
                }
        );

        long now = System.currentTimeMillis();
        float baseHue = (now % CYCLE_TIME_MS) / (float) CYCLE_TIME_MS;

        List<StyledCodePoint> output =
                new ArrayList<>(input.size());

        boolean rainbowEnabled = false;

        int rainbowCharacterIndex = 0;
        int i = 0;

        while (i < input.size()) {
            if (matchesMarker(input, i)) {
                rainbowEnabled =
                        !rainbowEnabled;
                i += MARKER_CODE_POINTS.length;
                continue;
            }

            StyledCodePoint current =
                    input.get(i);

            Style style = current.style();

            if (rainbowEnabled) {
                float hue = (baseHue + rainbowCharacterIndex * CHARACTER_HUE_OFFSET) % 1.0F;
                int rgb = hsvToRgb(hue, 1.0F, 1.0F);

                style =
                        style.withColor(
                                TextColor.fromRgb(rgb)
                        );

                rainbowCharacterIndex++;
            }

            output.add(
                    new StyledCodePoint(
                            style,
                            current.codePoint()
                    )
            );

            i++;
        }

        return sink -> {
            int outputIndex = 0;
            for (StyledCodePoint character : output) {

                if (!sink.accept(
                        outputIndex,
                        character.style(),
                        character.codePoint()
                )) {
                    return false;
                }
                outputIndex +=
                        Character.charCount(
                                character.codePoint()
                        );
            }
            return true;
        };
    }

    private static boolean containsMarker(
            FormattedCharSequence source
    ) {
        int[] matched = new int[]{0};
        boolean[] found = new boolean[]{false};

        source.accept(
                (index, style, codePoint) -> {
                    int expected =
                            MARKER_CODE_POINTS[
                                    matched[0]
                            ];
                    if (codePoint == expected) {
                        matched[0]++;
                        if (matched[0]
                                == MARKER_CODE_POINTS.length) {
                            found[0] = true;
                            return false;
                        }

                    } else {
                        matched[0] = codePoint == MARKER_CODE_POINTS[0] ? 1 : 0;
                    }
                    return true;
                }
        );
        return found[0];
    }

    private static boolean matchesMarker(
            List<StyledCodePoint> input,
            int start
    ) {
        if (start + MARKER_CODE_POINTS.length
                > input.size()) {
            return false;
        }

        for (int offset = 0;
             offset < MARKER_CODE_POINTS.length;
             offset++) {

            if (input.get(start + offset)
                    .codePoint()
                    != MARKER_CODE_POINTS[offset]) {
                return false;
            }
        }
        return true;
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

        float red;
        float green;
        float blue;

        switch (sector % 6) {

            case 0 -> {
                red = value;
                green = t;
                blue = p;
            }
            case 1 -> {
                red = q;
                green = value;
                blue = p;
            }
            case 2 -> {
                red = p;
                green = value;
                blue = t;
            }
            case 3 -> {
                red = p;
                green = q;
                blue = value;
            }
            case 4 -> {
                red = t;
                green = p;
                blue = value;
            }
            default -> {
                red = value;
                green = p;
                blue = q;
            }
        }

        int r = clampColor(Math.round(red * 255.0F));
        int g = clampColor(Math.round(green * 255.0F));
        int b = clampColor(Math.round(blue * 255.0F));

        return (r << 16) | (g << 8) | b;
    }

    private static int clampColor(int value) {
        return Math.max(0, Math.min(255, value));
    }

    private record StyledCodePoint(
            Style style,
            int codePoint
    ){
    }
}
