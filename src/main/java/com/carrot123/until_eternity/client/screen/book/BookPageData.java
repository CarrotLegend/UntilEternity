package com.carrot123.until_eternity.client.screen.book;

import net.minecraft.resources.ResourceLocation;

import java.util.List;

public record BookPageData(
        String titleKey,
        List<String> paragraphKeys,
        ResourceLocation illustration
) {
    public BookPageData {
        paragraphKeys = List.copyOf(paragraphKeys);
    }

    public static BookPageData text(String titleKey, String... paragraphKeys) {
        return new BookPageData(titleKey, List.of(paragraphKeys), null);
    }

    public static BookPageData illustrated(
            String titleKey,
            ResourceLocation illustration,
            String... paragraphKeys
    ) {
        return new BookPageData(titleKey, List.of(paragraphKeys), illustration);
    }
}
