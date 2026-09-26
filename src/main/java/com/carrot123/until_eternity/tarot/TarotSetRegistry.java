package com.carrot123.until_eternity.tarot;

import com.carrot123.until_eternity.until_eternity;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.resources.ResourceLocation;

public final class TarotSetRegistry {
    private static final List<TarotSetDefinition> DEFINITIONS = List.of(
            set("journeys_midpoint", 2,
                    "+the_fool",
                    "+the_chariot"),

            set("flash_of_inspiration", 3,
                    "+the_magician",
                    "+the_star"),

            set("night_walker", 4,
                    "+the_moon",
                    "+the_hermit"),

            set("life_death_boundary", 1,
                    "+death",
                    "+wheel_of_fortune"),

            set("foresight", 2,
                    "+the_high_priestess",
                    "+wheel_of_fortune"),

            set("iron_wrist", 2,
                    "+the_emperor",
                    "+strength"),

            set("desperado", 3,
                    "-the_fool",
                    "-the_chariot"),

            set("burning_desire", 1,
                    "-the_lovers",
                    "+the_devil"),

            set("hero", 1,
                    "+the_fool",
                    "+the_magician",
                    "+the_chariot"),

            set("ascetic", 4,
                    "+the_hermit",
                    "+the_hanged_man",
                    "+temperance"),

            set("misfortune", 2,
                    "-wheel_of_fortune",
                    "-the_moon",
                    "+the_tower"),

            set("berserk", 1,
                    "-the_chariot",
                    "-strength",
                    "+the_devil"),

            set("apocalypse", 3,
                    "+death",
                    "+the_devil",
                    "+the_tower",
                    "+judgement"),

            set("celestial", 8,
                    "+the_star",
                    "+the_moon",
                    "+the_sun",
                    "+the_world"),

            set("empire", 2,
                    "+the_empress",
                    "+the_emperor",
                    "+the_hierophant",
                    "+justice"),

            set("death_refusal", 2,
                    "-death",
                    "+the_tower")
    );

    private static final Map<ResourceLocation, TarotSetEffect> EFFECTS =
            createEffects();

    private TarotSetRegistry() {
    }

    public static List<TarotSetDefinition> definitions() {
        return DEFINITIONS;
    }

    public static Map<ResourceLocation, TarotSetEffect> effects() {
        return EFFECTS;
    }

    public static ResourceLocation id(String path) {
        return new ResourceLocation(
                until_eternity.MODID,
                path
        );
    }

    private static TarotSetDefinition set(
            String id,
            int descriptionLineCount,
            String... cards
    ) {
        Map<ResourceLocation, TarotCardRequirement> requirements =
                new LinkedHashMap<>();

        for (String card : cards) {
            char direction = card.charAt(0);

            if (direction != '+' && direction != '-') {
                throw new IllegalArgumentException(
                        "Invalid tarot orientation: " + card
                );
            }

            requirements.put(
                    new ResourceLocation(
                            "tarotcards",
                            card.substring(1)
                    ),
                    direction == '+'
                            ? TarotCardRequirement.UPRIGHT
                            : TarotCardRequirement.REVERSED
            );
        }

        return new TarotSetDefinition(
                id(id),
                requirements,
                descriptionLineCount
        );
    }

    private static Map<ResourceLocation, TarotSetEffect> createEffects() {
        Map<ResourceLocation, TarotSetEffect> effects =
                new LinkedHashMap<>();

        for (TarotSetDefinition definition : DEFINITIONS) {
            if (effects.put(
                    definition.id(),
                    TarotSetEffectManager.effect(definition.id())
            ) != null) {
                throw new IllegalStateException(
                        "Duplicate tarot set: "
                                + definition.id()
                );
            }
        }

        return Map.copyOf(effects);
    }
}