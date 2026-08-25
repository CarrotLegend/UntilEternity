package com.carrot123.until_eternity.client.screen;

import com.carrot123.until_eternity.menu.EndCraftingTableMenu;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.Util;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import org.joml.Matrix4f;

public final class EndCraftingTableScreen extends AbstractContainerScreen<EndCraftingTableMenu> {
    private static final int PANEL = 0xFFC6C6C6;
    private static final int LIGHT = 0xFFFFFFFF;
    private static final int DARK = 0xFF555555;
    private static final int SLOT = 0xFF8B8B8B;
    private static final int GLOW_BLUE = 0x3FA9FF;
    private static final int CRAFT_FLASH_BLUE = 0x80DFFF;
    private static final long BORDER_PULSE_PERIOD_MILLIS = 2_000L;
    private static final int CRAFT_FLASH_DURATION = 8;

    private int lastCraftSuccessSerial;
    private int craftFlashTicks;

    public EndCraftingTableScreen(EndCraftingTableMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 218;
        imageHeight = 204;
        inventoryLabelX = 28;
        inventoryLabelY = 105;
        titleLabelX = 10;
        titleLabelY = 6;
    }

    @Override
    protected void init() {
        super.init();
        lastCraftSuccessSerial = menu.craftSuccessSerial();
        craftFlashTicks = 0;
    }

    @Override
    protected void containerTick() {
        super.containerTick();
        int serial = menu.craftSuccessSerial();
        if (serial != lastCraftSuccessSerial) {
            lastCraftSuccessSerial = serial;
            craftFlashTicks = CRAFT_FLASH_DURATION;
        } else if (craftFlashTicks > 0) {
            craftFlashTicks--;
        }
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        int x = leftPos;
        int y = topPos;
        float pulse = borderPulse(Util.getMillis());
        drawOutline(graphics, x - 4, y - 4, x + imageWidth + 4, y + imageHeight + 4,
                colorWithAlpha(GLOW_BLUE, pulseAlpha(12, 38, pulse)));
        drawOutline(graphics, x - 3, y - 3, x + imageWidth + 3, y + imageHeight + 3,
                colorWithAlpha(GLOW_BLUE, pulseAlpha(24, 64, pulse)));
        drawOutline(graphics, x - 2, y - 2, x + imageWidth + 2, y + imageHeight + 2,
                colorWithAlpha(GLOW_BLUE, pulseAlpha(42, 100, pulse)));
        graphics.fill(x, y, x + imageWidth, y + imageHeight, DARK);
        graphics.fill(x + 1, y + 1, x + imageWidth - 1, y + imageHeight - 1, LIGHT);
        graphics.fill(x + 2, y + 2, x + imageWidth - 2, y + imageHeight - 2, PANEL);
        drawOutline(graphics, x, y, x + imageWidth, y + imageHeight,
                colorWithAlpha(GLOW_BLUE, pulseAlpha(150, 255, pulse)));
        for (int row = 0; row < 5; row++) {
            for (int column = 0; column < 5; column++) {
                int slotX = x + 9 + column * 18;
                int slotY = y + 17 + row * 18;
                drawSlot(graphics, slotX, slotY);
                drawCraftFlash(graphics, slotX, slotY);
            }
        }
        drawSlot(graphics, x + 141, y + 53);
        for (int row = 0; row < 3; row++) for (int column = 0; column < 9; column++)
            drawSlot(graphics, x + 27 + column * 18, y + 115 + row * 18);
        for (int column = 0; column < 9; column++) drawSlot(graphics, x + 27 + column * 18, y + 173);
        drawCraftingArrow(graphics, x, y);
    }

    private static void drawSlot(GuiGraphics graphics, int x, int y) {
        graphics.fill(x, y, x + 18, y + 18, DARK);
        graphics.fill(x + 1, y + 1, x + 18, y + 18, LIGHT);
        graphics.fill(x + 2, y + 2, x + 17, y + 17, SLOT);
    }

    private void drawCraftFlash(GuiGraphics graphics, int x, int y) {
        if (craftFlashTicks <= 0) return;
        float progress = craftFlashTicks / (float) CRAFT_FLASH_DURATION;
        int fillAlpha = Math.round(160.0F * progress);
        int edgeAlpha = Math.round(220.0F * progress);
        graphics.fill(x + 2, y + 2, x + 17, y + 17,
                colorWithAlpha(CRAFT_FLASH_BLUE, fillAlpha));
        drawOutline(graphics, x + 1, y + 1, x + 18, y + 18,
                colorWithAlpha(CRAFT_FLASH_BLUE, edgeAlpha));
    }

    private static float borderPulse(long timeMillis) {
        double phase = timeMillis / (double) BORDER_PULSE_PERIOD_MILLIS;
        return (float) (0.5D + 0.5D * Math.sin(phase * Math.PI * 2.0D));
    }

    private static int pulseAlpha(int minimum, int maximum, float pulse) {
        return Math.round(minimum + (maximum - minimum) * pulse);
    }

    private static int colorWithAlpha(int rgb, int alpha) {
        return alpha << 24 | rgb;
    }

    private static void drawOutline(
            GuiGraphics graphics, int left, int top, int right, int bottom, int color) {
        graphics.fill(left, top, right, top + 1, color);
        graphics.fill(left, bottom - 1, right, bottom, color);
        graphics.fill(left, top + 1, left + 1, bottom - 1, color);
        graphics.fill(right - 1, top + 1, right, bottom - 1, color);
    }

    private static void drawCraftingArrow(GuiGraphics graphics, int x, int y) {
        graphics.flush();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);

        Matrix4f pose = graphics.pose().last().pose();
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder vertices = tesselator.getBuilder();
        vertices.begin(VertexFormat.Mode.TRIANGLES, DefaultVertexFormat.POSITION_COLOR);

        int body = 0xFF555555;
        triangle(vertices, pose, x + 109, y + 61, x + 128, y + 61, x + 128, y + 65, body);
        triangle(vertices, pose, x + 109, y + 61, x + 128, y + 65, x + 109, y + 65, body);
        triangle(vertices, pose, x + 126, y + 55, x + 135, y + 63, x + 126, y + 71, body);

        int highlight = 0xFFC6C6C6;
        triangle(vertices, pose, x + 110, y + 61, x + 127, y + 61, x + 127, y + 62, highlight);
        triangle(vertices, pose, x + 110, y + 61, x + 127, y + 62, x + 110, y + 62, highlight);
        triangle(vertices, pose, x + 126, y + 56, x + 133, y + 63, x + 126, y + 61, highlight);

        tesselator.end();
        RenderSystem.disableBlend();
    }

    private static void triangle(
            BufferBuilder vertices,
            Matrix4f pose,
            float x0,
            float y0,
            float x1,
            float y1,
            float x2,
            float y2,
            int color) {
        vertex(vertices, pose, x0, y0, color);
        vertex(vertices, pose, x1, y1, color);
        vertex(vertices, pose, x2, y2, color);
    }

    private static void vertex(BufferBuilder vertices, Matrix4f pose, float x, float y, int color) {
        vertices.vertex(pose, x, y, 0.0F)
                .color(color >> 16 & 0xFF, color >> 8 & 0xFF, color & 0xFF, color >>> 24)
                .endVertex();
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);
    }
}
