package com.carrot123.until_eternity.gametest;

import com.carrot123.until_eternity.item.MobContainerItem;
import com.carrot123.until_eternity.item.ModItems;
import com.carrot123.until_eternity.until_eternity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Cow;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.entity.monster.warden.Warden;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@GameTestHolder(until_eternity.MODID)
@PrefixGameTestTemplate(false)
public final class MobContainerGameTests {
    private MobContainerGameTests() {
    }

    @GameTest(template = "statue", timeoutTicks = 100)
    public static void cowRoundTripKeepsIdentityAndLivingState(
            GameTestHelper helper) {
        Player player = helper.makeMockPlayer();
        Cow cow = helper.spawn(EntityType.COW, new BlockPos(1, 2, 1));
        UUID uuid = cow.getUUID();
        cow.setHealth(7.0F);
        cow.setCustomName(Component.literal("Bessie"));
        cow.setCustomNameVisible(true);
        cow.setSilent(true);
        cow.setNoAi(true);
        cow.setPersistenceRequired();
        cow.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 600, 2));

        ItemStack container = capture(helper, player, cow);
        helper.assertTrue(cow.isRemoved(),
                "The original cow must be discarded after serialization");
        helper.assertTrue(container.getTagElement(
                        MobContainerItem.TAG_STORED_ENTITY) != null,
                "The container must hold StoredEntity NBT");

        List<Component> tooltip = new ArrayList<>();
        ModItems.MOB_CONTAINER.get().appendHoverText(
                container, helper.getLevel(), tooltip, TooltipFlag.NORMAL);
        helper.assertTrue(tooltip.size() == 2
                        && tooltip.get(1).getString().contains("Bessie"),
                "The tooltip must use the stored custom name");

        release(helper, player, container, new BlockPos(3, 1, 1));
        Entity restoredEntity = helper.getLevel().getEntity(uuid);
        helper.assertTrue(restoredEntity instanceof Cow,
                "The same cow UUID must be restored");
        Cow restored = (Cow) restoredEntity;
        helper.assertTrue(Math.abs(restored.getHealth() - 7.0F) < 0.01F,
                "Health must survive the full NBT round trip");
        helper.assertTrue(restored.hasCustomName()
                        && "Bessie".equals(restored.getCustomName().getString())
                        && restored.isCustomNameVisible(),
                "Custom name state must survive the round trip");
        helper.assertTrue(restored.isSilent()
                        && restored.isNoAi()
                        && restored.isPersistenceRequired(),
                "Mob flags must survive the round trip");
        helper.assertTrue(restored.hasEffect(MobEffects.REGENERATION),
                "Potion effects must survive the round trip");
        helper.assertTrue(!MobContainerItem.hasStoredEntity(container),
                "The container must clear only after a successful release");
        helper.succeed();
    }

    @GameTest(template = "statue", timeoutTicks = 100)
    public static void specializedMobDataSurvivesRoundTrips(
            GameTestHelper helper) {
        Player player = helper.makeMockPlayer();

        Villager villager = helper.spawn(
                EntityType.VILLAGER, new BlockPos(1, 2, 1));
        villager.setVillagerData(villager.getVillagerData()
                .setProfession(VillagerProfession.LIBRARIAN)
                .setLevel(4));
        villager.setVillagerXp(87);
        villager.getOffers().add(new MerchantOffer(
                new ItemStack(Items.EMERALD, 3),
                new ItemStack(Items.BOOK),
                12,
                5,
                0.05F));
        UUID villagerId = villager.getUUID();
        ItemStack container = capture(helper, player, villager);
        release(helper, player, container, new BlockPos(3, 1, 1));
        Villager restoredVillager = (Villager) helper.getLevel()
                .getEntity(villagerId);
        helper.assertTrue(restoredVillager != null
                        && restoredVillager.getVillagerData().getProfession()
                        == VillagerProfession.LIBRARIAN
                        && restoredVillager.getVillagerData().getLevel() == 4
                        && restoredVillager.getVillagerXp() == 87
                        && restoredVillager.getOffers().size() == 1,
                "Villager profession, level, XP, and trades must survive");

        Wolf wolf = helper.spawn(EntityType.WOLF, new BlockPos(1, 2, 3));
        wolf.setOwnerUUID(player.getUUID());
        wolf.setTame(true);
        wolf.setOrderedToSit(true);
        wolf.setHealth(8.0F);
        wolf.setCustomName(Component.literal("Frost"));
        UUID wolfId = wolf.getUUID();
        container = capture(helper, player, wolf);
        release(helper, player, container, new BlockPos(3, 1, 3));
        Wolf restoredWolf = (Wolf) helper.getLevel().getEntity(wolfId);
        helper.assertTrue(restoredWolf != null
                        && restoredWolf.isTame()
                        && player.getUUID().equals(restoredWolf.getOwnerUUID())
                        && restoredWolf.isOrderedToSit()
                        && Math.abs(restoredWolf.getHealth() - 8.0F) < 0.01F
                        && "Frost".equals(restoredWolf.getName().getString()),
                "Wolf owner, sitting state, health, and name must survive");

        Zombie zombie = helper.spawn(
                EntityType.ZOMBIE, new BlockPos(1, 2, 5));
        ItemStack sword = new ItemStack(Items.DIAMOND_SWORD);
        sword.setHoverName(Component.literal("Stored Blade"));
        zombie.setItemSlot(EquipmentSlot.MAINHAND, sword);
        zombie.setDropChance(EquipmentSlot.MAINHAND, 0.75F);
        zombie.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 400, 1));
        UUID zombieId = zombie.getUUID();
        container = capture(helper, player, zombie);
        release(helper, player, container, new BlockPos(3, 1, 5));
        Zombie restoredZombie = (Zombie) helper.getLevel().getEntity(zombieId);
        helper.assertTrue(restoredZombie != null
                        && "Stored Blade".equals(restoredZombie
                                .getMainHandItem().getHoverName().getString())
                        && restoredZombie.hasEffect(MobEffects.DAMAGE_BOOST),
                "Equipment NBT and potion effects must survive");
        helper.succeed();
    }

    @GameTest(template = "statue", timeoutTicks = 100)
    public static void uuidCollisionKeepsStoredEntity(
            GameTestHelper helper) {
        Player player = helper.makeMockPlayer();
        Cow original = helper.spawn(EntityType.COW, new BlockPos(1, 2, 1));
        UUID uuid = original.getUUID();
        ItemStack container = capture(helper, player, original);

        ServerLevel level = helper.getLevel();
        Cow duplicate = EntityType.COW.create(level);
        helper.assertTrue(duplicate != null,
                "The UUID collision fixture must be creatable");
        duplicate.setUUID(uuid);
        duplicate.setPos(helper.absolutePos(new BlockPos(5, 2, 5)).getCenter());
        helper.assertTrue(level.addFreshEntity(duplicate),
                "The UUID collision fixture must enter the world");

        InteractionResult result = releaseAttempt(
                helper, player, container, new BlockPos(3, 1, 1));
        helper.assertTrue(result == InteractionResult.FAIL,
                "A duplicate UUID must make release fail safely");
        helper.assertTrue(MobContainerItem.hasStoredEntity(container),
                "UUID collision must not delete StoredEntity NBT");
        helper.assertTrue(level.getEntity(uuid) == duplicate,
                "The loaded duplicate must remain the only entity with that UUID");
        helper.succeed();
    }

    @GameTest(template = "statue", timeoutTicks = 100)
    public static void wardenAndRidingMobsAreRejectedWithoutMutation(
            GameTestHelper helper) {
        Player player = helper.makeMockPlayer();
        ItemStack container = new ItemStack(ModItems.MOB_CONTAINER.get());
        player.setItemInHand(InteractionHand.MAIN_HAND, container);
        player.setShiftKeyDown(true);

        Warden warden = helper.spawn(
                EntityType.WARDEN, new BlockPos(1, 2, 1));
        InteractionResult wardenResult = ModItems.MOB_CONTAINER.get()
                .interactLivingEntity(
                        container, player, warden, InteractionHand.MAIN_HAND);
        helper.assertTrue(wardenResult == InteractionResult.PASS
                        && !warden.isRemoved()
                        && !MobContainerItem.hasStoredEntity(container),
                "Warden must always be rejected without mutation");

        Cow mount = helper.spawn(EntityType.COW, new BlockPos(2, 2, 2));
        Cow rider = helper.spawn(EntityType.COW, new BlockPos(2, 2, 2));
        helper.assertTrue(rider.startRiding(mount, true),
                "The riding safety fixture must be established");
        InteractionResult riderResult = ModItems.MOB_CONTAINER.get()
                .interactLivingEntity(
                        container, player, rider, InteractionHand.MAIN_HAND);
        helper.assertTrue(riderResult == InteractionResult.PASS
                        && !rider.isRemoved()
                        && !MobContainerItem.hasStoredEntity(container),
                "Riding mobs must be rejected without changing the container");
        helper.succeed();
    }

    private static ItemStack capture(
            GameTestHelper helper,
            Player player,
            LivingEntity target) {
        ItemStack container = new ItemStack(ModItems.MOB_CONTAINER.get());
        player.setItemInHand(InteractionHand.MAIN_HAND, container);
        player.setShiftKeyDown(true);
        InteractionResult result = ModItems.MOB_CONTAINER.get()
                .interactLivingEntity(
                        container, player, target, InteractionHand.MAIN_HAND);
        player.setShiftKeyDown(false);
        helper.assertTrue(result == InteractionResult.CONSUME,
                "A valid server-side capture must consume the interaction");
        return container;
    }

    private static void release(
            GameTestHelper helper,
            Player player,
            ItemStack container,
            BlockPos clickedPos) {
        InteractionResult result = releaseAttempt(
                helper, player, container, clickedPos);
        helper.assertTrue(result == InteractionResult.CONSUME,
                "A valid server-side release must consume the interaction");
    }

    private static InteractionResult releaseAttempt(
            GameTestHelper helper,
            Player player,
            ItemStack container,
            BlockPos clickedPos) {
        helper.setBlock(clickedPos, Blocks.STONE);
        BlockPos absolute = helper.absolutePos(clickedPos);
        player.setItemInHand(InteractionHand.MAIN_HAND, container);
        BlockHitResult hit = new BlockHitResult(
                Vec3.atCenterOf(absolute),
                Direction.UP,
                absolute,
                false);
        return ModItems.MOB_CONTAINER.get().useOn(
                new UseOnContext(player, InteractionHand.MAIN_HAND, hit));
    }
}
