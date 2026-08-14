package com.carrot123.until_eternity.client.tooltip;

import net.minecraft.network.chat.FormattedText;
import net.minecraft.world.inventory.tooltip.TooltipComponent;

public record HorrorHuntTooltipComponent(
        FormattedText text
) implements TooltipComponent {
}
