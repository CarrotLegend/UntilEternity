package com.carrot123.until_eternity.trial;

import com.carrot123.until_eternity.block.ModBlocks;
import com.carrot123.until_eternity.item.ModItems;
import com.carrot123.until_eternity.until_eternity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = until_eternity.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class TrialPortalPlacement {
    private TrialPortalPlacement() {}

    @SubscribeEvent public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        if (!event.getItemStack().is(ModItems.TRIAL_KEY.get())) return;
        boolean success = event.getLevel().isClientSide || create(event.getLevel(), event.getEntity());
        event.setCancellationResult(success ? InteractionResult.SUCCESS : InteractionResult.FAIL);
        event.setCanceled(true);
    }

    public static boolean create(Level level, Player player) {
        if (!(level instanceof ServerLevel server) || player == null || level.dimension().equals(TrialRealm.KEY)) return false;
        Direction facing = player.getDirection();
        Direction along = facing.getClockWise();
        Direction.Axis axis = along.getAxis();
        for (int distance = 2; distance <= 5; distance++) {
            BlockPos first = player.blockPosition().relative(facing, distance);
            if (!canPlace(server, first, along)) continue;
            BlockState state = ModBlocks.TRIAL_PORTAL.get().defaultBlockState().setValue(TrialPortalBlock.AXIS, axis);
            long expiry = server.getGameTime() + 2400;
            for (int x = 0; x < 2; x++) for (int y = 0; y < 3; y++) {
                BlockPos cell = first.relative(along, x).above(y);
                server.setBlock(cell, state, 3);
                if (server.getBlockEntity(cell) instanceof TrialPortalBlockEntity portal) portal.setExpiresAt(expiry);
                server.sendParticles(ParticleTypes.PORTAL, cell.getX() + 0.5, cell.getY() + 0.5, cell.getZ() + 0.5,
                        8, 0.25, 0.25, 0.25, 0.02);
            }
            server.playSound(null, first, SoundEvents.PORTAL_TRIGGER, SoundSource.BLOCKS, 0.8f, 1.0f);
            return true;
        }
        return false;
    }

    private static boolean canPlace(ServerLevel level, BlockPos first, Direction along) {
        for (int x = 0; x < 2; x++) {
            BlockPos base = first.relative(along, x);
            if (!level.getBlockState(base.below()).isFaceSturdy(level, base.below(), Direction.UP)) return false;
            for (int y = 0; y < 3; y++) {
                BlockPos cell = base.above(y);
                if (!level.getWorldBorder().isWithinBounds(cell) || !level.getBlockState(cell).canBeReplaced()) return false;
                if (!level.getEntities(null, new AABB(cell)).isEmpty()) return false;
            }
        }
        return true;
    }
}
