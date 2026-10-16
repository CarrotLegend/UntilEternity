package com.carrot123.until_eternity.trial;

import com.carrot123.until_eternity.block.entity.ModBlockEntities;
import com.carrot123.until_eternity.until_eternity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.BossEvent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public final class TrialMonumentBlockEntity extends BlockEntity {
    public static final String MOB_RUN = "UntilEternityTrialRun";
    public static final String MOB_MONUMENT = "UntilEternityTrialMonument";
    public enum Phase { IDLE, PREPARE, ACTIVE, INTERMISSION, FAILED, COMPLETE }

    private UUID owner;
    private UUID run;
    private Phase phase = Phase.IDLE;
    private int waveIndex, ticksLeft, waveLimit;
    private TrialWaves.Config snapshot;
    private final Set<UUID> mobs = new HashSet<>();
    private final List<ItemStack> rewards = new ArrayList<>();
    private ServerBossEvent bar;
    private boolean checkOldRun;

    public TrialMonumentBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.TRIAL_MONUMENT.get(), pos, state);
    }

    public void place(ServerPlayer player) {
        owner = player.getUUID();
        setChanged();
        start(player);
    }

    public void interact(ServerPlayer player) {
        if (!player.getUUID().equals(owner)) {
            player.displayClientMessage(Component.translatable("message.until_eternity.trial.not_owner"), true);
            return;
        }
        if (!rewards.isEmpty()) { claim(player); return; }
        if (phase == Phase.IDLE || phase == Phase.FAILED || phase == Phase.COMPLETE) start(player);
    }

    private void start(ServerPlayer player) {
        if (!(level instanceof ServerLevel server) || !server.dimension().equals(TrialRealm.KEY)) return;
        TrialWaves.Config config = TrialWaves.current();
        if (config == null || config.waves().isEmpty()) {
            player.displayClientMessage(Component.translatable("message.until_eternity.trial.no_waves"), true);
            return;
        }
        if (!TrialRunEvents.reserve(server, worldPosition)) {
            player.displayClientMessage(Component.translatable("message.until_eternity.trial.too_close"), true);
            return;
        }
        snapshot = config;
        run = UUID.randomUUID();
        waveIndex = 0;
        phase = Phase.PREPARE;
        ticksLeft = 60;
        waveLimit = 60;
        mobs.clear();
        bar = new ServerBossEvent(Component.translatable("title.until_eternity.trial.start"), BossEvent.BossBarColor.PURPLE,
                BossEvent.BossBarOverlay.PROGRESS);
        title(player, "title.until_eternity.trial.start");
        setChanged();
    }

    public static void tick(Level level, BlockPos pos, BlockState state, TrialMonumentBlockEntity monument) {
        if (level instanceof ServerLevel server) monument.serverTick(server);
    }

    private void serverTick(ServerLevel server) {
        if (checkOldRun) {
            checkOldRun = false;
            if (running()) { fail(server, "restart"); return; }
        }
        if (!running()) return;
        ServerPlayer player = server.getServer().getPlayerList().getPlayer(owner);
        if (player == null || player.isDeadOrDying() || player.level() != server
                || player.distanceToSqr(Vec3.atCenterOf(worldPosition)) > 48 * 48) {
            fail(server, "owner absent"); return;
        }
        if (phase == Phase.ACTIVE) {
            for (UUID id : mobs) {
                Entity entity = server.getEntity(id);
                if (entity == null || entity.isRemoved() || !entity.isAlive()
                        || entity.distanceToSqr(Vec3.atCenterOf(worldPosition)) > 64 * 64
                        || !entity.getPersistentData().hasUUID(MOB_RUN)
                        || !entity.getPersistentData().getUUID(MOB_RUN).equals(run)) {
                    fail(server, "tracked mob missing"); return;
                }
            }
        }
        updateBar(server);
        if (--ticksLeft > 0) { if (server.getGameTime() % 20 == 0) setChanged(); return; }
        if (phase == Phase.PREPARE || phase == Phase.INTERMISSION) spawnWave(server);
        else if (phase == Phase.ACTIVE) fail(server, "timeout");
    }

    private void spawnWave(ServerLevel server) {
        TrialWaves.Wave wave = snapshot.waves().get(waveIndex);
        mobs.clear();
        for (TrialWaves.MobSpec spec : wave.mobs()) for (int i = 0; i < spec.count(); i++) {
            Mob mob = null;
            Entity created = spec.type().create(server);
            if (created instanceof Mob m) mob = m;
            if (mob == null || !spawnOne(server, mob)) {
                if (mob != null) mob.discard();
                fail(server, "spawn failure"); return;
            }
        }
        phase = Phase.ACTIVE;
        ticksLeft = wave.timeTicks();
        waveLimit = ticksLeft;
        setChanged();
    }

    private boolean spawnOne(ServerLevel server, Mob mob) {
        for (int attempt = 0; attempt < 32; attempt++) {
            double angle = server.random.nextDouble() * Math.PI * 2;
            double radius = 7 + server.random.nextDouble() * 9;
            BlockPos horizontal = BlockPos.containing(worldPosition.getX() + 0.5 + Math.cos(angle) * radius,
                    worldPosition.getY(), worldPosition.getZ() + 0.5 + Math.sin(angle) * radius);
            BlockPos feet = server.getHeightmapPos(net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                    horizontal);
            if (!server.getWorldBorder().isWithinBounds(feet)
                    || Math.abs(feet.getY() - worldPosition.getY()) > 12
                    || !server.getBlockState(feet.below()).isFaceSturdy(server, feet.below(), Direction.UP)) continue;
            mob.moveTo(feet.getX() + 0.5, feet.getY(), feet.getZ() + 0.5, server.random.nextFloat() * 360, 0);
            if (!server.noCollision(mob, mob.getBoundingBox()) || !server.getEntities(mob, mob.getBoundingBox()).isEmpty()) continue;
            mob.getPersistentData().putUUID(MOB_RUN, run);
            mob.getPersistentData().putLong(MOB_MONUMENT, worldPosition.asLong());
            if (!server.addFreshEntity(mob)) return false;
            mobs.add(mob.getUUID());
            server.sendParticles(ParticleTypes.REVERSE_PORTAL, mob.getX(), mob.getY() + 1, mob.getZ(),
                    20, 0.5, 0.7, 0.5, 0.05);
            server.playSound(null, feet, SoundEvents.ENDERMAN_TELEPORT, SoundSource.HOSTILE, 0.8f, 0.8f);
            return true;
        }
        return false;
    }

    public void onMobDeath(UUID mobId, UUID runId) {
        if (phase != Phase.ACTIVE || !runId.equals(run) || !mobs.remove(mobId)) return;
        if (mobs.isEmpty() && level instanceof ServerLevel server) {
            TrialWaves.Wave wave = snapshot.waves().get(waveIndex);
            rewards.add(new ItemStack(wave.reward(), wave.rewardCount()));
            waveIndex++;
            if (waveIndex >= snapshot.waves().size()) finish(server);
            else { phase = Phase.INTERMISSION; ticksLeft = 100; waveLimit = 100; }
        }
        setChanged();
    }

    private void finish(ServerLevel server) {
        ServerPlayer player = server.getServer().getPlayerList().getPlayer(owner);
        if (player != null) title(player, "title.until_eternity.trial.complete");
        server.playSound(null, worldPosition, SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundSource.BLOCKS, 1, 1);
        server.sendParticles(ParticleTypes.TOTEM_OF_UNDYING, worldPosition.getX() + 0.5, worldPosition.getY() + 1,
                worldPosition.getZ() + 0.5, 70, 1, 1, 1, 0.1);
        closeRun(server);
        phase = Phase.COMPLETE;
        setChanged();
    }

    public void fail(ServerLevel server, String reason) {
        if (!running()) return;
        until_eternity.LOGGER.info("Trial at {} failed: {}", worldPosition, reason);
        ServerPlayer player = server.getServer().getPlayerList().getPlayer(owner);
        if (player != null) title(player, "title.until_eternity.trial.failed");
        server.playSound(null, worldPosition, SoundEvents.WITHER_DEATH, SoundSource.BLOCKS, 0.6f, 0.8f);
        cleanupMobs(server);
        closeRun(server);
        phase = Phase.FAILED;
        waveIndex = ticksLeft = waveLimit = 0;
        setChanged();
    }

    private void cleanupMobs(ServerLevel server) {
        for (UUID id : mobs) {
            Entity entity = server.getEntity(id);
            if (entity != null && run != null && entity.getPersistentData().hasUUID(MOB_RUN)
                    && run.equals(entity.getPersistentData().getUUID(MOB_RUN))) entity.discard();
        }
        mobs.clear();
    }

    private void closeRun(ServerLevel server) {
        TrialRunEvents.release(server, worldPosition);
        if (bar != null) { bar.removeAllPlayers(); bar = null; }
        run = null;
        snapshot = null;
    }

    private void claim(ServerPlayer player) {
        List<ItemStack> simulated = new ArrayList<>();
        for (ItemStack slot : player.getInventory().items) simulated.add(slot.copy());
        for (ItemStack reward : rewards) {
            int left = reward.getCount();
            for (ItemStack slot : simulated) {
                if (!slot.isEmpty() && ItemStack.isSameItemSameTags(slot, reward)) {
                    int moved = Math.min(left, slot.getMaxStackSize() - slot.getCount());
                    slot.grow(moved);
                    left -= moved;
                }
            }
            for (int i = 0; i < simulated.size() && left > 0; i++) {
                if (simulated.get(i).isEmpty()) {
                    int moved = Math.min(left, reward.getMaxStackSize());
                    ItemStack placed = reward.copy();
                    placed.setCount(moved);
                    simulated.set(i, placed);
                    left -= moved;
                }
            }
            if (left > 0) {
                player.displayClientMessage(Component.translatable("message.until_eternity.trial.inventory_full"), true);
                return;
            }
        }
        for (ItemStack reward : rewards) player.getInventory().add(reward.copy());
        rewards.clear();
        setChanged();
    }

    private void updateBar(ServerLevel server) {
        if (bar == null) bar = new ServerBossEvent(Component.empty(), BossEvent.BossBarColor.PURPLE, BossEvent.BossBarOverlay.PROGRESS);
        bar.setName(Component.translatable("bossbar.until_eternity.trial", waveIndex + 1, snapshot.waves().size(),
                mobs.size(), Math.max(0, (ticksLeft + 19) / 20)));
        bar.setProgress(waveLimit == 0 ? 1f : Math.max(0f, Math.min(1f, (float) ticksLeft / waveLimit)));
        for (ServerPlayer player : server.players()) {
            boolean show = player.getUUID().equals(owner) || player.distanceToSqr(Vec3.atCenterOf(worldPosition)) <= 48 * 48;
            if (show) bar.addPlayer(player); else bar.removePlayer(player);
        }
    }

    public void destroy() {
        if (!(level instanceof ServerLevel server)) return;
        if (running()) fail(server, "monument broken");
        for (ItemStack reward : rewards) net.minecraft.world.Containers.dropItemStack(server,
                worldPosition.getX() + 0.5, worldPosition.getY() + 0.5, worldPosition.getZ() + 0.5, reward);
        rewards.clear();
    }

    private boolean running() { return phase == Phase.PREPARE || phase == Phase.ACTIVE || phase == Phase.INTERMISSION; }
    public boolean hasRun(UUID runId) { return running() && run != null && run.equals(runId); }
    public UUID owner() { return owner; }
    private static void title(ServerPlayer player, String key) {
        player.connection.send(new ClientboundSetTitleTextPacket(Component.translatable(key)));
    }

    @Override public void onChunkUnloaded() {
        if (level instanceof ServerLevel server && running()) fail(server, "chunk unloaded");
        super.onChunkUnloaded();
    }
    @Override protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        if (owner != null) tag.putUUID("Owner", owner);
        if (run != null) tag.putUUID("Run", run);
        tag.putString("Phase", phase.name());
        tag.putInt("Wave", waveIndex);
        tag.putInt("TicksLeft", ticksLeft);
        tag.putInt("WaveLimit", waveLimit);
        ListTag tracked = new ListTag();
        for (UUID id : mobs) { CompoundTag entry = new CompoundTag(); entry.putUUID("Id", id); tracked.add(entry); }
        tag.put("Mobs", tracked);
        ListTag pending = new ListTag();
        for (ItemStack reward : rewards) pending.add(reward.save(new CompoundTag()));
        tag.put("Rewards", pending);
    }
    @Override public void load(CompoundTag tag) {
        super.load(tag);
        owner = tag.hasUUID("Owner") ? tag.getUUID("Owner") : null;
        run = tag.hasUUID("Run") ? tag.getUUID("Run") : null;
        try { phase = Phase.valueOf(tag.getString("Phase")); }
        catch (IllegalArgumentException ex) { phase = Phase.IDLE; }
        waveIndex = tag.getInt("Wave");
        ticksLeft = tag.getInt("TicksLeft");
        waveLimit = tag.getInt("WaveLimit");
        mobs.clear();
        for (Tag entry : tag.getList("Mobs", Tag.TAG_COMPOUND)) if (((CompoundTag) entry).hasUUID("Id")) mobs.add(((CompoundTag) entry).getUUID("Id"));
        rewards.clear();
        for (Tag entry : tag.getList("Rewards", Tag.TAG_COMPOUND)) rewards.add(ItemStack.of((CompoundTag) entry));
        checkOldRun = running();
    }
}
