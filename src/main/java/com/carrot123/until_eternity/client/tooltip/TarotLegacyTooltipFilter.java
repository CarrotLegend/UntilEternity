package com.carrot123.until_eternity.client.tooltip;

import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;

public final class TarotLegacyTooltipFilter {
    private TarotLegacyTooltipFilter() {
    }

    public static void removeOriginalDescription(List<Component> lines, String descriptionId) {
        String prefix = descriptionId + ".desc";
        lines.removeIf(line -> line.getContents() instanceof TranslatableContents text
                && text.getKey().startsWith(prefix));
    }
}
