package com.carrot123.until_eternity.client.trial;

import com.carrot123.until_eternity.network.ModNetworking;
import com.carrot123.until_eternity.network.SelectTrialDifficultyC2SPacket;
import com.carrot123.until_eternity.trial.TrialDifficulty;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;

import java.util.ArrayList;
import java.util.List;

public final class TrialDialogueScreen extends Screen {
    private final BlockPos pos;
    private final List<Option> options = new ArrayList<>();
    private List<FormattedCharSequence> introduction = List.of();
    private int left, top, panelWidth, panelHeight, contentTop, optionsTop, optionsBottom, scroll, maxScroll;
    private boolean submitted;

    public TrialDialogueScreen(BlockPos pos) {
        super(Component.translatable("gui.until_eternity.trial.dialogue_title"));
        this.pos = pos;
    }

    @Override protected void init() {
        panelWidth = Math.max(120, Math.min(392, width - 24));
        panelHeight = Math.max(90, Math.min(260, height - 24));
        left = (width - panelWidth) / 2;
        top = (height - panelHeight) / 2;
        int textWidth = panelWidth - 32;
        List<FormattedCharSequence> lines = new ArrayList<>(font.split(
                Component.translatable("gui.until_eternity.trial.dialogue_intro_1"), textWidth));
        lines.add(Component.empty().getVisualOrderText());
        lines.addAll(font.split(Component.translatable("gui.until_eternity.trial.dialogue_intro_2"), textWidth));
        introduction = List.copyOf(lines);
        options.clear();
        for (TrialDifficulty difficulty : TrialDifficulty.values()) {
            String name = difficulty.name().toLowerCase(java.util.Locale.ROOT);
            List<FormattedCharSequence> description = font.split(
                    Component.translatable("gui.until_eternity.trial.difficulty." + name + ".description"), textWidth - 12);
            options.add(new Option(difficulty, Component.translatable("gui.until_eternity.trial.difficulty." + name),
                    description, 17 + description.size() * font.lineHeight));
        }
        options.add(new Option(null, Component.translatable("gui.until_eternity.trial.leave"), List.of(), 21));
        contentTop = top + 27;
        optionsTop = contentTop + introduction.size() * font.lineHeight + 10;
        optionsBottom = top + panelHeight - 13;
        int contentHeight = options.stream().mapToInt(Option::height).sum();
        maxScroll = Math.max(0, optionsTop + contentHeight - optionsBottom);
        scroll = Math.min(scroll, maxScroll);
    }

    @Override public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        graphics.fill(left, top, left + panelWidth, top + panelHeight, 0xED141126);
        graphics.fill(left + 3, top + 3, left + panelWidth - 3, top + 4, 0xFF8B6AB7);
        graphics.drawCenteredString(font, title, width / 2, top + 10, 0xFFE4CBFF);
        graphics.enableScissor(left + 8, contentTop, left + panelWidth - 8, optionsBottom);
        int bodyY = contentTop - scroll;
        for (FormattedCharSequence line : introduction) {
            graphics.drawString(font, line, left + 16, bodyY, 0xFFD9D0E8, false);
            bodyY += font.lineHeight;
        }
        int y = optionsTop - scroll;
        for (Option option : options) {
            boolean hovered = mouseX >= left + 12 && mouseX < left + panelWidth - 12
                    && mouseY >= Math.max(y, contentTop) && mouseY < Math.min(y + option.height, optionsBottom);
            if (y + option.height > contentTop && y < optionsBottom) {
                graphics.fill(left + 12, y, left + panelWidth - 12, y + option.height - 2,
                        hovered ? 0xA074538F : 0x80332745);
                graphics.drawString(font, option.label, left + 19, y + 4, hovered ? 0xFFFFE7B7 : 0xFFF1E4F8, false);
                int descriptionY = y + 15;
                for (FormattedCharSequence line : option.description) {
                    graphics.drawString(font, line, left + 23, descriptionY, 0xFFCCC0D8, false);
                    descriptionY += font.lineHeight;
                }
            }
            y += option.height;
        }
        graphics.disableScissor();
        super.render(graphics, mouseX, mouseY, partialTick);
    }

    @Override public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0 && !submitted && mouseX >= left + 12 && mouseX < left + panelWidth - 12
                && mouseY >= contentTop && mouseY < optionsBottom) {
            int y = optionsTop - scroll;
            for (Option option : options) {
                if (mouseY >= y && mouseY < y + option.height - 2) {
                    if (option.difficulty != null) {
                        submitted = true;
                        ModNetworking.CHANNEL.sendToServer(new SelectTrialDifficultyC2SPacket(pos, option.difficulty.ordinal()));
                    }
                    onClose();
                    return true;
                }
                y += option.height;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        if (maxScroll > 0) {
            scroll = Math.max(0, Math.min(maxScroll, scroll - (int)Math.round(delta * 22)));
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, delta);
    }

    @Override public boolean isPauseScreen() { return false; }
    private record Option(TrialDifficulty difficulty, Component label, List<FormattedCharSequence> description, int height) {}
}
