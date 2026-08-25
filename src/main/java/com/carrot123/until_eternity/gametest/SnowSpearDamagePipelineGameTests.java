package com.carrot123.until_eternity.gametest;

import com.carrot123.until_eternity.item.ModItems;
import com.carrot123.until_eternity.item.SnowSpear;
import com.carrot123.until_eternity.until_eternity;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(until_eternity.MODID)
@PrefixGameTestTemplate(false)
public final class SnowSpearDamagePipelineGameTests {
    private SnowSpearDamagePipelineGameTests() {
    }

    @GameTest(template = "statue", timeoutTicks = 100)
    public static void fullStrengthHitUsesOneOwnedFrostDamagePipeline(
            GameTestHelper helper) {
        Player attacker = snowSpearUser(helper);
        TrackingZombie victim = trackingZombie(helper, 2_000.0F);
        ItemStack spear = attacker.getMainHandItem();

        double attackModifier = spear.getAttributeModifiers(EquipmentSlot.MAINHAND)
                .get(Attributes.ATTACK_DAMAGE)
                .stream()
                .filter(modifier -> modifier.getOperation()
                        == AttributeModifier.Operation.ADDITION)
                .mapToDouble(AttributeModifier::getAmount)
                .sum();
        helper.assertTrue(Math.abs(
                        Attributes.ATTACK_DAMAGE.getDefaultValue()
                                + attackModifier
                                - SnowSpear.BASE_ATTACK_DAMAGE) < 0.0001D,
                "The default main-hand attack damage panel must total 500");

        attacker.resetAttackStrengthTicker();
        helper.runAfterDelay(30, () -> {
            DamageEvents listener = new DamageEvents(victim);
            MinecraftForge.EVENT_BUS.register(listener);
            float startingHealth = victim.getHealth();
            try {
                attacker.attack(victim);
            } finally {
                MinecraftForge.EVENT_BUS.unregister(listener);
            }

            helper.assertTrue(victim.hurtCalls == 1,
                    "Snow Spear must enter Entity.hurt exactly once");
            helper.assertTrue(listener.attackEvents == 1
                            && listener.hurtEvents == 1
                            && listener.damageEvents == 1,
                    "Each Forge living damage stage must run exactly once");
            helper.assertTrue(listener.source != null
                            && listener.source.is(SnowSpear.FROST_BITTEN_KEY),
                    "The only damage source must be frost_bitten");
            helper.assertTrue(listener.source.getEntity() == attacker
                            && listener.source.getDirectEntity() == attacker,
                    "The frost source must retain direct and causing player ownership");
            helper.assertTrue(Math.abs(
                            startingHealth - victim.getHealth()
                                    - SnowSpear.BASE_ATTACK_DAMAGE) < 0.01F,
                    "A full-strength unmodified hit must deal 500 damage");
            helper.assertTrue(spear.getDamageValue() == 1,
                    "One successful attack must consume one durability");
            helper.succeed();
        });
    }

    @GameTest(template = "statue", timeoutTicks = 100)
    public static void lethalFrostHitKeepsPlayerKillCredit(
            GameTestHelper helper) {
        Player attacker = snowSpearUser(helper);
        TrackingZombie victim = trackingZombie(helper, 100.0F);
        attacker.resetAttackStrengthTicker();

        helper.runAfterDelay(30, () -> {
            attacker.attack(victim);
            helper.assertTrue(victim.hurtCalls == 1,
                    "A lethal Snow Spear hit must still use one hurt call");
            helper.assertTrue(!victim.isAlive(),
                    "A full-strength Snow Spear hit must be lethal here");
            helper.assertTrue(victim.getKillCredit() == attacker,
                    "The attacking player must receive kill credit");
            helper.succeed();
        });
    }

