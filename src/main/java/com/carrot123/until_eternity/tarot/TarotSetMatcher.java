package com.carrot123.until_eternity.tarot;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import net.minecraft.resources.ResourceLocation;

public final class TarotSetMatcher {
    private TarotSetMatcher() {
    }

    public static Set<ResourceLocation> match(TarotDeckSnapshot snapshot,
            Collection<TarotSetDefinition> definitions) {
        Set<ResourceLocation> matched = new HashSet<>();
        for (TarotSetDefinition definition : definitions) {
            boolean complete = definition.requiredCards().entrySet().stream()
                    .allMatch(entry -> entry.getValue().accepts(snapshot.cards().get(entry.getKey())));
            if (complete) {
                matched.add(definition.id());
            }
        }
        return Set.copyOf(matched);
    }
}
