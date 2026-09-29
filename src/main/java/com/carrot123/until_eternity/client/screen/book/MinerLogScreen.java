package com.carrot123.until_eternity.client.screen.book;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;

import java.util.ArrayList;
import java.util.List;

public final class MinerLogScreen extends Screen {
    private static final BookDefinition BOOK = LoreBookDefinitions.MINER_LOG;
    private static final int COLUMN_GAP = 16;
    private static final int LEFT_X = BOOK.contentLeft();
    private static final int COLUMN_WIDTH = (BOOK.contentWidth() - COLUMN_GAP) / 2;
    private static final int RIGHT_X = LEFT_X + COLUMN_WIDTH + COLUMN_GAP;
    private static final int LEFT_BODY_Y = BOOK.contentTop();
    private static final int RIGHT_BODY_Y = BOOK.contentTop();
    private static final int CONTENT_BOTTOM = BOOK.contentBottom();

    private final List<View> views = new ArrayList<>();
    private int viewIndex;
    private int leftPos;
    private int topPos;
    private ImageButton previousButton;
    private ImageButton nextButton;

    public MinerLogScreen() {
        super(Component.translatable(BOOK.titleKey()));
    }

    @Override
    protected void init() {
        leftPos = (width - BOOK.width()) / 2;
        topPos = (height - BOOK.height()) / 2;
        buildViews();
        viewIndex = Math.min(viewIndex, views.size() - 1);
        previousButton = addRenderableWidget(new ImageButton(
                leftPos + 23, topPos + 168, 16, 16,
                16, 64, 0, BOOK.buttons(), 128, 96,
                button -> turn(-1),
                Component.translatable("gui.until_eternity.lore_book.previous")
        ));
        nextButton = addRenderableWidget(new ImageButton(
                leftPos + 217, topPos + 168, 16, 16,
                96, 64, 0, BOOK.buttons(), 128, 96,
                button -> turn(1),
                Component.translatable("gui.until_eternity.lore_book.next")
        ));
        updateButtons();
    }

    private void buildViews() {
        views.clear();
        int lineHeight = font.lineHeight;
        int leftCapacity = (CONTENT_BOTTOM - LEFT_BODY_Y) / lineHeight;
        int rightCapacity = (CONTENT_BOTTOM - RIGHT_BODY_Y) / lineHeight;
        int viewCapacity = leftCapacity + rightCapacity;
        for (BookPageData page : BOOK.pages()) {
            List<FormattedCharSequence> lines = new ArrayList<>();
            for (String key : page.paragraphKeys()) {
                if (!lines.isEmpty()) {
                    lines.add(Component.literal(" ").getVisualOrderText());
                }
                Component paragraph = Component.translatable(key);
                if (key.contains(".emphasis.")) {
                    paragraph = paragraph.copy().withStyle(style -> style.withBold(true));
                }
                lines.addAll(font.split(paragraph, COLUMN_WIDTH));
            }
            int offset = 0;
            boolean continued = false;
            do {
                int end = Math.min(lines.size(), offset + viewCapacity);
                views.add(new View(
                        page.titleKey() + (continued ? ".continued" : ""),
                        List.copyOf(lines.subList(offset, end))
                ));
                offset = end;
                continued = true;
            } while (offset < lines.size());
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        graphics.pose().pushPose();
        graphics.pose().translate(leftPos, topPos, 0);
        graphics.pose().scale(2, 2, 1);
        graphics.blit(BOOK.background(), 0, 0, 0, 0, 128, 96, 128, 96);
        graphics.pose().popPose();

        View view = views.get(viewIndex);
        graphics.drawCenteredString(
                font, Component.translatable(view.titleKey()),
                leftPos + 128,
                topPos + 18, BOOK.titleColor()
        );
        int leftCapacity = (CONTENT_BOTTOM - LEFT_BODY_Y) / font.lineHeight;
        for (int index = 0; index < view.lines().size(); index++) {
            boolean right = index >= leftCapacity;
            int columnLine = right ? index - leftCapacity : index;
            int x = leftPos + (right ? RIGHT_X : LEFT_X);
            int y = topPos + (right ? RIGHT_BODY_Y : LEFT_BODY_Y)
                    + columnLine * font.lineHeight;
            graphics.drawString(font, view.lines().get(index), x, y, BOOK.textColor(), false);
        }
        graphics.drawCenteredString(
                font,
                Component.translatable("gui.until_eternity.lore_book.page", viewIndex + 1, views.size()),
                leftPos + 128, topPos + 174, BOOK.pageColor()
        );
        super.render(graphics, mouseX, mouseY, partialTick);
        if (previousButton.isHoveredOrFocused() && previousButton.visible) {
            graphics.fill(leftPos + 23, topPos + 168, leftPos + 39, topPos + 184, 0x44FFFFFF);
        }
        if (nextButton.isHoveredOrFocused() && nextButton.visible) {
            graphics.fill(leftPos + 217, topPos + 168, leftPos + 233, topPos + 184, 0x44FFFFFF);
        }
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == InputConstants.KEY_LEFT) {
            turn(-1);
            return true;
        }
        if (keyCode == InputConstants.KEY_RIGHT) {
            turn(1);
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private void turn(int direction) {
        int next = viewIndex + direction;
        if (next >= 0 && next < views.size()) {
            viewIndex = next;
            updateButtons();
        }
    }

    private void updateButtons() {
        previousButton.visible = viewIndex > 0;
        nextButton.visible = viewIndex < views.size() - 1;
    }

    private record View(String titleKey, List<FormattedCharSequence> lines) {
    }
}
