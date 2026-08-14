package com.carrot123.until_eternity.compat.ironsspellbooks;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

public final class StaffUpgradeModifierIds {
    public static final UUID SPELL_POWER = create("spell_power");
    public static final UUID COOLDOWN_REDUCTION =
            create("cooldown_reduction");
    public static final UUID CAST_TIME_REDUCTION =
            create("cast_time_reduction");

    private StaffUpgradeModifierIds() {
    }

    private static UUID create(String modifierKey) {
        return UUID.nameUUIDFromBytes(
                ("until_eternity:staff_upgrade/" + modifierKey)
                        .getBytes(StandardCharsets.UTF_8));
    }
}
