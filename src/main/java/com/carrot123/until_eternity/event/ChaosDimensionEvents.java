package com.carrot123.until_eternity.event;

import com.carrot123.until_eternity.block.ModBlocks;
import com.carrot123.until_eternity.until_eternity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.ZombifiedPiglin;
import net.minecraft.world.entity.monster.piglin.Piglin;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.portal.PortalShape;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Optional;

@SuppressWarnings("null")
@Mod.EventBusSubscriber(modid = until_eternity.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ChaosDimensionEvents {

    private static final ResourceKey<Level> CHAOS_REALM =
            ResourceKey.create(net.minecraft.core.registries.Registries.DIMENSION,
                    new ResourceLocation(until_eternity.MODID + ":chaos_realm"));

    
    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        Player player = event.getEntity();
        Level level = event.getLevel();
        BlockPos pos = event.getPos();
        ItemStack held = player.getItemInHand(event.getHand());

        if (!held.is(Items.SPIDER_EYE)) return;

        BlockState clickedState = level.getBlockState(pos);

        String blockName = clickedState.getBlock().getDescriptionId();
        if (!blockName.contains("deepslate") && !clickedState.is(Blocks.DEEPSLATE)) {
            return;
        }

        if (level.isClientSide) return;

        Optional<PortalShape> optional = PortalShape.findEmptyPortalShape(
                level, pos.relative(event.getFace()), Direction.Axis.X);

        if (optional.isEmpty()) {
            optional = PortalShape.findEmptyPortalShape(
                    level, pos.relative(event.getFace()), Direction.Axis.Z);
        }

        if (optional.isPresent()) {
            PortalShape shape = optional.get();
            if (shape.isValid()) {
                shape.createPortalBlocks();

                BlockPos clicked = pos.relative(event.getFace());
                int searchRadius = 25;
                for (BlockPos portalPos : BlockPos.betweenClosed(
                        clicked.offset(-searchRadius, -searchRadius, -searchRadius),
                        clicked.offset(searchRadius, searchRadius, searchRadius))) {
                    if (level.getBlockState(portalPos).is(Blocks.NETHER_PORTAL)) {
                        Direction.Axis axis = level.getBlockState(portalPos)
                                .getValue(net.minecraft.world.level.block.state.properties.BlockStateProperties.HORIZONTAL_AXIS);
                        level.setBlock(portalPos,
                                ModBlocks.CHAOS_PORTAL.get().defaultBlockState()
                                        .setValue(net.minecraft.world.level.block.state.properties.BlockStateProperties.HORIZONTAL_AXIS, axis),
                                3);
                    }
                }

                level.playSound(null, pos, SoundEvents.FLINTANDSTEEL_USE,
                        SoundSource.BLOCKS, 1.0F, level.random.nextFloat() * 0.4F + 0.8F);

                if (!player.isCreative()) {
                    held.shrink(1);
                }

                event.setCanceled(true);
            }
        }
    }

    @SubscribeEvent
    public static void onEntityJoinLevel(EntityJoinLevelEvent event) {
        if (event.getLevel().isClientSide()) return;

        if (event.getEntity() instanceof Piglin piglin) {
            if (event.getLevel().dimension() == CHAOS_REALM) {
                ZombifiedPiglin zombified = piglin.convertTo(EntityType.ZOMBIFIED_PIGLIN, true);
                if (zombified != null) {
                    zombified.setPersistenceRequired();
                }
            }
        }
    }

    @SubscribeEvent
    public static void onLevelTick(TickEvent.LevelTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (event.level.isClientSide()) return;

        if (event.level instanceof ServerLevel serverLevel
                && serverLevel.dimension() == CHAOS_REALM) {

            if (serverLevel.getGameTime() % 100 == 0) {
                for (Piglin piglin : serverLevel.getEntitiesOfClass(Piglin.class,
                        new net.minecraft.world.phys.AABB(
                                -30000000, serverLevel.getMinBuildHeight(), -30000000,
                                30000000, serverLevel.getMaxBuildHeight(), 30000000))) {
                    ZombifiedPiglin zombified = piglin.convertTo(EntityType.ZOMBIFIED_PIGLIN, true);
                    if (zombified != null) {
                        zombified.setPersistenceRequired();
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        BlockState state = event.getState();
        if (state.is(ModBlocks.CHAOS_PORTAL.get())) {
            Player player = event.getPlayer();
            if (player != null && player.isCreative() && player.isShiftKeyDown()) {
                return;
            }
            event.setCanceled(true);
        }
    }

    //传送门被破坏
    @SubscribeEvent
    public static void onNeighborNotify(BlockEvent.NeighborNotifyEvent event) {
        Level level = (Level) event.getLevel();
        if (level.isClientSide()) return;

        BlockPos pos = event.getPos();
        BlockState state = level.getBlockState(pos);

        String blockName = state.getBlock().getDescriptionId();
        if (!blockName.contains("deepslate") && !state.is(Blocks.DEEPSLATE)) return;

        for (Direction dir : Direction.values()) {
            BlockPos neighbor = pos.relative(dir);
            BlockState neighborState = level.getBlockState(neighbor);
            if (neighborState.is(ModBlocks.CHAOS_PORTAL.get())) {
                checkAndRemovePortal(level, neighbor);
            }
        }
    }

    private static void checkAndRemovePortal(Level level, BlockPos portalPos) {
        boolean hasFrame = false;
        for (Direction dir : Direction.values()) {
            BlockPos framePos = portalPos.relative(dir);
            BlockState frameState = level.getBlockState(framePos);
            String name = frameState.getBlock().getDescriptionId();
            if (name.contains("deepslate")) {
                hasFrame = true;
                break;
            }
        }
        if (!hasFrame) {
            level.setBlock(portalPos, Blocks.AIR.defaultBlockState(), 3);
            level.playSound(null, portalPos, SoundEvents.GLASS_BREAK,
                    SoundSource.BLOCKS, 1.0F, 1.0F);
        }
    }
}
