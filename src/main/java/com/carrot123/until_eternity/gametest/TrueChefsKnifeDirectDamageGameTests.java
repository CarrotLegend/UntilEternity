package com.carrot123.until_eternity.gametest;

import com.carrot123.until_eternity.combat.TrueChefsKnifeAttackContext;
import com.carrot123.until_eternity.combat.TrueChefsKnifeDirectDamage;
import com.carrot123.until_eternity.item.ModItems;
import com.carrot123.until_eternity.registry.ModMobEffects;
import com.carrot123.until_eternity.until_eternity;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.InteractionHand;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(until_eternity.MODID)
@PrefixGameTestTemplate(false)
public final class TrueChefsKnifeDirectDamageGameTests {
    private static final float BASE_DAMAGE = 5.0F;

    private TrueChefsKnifeDirectDamageGameTests() {
    }

    @GameTest(template = "statue")
    public static void directDamageIgnoresVanillaDefences(GameTestHelper helper) {
        Player attacker = helper.makeMockPlayer();
        Zombie victim = zombie(helper, 20.0F);
        victim.getAttribute(Attributes.ARMOR).setBaseValue(30.0D);
        victim.getAttribute(Attributes.ARMOR_TOUGHNESS).setBaseValue(20.0D);
        victim.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 600, 4));
        victim.invulnerableTime = 20;
        victim.setAbsorptionAmount(12.0F);

        boolean result = apply(attacker, victim, BASE_DAMAGE);

        helper.assertTrue(result, "The direct hit must succeed");
        assertFloat(helper, 15.0F, victim.getHealth(), "Defences must not reduce HP loss");
        assertFloat(helper, 12.0F, victim.getAbsorptionAmount(), "Absorption must not be consumed");
        helper.assertTrue(victim.invulnerableTime == 20, "Damage state must set vanilla i-frames");

        Player shielded = helper.makeMockPlayer();
        shielded.setItemInHand(InteractionHand.OFF_HAND, new ItemStack(Items.SHIELD));
        shielded.startUsingItem(InteractionHand.OFF_HAND);
        helper.assertTrue(apply(attacker, shielded, BASE_DAMAGE),
                "A raised shield must not reject direct damage");
        assertFloat(helper, 15.0F, shielded.getHealth(),
                "A raised shield must not reduce direct HP loss");
        helper.succeed();
    }

    @GameTest(template = "statue")
    public static void cancelledAndReducingHooksCannotLowerBaseDamage(GameTestHelper helper) {
        Player attacker = helper.makeMockPlayer();
        for (HookMode mode : new HookMode[]{
                HookMode.CANCEL_ATTACK,
                HookMode.HURT_ZERO,
                HookMode.DAMAGE_ZERO,
                HookMode.DAMAGE_NEGATIVE,
                HookMode.DAMAGE_NAN}) {
            Zombie victim = zombie(helper, 20.0F);
            HookMutator listener = new HookMutator(victim, mode);
            MinecraftForge.EVENT_BUS.register(listener);
            try {
                helper.assertTrue(apply(attacker, victim, BASE_DAMAGE),
                        "Direct hit must succeed for " + mode);
            } finally {
                MinecraftForge.EVENT_BUS.unregister(listener);
            }
            assertFloat(helper, 15.0F, victim.getHealth(),
                    mode + " must not lower base damage");
            victim.discard();
        }
        helper.succeed();
    }

    @GameTest(template = "statue")
    public static void positiveHookDamageStacksAndNeverHeals(GameTestHelper helper) {
        Player attacker = helper.makeMockPlayer();
        Zombie increased = zombie(helper, 20.0F);
        HookMutator increaseListener = new HookMutator(increased, HookMode.HURT_AND_DAMAGE_INCREASE);
        MinecraftForge.EVENT_BUS.register(increaseListener);
        try {
            apply(attacker, increased, BASE_DAMAGE);
        } finally {
            MinecraftForge.EVENT_BUS.unregister(increaseListener);
        }
        assertFloat(helper, 11.0F, increased.getHealth(),
                "Hurt +50%, then LOWEST damage +20%, must be preserved sequentially");

        Zombie preDamaged = zombie(helper, 20.0F);
        HookMutator preDamageListener = new HookMutator(preDamaged, HookMode.EVENT_DEALS_MORE);
        MinecraftForge.EVENT_BUS.register(preDamageListener);
        try {
            apply(attacker, preDamaged, BASE_DAMAGE);
        } finally {
            MinecraftForge.EVENT_BUS.unregister(preDamageListener);
        }
        assertFloat(helper, 3.0F, preDamaged.getHealth(),
                "The helper must never heal damage already dealt by an event");
        helper.succeed();
    }

    @GameTest(template = "statue")
    public static void rejectingHurtEntityStillTakesKnifeDamage(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        Player knifeUser = helper.makeMockPlayer();
        knifeUser.setItemInHand(InteractionHand.MAIN_HAND,
                new ItemStack(ModItems.TRUE_CHEFS_KNIFE.get()));
        RejectingZombie knifeVictim = rejectingZombie(level, helper.absolutePos(new BlockPos(1, 2, 1)));
        knifeUser.attack(knifeVictim);

        helper.assertTrue(knifeVictim.getHealth() < 1024.0F,
                "Knife attack must reduce HP when hurt() always returns false");
        helper.assertTrue(knifeVictim.hurtCalls == 0,
                "Knife primary hit must never invoke victim.hurt()");
        MobEffectInstance frenzy = knifeUser.getEffect(ModMobEffects.COOKING_FRENZY.get());
        helper.assertTrue(frenzy != null && frenzy.getAmplifier() == 0,
                "A successful full attack must advance Cooking Frenzy exactly once");

        Player swordUser = helper.makeMockPlayer();
        swordUser.setItemInHand(InteractionHand.MAIN_HAND,
                new ItemStack(Items.IRON_SWORD));
        RejectingZombie swordVictim = rejectingZombie(level, helper.absolutePos(new BlockPos(2, 2, 1)));
        swordUser.attack(swordVictim);
        assertFloat(helper, 1024.0F, swordVictim.getHealth(),
                "Ordinary weapon damage must still be rejected");
        helper.assertTrue(swordVictim.hurtCalls == 1,
                "Ordinary attack must use the target's hurt() implementation");
        helper.succeed();
    }

    @GameTest(template = "statue")
    public static void multipartResolutionAndDeathUseParentLivingEntity(GameTestHelper helper) {
        Player attacker = helper.makeMockPlayer();
        attacker.setItemInHand(InteractionHand.MAIN_HAND,
                new ItemStack(ModItems.TRUE_CHEFS_KNIFE.get()));
        EnderDragon dragon = EntityType.ENDER_DRAGON.create(helper.getLevel());
        helper.assertTrue(dragon != null, "Dragon must be creatable");
        helper.assertTrue(TrueChefsKnifeAttackContext.resolveVictim(dragon.head) == dragon,
                "Dragon part must resolve to its living parent");
        helper.assertTrue(TrueChefsKnifeAttackContext.resolveVictim(dragon) == dragon,
                "A living target must resolve to itself");
        float dragonHealth = dragon.getHealth();
        attacker.attack(dragon.head);
        helper.assertTrue(dragon.getHealth() < dragonHealth,
                "Attacking a dragon part must reduce parent HP");

        DeathTrackingZombie lethalVictim = new DeathTrackingZombie(helper.getLevel());
        lethalVictim.setPos(helper.absolutePos(new BlockPos(3, 2, 1)).getCenter());
        helper.getLevel().addFreshEntity(lethalVictim);
        helper.assertTrue(apply(attacker, lethalVictim, 100.0F), "Lethal hit must succeed");
        helper.assertTrue(lethalVictim.dieCalls == 1,
                "Lethal direct damage must enter virtual die(source) exactly once");
        helper.assertTrue(lethalVictim.getHealth() <= 0.0F,
                "Lethal victim must not remain as an ordinary positive-health entity");
        helper.succeed();
    }

    private static boolean apply(Player attacker, LivingEntity victim, float amount) {
        DamageSource source = attacker.damageSources().playerAttack(attacker);
        return TrueChefsKnifeDirectDamage.apply(attacker, victim, source, amount);
    }

    private static Zombie zombie(GameTestHelper helper, float health) {
        Zombie zombie = helper.spawnWithNoFreeWill(EntityType.ZOMBIE, new BlockPos(1, 2, 1));
        zombie.getAttribute(Attributes.MAX_HEALTH).setBaseValue(health);
        zombie.setHealth(health);
        return zombie;
    }

    private static RejectingZombie rejectingZombie(ServerLevel level, BlockPos pos) {
        RejectingZombie zombie = new RejectingZombie(level);
        zombie.setPos(pos.getCenter());
        zombie.getAttribute(Attributes.MAX_HEALTH).setBaseValue(1024.0D);
        zombie.setHealth(1024.0F);
        level.addFreshEntity(zombie);
        return zombie;
    }

    private static void assertFloat(
            GameTestHelper helper,
            float expected,
            float actual,
            String message) {
        helper.assertTrue(Math.abs(expected - actual) < 0.0001F,
                message + ": expected " + expected + ", got " + actual);
    }

    private enum HookMode {
        CANCEL_ATTACK,
        HURT_ZERO,
        DAMAGE_ZERO,
        DAMAGE_NEGATIVE,
        DAMAGE_NAN,
        HURT_AND_DAMAGE_INCREASE,
        EVENT_DEALS_MORE
    }

    private static final class HookMutator {
        private final LivingEntity victim;
        private final HookMode mode;

        private HookMutator(LivingEntity victim, HookMode mode) {
            this.victim = victim;
            this.mode = mode;
        }

        @SubscribeEvent
        public void onAttack(LivingAttackEvent event) {
            if (event.getEntity() == victim && mode == HookMode.CANCEL_ATTACK) {
                event.setCanceled(true);
            }
        }

        @SubscribeEvent
        public void onHurt(LivingHurtEvent event) {
            if (event.getEntity() != victim) {
                return;
            }
            if (mode == HookMode.HURT_ZERO) {
                event.setAmount(0.0F);
            } else if (mode == HookMode.HURT_AND_DAMAGE_INCREASE) {
                event.setAmount(event.getAmount() * 1.5F);
            }
        }

        @SubscribeEvent(priority = EventPriority.LOWEST)
        public void onDamage(LivingDamageEvent event) {
            if (event.getEntity() != victim) {
                return;
            }
            switch (mode) {
                case DAMAGE_ZERO -> event.setAmount(0.0F);
                case DAMAGE_NEGATIVE -> event.setAmount(-4.0F);
                case DAMAGE_NAN -> event.setAmount(Float.NaN);
                case HURT_AND_DAMAGE_INCREASE -> event.setAmount(event.getAmount() * 1.2F);
                case EVENT_DEALS_MORE -> victim.setHealth(3.0F);
                default -> {
                }
            }
        }
    }

    private static class RejectingZombie extends Zombie {
        private int hurtCalls;

        private RejectingZombie(ServerLevel level) {
            super(EntityType.ZOMBIE, level);
        }

        @Override
        public boolean hurt(DamageSource source, float amount) {
            hurtCalls++;
            return false;
        }
    }

    private static final class DeathTrackingZombie extends Zombie {
        private int dieCalls;

        private DeathTrackingZombie(ServerLevel level) {
            super(EntityType.ZOMBIE, level);
        }

        @Override
        public void die(DamageSource source) {
            dieCalls++;
            super.die(source);
        }
    }
}
