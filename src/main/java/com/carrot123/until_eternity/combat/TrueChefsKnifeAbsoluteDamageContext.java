package com.carrot123.until_eternity.combat;

import com.carrot123.until_eternity.compat.ScopedValueStack;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.entity.PartEntity;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;

/** Authenticates the one real Player.attack hurt call made with the True Chef's Knife. */
public final class TrueChefsKnifeAbsoluteDamageContext {
    public static final ResourceLocation WEAPON_ID =
            new ResourceLocation("until_eternity", "true_chefs_knife");

    private static final ScopedValueStack<Attack> ACTIVE = new ScopedValueStack<>();
    private static volatile Consumer<DamageSource> bypassMarker;

    private TrueChefsKnifeAbsoluteDamageContext() {
    }

    /** Installed only when both Goety Revelation and RevelationFix are present. */
    public static void installBypassMarker(Consumer<DamageSource> marker) {
        bypassMarker = Objects.requireNonNull(marker, "marker");
    }

    public static boolean withAttack(
            Player player,
            Entity target,
            DamageSource source,
            float originalDamage,
            Supplier<Boolean> action
    ) {
        LivingEntity victim = resolveVictim(target);
        Consumer<DamageSource> marker = bypassMarker;
        ResourceLocation weaponId = ForgeRegistries.ITEMS.getKey(
                player.getMainHandItem().getItem());
        boolean eligible = marker != null
                && !player.level().isClientSide
                && victim != null
                && victim.isAlive()
                && !victim.isRemoved()
                && !victim.isSpectator()
                && WEAPON_ID.equals(weaponId)
                && source.is(DamageTypes.PLAYER_ATTACK)
                && source.getEntity() == player
                && source.getDirectEntity() == player;

        return ACTIVE.withValue(
                new Attack(player, victim, source, originalDamage, eligible),
                () -> {
                    if (eligible) {
                        marker.accept(source);
                    }
                    return action.get();
                }
        );
    }

    public static boolean matches(LivingEntity victim, DamageSource source) {
        Attack attack = ACTIVE.current(null);
        return attack != null
                && attack.eligible
                && attack.victim == victim
                && attack.source == source
                && source.getEntity() == attack.player
                && source.getDirectEntity() == attack.player;
    }

    public static boolean isActiveFor(LivingEntity victim) {
        Attack attack = ACTIVE.current(null);
        return attack != null && attack.eligible && attack.victim == victim;
    }

    public static float originalDamage(LivingEntity victim, DamageSource source) {
        Attack attack = ACTIVE.current(null);
        return matches(victim, source) ? attack.originalDamage : 0.0F;
    }

    public static boolean claimSplit(LivingEntity victim, DamageSource source) {
        Attack attack = ACTIVE.current(null);
        if (!matches(victim, source) || attack.splitClaimed) {
            return false;
        }
        attack.splitClaimed = true;
        return true;
    }

    private static LivingEntity resolveVictim(Entity target) {
        if (target instanceof LivingEntity livingEntity) {
            return livingEntity;
        }
        if (target instanceof PartEntity<?> part
                && part.getParent() instanceof LivingEntity livingParent) {
            return livingParent;
        }
        return null;
    }

    private static final class Attack {
        private final Player player;
        private final LivingEntity victim;
        private final DamageSource source;
        private final float originalDamage;
        private final boolean eligible;
        private boolean splitClaimed;

        private Attack(Player player, LivingEntity victim, DamageSource source,
                       float originalDamage, boolean eligible) {
            this.player = player;
            this.victim = victim;
            this.source = source;
            this.originalDamage = originalDamage;
            this.eligible = eligible;
        }
    }
}
