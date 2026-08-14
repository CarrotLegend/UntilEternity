package com.carrot123.until_eternity.mixin.client;

import com.carrot123.until_eternity.client.tooltip.HorrorHuntNameRenderer;
import com.carrot123.until_eternity.item.ModItems;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Gui.class)
public abstract class HorrorHuntSelectedItemNameMixin {
    @Shadow
    protected Minecraft minecraft;

    @Shadow
    protected int toolHighlightTimer;

    @Shadow
    protected ItemStack lastToolHighlight;

    @Shadow
    protected int screenWidth;

    @Shadow
    protected int screenHeight;

    @Inject(
            method = "renderSelectedItemName"
                    + "(Lnet/minecraft/client/gui/GuiGraphics;I)V",
            at = @At("HEAD"),
            cancellable = true,
            require = 1,
            remap = false
    )
    private void untilEternity$renderHorrorHuntSelectedItemName(
            GuiGraphics graphics,
            int yShift,
            CallbackInfo callback
    ) {
        if (!lastToolHighlight.is(ModItems.HORROR_HUNT.get())) {
            return;
        }

        minecraft.getProfiler().push("selectedItemName");
        try {
            if (toolHighlightTimer > 0 && !lastToolHighlight.isEmpty()) {
                renderHorrorHuntName(graphics, yShift);
            }
        } finally {
            minecraft.getProfiler().pop();
        }
        callback.cancel();
    }

    private void renderHorrorHuntName(
            GuiGraphics graphics,
            int yShift
    ) {
        MutableComponent vanillaName = Component.empty()
                .append(lastToolHighlight.getHoverName())
                .withStyle(lastToolHighlight.getRarity().getStyleModifier());
        if (lastToolHighlight.hasCustomHoverName()) {
            vanillaName.withStyle(ChatFormatting.ITALIC);
        }
        Component highlightTip = lastToolHighlight.getHighlightTip(vanillaName);

        Font font = IClientItemExtensions.of(lastToolHighlight).getFont(
                lastToolHighlight,
                IClientItemExtensions.FontContext.SELECTED_ITEM_NAME);
        if (font == null) {
            font = ((Gui) (Object) this).getFont();
        }

        FormattedCharSequence text =
                HorrorHuntNameRenderer.visualText(highlightTip);
        int textWidth = font.width(text);
        int x = (screenWidth - textWidth) / 2;
        int y = screenHeight - Math.max(yShift, 59);
        if (!minecraft.gameMode.canHurtPlayer()) {
            y += 14;
        }

        int fadeAlpha = (int) (toolHighlightTimer * 256.0F / 10.0F);
        fadeAlpha = Math.min(fadeAlpha, 255);
        if (fadeAlpha <= 0) {
            return;
        }

        graphics.fill(
                x - 2,
                y - 2,
                x + textWidth + 2,
                y + 11,
                minecraft.options.getBackgroundColor(0));
        HorrorHuntNameRenderer.render(
                font,
                text,
                x,
                y,
                graphics.pose().last().pose(),
                graphics.bufferSource(),
                fadeAlpha);
    }
}
