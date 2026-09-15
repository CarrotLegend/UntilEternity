package com.carrot123.until_eternity.combat;

import com.carrot123.until_eternity.compat.ScopedValueStack;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.function.Supplier;

/** Authenticates a real Player.attack call; a player-attributed /damage is insufficient. */
public final class GravititePickaxeAttackContext {
    private static final ResourceLocation PICKAXE = new ResourceLocation("aether", "gravitite_pickaxe");
    private static final ResourceLocation SLIDER = new ResourceLocation("aether", "slider");
    private static final ScopedValueStack<Attack> ACTIVE = new ScopedValueStack<>();

    private GravititePickaxeAttackContext() {
    }

    public static boolean withAttack(Player player, Entity target, DamageSource source, Supplier<Boolean> action) {
        // Snapshot the hand at the actual hurt invocation, before other damage listeners can change it.
        boolean eligible = !player.level().isClientSide
                && PICKAXE.equals(ForgeRegistries.ITEMS.getKey(player.getMainHandItem().getItem()))
                && SLIDER.equals(ForgeRegistries.ENTITY_TYPES.getKey(target.getType()))
                && source.is(DamageTypes.PLAYER_ATTACK)
                && source.getEntity() == player
                && source.getDirectEntity() == player;
        // Even an ineligible nested attack must mask the outer attack.
        return ACTIVE.withValue(new Attack(player, target, source, eligible), action);
    }

    public static boolean claim(Entity target, DamageSource source) {
        Attack attack = ACTIVE.current(null);
        if (attack == null || !attack.eligible || attack.claimed
                || attack.target != target || attack.source != source
                || source.getEntity() != attack.player || source.getDirectEntity() != attack.player) {
            return false;
        }
        attack.claimed = true;
        return true;
    }

    private static final class Attack {
        private final Player player;
        private final Entity target;
        private final DamageSource source;
        private final boolean eligible;
        private boolean claimed;

        private Attack(Player player, Entity target, DamageSource source, boolean eligible) {
            this.player = player;
            this.target = target;
            this.source = source;
            this.eligible = eligible;
        }
    }
}
