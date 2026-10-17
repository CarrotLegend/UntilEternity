package com.carrot123.until_eternity.event;

import com.carrot123.until_eternity.entity.StormPriestessEntity;
import com.carrot123.until_eternity.registry.ModEntities;
import com.carrot123.until_eternity.until_eternity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

@Mod.EventBusSubscriber(modid = until_eternity.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class StormPriestessSummonEvents {
    private static final ResourceLocation GODDESS_STATUE_ID =
            new ResourceLocation("cataclysm", "goddess_statue");
    private static final ResourceLocation LACRIMA_ID =
            new ResourceLocation("cataclysm", "lacrima");

    @SubscribeEvent
    public static void onRightClickStatue(PlayerInteractEvent.RightClickBlock event) {
        Player player = event.getEntity();
        if (player.isSpectator()) {
            return;
        }

        Item lacrima = ForgeRegistries.ITEMS.getValue(LACRIMA_ID);
        ItemStack heldStack = player.getItemInHand(event.getHand());
        if (lacrima == null || !heldStack.is(lacrima)) {
            return;
        }

        Level level = player.level();
        BlockPos clickedPos = event.getPos();
        Block statueBlock = ForgeRegistries.BLOCKS.getValue(GODDESS_STATUE_ID);
        BlockState clickedState = level.getBlockState(clickedPos);
        if (statueBlock == null || !clickedState.is(statueBlock)) {
            return;
        }

        // 阻止原方块与物品继续处理本次交互，客户端只反馈成功，不修改实体或背包。
        event.setCancellationResult(InteractionResult.sidedSuccess(level.isClientSide));
        event.setCanceled(true);
        if (level.isClientSide || !(player instanceof ServerPlayer serverPlayer)
                || !(level instanceof ServerLevel serverLevel)) {
            return;
        }

        // 灾变女神雕像是双格方块，统一用下半部分的坐标识别召唤来源。
        BlockPos statuePos = clickedPos;
        if (clickedState.hasProperty(BlockStateProperties.DOUBLE_BLOCK_HALF)
                && clickedState.getValue(BlockStateProperties.DOUBLE_BLOCK_HALF) == DoubleBlockHalf.UPPER) {
            statuePos = clickedPos.below();
        }
        BlockState baseState = serverLevel.getBlockState(statuePos);
        if (!baseState.is(statueBlock)) {
            return;
        }

        BlockPos sourcePos = statuePos;
        boolean alreadySummoned = !serverLevel.getEntitiesOfClass(StormPriestessEntity.class,
                new AABB(statuePos).inflate(8.0D),
                priestess -> priestess.isAlive() && priestess.isSummonedFrom(sourcePos)).isEmpty();
        if (alreadySummoned) {
            serverPlayer.displayClientMessage(
                    Component.translatable("message.until_eternity.priestess.already_summoned"), true);
            return;
        }

        Direction facing = baseState.hasProperty(HorizontalDirectionalBlock.FACING)
                ? baseState.getValue(HorizontalDirectionalBlock.FACING) : Direction.NORTH;
        Direction[] directions = {
                facing, facing.getClockWise(), facing.getCounterClockWise(), facing.getOpposite()
        };

        // 优先在雕像朝向的一侧寻找地面和足够的实体空间，失败不扣材料。
        for (int radius = 1; radius <= 2; radius++) {
            for (Direction direction : directions) {
                BlockPos spawnPos = statuePos.relative(direction, radius);
                if (!isSafeSpawnPos(serverLevel, spawnPos)) {
                    continue;
                }

                StormPriestessEntity priestess = ModEntities.STORM_PRIESTESS.get().create(serverLevel);
                if (priestess == null) {
                    return;
                }
                priestess.bindToStatue(statuePos);
                priestess.moveTo(spawnPos.getX() + 0.5D, spawnPos.getY(), spawnPos.getZ() + 0.5D,
                        direction.getOpposite().toYRot(), 0.0F);
                if (!serverLevel.noCollision(priestess)) {
                    continue;
                }

                if (serverLevel.addFreshEntity(priestess)) {
                    if (!serverPlayer.getAbilities().instabuild) {
                        heldStack.shrink(1);
                    }
                    return;
                }
            }
        }

        serverPlayer.displayClientMessage(
                Component.translatable("message.until_eternity.priestess.no_space"), true);
    }

    private static boolean isSafeSpawnPos(ServerLevel level, BlockPos pos) {
        BlockPos floor = pos.below();
        if (!level.isLoaded(floor) || !level.isLoaded(pos) || !level.isLoaded(pos.above())) {
            return false;
        }
        return level.getBlockState(floor).isFaceSturdy(level, floor, Direction.UP)
                && level.getFluidState(pos).isEmpty()
                && level.getFluidState(pos.above()).isEmpty()
                && level.getBlockState(pos).getCollisionShape(level, pos).isEmpty()
                && level.getBlockState(pos.above()).getCollisionShape(level, pos.above()).isEmpty();
    }
}
