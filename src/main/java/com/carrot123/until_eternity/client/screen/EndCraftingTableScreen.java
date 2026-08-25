package com.carrot123.until_eternity.client.screen;

import com.carrot123.until_eternity.menu.EndCraftingTableMenu;
import com.carrot123.until_eternity.until_eternity;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public final class EndCraftingTableScreen extends AbstractContainerScreen<EndCraftingTableMenu> {
    private static final ResourceLocation TEXTURE = new ResourceLocation(
            until_eternity.MODID,
            "textures/gui/container/end_crafting_table.png");

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
        graphics.blit(
                TEXTURE,
                leftPos,
                topPos,
                0.0F,
                0.0F,
                imageWidth,
                imageHeight,
                256,
                256);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);
    }
}
