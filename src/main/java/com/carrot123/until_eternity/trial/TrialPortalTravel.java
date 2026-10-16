package com.carrot123.until_eternity.trial;

import com.carrot123.until_eternity.block.ModBlocks;
import com.carrot123.until_eternity.item.ModItems;
import com.carrot123.until_eternity.until_eternity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

@Mod.EventBusSubscriber(modid = until_eternity.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class TrialPortalTravel {
    private static final String KEY = "UntilEternityTrial";
    private static final Map<UUID, Contact> CONTACTS = new HashMap<>();
    private static final Map<UUID, Long> EXIT_REQUIRED = new HashMap<>();
    private TrialPortalTravel() {}

    public static void touch(Level level, Entity entity) {
        if (!(level instanceof ServerLevel source) || !(entity instanceof ServerPlayer player)
                || entity.isPassenger() || entity.isVehicle() || !entity.canChangeDimensions()) return;
        if (EXIT_REQUIRED.containsKey(player.getUUID())) {
            EXIT_REQUIRED.put(player.getUUID(), source.getGameTime());
            return;
        }
        if (entity.isOnPortalCooldown()) { CONTACTS.remove(entity.getUUID()); return; }
        long now = source.getGameTime();
        Contact contact = CONTACTS.computeIfAbsent(player.getUUID(), id -> new Contact(source.dimension()));
        if (!contact.dimension.equals(source.dimension()) || contact.lastTouched < now - 1) {
            contact = new Contact(source.dimension());
            CONTACTS.put(player.getUUID(), contact);
        }
        if (contact.lastCounted != now) { contact.ticks++; contact.lastCounted = now; }
        contact.lastTouched = now;
        if (contact.ticks < Math.max(20, entity.getPortalWaitTime())) return;
        CONTACTS.remove(player.getUUID());
        travel(player, source);
    }

    private static void travel(ServerPlayer player, ServerLevel source) {
        boolean outbound = source.dimension().equals(TrialRealm.KEY);
        MinecraftServer server = source.getServer();
        CompoundTag data = persisted(player);
        ServerLevel destination;
        Vec3 target;
        float yaw = player.getYRot(), pitch = player.getXRot();
        if (outbound) {
            ResourceLocation id = ResourceLocation.tryParse(data.getString("ReturnDimension"));
            ResourceKey<Level> key = id == null ? Level.OVERWORLD : ResourceKey.create(Registries.DIMENSION, id);
            destination = server.getLevel(key);
            if (destination == null) destination = server.overworld();
            target = data.contains("ReturnX", Tag.TAG_DOUBLE)
                    ? new Vec3(data.getDouble("ReturnX"), data.getDouble("ReturnY"), data.getDouble("ReturnZ"))
                    : Vec3.atBottomCenterOf(destination.getSharedSpawnPos());
            yaw = data.contains("ReturnYaw") ? data.getFloat("ReturnYaw") : yaw;
            pitch = data.contains("ReturnPitch") ? data.getFloat("ReturnPitch") : pitch;
            if (!safe(destination, target)) {
                destination = server.overworld();
                target = safeSpawn(destination);
            }
        } else {
            destination = server.getLevel(TrialRealm.KEY);
            if (destination == null) return;
            target = Vec3.atBottomCenterOf(TrialRealm.ENTRY);
            if (!safe(destination, target)) return;
            data.putString("ReturnDimension", source.dimension().location().toString());
            data.putDouble("ReturnX", player.getX());
            data.putDouble("ReturnY", player.getY());
            data.putDouble("ReturnZ", player.getZ());
            data.putFloat("ReturnYaw", yaw);
            data.putFloat("ReturnPitch", pitch);
            ensureReturnPortal(destination);
        }
        player.setPortalCooldown();
        Entity result = player.changeDimension(destination, new FixedTeleporter(target, yaw, pitch));
        if (result instanceof ServerPlayer moved) {
            moved.setPortalCooldown();
            EXIT_REQUIRED.put(moved.getUUID(), destination.getGameTime());
            if (outbound) clearReturn(persisted(moved));
            else grantMonument(moved);
        }
    }

    private static void clearReturn(CompoundTag data) {
        for (String key : new String[]{"ReturnDimension", "ReturnX", "ReturnY", "ReturnZ", "ReturnYaw", "ReturnPitch"}) data.remove(key);
    }

    private static boolean safe(ServerLevel level, Vec3 target) {
        BlockPos feet = BlockPos.containing(target);
        return level.getWorldBorder().isWithinBounds(feet) && feet.getY() > level.getMinBuildHeight()
                && level.getBlockState(feet.below()).isFaceSturdy(level, feet.below(), Direction.UP)
                && level.getBlockState(feet).getCollisionShape(level, feet).isEmpty()
                && level.getBlockState(feet.above()).getCollisionShape(level, feet.above()).isEmpty()
                && level.noCollision(new AABB(target.x - 0.3, target.y, target.z - 0.3,
                        target.x + 0.3, target.y + 1.8, target.z + 0.3));
    }

    private static Vec3 safeSpawn(ServerLevel level) {
        BlockPos spawn = level.getSharedSpawnPos();
        for (int dy = 0; dy <= 16; dy++) {
            Vec3 pos = Vec3.atBottomCenterOf(spawn.above(dy));
            if (safe(level, pos)) return pos;
        }
        return Vec3.atBottomCenterOf(level.getHeightmapPos(net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, spawn));
    }

    private static void ensureReturnPortal(ServerLevel level) {
        BlockPos first = TrialRealm.RETURN_PORTAL;
        BlockState state = ModBlocks.TRIAL_PORTAL.get().defaultBlockState().setValue(TrialPortalBlock.AXIS, Direction.Axis.Z);
        for (int x = 0; x < 2; x++) for (int y = 0; y < 3; y++) {
            BlockPos pos = first.south(x).above(y);
            if (level.getBlockState(pos).isAir()) {
                level.setBlock(pos, state, 3);
                if (level.getBlockEntity(pos) instanceof TrialPortalBlockEntity portal) portal.setExpiresAt(-1);
            }
        }
        level.playSound(null, first, SoundEvents.PORTAL_TRIGGER, SoundSource.BLOCKS, 0.6f, 1.0f);
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

    @SubscribeEvent public static void onClone(PlayerEvent.Clone event) {
        CompoundTag old = event.getOriginal().getPersistentData().getCompound(Player.PERSISTED_NBT_TAG);
        if (old.contains(KEY, Tag.TAG_COMPOUND)) persisted(event.getEntity()).merge(old.getCompound(KEY).copy());
    }
    @SubscribeEvent public static void onChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.getEntity() instanceof ServerPlayer player && event.getTo().equals(TrialRealm.KEY)
                && player.level() instanceof ServerLevel level) {
            ensureReturnPortal(level);
            grantMonument(player);
        }
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
    }
    @SubscribeEvent public static void onLogout(PlayerLoggedOutEvent event) {
        CONTACTS.remove(event.getEntity().getUUID());
        EXIT_REQUIRED.remove(event.getEntity().getUUID());
    }
    @SubscribeEvent public static void onStop(ServerStoppedEvent event) { CONTACTS.clear(); EXIT_REQUIRED.clear(); }

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
