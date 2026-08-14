package com.carrot123.until_eternity.compat.ironsspellbooks;

import java.util.Arrays;
import java.util.Optional;

public enum StaffAffix {
    ANCIENT("ancient", 0.10D, -0.05D, 0.0D),
    NEWBORN("newborn", -0.05D, 0.10D, 0.0D),
    DECADENT("decadent", -0.05D, -0.05D, -0.05D),
    REFINED("refined", 0.0D, 0.05D, 0.05D),
    NOBLE("noble", 0.05D, -0.05D, 0.10D),
    MODEST("modest", -0.10D, 0.20D, 0.0D),
    DIVINE("divine", 0.15D, 0.10D, 0.20D);

    private final String id;
    private final String translationKey;
    private final double spellPower;
    private final double castTimeReduction;
    private final double cooldownReduction;

    StaffAffix(
            String id,
            double spellPower,
            double castTimeReduction,
            double cooldownReduction
    ) {
        this.id = id;
        this.translationKey = "affix.until_eternity." + id;
        this.spellPower = spellPower;
        this.castTimeReduction = castTimeReduction;
        this.cooldownReduction = cooldownReduction;
    }

    public String id() {
        return id;
    }

    public String translationKey() {
        return translationKey;
    }

    public double spellPower() {
        return spellPower;
    }

    public double castTimeReduction() {
        return castTimeReduction;
    }

    public double cooldownReduction() {
        return cooldownReduction;
    }

    public static Optional<StaffAffix> byId(String id) {
        if (id == null || id.isBlank()) {
            return Optional.empty();
        }
        return Arrays.stream(values())
                .filter(affix -> affix.id.equals(id))
                .findFirst();
    }
}
