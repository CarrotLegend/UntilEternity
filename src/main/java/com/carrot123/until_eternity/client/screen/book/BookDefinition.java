package com.carrot123.until_eternity.client.screen.book;

import java.util.List;

import net.minecraft.resources.ResourceLocation;

public record BookDefinition(
        String titleKey,
        ResourceLocation background,
        ResourceLocation buttons,
        int width,
        int height,
        int contentLeft,
        int contentTop,
        int contentWidth,
        int contentBottom,
        int titleColor,
        int textColor,
        int pageColor,
        boolean showPageNumber,

        boolean twoPageSpread,
        int pageGap,

        List<BookPageData> pages
) {
    public BookDefinition {
        pages = List.copyOf(pages);

        if (pages.isEmpty()) {
            throw new IllegalArgumentException(
                    "A lore book needs at least one page"
            );
        }

        if (pageGap < 0) {
            throw new IllegalArgumentException(
                    "Page gap cannot be negative"
            );
        }
    }
}