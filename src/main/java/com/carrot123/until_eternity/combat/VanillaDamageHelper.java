package com.carrot123.until_eternity.combat;

import java.lang.reflect.Method;
import java.util.List;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;

public final class VanillaDamageHelper {

    private static final Method ARMOR_METHOD =
            findMethod(
                    List.of(
                            "getDamageAfterArmorAbsorb",
                            "m_21161_"
                    )
            );

    private static final Method MAGIC_METHOD =
            findMethod(
                    List.of(
                            "getDamageAfterMagicAbsorb",
                            "m_6515_"
                    )
            );

    private VanillaDamageHelper() {
    }

    private static Method findMethod(
            List<String> candidates
    ) {
        for (String name : candidates) {
            try {
                Method method =
                        LivingEntity.class.getDeclaredMethod(
                                name,
                                DamageSource.class,
                                float.class
                        );

                method.setAccessible(true);
                return method;
            } catch (NoSuchMethodException ignored) {
            }
        }

        throw new RuntimeException(
                "Cannot find LivingEntity damage method: "
                        + candidates
        );
    }

    public static float getDamageAfterArmorAbsorb(
            LivingEntity entity,
            DamageSource source,
            float amount
    ) {
        try {
            return ((Float) ARMOR_METHOD.invoke(
                    entity,
                    source,
                    amount
            )).floatValue();
        } catch (Exception exception) {
            throw new RuntimeException(
                    "Failed to invoke getDamageAfterArmorAbsorb",
                    exception
            );
        }
    }

    public static float getDamageAfterMagicAbsorb(
            LivingEntity entity,
            DamageSource source,
            float amount
    ) {
        try {
            return ((Float) MAGIC_METHOD.invoke(
                    entity,
                    source,
                    amount
            )).floatValue();
        } catch (Exception exception) {
            throw new RuntimeException(
                    "Failed to invoke getDamageAfterMagicAbsorb",
                    exception
            );
        }
    }
}