package com.carrot123.until_eternity.compat.eternalcareer;

import net.minecraft.ChatFormatting;

public enum ChefRank {

    NONE(
            0,
            null,
            null
    ),

    APPRENTICE(
            1,
            "chef_rank.until_eternity.apprentice",
            ChatFormatting.YELLOW
    ),

    INTERMEDIATE(
            2,
            "chef_rank.until_eternity.intermediate",
            ChatFormatting.GREEN
    ),

    ADVANCED(
            3,
            "chef_rank.until_eternity.advanced",
            ChatFormatting.BLUE
    ),

    SENIOR(
            4,
            "chef_rank.until_eternity.senior",
            ChatFormatting.GOLD
    ),

    MASTER(
            5,
            "chef_rank.until_eternity.master",
            null
    );

    private final int id;
    private final String translationKey;
    private final ChatFormatting color;

    ChefRank(
            int id,
            String translationKey,
            ChatFormatting color
    ) {
        this.id = id;
        this.translationKey = translationKey;
        this.color = color;
    }

    public int id() {
        return id;
    }

    public String translationKey() {
        return translationKey;
    }

    public ChatFormatting color() {
        return color;
    }

    public boolean isRanked() {
        return this != NONE;
    }

    public static ChefRank byId(int id) {
        return switch (id) {
            case 1 -> APPRENTICE;
            case 2 -> INTERMEDIATE;
            case 3 -> ADVANCED;
            case 4 -> SENIOR;
            case 5 -> MASTER;
            default -> NONE;
        };
    }
}