package com.carrot123.until_eternity.client.screen;

import com.carrot123.until_eternity.menu.EndCraftingTableMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public final class EndCraftingTableScreen extends AbstractContainerScreen<EndCraftingTableMenu> {
    private static final int PANEL = 0xFFC6C6C6;
    private static final int LIGHT = 0xFFFFFFFF;
    private static final int DARK = 0xFF555555;
    private static final int SLOT = 0xFF8B8B8B;

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
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        int x = leftPos;
        int y = topPos;
        graphics.fill(x, y, x + imageWidth, y + imageHeight, DARK);
        graphics.fill(x + 1, y + 1, x + imageWidth - 1, y + imageHeight - 1, LIGHT);
        graphics.fill(x + 2, y + 2, x + imageWidth - 2, y + imageHeight - 2, PANEL);
        for (int row = 0; row < 5; row++) for (int column = 0; column < 5; column++)
            drawSlot(graphics, x + 9 + column * 18, y + 17 + row * 18);
        drawSlot(graphics, x + 141, y + 53);
        for (int row = 0; row < 3; row++) for (int column = 0; column < 9; column++)
            drawSlot(graphics, x + 27 + column * 18, y + 115 + row * 18);
        for (int column = 0; column < 9; column++) drawSlot(graphics, x + 27 + column * 18, y + 173);
        graphics.fill(x + 110, y + 61, x + 132, y + 65, DARK);
        graphics.fill(x + 128, y + 57, x + 132, y + 69, DARK);
    }

    private static void drawSlot(GuiGraphics graphics, int x, int y) {
        graphics.fill(x, y, x + 18, y + 18, DARK);
        graphics.fill(x + 1, y + 1, x + 18, y + 18, LIGHT);
        graphics.fill(x + 2, y + 2, x + 17, y + 17, SLOT);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);
    }
}
