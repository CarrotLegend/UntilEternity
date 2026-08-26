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
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.animal.allay.Allay;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Cow;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.entity.monster.warden.Warden;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.Skeleton;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.entity.npc.WanderingTrader;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
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
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
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
        ServerPlayer player = helper.makeMockServerPlayerInLevel();

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
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
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

        boolean result = releaseAttempt(
                helper, player, container, new BlockPos(3, 1, 1));
        helper.assertTrue(!result,
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
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        ItemStack container = new ItemStack(ModItems.MOB_CONTAINER.get());
        player.setItemInHand(InteractionHand.MAIN_HAND, container);
        player.setShiftKeyDown(true);

        Warden warden = helper.spawn(
                EntityType.WARDEN, new BlockPos(1, 2, 1));
        boolean wardenResult = ModItems.MOB_CONTAINER.get().tryCapture(
                player, InteractionHand.MAIN_HAND, warden);
        helper.assertTrue(!wardenResult
                        && !warden.isRemoved()
                        && !MobContainerItem.hasStoredEntity(container),
                "Warden must always be rejected without mutation");

        Cow mount = helper.spawn(EntityType.COW, new BlockPos(2, 2, 2));
        Cow rider = helper.spawn(EntityType.COW, new BlockPos(2, 2, 2));
        helper.assertTrue(rider.startRiding(mount, true),
                "The riding safety fixture must be established");
        boolean riderResult = ModItems.MOB_CONTAINER.get().tryCapture(
                player, InteractionHand.MAIN_HAND, rider);
        helper.assertTrue(!riderResult
                        && !rider.isRemoved()
                        && !MobContainerItem.hasStoredEntity(container),
                "Riding mobs must be rejected without changing the container");
        helper.succeed();
    }

    @GameTest(template = "statue", timeoutTicks = 100)
    public static void entityEventsPreemptSpecificAndGeneralInteractions(
            GameTestHelper helper) {
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        player.setShiftKeyDown(true);

        WanderingTrader trader = helper.spawn(
                EntityType.WANDERING_TRADER, new BlockPos(1, 2, 1));
        ItemStack traderContainer = new ItemStack(
                ModItems.MOB_CONTAINER.get());
        player.setItemInHand(InteractionHand.MAIN_HAND, traderContainer);
        PlayerInteractEvent.EntityInteractSpecific specific =
                new PlayerInteractEvent.EntityInteractSpecific(
                        player,
                        InteractionHand.MAIN_HAND,
                        trader,
                        Vec3.ZERO);
        MinecraftForge.EVENT_BUS.post(specific);
        helper.assertTrue(specific.isCanceled()
                        && specific.getCancellationResult().consumesAction()
                        && trader.isRemoved()
                        && MobContainerItem.hasStoredEntity(traderContainer),
                "Specific entity interaction must be canceled before trading");

        Villager villager = helper.spawn(
                EntityType.VILLAGER, new BlockPos(1, 2, 3));
        ItemStack villagerContainer = new ItemStack(
                ModItems.MOB_CONTAINER.get());
        player.setItemInHand(InteractionHand.MAIN_HAND, villagerContainer);
        PlayerInteractEvent.EntityInteract general =
                new PlayerInteractEvent.EntityInteract(
                        player,
                        InteractionHand.MAIN_HAND,
                        villager);
        MinecraftForge.EVENT_BUS.post(general);
        helper.assertTrue(general.isCanceled()
                        && villager.isRemoved()
                        && MobContainerItem.hasStoredEntity(villagerContainer),
                "General entity interaction must be canceled before trading");

        Allay allay = helper.spawn(
                EntityType.ALLAY, new BlockPos(1, 2, 5));
        allay.setItemInHand(
                InteractionHand.MAIN_HAND,
                new ItemStack(Items.DIAMOND));
        ItemStack allayContainer = new ItemStack(
                ModItems.MOB_CONTAINER.get());
        player.setItemInHand(InteractionHand.MAIN_HAND, allayContainer);
        PlayerInteractEvent.EntityInteract allayEvent =
                new PlayerInteractEvent.EntityInteract(
                        player,
                        InteractionHand.MAIN_HAND,
                        allay);
        MinecraftForge.EVENT_BUS.post(allayEvent);
        helper.assertTrue(allayEvent.isCanceled()
                        && allay.isRemoved()
                        && MobContainerItem.hasStoredEntity(allayContainer),
                "Allay item interaction must be preempted by capture");

        Wolf wolf = helper.spawn(
                EntityType.WOLF, new BlockPos(1, 2, 5));
        wolf.setOrderedToSit(true);
        ItemStack wolfContainer = new ItemStack(
                ModItems.MOB_CONTAINER.get());
        player.setItemInHand(InteractionHand.MAIN_HAND, wolfContainer);
        PlayerInteractEvent.EntityInteract wolfEvent =
                new PlayerInteractEvent.EntityInteract(
                        player,
                        InteractionHand.MAIN_HAND,
                        wolf);
        MinecraftForge.EVENT_BUS.post(wolfEvent);
        helper.assertTrue(wolfEvent.isCanceled()
                        && wolf.isRemoved()
                        && MobContainerItem.hasStoredEntity(wolfContainer)
                        && wolfContainer.getTagElement(
                                MobContainerItem.TAG_STORED_ENTITY)
                                .getBoolean("Sitting"),
                "Wolf interaction must be preempted without toggling sitting state");
        helper.succeed();
    }

    @GameTest(template = "statue", timeoutTicks = 100)
    public static void failedCaptureStillCancelsTheEntityInteraction(
            GameTestHelper helper) {
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        player.setShiftKeyDown(true);
        ItemStack container = new ItemStack(ModItems.MOB_CONTAINER.get());
        player.setItemInHand(InteractionHand.MAIN_HAND, container);
        Warden warden = helper.spawn(
                EntityType.WARDEN, new BlockPos(1, 2, 1));

        PlayerInteractEvent.EntityInteract event =
                new PlayerInteractEvent.EntityInteract(
                        player,
                        InteractionHand.MAIN_HAND,
                        warden);
        MinecraftForge.EVENT_BUS.post(event);
        helper.assertTrue(event.isCanceled()
                        && event.getCancellationResult().consumesAction()
                        && !warden.isRemoved()
                        && !MobContainerItem.hasStoredEntity(container),
                "Forbidden targets must remain while their own interaction stays canceled");
        helper.succeed();
    }

    @GameTest(template = "statue", timeoutTicks = 100)
    public static void survivalAndCreativeShareHostileReleasePath(
            GameTestHelper helper) {
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        player.setGameMode(GameType.SURVIVAL);

        Zombie zombie = helper.spawn(
                EntityType.ZOMBIE, new BlockPos(1, 2, 1));
        zombie.setItemSlot(
                EquipmentSlot.MAINHAND,
                new ItemStack(Items.DIAMOND_SWORD));
        UUID zombieId = zombie.getUUID();
        ItemStack container = capture(helper, player, zombie);
        release(helper, player, container, new BlockPos(3, 1, 1));
        Zombie restoredZombie = (Zombie) helper.getLevel().getEntity(zombieId);
        helper.assertTrue(restoredZombie != null
                        && restoredZombie.getMainHandItem().is(Items.DIAMOND_SWORD),
                "Survival must restore hostile mob equipment");

        Skeleton skeleton = helper.spawn(
                EntityType.SKELETON, new BlockPos(1, 2, 3));
        UUID skeletonId = skeleton.getUUID();
        container = capture(helper, player, skeleton);
        release(helper, player, container, new BlockPos(3, 1, 3));
        helper.assertTrue(helper.getLevel().getEntity(skeletonId) instanceof Skeleton,
                "Survival must restore skeletons");

        player.setGameMode(GameType.CREATIVE);
        Creeper creeper = helper.spawn(
                EntityType.CREEPER, new BlockPos(1, 2, 5));
        UUID creeperId = creeper.getUUID();
        container = capture(helper, player, creeper);
        helper.assertTrue(player.getMainHandItem() == container
                        && MobContainerItem.hasStoredEntity(
                                player.getMainHandItem()),
                "Creative capture must mutate the real held stack");
        release(helper, player, container, new BlockPos(3, 1, 5));
        helper.assertTrue(helper.getLevel().getEntity(creeperId) instanceof Creeper,
                "Creative must use the same hostile release path");
        helper.succeed();
    }

    @GameTest(template = "statue", timeoutTicks = 100)
    public static void filledContainerPreemptsBlocksWhileEmptyContainerPasses(
            GameTestHelper helper) {
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        Cow cow = helper.spawn(EntityType.COW, new BlockPos(1, 2, 1));
        UUID cowId = cow.getUUID();
        ItemStack container = capture(helper, player, cow);
        BlockPos clicked = new BlockPos(3, 1, 1);
        helper.setBlock(clicked, Blocks.CHEST);
        BlockPos absolute = helper.absolutePos(clicked);
        BlockHitResult hit = new BlockHitResult(
                Vec3.atCenterOf(absolute), Direction.UP, absolute, false);

        PlayerInteractEvent.RightClickBlock filled =
                new PlayerInteractEvent.RightClickBlock(
                        player,
                        InteractionHand.MAIN_HAND,
                        absolute,
                        hit);
        MinecraftForge.EVENT_BUS.post(filled);
        helper.assertTrue(filled.isCanceled()
                        && helper.getLevel().getEntity(cowId) instanceof Cow
                        && !MobContainerItem.hasStoredEntity(container),
                "A filled container must release before the chest can open");

        ItemStack empty = new ItemStack(ModItems.MOB_CONTAINER.get());
        player.setItemInHand(InteractionHand.MAIN_HAND, empty);
        PlayerInteractEvent.RightClickBlock emptyEvent =
                new PlayerInteractEvent.RightClickBlock(
                        player,
                        InteractionHand.MAIN_HAND,
                        absolute,
                        hit);
        MinecraftForge.EVENT_BUS.post(emptyEvent);
        helper.assertTrue(!emptyEvent.isCanceled(),
                "An empty container must not intercept block use");
        helper.succeed();
    }

    private static ItemStack capture(
            GameTestHelper helper,
            ServerPlayer player,
            LivingEntity target) {
        ItemStack container = new ItemStack(ModItems.MOB_CONTAINER.get());
        player.setItemInHand(InteractionHand.MAIN_HAND, container);
        player.setShiftKeyDown(true);
        boolean result = ModItems.MOB_CONTAINER.get().tryCapture(
                player, InteractionHand.MAIN_HAND, target);
        player.setShiftKeyDown(false);
        helper.assertTrue(result,
                "A valid server-side capture must succeed");
        return container;
    }

    private static void release(
            GameTestHelper helper,
            ServerPlayer player,
            ItemStack container,
            BlockPos clickedPos) {
        boolean result = releaseAttempt(
                helper, player, container, clickedPos);
        helper.assertTrue(result,
                "A valid server-side release must succeed");
    }

    private static boolean releaseAttempt(
            GameTestHelper helper,
            ServerPlayer player,
            ItemStack container,
            BlockPos clickedPos) {
        helper.setBlock(clickedPos, Blocks.STONE);
        BlockPos absolute = helper.absolutePos(clickedPos);
        player.setItemInHand(InteractionHand.MAIN_HAND, container);
        return ModItems.MOB_CONTAINER.get().tryRelease(
                player,
                InteractionHand.MAIN_HAND,
                helper.getLevel(),
                absolute,
                Direction.UP);
    }
}
