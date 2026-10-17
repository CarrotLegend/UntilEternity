package com.carrot123.until_eternity.trial;

import com.carrot123.until_eternity.item.ModItems;
import com.carrot123.until_eternity.until_eternity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.portal.PortalInfo;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.util.ITeleporter;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedOutEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.Predicate;

@Mod.EventBusSubscriber(modid = until_eternity.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class TrialPortalTravel {
    private static final String KEY = "UntilEternityTrial";
    private static final int SEARCH_RADIUS = 12;
    private static final int SEARCH_VERTICAL = 4;
    private static final int FAILED_RETRY_TICKS = 200;
    private static final Map<UUID, Contact> CONTACTS = new HashMap<>();
    private static final Map<UUID, Long> EXIT_REQUIRED = new HashMap<>();
    private static final Map<UUID, Long> RETRY_AFTER = new HashMap<>();
    private TrialPortalTravel() {}

    public static void touch(Level level, BlockPos portalPos, Entity entity) {
        if (!(level instanceof ServerLevel source) || !(entity instanceof ServerPlayer player)
                || entity.isPassenger() || entity.isVehicle() || !entity.canChangeDimensions()) return;
        if (EXIT_REQUIRED.containsKey(player.getUUID())) {
            EXIT_REQUIRED.put(player.getUUID(), source.getGameTime());
            return;
        }
        if (entity.isOnPortalCooldown()) { CONTACTS.remove(entity.getUUID()); return; }
        if (RETRY_AFTER.getOrDefault(player.getUUID(), Long.MIN_VALUE)
                > source.getServer().overworld().getGameTime()) return;
        long now = source.getGameTime();
        Contact contact = CONTACTS.computeIfAbsent(player.getUUID(), id -> new Contact(source.dimension()));
        if (!contact.dimension.equals(source.dimension()) || contact.lastTouched < now - 1) {
            contact = new Contact(source.dimension());
            CONTACTS.put(player.getUUID(), contact);
        }
        if (contact.lastCounted != now) { contact.ticks++; contact.lastCounted = now; }
        contact.lastTouched = now;
        if (contact.ticks < 2) return;
        CONTACTS.remove(player.getUUID());
        travel(player, source, portalPos);
    }

    private static void travel(ServerPlayer player, ServerLevel source, BlockPos portalPos) {
        if (!(source.getBlockEntity(portalPos) instanceof TrialPortalBlockEntity portal)) return;
        boolean outbound = source.dimension().equals(TrialRealm.KEY);
        if (!outbound && !source.dimension().equals(Level.OVERWORLD)) return;
        MinecraftServer server = source.getServer();
        CompoundTag data = existingData(player);
        CompoundTag previousReturn = data.copy();
        ServerLevel destination;
        Vec3 target;
        Vec3 preferred;
        float yaw = player.getYRot(), pitch = player.getXRot();
        if (outbound) {
            destination = server.overworld();
            boolean recordedOverworld = Level.OVERWORLD.location().toString().equals(data.getString("ReturnDimension"));
            preferred = recordedOverworld && data.contains("ReturnX", Tag.TAG_DOUBLE)
                    && data.contains("ReturnY", Tag.TAG_DOUBLE) && data.contains("ReturnZ", Tag.TAG_DOUBLE)
                    ? new Vec3(data.getDouble("ReturnX"), data.getDouble("ReturnY"), data.getDouble("ReturnZ"))
                    : null;
            if (recordedOverworld) {
                yaw = data.contains("ReturnYaw") ? data.getFloat("ReturnYaw") : yaw;
                pitch = data.contains("ReturnPitch") ? data.getFloat("ReturnPitch") : pitch;
            }
            target = preferred == null ? null : findSafeDestination(destination, player, preferred);
            if (target == null) target = safeSpawn(destination, player);
            if (target == null) {
                failedDestination(player, destination, preferred == null
                        ? Vec3.atBottomCenterOf(destination.getSharedSpawnPos()) : preferred,
                        preferred == null ? "missing_return_and_no_safe_spawn" : "no_safe_return_or_spawn");
                return;
            }
        } else {
            destination = server.getLevel(TrialRealm.KEY);
            if (destination == null) return;
            preferred = Vec3.atBottomCenterOf(TrialRealm.ENTRY);
            target = findSafeDestination(destination, player, preferred);
            if (target == null) {
                failedDestination(player, destination, preferred, "no_safe_entry");
                return;
            }
            data = persisted(player);
            data.putString("ReturnDimension", source.dimension().location().toString());
            data.putDouble("ReturnX", player.getX());
            data.putDouble("ReturnY", player.getY());
            data.putDouble("ReturnZ", player.getZ());
            data.putFloat("ReturnYaw", yaw);
            data.putFloat("ReturnPitch", pitch);
        }
        Entity result = player.changeDimension(destination, new FixedTeleporter(target, yaw, pitch));
        if (result instanceof ServerPlayer moved && moved.serverLevel() == destination) {
            RETRY_AFTER.remove(moved.getUUID());
            moved.setPortalCooldown();
            EXIT_REQUIRED.put(moved.getUUID(), destination.getGameTime());
            if (outbound) clearReturn(persisted(moved));
            else grantMonument(moved);
            TrialPortalPlacement.remove(source, portalPos, portal);
        } else if (!outbound) {
            clearReturn(data);
            data.merge(previousReturn);
        }
    }

    private static void clearReturn(CompoundTag data) {
        for (String key : new String[]{"ReturnDimension", "ReturnX", "ReturnY", "ReturnZ", "ReturnYaw", "ReturnPitch"}) data.remove(key);
    }

    private static void failedDestination(ServerPlayer player, ServerLevel destination, Vec3 preferred, String reason) {
        RETRY_AFTER.put(player.getUUID(), player.server.overworld().getGameTime() + FAILED_RETRY_TICKS);
        player.displayClientMessage(Component.translatable("message.until_eternity.trial.no_safe_destination"), true);
        until_eternity.LOGGER.warn("Trial portal destination search failed: player={} dimension={} preferred={} "
                        + "horizontalRadius={} verticalRange=+/-{} reason={}",
                player.getUUID(), destination.dimension().location(), preferred,
                SEARCH_RADIUS, SEARCH_VERTICAL, reason);
    }

    private static Vec3 findSafeDestination(ServerLevel level, ServerPlayer player, Vec3 preferred) {
        return findNearby(preferred, candidate -> safe(level, player, candidate));
    }

    static Vec3 findNearby(Vec3 preferred, Predicate<Vec3> isSafe) {
        if (!Double.isFinite(preferred.x) || !Double.isFinite(preferred.y) || !Double.isFinite(preferred.z)) return null;
        if (isSafe.test(preferred)) return preferred;

        BlockPos origin = BlockPos.containing(preferred);
        List<BlockPos> offsets = new ArrayList<>();
        for (int dx = -SEARCH_RADIUS; dx <= SEARCH_RADIUS; dx++) {
            for (int dz = -SEARCH_RADIUS; dz <= SEARCH_RADIUS; dz++) {
                if (dx * dx + dz * dz <= SEARCH_RADIUS * SEARCH_RADIUS) offsets.add(new BlockPos(dx, 0, dz));
            }
        }
        offsets.sort(Comparator.comparingDouble((BlockPos offset) -> {
            double x = origin.getX() + offset.getX() + 0.5 - preferred.x;
            double z = origin.getZ() + offset.getZ() + 0.5 - preferred.z;
            return x * x + z * z;
        }).thenComparingInt(BlockPos::getX).thenComparingInt(BlockPos::getZ));

        for (int distance = 0; distance <= SEARCH_VERTICAL; distance++) {
            for (int sign : distance == 0 ? new int[]{0} : new int[]{1, -1}) {
                for (BlockPos offset : offsets) {
                    Vec3 candidate = Vec3.atBottomCenterOf(origin.offset(offset.getX(), distance * sign, offset.getZ()));
                    if (!candidate.equals(preferred) && isSafe.test(candidate)) return candidate;
                }
            }
        }
        return null;
    }

    private static boolean safe(ServerLevel level, ServerPlayer player, Vec3 target) {
        if (!Double.isFinite(target.x) || !Double.isFinite(target.y) || !Double.isFinite(target.z)) return false;
        BlockPos feet = BlockPos.containing(target);
        EntityDimensions dimensions = player.getDimensions(Pose.STANDING);
        AABB box = dimensions.makeBoundingBox(target);
        return feet.getY() > level.getMinBuildHeight()
                && box.maxY <= level.getMaxBuildHeight()
                && level.getWorldBorder().isWithinBounds(box)
                && level.getBlockState(feet.below()).isFaceSturdy(level, feet.below(), Direction.UP)
                && level.getBlockState(feet).getCollisionShape(level, feet).isEmpty()
                && level.getBlockState(feet.above()).getCollisionShape(level, feet.above()).isEmpty()
                && level.noCollision(player, box)
                && clearOfHazards(level, box);
    }

    private static boolean clearOfHazards(ServerLevel level, AABB box) {
        BlockPos min = BlockPos.containing(box.minX + 1.0E-7, box.minY + 1.0E-7, box.minZ + 1.0E-7);
        BlockPos max = BlockPos.containing(box.maxX - 1.0E-7, box.maxY - 1.0E-7, box.maxZ - 1.0E-7);
        for (BlockPos pos : BlockPos.betweenClosed(min, max)) {
            BlockState state = level.getBlockState(pos);
            if (!state.getFluidState().isEmpty() || state.is(Blocks.FIRE) || state.is(Blocks.SOUL_FIRE)) return false;
        }
        return true;
    }

    private static Vec3 safeSpawn(ServerLevel level, ServerPlayer player) {
        BlockPos spawn = level.getSharedSpawnPos();
        for (int radius = 0; radius <= 16; radius++) {
            for (int dx = -radius; dx <= radius; dx++) for (int dz = -radius; dz <= radius; dz++) {
                if (Math.max(Math.abs(dx), Math.abs(dz)) != radius) continue;
                BlockPos near = spawn.offset(dx, 0, dz);
                BlockPos surface = level.getHeightmapPos(net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, near);
                Vec3 pos = Vec3.atBottomCenterOf(surface);
                if (safe(level, player, pos)) return pos;
                for (int dy = 0; dy <= 16; dy++) {
                    pos = Vec3.atBottomCenterOf(near.above(dy));
                    if (safe(level, player, pos)) return pos;
                }
            }
        }
        return null;
    }

    private static void grantMonument(ServerPlayer player) {
        CompoundTag data = persisted(player);
        if (data.getBoolean("MonumentGranted")) return;
        data.putBoolean("MonumentGranted", true);
        ItemStack stack = new ItemStack(ModItems.TRIAL_MONUMENT.get());
        if (!player.getInventory().add(stack)) player.drop(stack, false);
    }

    private static CompoundTag persisted(Player player) {
        CompoundTag root = player.getPersistentData();
        CompoundTag persisted = root.contains(Player.PERSISTED_NBT_TAG, Tag.TAG_COMPOUND)
                ? root.getCompound(Player.PERSISTED_NBT_TAG) : new CompoundTag();
        CompoundTag trial = persisted.contains(KEY, Tag.TAG_COMPOUND) ? persisted.getCompound(KEY) : new CompoundTag();
        persisted.put(KEY, trial);
        root.put(Player.PERSISTED_NBT_TAG, persisted);
        return trial;
    }

    private static CompoundTag existingData(Player player) {
        CompoundTag root = player.getPersistentData();
        if (!root.contains(Player.PERSISTED_NBT_TAG, Tag.TAG_COMPOUND)) return new CompoundTag();
        CompoundTag saved = root.getCompound(Player.PERSISTED_NBT_TAG);
        return saved.contains(KEY, Tag.TAG_COMPOUND) ? saved.getCompound(KEY) : new CompoundTag();
    }

    @SubscribeEvent public static void onClone(PlayerEvent.Clone event) {
        CompoundTag old = event.getOriginal().getPersistentData().getCompound(Player.PERSISTED_NBT_TAG);
        if (old.contains(KEY, Tag.TAG_COMPOUND)) persisted(event.getEntity()).merge(old.getCompound(KEY).copy());
    }
    @SubscribeEvent public static void onChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.getTo().equals(TrialRealm.KEY) && event.getEntity() instanceof ServerPlayer player)
            grantMonument(player);
    }
    @SubscribeEvent public static void onTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        Iterator<Map.Entry<UUID, Contact>> it = CONTACTS.entrySet().iterator();
        while (it.hasNext()) {
            Contact c = it.next().getValue();
            ServerLevel level = event.getServer().getLevel(c.dimension);
            if (level == null || level.getGameTime() > c.lastTouched + 1) it.remove();
        }
        long now = event.getServer().overworld().getGameTime();
        EXIT_REQUIRED.entrySet().removeIf(entry -> event.getServer().getPlayerList().getPlayer(entry.getKey()) == null
                || now > entry.getValue() + 1);
        RETRY_AFTER.entrySet().removeIf(entry -> event.getServer().getPlayerList().getPlayer(entry.getKey()) == null
                || now >= entry.getValue());
    }
    @SubscribeEvent public static void onLogout(PlayerLoggedOutEvent event) {
        CONTACTS.remove(event.getEntity().getUUID());
        EXIT_REQUIRED.remove(event.getEntity().getUUID());
        RETRY_AFTER.remove(event.getEntity().getUUID());
    }
    @SubscribeEvent public static void onStop(ServerStoppedEvent event) {
        CONTACTS.clear(); EXIT_REQUIRED.clear(); RETRY_AFTER.clear();
    }

    private static final class Contact {
        final ResourceKey<Level> dimension;
        int ticks;
        long lastTouched = Long.MIN_VALUE, lastCounted = Long.MIN_VALUE;
        Contact(ResourceKey<Level> dimension) { this.dimension = dimension; }
    }
    private record FixedTeleporter(Vec3 target, float yaw, float pitch) implements ITeleporter {
        @Override public PortalInfo getPortalInfo(Entity entity, ServerLevel destination,
                Function<ServerLevel, PortalInfo> defaultInfo) {
            return new PortalInfo(target, Vec3.ZERO, yaw, pitch);
        }
        @Override public Entity placeEntity(Entity entity, ServerLevel current, ServerLevel destination,
                float yaw, Function<Boolean, Entity> reposition) { return reposition.apply(false); }
    }
}
