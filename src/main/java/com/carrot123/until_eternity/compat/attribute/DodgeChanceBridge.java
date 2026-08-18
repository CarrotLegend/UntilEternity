package com.carrot123.until_eternity.compat.attribute;

import com.obscuria.obscureapi.registry.ObscureAPIAttributes;
import net.minecraft.world.entity.LivingEntity;

public final class DodgeChanceBridge {
    private DodgeChanceBridge() {
    }

    public static double combineWithResidual(
            double terraChance,
            LivingEntity entity) {
        return AttributeModifierConversions.combineDodgeChance(
                terraChance,
                ObscureAPIAttributes.getDodge(entity));
    }
}
