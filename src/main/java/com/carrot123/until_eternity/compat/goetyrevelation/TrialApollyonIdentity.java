package com.carrot123.until_eternity.compat.goetyrevelation;

import com.Polarice3.Goety.common.entities.boss.Apostle;
import net.minecraft.world.entity.Entity;
import z1gned.goetyrevelation.util.ApollyonAbilityHelper;

/** Only referenced while the optional Goety Revelation summon context is active. */
public final class TrialApollyonIdentity {
    private TrialApollyonIdentity() {}

    public static boolean isApollyon(Entity entity) {
        return entity instanceof Apostle && entity instanceof ApollyonAbilityHelper helper
                && helper.allTitlesApostle_1_20_1$isApollyon();
    }
}
