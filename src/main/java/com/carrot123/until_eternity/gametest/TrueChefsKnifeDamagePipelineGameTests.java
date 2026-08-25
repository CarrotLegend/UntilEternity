package com.carrot123.until_eternity.gametest;

import com.carrot123.until_eternity.combat.TrueChefsKnifeAttackContext;
import com.carrot123.until_eternity.item.ModItems;
import com.carrot123.until_eternity.registry.ModMobEffects;
import com.carrot123.until_eternity.until_eternity;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(until_eternity.MODID)
@PrefixGameTestTemplate(false)
public final class TrueChefsKnifeDamagePipelineGameTests {
    private TrueChefsKnifeDamagePipelineGameTests() {
    }

    @GameTest(template = "statue")
    public static void playerAttackUsesVictimHurtAndPreservesCancelledHooks(
            GameTestHelper helper) {
        Player attacker = knifeUser(helper);
        TrackingZombie victim = trackingZombie(helper, new BlockPos(1, 2, 1), 40.0F);
        CancelAndZeroHooks listener = new CancelAndZeroHooks(victim);
        MinecraftForge.EVENT_BUS.register(listener);
        float startingHealth = victim.getHealth();
        try {
            attacker.attack(victim);
        } finally {
            MinecraftForge.EVENT_BUS.unregister(listener);
        }

        helper.assertTrue(victim.hurtCalls == 1,
                "The knife must invoke the target's real hurt method exactly once");
        helper.assertTrue(victim.getHealth() < startingHealth,
                "Cancelled and zeroed Forge hooks must not reject the scoped knife hit");
        helper.assertTrue(listener.attackEvents == 1
                        && listener.hurtEvents == 1
                        && listener.damageEvents == 1,
                "The hit must traverse all standard Forge living damage events");
        MobEffectInstance frenzy = attacker.getEffect(ModMobEffects.COOKING_FRENZY.get());
        helper.assertTrue(frenzy != null && frenzy.getAmplifier() == 0,
                "One successful knife hit must add exactly one Cooking Frenzy stack");
        helper.succeed();
    }

    @GameTest(template = "statue")
    public static void attackContextIsClearedAfterTheWrappedHurtCall(
            GameTestHelper helper) {
        Player attacker = knifeUser(helper);
        TrackingZombie victim = trackingZombie(helper, new BlockPos(1, 2, 1), 40.0F);
        attacker.attack(victim);
        float healthAfterKnife = victim.getHealth();

        CancelAttack listener = new CancelAttack(victim);
        MinecraftForge.EVENT_BUS.register(listener);
        boolean ordinaryResult;
        try {
            ordinaryResult = victim.hurt(
                    attacker.damageSources().playerAttack(attacker), 4.0F);
        } finally {
            MinecraftForge.EVENT_BUS.unregister(listener);
        }

        helper.assertTrue(!ordinaryResult,
                "Damage after Player.attack must no longer match the knife context");
        assertFloat(helper, healthAfterKnife, victim.getHealth(),
                "A later cancelled hit must not inherit the knife bypass");
        helper.succeed();
    }

    @GameTest(template = "statue")
    public static void multipartResolvesParentAndLethalHitUsesVirtualDie(
            GameTestHelper helper) {
        Player attacker = knifeUser(helper);
        EnderDragon dragon = EntityType.ENDER_DRAGON.create(helper.getLevel());
        helper.assertTrue(dragon != null, "Dragon must be creatable");
        helper.assertTrue(TrueChefsKnifeAttackContext.resolveVictim(dragon.head) == dragon,
                "A Forge multipart part must resolve to its living parent");
        helper.assertTrue(TrueChefsKnifeAttackContext.resolveVictim(dragon) == dragon,
                "A living target must resolve to itself");

        DeathTrackingZombie victim = new DeathTrackingZombie(helper.getLevel());
        victim.setPos(helper.absolutePos(new BlockPos(2, 2, 1)).getCenter());
        victim.setHealth(1.0F);
        helper.getLevel().addFreshEntity(victim);
        attacker.attack(victim);
        helper.assertTrue(victim.hurtCalls == 1,
                "A lethal knife hit must still enter victim.hurt");
        helper.assertTrue(victim.dieCalls == 1,
                "Vanilla damage processing must invoke virtual die exactly once");
        helper.succeed();
    }

    private static Player knifeUser(GameTestHelper helper) {
        Player player = helper.makeMockPlayer();
        player.setItemInHand(InteractionHand.MAIN_HAND,
                new ItemStack(ModItems.TRUE_CHEFS_KNIFE.get()));
        return player;
    }

    private static TrackingZombie trackingZombie(
            GameTestHelper helper, BlockPos pos, float health) {
        TrackingZombie zombie = new TrackingZombie(helper.getLevel());
        zombie.setPos(helper.absolutePos(pos).getCenter());
        zombie.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.MAX_HEALTH)
                .setBaseValue(health);
        zombie.setHealth(health);
        helper.getLevel().addFreshEntity(zombie);
        return zombie;
    }

    private static void assertFloat(
            GameTestHelper helper, float expected, float actual, String message) {
        helper.assertTrue(Math.abs(expected - actual) < 0.0001F,
                message + ": expected " + expected + ", got " + actual);
    }

    private static class TrackingZombie extends Zombie {
        protected int hurtCalls;

        private TrackingZombie(ServerLevel level) {
            super(EntityType.ZOMBIE, level);
        }

        @Override
        public boolean hurt(DamageSource source, float amount) {
            hurtCalls++;
            return super.hurt(source, amount);
        }
    }

    private static final class DeathTrackingZombie extends TrackingZombie {
        private int dieCalls;

        private DeathTrackingZombie(ServerLevel level) {
            super(level);
        }

        @Override
        public void die(DamageSource source) {
            dieCalls++;
            super.die(source);
        }
    }

    private static final class CancelAttack {
        private final Zombie victim;

        private CancelAttack(Zombie victim) {
            this.victim = victim;
        }

        @SubscribeEvent
        public void onAttack(LivingAttackEvent event) {
            if (event.getEntity() == victim) {
                event.setCanceled(true);
            }
        }
    }

    private static final class CancelAndZeroHooks {
        private final Zombie victim;
        private int attackEvents;
        private int hurtEvents;
        private int damageEvents;

        private CancelAndZeroHooks(Zombie victim) {
            this.victim = victim;
        }

        @SubscribeEvent
        public void onAttack(LivingAttackEvent event) {
            if (event.getEntity() == victim) {
                attackEvents++;
                event.setCanceled(true);
            }
        }

        @SubscribeEvent
        public void onHurt(LivingHurtEvent event) {
            if (event.getEntity() == victim) {
                hurtEvents++;
                event.setAmount(0.0F);
            }
        }

        @SubscribeEvent
        public void onDamage(LivingDamageEvent event) {
            if (event.getEntity() == victim) {
                damageEvents++;
                event.setAmount(0.0F);
            }
        }
    }
}
