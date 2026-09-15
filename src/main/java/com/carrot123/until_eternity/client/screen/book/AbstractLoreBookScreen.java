package com.carrot123.until_eternity.client.screen.book;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;

import java.util.List;

abstract class AbstractLoreBookScreen extends Screen {
    private static final int BUTTON_SIZE = 16;
    private static final int ILLUSTRATION_WIDTH = 64;
    private static final int ILLUSTRATION_HEIGHT = 48;

    private final BookDefinition definition;
    private int pageIndex;
    private int leftPos;
    private int topPos;
    private ImageButton previousButton;
    private ImageButton nextButton;

    protected AbstractLoreBookScreen(BookDefinition definition) {
        super(Component.translatable(definition.titleKey()));
        this.definition = definition;
    }

    @Override
    protected void init() {
        leftPos = (width - definition.width()) / 2;
        topPos = (height - definition.height()) / 2;
        if (definition.pages().size() > 1) {
            previousButton = addRenderableWidget(new ImageButton(
                    leftPos + 23, topPos + 158, BUTTON_SIZE, BUTTON_SIZE,
                    0, 0, BUTTON_SIZE, definition.buttons(), 32, 32,
                    button -> turnPage(-1),
                    Component.translatable("gui.until_eternity.lore_book.previous")));
            nextButton = addRenderableWidget(new ImageButton(
                    leftPos + definition.width() - 39, topPos + 158,
                    BUTTON_SIZE, BUTTON_SIZE, 16, 0, BUTTON_SIZE,
                    definition.buttons(), 32, 32,
                    button -> turnPage(1),
                    Component.translatable("gui.until_eternity.lore_book.next")));
            updateButtons();
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        graphics.blit(definition.background(), leftPos, topPos, 0, 0,
                definition.width(), definition.height(), definition.width(), definition.height());
        renderPage(graphics, definition.pages().get(pageIndex));
        super.render(graphics, mouseX, mouseY, partialTick);
    }

    private void renderPage(GuiGraphics graphics, BookPageData page) {
        int contentX = leftPos + definition.contentLeft();
        int titleY = topPos + definition.contentTop();
        graphics.drawCenteredString(font, Component.translatable(page.titleKey()),
                contentX + definition.contentWidth() / 2, titleY, definition.titleColor());

        int textY = titleY + 17;
        if (page.illustration() != null) {
            int imageX = contentX + (definition.contentWidth() - ILLUSTRATION_WIDTH) / 2;
            graphics.blit(page.illustration(), imageX, textY, 0, 0,
                    ILLUSTRATION_WIDTH, ILLUSTRATION_HEIGHT,
                    ILLUSTRATION_WIDTH, ILLUSTRATION_HEIGHT);
            textY += ILLUSTRATION_HEIGHT + 5;
        }

        int bottom = topPos + definition.contentBottom();
        for (String paragraphKey : page.paragraphKeys()) {
            List<FormattedCharSequence> lines = font.split(
                    Component.translatable(paragraphKey), definition.contentWidth());
            for (FormattedCharSequence line : lines) {
                if (textY + font.lineHeight > bottom) {
                    return;
                }
                graphics.drawString(font, line, contentX, textY, definition.textColor(), false);
                textY += font.lineHeight;
            }
            textY += 4;
        }

        if (definition.showPageNumber()) {
            Component pageNumber = Component.translatable(
                    "gui.until_eternity.lore_book.page", pageIndex + 1, definition.pages().size());
            graphics.drawCenteredString(font, pageNumber,
                    leftPos + definition.width() / 2, topPos + 174, definition.pageColor());
        }
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (definition.pages().size() > 1 && keyCode == InputConstants.KEY_LEFT) {
            turnPage(-1);
            return true;
        }
        if (definition.pages().size() > 1 && keyCode == InputConstants.KEY_RIGHT) {
            turnPage(1);
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private void turnPage(int delta) {
        int target = pageIndex + delta;
        if (target >= 0 && target < definition.pages().size()) {
            pageIndex = target;
            updateButtons();
        }
    }

    private void updateButtons() {
        if (previousButton != null) {
            previousButton.visible = pageIndex > 0;
        }
        if (nextButton != null) {
            nextButton.visible = pageIndex + 1 < definition.pages().size();
        }
    }
}
