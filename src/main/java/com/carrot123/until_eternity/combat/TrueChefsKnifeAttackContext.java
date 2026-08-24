package com.carrot123.until_eternity.combat;

import com.carrot123.until_eternity.compat.ScopedValueStack;
import com.carrot123.until_eternity.item.ModItems;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

import java.util.function.Supplier;

public final class TrueChefsKnifeAttackContext {
    private static final ScopedValueStack<Attack> ACTIVE_ATTACK = new ScopedValueStack<>();

    private TrueChefsKnifeAttackContext() {
    }

    public static boolean isEligible(Player player, Entity target) {
        return !player.level().isClientSide
                && player.getMainHandItem().is(ModItems.TRUE_CHEFS_KNIFE.get())
                && target instanceof LivingEntity livingTarget
                && livingTarget.isAlive()
                && !livingTarget.isRemoved()
                && !livingTarget.isSpectator();
    }

    public static boolean withAttack(
            Player player,
            LivingEntity target,
            float originalDamage,
            Supplier<Boolean> action) {
        return ACTIVE_ATTACK.withValue(new Attack(player, target, originalDamage), action);
    }

    public static boolean matches(LivingEntity target, DamageSource source) {
        Attack attack = ACTIVE_ATTACK.current(null);
        return attack != null
                && attack.target() == target
                && source.getEntity() == attack.player();
    }

    public record Attack(Player player, LivingEntity target, float originalDamage) {
    }
}
