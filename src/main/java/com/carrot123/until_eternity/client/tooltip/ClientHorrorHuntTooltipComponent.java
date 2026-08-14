package com.carrot123.until_eternity.client.tooltip;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.util.FormattedCharSequence;
import org.joml.Matrix4f;

public final class ClientHorrorHuntTooltipComponent
        implements ClientTooltipComponent {
    private final FormattedCharSequence text;

    public ClientHorrorHuntTooltipComponent(
            HorrorHuntTooltipComponent component
    ) {
        this.text = HorrorHuntNameRenderer.visualText(component.text());
    }

    @Override
    public int getHeight() {
        return HorrorHuntNameStyle.COMPONENT_HEIGHT;
    }

    @Override
    public int getWidth(Font font) {
        return reservedWidth(font);
    }

    @Override
    public void renderText(
            Font font,
            int x,
            int y,
            Matrix4f matrix,
            MultiBufferSource.BufferSource bufferSource
    ) {
        int textWidth = font.width(text);
        int componentWidth = HorrorHuntNameRenderer.reservedWidth(font, text);
        float textX = x + (componentWidth - textWidth) / 2.0F;
        float textY = y + (HorrorHuntNameStyle.COMPONENT_HEIGHT
                - HorrorHuntNameStyle.TEXT_HEIGHT) / 2.0F;
        HorrorHuntNameRenderer.render(
                font,
                text,
                textX,
                textY,
                matrix,
                bufferSource,
                255);
    }

    private int reservedWidth(Font font) {
        return HorrorHuntNameRenderer.reservedWidth(font, text);
    }
}
