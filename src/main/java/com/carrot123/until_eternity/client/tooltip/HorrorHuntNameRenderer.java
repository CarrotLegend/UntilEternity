package com.carrot123.until_eternity.client.tooltip;

import net.minecraft.Util;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.TextColor;
import net.minecraft.util.FormattedCharSequence;
import org.joml.Matrix4f;

public final class HorrorHuntNameRenderer {
    private static final int FULL_BRIGHT = 0x00F000F0;

    private HorrorHuntNameRenderer() {
    }

    public static FormattedCharSequence visualText(FormattedText text) {
        return stripColor(Language.getInstance().getVisualOrder(text));
    }

    public static int reservedWidth(
            Font font,
            FormattedCharSequence text
    ) {
        return (int) Math.ceil(
                font.width(text) * HorrorHuntNameStyle.MAX_SCALE) + 4;
    }

    public static void render(
            Font font,
            FormattedCharSequence text,
            float x,
            float y,
            Matrix4f matrix,
            MultiBufferSource.BufferSource bufferSource,
            int fadeAlpha
    ) {
        int textWidth = font.width(text);
        float centerX = x + textWidth / 2.0F;
        float centerY = y + HorrorHuntNameStyle.TEXT_HEIGHT / 2.0F;

        float scale = twitchScale(
                Util.getMillis() / HorrorHuntNameStyle.FRAME_MILLIS);
        Matrix4f ghostMatrix = new Matrix4f(matrix)
                .translate(centerX, centerY, 0.0F)
                .scale(scale, scale, 1.0F)
                .translate(-centerX, -centerY, 0.0F);
        renderGhostOutlineOnly(
                font,
                text,
                x,
                y,
                HorrorHuntNameStyle.applyFade(
                        HorrorHuntNameStyle.GHOST_OUTLINE_COLOR,
                        fadeAlpha),
                ghostMatrix,
                bufferSource);

        renderMainOutlinedText(
                font,
                text,
                x,
                y,
                HorrorHuntNameStyle.applyFade(
                        HorrorHuntNameStyle.MAIN_COLOR,
                        fadeAlpha),
                HorrorHuntNameStyle.applyFade(
                        HorrorHuntNameStyle.OUTLINE_COLOR,
                        fadeAlpha),
                matrix,
                bufferSource);
    }

    static float twitchScale(long frame) {
        long hash = frame;
        hash ^= hash >>> 33;
        hash *= 0xff51afd7ed558ccdl;
        hash ^= hash >>> 33;
        hash *= 0xc4ceb9fe1a85ec53l;
        hash ^= hash >>> 33;
        float unit = (hash & 0xFFFFL) / 65535.0F;
        return HorrorHuntNameStyle.MIN_SCALE
                + unit * (HorrorHuntNameStyle.MAX_SCALE
                - HorrorHuntNameStyle.MIN_SCALE);
    }

    static FormattedCharSequence stripColor(
            FormattedCharSequence sequence
    ) {
        return sink -> sequence.accept((index, style, codePoint) ->
                sink.accept(
                        index,
                        style.withColor((TextColor) null),
                        codePoint));
    }

    private static void renderGhostOutlineOnly(
            Font font,
            FormattedCharSequence text,
            float x,
            float y,
            int color,
            Matrix4f matrix,
            MultiBufferSource.BufferSource bufferSource
    ) {
        for (int[] offset : HorrorHuntNameStyle.OUTLINE_OFFSETS) {
            drawGhostGlyph(
                    font,
                    text,
                    x + offset[0],
                    y + offset[1],
                    color,
                    matrix,
                    bufferSource);
        }
    }

    private static void renderMainOutlinedText(
            Font font,
            FormattedCharSequence text,
            float x,
            float y,
            int mainColor,
            int outlineColor,
            Matrix4f matrix,
            MultiBufferSource.BufferSource bufferSource
    ) {
        font.drawInBatch8xOutline(
                text,
                x,
                y,
                mainColor,
                outlineColor,
                matrix,
                bufferSource,
                FULL_BRIGHT);
    }

    private static void drawGhostGlyph(
            Font font,
            FormattedCharSequence text,
            float x,
            float y,
            int color,
            Matrix4f matrix,
            MultiBufferSource.BufferSource bufferSource
    ) {
        font.drawInBatch(
                text,
                x,
                y,
                color,
                false,
                matrix,
                bufferSource,
                Font.DisplayMode.NORMAL,
                0,
                FULL_BRIGHT
        );
    }
}
