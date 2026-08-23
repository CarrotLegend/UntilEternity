package com.carrot123.until_eternity.compat.eternalcareer;

import net.minecraft.ChatFormatting;

public enum ChefRank {

    NONE(
            0,
            null,
            null,
            null
    ),

    APPRENTICE(
            1,
            "chef_apprentice_badge",
            "chef_rank.until_eternity.apprentice",
            ChatFormatting.YELLOW
    ),

    INTERMEDIATE(
            2,
            "intermediate_chef_badge",
            "chef_rank.until_eternity.intermediate",
            ChatFormatting.GREEN
    ),

    ADVANCED(
            3,
            "advanced_chef_badge",
            "chef_rank.until_eternity.advanced",
            ChatFormatting.BLUE
    ),

    SENIOR(
            4,
            "senior_technician_badge",
            "chef_rank.until_eternity.senior",
            ChatFormatting.GOLD
    ),

    MASTER(
            5,
            "master_chef_badge",
            "chef_rank.until_eternity.master",
            null
    );

    private final int id;
    private final String badgePath;
    private final String translationKey;
    private final ChatFormatting color;

    ChefRank(
            int id,
            String badgePath,
            String translationKey,
            ChatFormatting color
    ) {
        this.id = id;
        this.badgePath = badgePath;
        this.translationKey = translationKey;
        this.color = color;
    }

    public int id() {
        return this.id;
    }

    public String badgePath() {
        return this.badgePath;
    }

    public String translationKey() {
        return this.translationKey;
    }

    public ChatFormatting color() {
        return this.color;
    }

    public boolean isRanked() {
        return this != NONE;
    }

    public ChefRank previous() {
        if (this == NONE) {
            return NONE;
        }

        return byId(this.id - 1);
    }

    public static ChefRank byId(int id) {
        for (ChefRank rank : values()) {
            if (rank.id == id) {
                return rank;
            }
        }

        return NONE;
    }
}
