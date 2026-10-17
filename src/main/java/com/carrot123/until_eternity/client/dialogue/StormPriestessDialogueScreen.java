package com.carrot123.until_eternity.client.dialogue;

import com.carrot123.until_eternity.network.ModNetworking;
import com.carrot123.until_eternity.network.SelectStormPriestessExchangeC2SPacket;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;

import java.util.List;

public class StormPriestessDialogueScreen extends Screen {
    private final int priestessId;
    private boolean insufficient;
    private boolean submitted;
    private List<FormattedCharSequence> dialogueLines = List.of();

    public StormPriestessDialogueScreen(int priestessId) {
        super(Component.translatable("entity.until_eternity.storm_priestess"));
        this.priestessId = priestessId;
    }

    public int getPriestessId() {
        return priestessId;
    }

    public void showInsufficient() {
        insufficient = true;
        submitted = false;
        refreshDialogue();
    }

    @Override
    protected void init() {
        refreshDialogue();
    }

    private void refreshDialogue() {
        clearWidgets();
        Component sentence = Component.literal("[")
                .append(title.copy().withStyle(ChatFormatting.YELLOW))
                .append("]: ")
                .append(Component.translatable(insufficient
                        ? "gui.until_eternity.priestess.insufficient"
                        : "gui.until_eternity.priestess.greeting"));
        dialogueLines = font.split(sentence, Math.min(300, Math.max(70, width - 20)));
        int buttonY = height / 2 + dialogueLines.size() * 12 + 12;

        if (!insufficient) {
            addChoice(Component.translatable("gui.until_eternity.priestess.exchange_altar"), buttonY,
                    button -> exchange(SelectStormPriestessExchangeC2SPacket.ALTAR));
            addChoice(Component.translatable("gui.until_eternity.priestess.exit"), buttonY + 12,
                    button -> onClose());
        } else {
            addChoice(Component.translatable("gui.until_eternity.priestess.exit"), buttonY,
                    button -> onClose());
        }
    }

    private void addChoice(Component message, int y, Button.OnPress onPress) {
        ChoiceButton button = new ChoiceButton(Component.literal("[").append(message).append("]"), onPress);
        button.setX(width / 2 - button.getWidth() / 2);
        button.setY(y);
        this.addRenderableWidget(button);
    }

    private void exchange(byte selection) {
        if (submitted) {
            return;
        }
        submitted = true;
        ModNetworking.CHANNEL.sendToServer(new SelectStormPriestessExchangeC2SPacket(priestessId, selection));
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        int y = height / 2;
        for (FormattedCharSequence line : dialogueLines) {
            int lineWidth = font.width(line) + 2;
            int x = width / 2 - lineWidth / 2;
            graphics.fillGradient(x, y, x + lineWidth, y + 12, 0x66000000, 0x66000000);
            graphics.drawString(font, line, x + 1, y + 1, 0xFFFFFF, false);
            y += 12;
        }
        super.render(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    public void renderBackground(GuiGraphics graphics) {
        // 对齐武神女王风格：不渲染整个屏幕的暗色遮罩。
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private static class ChoiceButton extends Button {
        private ChoiceButton(Component message, OnPress onPress) {
            super(Button.builder(message, onPress).pos(0, 0).size(0, 12).createNarration(DEFAULT_NARRATION));
            this.width = Minecraft.getInstance().font.width(message) + 2;
        }

        @Override
        public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
            graphics.fillGradient(getX(), getY(), getX() + width, getY() + height,
                    0x66000000, 0x66000000);
            graphics.drawString(Minecraft.getInstance().font, getMessage(),
                    getX() + 1, getY() + 1, isHovered() ? 0xFFFF55 : 0xFFFFFF, false);
        }
    }
}