    @GameTest(template = "statue", timeoutTicks = 100)
    public static void ordinarySwordKeepsVanillaPlayerAttackSource(
            GameTestHelper helper) {
        Player attacker = helper.makeMockPlayer();
        attacker.setItemInHand(InteractionHand.MAIN_HAND,
                new ItemStack(Items.IRON_SWORD));
        TrackingZombie victim = trackingZombie(helper, 100.0F);
        attacker.resetAttackStrengthTicker();

        helper.runAfterDelay(30, () -> {
            DamageEvents listener = new DamageEvents(victim);
            MinecraftForge.EVENT_BUS.register(listener);
            try {
                attacker.attack(victim);
            } finally {
                MinecraftForge.EVENT_BUS.unregister(listener);
            }
            helper.assertTrue(victim.hurtCalls == 1,
                    "An ordinary sword must retain its single vanilla hurt call");
            helper.assertTrue(listener.source != null
                            && !listener.source.is(SnowSpear.FROST_BITTEN_KEY),
                    "An ordinary sword must not be converted to frost_bitten");
            helper.succeed();
        });
    }

    @GameTest(template = "statue", timeoutTicks = 100)
    public static void criticalAndEnchantedHitKeepsVanillaScaling(
            GameTestHelper helper) {
        Player attacker = snowSpearUser(helper);
        ItemStack spear = attacker.getMainHandItem();
        spear.enchant(Enchantments.SHARPNESS, 5);
        TrackingZombie victim = trackingZombie(helper, 1_000.0F);
        attacker.resetAttackStrengthTicker();

        helper.runAfterDelay(30, () -> {
            DamageEvents listener = new DamageEvents(victim);
            MinecraftForge.EVENT_BUS.register(listener);
            float startingHealth = victim.getHealth();
            attacker.fallDistance = 1.0F;
            attacker.setOnGround(false);
            try {
                attacker.attack(victim);
            } finally {
                MinecraftForge.EVENT_BUS.unregister(listener);
            }

            helper.assertTrue(victim.hurtCalls == 1,
                    "Critical enchanted Snow Spear attacks must still hurt once");
            helper.assertTrue(listener.attackEvents == 1
                            && listener.hurtEvents == 1
                            && listener.damageEvents == 1,
                    "Critical enchanted attacks must keep one Forge event chain");
            helper.assertTrue(listener.source != null
                            && listener.source.is(SnowSpear.FROST_BITTEN_KEY),
                    "Critical enchanted attacks must retain frost_bitten");
            helper.assertTrue(startingHealth - victim.getHealth()
                            > SnowSpear.BASE_ATTACK_DAMAGE,
                    "Vanilla critical and enchantment bonuses must scale above 500");
            helper.succeed();
        });
    }

    private static Player snowSpearUser(GameTestHelper helper) {
        Player player = helper.makeMockPlayer();
        player.setItemInHand(InteractionHand.MAIN_HAND,
                new ItemStack(ModItems.SNOW_SPEAR.get()));
        return player;
    }

    private static TrackingZombie trackingZombie(
            GameTestHelper helper,
            float health) {
        TrackingZombie zombie = new TrackingZombie(helper.getLevel());
        zombie.setPos(helper.absolutePos(new BlockPos(1, 2, 1)).getCenter());
        zombie.getAttribute(Attributes.MAX_HEALTH).setBaseValue(health);
        zombie.getAttribute(Attributes.ARMOR).setBaseValue(0.0D);
        zombie.getAttribute(Attributes.ARMOR_TOUGHNESS).setBaseValue(0.0D);
        zombie.setHealth(health);
        helper.getLevel().addFreshEntity(zombie);
        return zombie;
    }

    private static final class TrackingZombie extends Zombie {
        private int hurtCalls;

        private TrackingZombie(ServerLevel level) {
            super(EntityType.ZOMBIE, level);
        }

        @Override
        public boolean hurt(DamageSource source, float amount) {
            hurtCalls++;
            return super.hurt(source, amount);
        }
    }

    private static final class DamageEvents {
        private final Zombie victim;
        private DamageSource source;
        private int attackEvents;
        private int hurtEvents;
        private int damageEvents;

        private DamageEvents(Zombie victim) {
            this.victim = victim;
        }

        @SubscribeEvent
        public void onAttack(LivingAttackEvent event) {
            if (event.getEntity() == victim) {
                attackEvents++;
                source = event.getSource();
            }
        }

        @SubscribeEvent
        public void onHurt(LivingHurtEvent event) {
            if (event.getEntity() == victim) {
                hurtEvents++;
            }
        }

        @SubscribeEvent
        public void onDamage(LivingDamageEvent event) {
            if (event.getEntity() == victim) {
                damageEvents++;
            }
        }
    }
}
