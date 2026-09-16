package com.carrot123.until_eternity.effect;

import com.carrot123.until_eternity.compat.ScopedValueStack;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;

import java.util.function.Supplier;

public final class VoidCorrosionDamageContext {
    private static final ScopedValueStack<Invocation> ACTIVE =
            new ScopedValueStack<>();

    private VoidCorrosionDamageContext() {
    }

    public static boolean hurt(
            LivingEntity target,
            DamageSource source,
            float amount
    ) {
        return withInvocation(
                target, source, () -> target.hurt(source, amount));
    }

    public static boolean isOwnPeriodicDamage(
            LivingEntity target,
            DamageSource source
    ) {
        return isActive(target, source);
    }

    static <T> T withInvocation(
            Object target,
            Object source,
            Supplier<T> action
    ) {
        return ACTIVE.withValue(new Invocation(target, source), action);
    }

    static boolean isActive(Object target, Object source) {
        Invocation invocation = ACTIVE.current(null);
        return invocation != null
                && invocation.target == target
                && invocation.source == source;
    }

    private record Invocation(Object target, Object source) {
    }
}
