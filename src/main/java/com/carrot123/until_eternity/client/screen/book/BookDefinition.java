package com.carrot123.until_eternity.client.screen.book;

import net.minecraft.resources.ResourceLocation;

import java.util.List;

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
        List<BookPageData> pages
) {
    public BookDefinition {
        pages = List.copyOf(pages);
        if (pages.isEmpty()) {
            throw new IllegalArgumentException("A lore book needs at least one page");
        }
    }
}
