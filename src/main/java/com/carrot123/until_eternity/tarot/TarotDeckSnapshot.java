package com.carrot123.until_eternity.tarot;

import java.util.Map;
import net.minecraft.resources.ResourceLocation;

public record TarotDeckSnapshot(Map<ResourceLocation, TarotOrientation> cards) {
    public TarotDeckSnapshot {
        cards = Map.copyOf(cards);
    }
}
