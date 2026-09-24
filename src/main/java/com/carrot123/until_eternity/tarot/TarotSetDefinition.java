package com.carrot123.until_eternity.tarot;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import net.minecraft.resources.ResourceLocation;

public record TarotSetDefinition(
        ResourceLocation id,
        Map<ResourceLocation, TarotCardRequirement> requiredCards,
        int descriptionLineCount
) {
    public TarotSetDefinition {
        Objects.requireNonNull(id);
        Objects.requireNonNull(requiredCards);

        if (requiredCards.isEmpty()
                || requiredCards.entrySet().stream().anyMatch(entry ->
                entry.getKey() == null || entry.getValue() == null)) {
            throw new IllegalArgumentException(
                    "A tarot set must require at least one card and valid orientations"
            );
        }

        requiredCards = Map.copyOf(requiredCards);

        if (descriptionLineCount < 1) {
            throw new IllegalArgumentException(
                    "A tarot set must have at least one description line"
            );
        }
    }

    public String getNameTranslationKey() {
        return "tarot_set." + id.getNamespace() + "." + id.getPath();
    }

    public List<String> getDescriptionTranslationKeys() {
        List<String> keys = new ArrayList<>(descriptionLineCount);
        for (int index = 0; index < descriptionLineCount; index++) {
            keys.add(getNameTranslationKey() + ".desc." + index);
        }
        return List.copyOf(keys);
    }
}