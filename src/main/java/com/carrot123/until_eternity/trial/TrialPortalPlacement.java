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
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedOutEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Mod.EventBusSubscriber(modid = until_eternity.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class TrialPortalPlacement {
    private static final Map<UUID, Long> LAST_USE = new HashMap<>();
    private TrialPortalPlacement() {}

    @SubscribeEvent public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        if (!event.getItemStack().is(ModItems.TRIAL_KEY.get())) return;
        boolean success = event.getLevel().isClientSide || create(event.getLevel(), event.getEntity(), event.getHand());
        event.setCancellationResult(success ? InteractionResult.SUCCESS : InteractionResult.FAIL);
        event.setCanceled(true);
    }

    public static boolean create(Level level, Player player, InteractionHand hand) {
        if (!(level instanceof ServerLevel server) || !(player instanceof ServerPlayer serverPlayer)
                || !(level.dimension().equals(Level.OVERWORLD) || level.dimension().equals(TrialRealm.KEY))) return false;
        long now = server.getGameTime();
        Long previous = LAST_USE.get(player.getUUID());
        if (previous != null && previous == now) return true;
        Direction facing = player.getDirection();
        Direction along = facing.getClockWise();
        Direction.Axis axis = along.getAxis();
        for (int distance = 2; distance <= 5; distance++) {
            BlockPos first = player.blockPosition().relative(facing, distance).relative(along.getOpposite(), 2);
            if (!canPlace(server, first, axis)) continue;
            BlockState state = ModBlocks.TRIAL_PORTAL.get().defaultBlockState().setValue(TrialPortalBlock.AXIS, axis);
            long expiry = server.getGameTime() + 2400;
            UUID id = UUID.randomUUID();
            for (int x = 0; x < TrialPortalGeometry.WIDTH; x++) for (int y = 0; y < TrialPortalGeometry.HEIGHT; y++) {
                BlockPos cell = TrialPortalGeometry.cell(first, axis, x, y);
                server.setBlock(cell, state, 3);
                if (server.getBlockEntity(cell) instanceof TrialPortalBlockEntity portal) portal.configure(id, first, expiry);
                server.sendParticles(ParticleTypes.PORTAL, cell.getX() + 0.5, cell.getY() + 0.5, cell.getZ() + 0.5,
                        8, 0.25, 0.25, 0.25, 0.02);
            }
            server.playSound(null, first, SoundEvents.PORTAL_TRIGGER, SoundSource.BLOCKS, 0.8f, 1.0f);
            LAST_USE.put(player.getUUID(), now);
            return true;
        }
        serverPlayer.displayClientMessage(Component.translatable("message.until_eternity.trial.portal_no_space"), true);
        return false;
    }

    @SubscribeEvent public static void onLogout(PlayerLoggedOutEvent event) { LAST_USE.remove(event.getEntity().getUUID()); }
    @SubscribeEvent public static void onStop(ServerStoppedEvent event) { LAST_USE.clear(); }

    public static void remove(ServerLevel level, BlockPos touched, TrialPortalBlockEntity portal) {
        BlockPos anchor = portal.anchor();
        UUID id = portal.portalId();
        if (anchor == null) anchor = legacyAnchor(level, touched, portal);
        if (anchor == null) { level.setBlock(touched, Blocks.AIR.defaultBlockState(), 3); return; }
        Direction.Axis axis = portal.getBlockState().getValue(TrialPortalBlock.AXIS);
        int width = portal.shapeVersion() == 2 ? TrialPortalGeometry.WIDTH : 2;
        int height = portal.shapeVersion() == 2 ? TrialPortalGeometry.HEIGHT : 3;
        for (int x = 0; x < width; x++) for (int y = 0; y < height; y++) {
            BlockPos cell = TrialPortalGeometry.cell(anchor, axis, x, y);
            if (level.getBlockEntity(cell) instanceof TrialPortalBlockEntity part
                    && part.shapeVersion() == portal.shapeVersion()
                    && (id != null ? id.equals(part.portalId()) : part.portalId() == null
                    && part.expiresAt() == portal.expiresAt())) {
                level.setBlock(cell, Blocks.AIR.defaultBlockState(), 3);
                level.sendParticles(ParticleTypes.REVERSE_PORTAL, cell.getX() + .5, cell.getY() + .5,
                        cell.getZ() + .5, 18, .3, .4, .3, .08);
            }
        }
        level.playSound(null, anchor, SoundEvents.ENDERMAN_TELEPORT, SoundSource.BLOCKS, .8f, .6f);
    }

    private static BlockPos legacyAnchor(ServerLevel level, BlockPos touched, TrialPortalBlockEntity portal) {
        Direction along = portal.getBlockState().getValue(TrialPortalBlock.AXIS) == Direction.Axis.X
                ? Direction.EAST : Direction.SOUTH;
        for (int dx = 0; dx <= 1; dx++) for (int dy = 0; dy <= 2; dy++) {
            BlockPos candidate = touched.relative(along.getOpposite(), dx).below(dy);
            boolean valid = true;
            for (int x = 0; x < 2 && valid; x++) for (int y = 0; y < 3; y++) {
                BlockPos cell = candidate.relative(along, x).above(y);
                if (!(level.getBlockEntity(cell) instanceof TrialPortalBlockEntity part)
                        || part.portalId() != null || part.expiresAt() != portal.expiresAt()) { valid = false; break; }
            }
            if (valid) return candidate;
        }
        return null;
    }

    private static boolean canPlace(ServerLevel level, BlockPos first, Direction.Axis axis) {
        for (int x = 0; x < TrialPortalGeometry.WIDTH; x++) {
            BlockPos base = TrialPortalGeometry.cell(first, axis, x, 0);
            if (!level.getBlockState(base.below()).isFaceSturdy(level, base.below(), Direction.UP)) return false;
            for (int y = 0; y < TrialPortalGeometry.HEIGHT; y++) {
                BlockPos cell = base.above(y);
                if (!level.getWorldBorder().isWithinBounds(cell) || !level.getBlockState(cell).canBeReplaced()) return false;
                if (!level.getEntities(null, new AABB(cell)).isEmpty()) return false;
            }
        }
        return true;
    }
}
