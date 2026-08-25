package com.carrot123.until_eternity.combat;

import com.carrot123.until_eternity.compat.ScopedValueStack;
import com.carrot123.until_eternity.item.ModItems;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.entity.PartEntity;

import java.util.function.Supplier;

public final class TrueChefsKnifeAttackContext {
    private static final ScopedValueStack<Attack> ACTIVE_ATTACK = new ScopedValueStack<>();

    private TrueChefsKnifeAttackContext() {
    }

    public static boolean isEligible(Player player, Entity target) {
        LivingEntity victim = resolveVictim(target);
        return !player.level().isClientSide
                && player.getMainHandItem().is(ModItems.TRUE_CHEFS_KNIFE.get())
                && victim != null
                && victim.isAlive()
                && !victim.isRemoved()
                && !victim.isSpectator();
    }

    public static LivingEntity resolveVictim(Entity target) {
        if (target instanceof LivingEntity livingEntity) {
            return livingEntity;
        }
        if (target instanceof PartEntity<?> part
                && part.getParent() instanceof LivingEntity livingParent) {
            return livingParent;
        }
        return null;
    }

    public static boolean withAttack(
            Player player,
            LivingEntity victim,
            float originalDamage,
            Supplier<Boolean> action) {
        return ACTIVE_ATTACK.withValue(
                new Attack(player, victim, originalDamage), action);
    }

    public static boolean matches(LivingEntity victim, DamageSource source) {
        Attack attack = ACTIVE_ATTACK.current(null);
        return attack != null
                && attack.victim() == victim
                && source.getEntity() == attack.player();
    }

    public record Attack(Player player, LivingEntity victim, float originalDamage) {
    }
}
