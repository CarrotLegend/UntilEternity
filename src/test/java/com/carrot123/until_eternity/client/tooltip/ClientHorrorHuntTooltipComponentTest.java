package com.carrot123.until_eternity.client.tooltip;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ClientHorrorHuntTooltipComponentTest {
    @Test
    void stripsRarityColorButPreservesTextAndNonColorStyle() {
        ResourceLocation customFont = new ResourceLocation(
                "until_eternity", "horror_hunt_test");
        Style epicCustomName = Style.EMPTY
                .withColor(ChatFormatting.LIGHT_PURPLE)
                .withItalic(true)
                .withBold(true)
                .withFont(customFont);
        FormattedCharSequence original = FormattedCharSequence.forward(
                "血月追猎", epicCustomName);

        List<Integer> codePoints = new ArrayList<>();
        List<Style> styles = new ArrayList<>();
        boolean completed = HorrorHuntNameRenderer
                .stripColor(original)
                .accept((index, style, codePoint) -> {
                    codePoints.add(codePoint);
                    styles.add(style);
                    return true;
                });

        assertTrue(completed);
        assertEquals("血月追猎", codePoints.stream()
                .collect(StringBuilder::new,
                        StringBuilder::appendCodePoint,
                        StringBuilder::append)
                .toString());
        assertEquals(4, styles.size());
        for (Style style : styles) {
            assertNull(style.getColor());
            assertTrue(style.isItalic());
            assertTrue(style.isBold());
            assertEquals(customFont, style.getFont());
        }
    }

    @Test
    void sharedColorsKeepGhostDarkerAndPartiallyTransparent() {
        assertEquals(0xFF820016, HorrorHuntNameStyle.MAIN_COLOR);
        assertEquals(0xFF000000, HorrorHuntNameStyle.OUTLINE_COLOR);
        assertEquals(0xC0430008,
                HorrorHuntNameStyle.GHOST_OUTLINE_COLOR);
        assertEquals(0xC0,
                HorrorHuntNameStyle.GHOST_OUTLINE_COLOR >>> 24);

        int mainRgb = HorrorHuntNameStyle.MAIN_COLOR & 0x00FFFFFF;
        int ghostRgb = HorrorHuntNameStyle.GHOST_OUTLINE_COLOR
                & 0x00FFFFFF;
        assertTrue(red(ghostRgb) < red(mainRgb));
        assertTrue(relativeLuminance(ghostRgb)
                < relativeLuminance(mainRgb));
    }

    @Test
    void outlineIsHollowAndTwitchRangeIsUnchanged() {
        assertEquals(8, HorrorHuntNameStyle.OUTLINE_OFFSETS.length);
        for (int[] offset : HorrorHuntNameStyle.OUTLINE_OFFSETS) {
            assertFalse(offset[0] == 0 && offset[1] == 0);
        }
        assertEquals(35, HorrorHuntNameStyle.FRAME_MILLIS);
        assertEquals(1.12F, HorrorHuntNameStyle.MIN_SCALE);
        assertEquals(1.26F, HorrorHuntNameStyle.MAX_SCALE);
        for (long frame = 0; frame < 512; frame++) {
            float scale = HorrorHuntNameRenderer.twitchScale(frame);
            assertTrue(scale >= HorrorHuntNameStyle.MIN_SCALE);
            assertTrue(scale <= HorrorHuntNameStyle.MAX_SCALE);
        }
    }

    @Test
    void hudFadeMultipliesSharedBaseAlpha() {
        assertEquals(0x80820016,
                HorrorHuntNameStyle.applyFade(
                        HorrorHuntNameStyle.MAIN_COLOR, 128));
        assertEquals(0x80000000,
                HorrorHuntNameStyle.applyFade(
                        HorrorHuntNameStyle.OUTLINE_COLOR, 128));
        assertEquals(HorrorHuntNameStyle.GHOST_OUTLINE_COLOR,
                HorrorHuntNameStyle.applyFade(
                        HorrorHuntNameStyle.GHOST_OUTLINE_COLOR, 255));
        assertEquals(0x60430008,
                HorrorHuntNameStyle.applyFade(
                        HorrorHuntNameStyle.GHOST_OUTLINE_COLOR, 128));
        assertEquals(0x00430008,
                HorrorHuntNameStyle.applyFade(
                        HorrorHuntNameStyle.GHOST_OUTLINE_COLOR, 0));
    }

    private static int red(int rgb) {
        return rgb >>> 16 & 0xFF;
    }

    private static double relativeLuminance(int rgb) {
        return 0.2126D * red(rgb)
                + 0.7152D * (rgb >>> 8 & 0xFF)
                + 0.0722D * (rgb & 0xFF);
    }
}
