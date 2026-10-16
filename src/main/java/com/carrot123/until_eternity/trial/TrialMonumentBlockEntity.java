package com.carrot123.until_eternity.trial;

import com.carrot123.until_eternity.block.entity.ModBlockEntities;
import com.carrot123.until_eternity.item.ModItems;
import com.carrot123.until_eternity.network.ModNetworking;
import com.carrot123.until_eternity.network.OpenTrialDialogueS2CPacket;
import com.carrot123.until_eternity.compat.goetyrevelation.TrialApollyonIdentity;
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
import net.minecraft.util.Unit;
import net.minecraft.world.BossEvent;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.monster.warden.Warden;
import net.minecraft.world.entity.monster.warden.WardenAi;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.PacketDistributor;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.HashMap;
import java.util.Map;
import java.util.List;

public final class TrialMonumentBlockEntity extends BlockEntity {
    public static final String MOB_RUN = "UntilEternityTrialRun";
    public static final String MOB_MONUMENT = "UntilEternityTrialMonument";
    public enum Phase { IDLE, PREPARE, ACTIVE, INTERMISSION, FAILED, COMPLETE }
    private static final UUID HEALTH_MODIFIER = UUID.fromString("40ac5f79-8e11-4050-a83c-ff3d0a0ea123");

    private UUID owner;
    private UUID run;
    private Phase phase = Phase.IDLE;
    private int waveIndex, ticksLeft, waveLimit;
    private TrialWaves.Config snapshot;
    private final Set<UUID> mobs = new HashSet<>();
    private boolean pendingRock;
    private boolean rewardIssued;
    private TrialDifficulty difficulty = TrialDifficulty.NORMAL;
    private ServerBossEvent bar;
    private boolean checkOldRun;
    private int outsideTicks;
    private final Map<UUID, Integer> missingTicks = new HashMap<>();
    private final Map<UUID, String> mobTrace = new HashMap<>();
    private TrialApollyonHandoff apollyonHandoff;

    public TrialMonumentBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.TRIAL_MONUMENT.get(), pos, state);
    }

    public void place(ServerPlayer player) {
        owner = player.getUUID();
        phase = Phase.IDLE;
        setChanged();
    }

    public void interact(ServerPlayer player) {
        if (!(level instanceof ServerLevel server) || !server.dimension().equals(TrialRealm.KEY)
                || player.level() != server || player.distanceToSqr(Vec3.atCenterOf(worldPosition)) > 64) return;
        if (!player.getUUID().equals(owner)) {
            player.displayClientMessage(Component.translatable("message.until_eternity.trial.not_owner"), true);
            return;
        }
        if (canClaim(phase, pendingRock, rewardIssued)) { deliverRock(player); return; }
        if (running()) {
            player.displayClientMessage(Component.translatable("message.until_eternity.trial.already_running",
                    Component.translatable("gui.until_eternity.trial.difficulty."
                            + difficulty.name().toLowerCase(java.util.Locale.ROOT))), true);
            return;
        }
        if (canSelect(phase, pendingRock, rewardIssued))
            ModNetworking.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new OpenTrialDialogueS2CPacket(worldPosition));
    }

    public void selectDifficulty(ServerPlayer player, TrialDifficulty selected) {
        if (!(level instanceof ServerLevel server) || !server.dimension().equals(TrialRealm.KEY)
                || !server.hasChunkAt(worldPosition) || server.getBlockEntity(worldPosition) != this
                || selected == null || owner == null || !owner.equals(player.getUUID())
                || player.level() != server || player.distanceToSqr(Vec3.atCenterOf(worldPosition)) > 64
                || !canSelect(phase, pendingRock, rewardIssued)) return;
        start(player, selected);
    }

    private void start(ServerPlayer player, TrialDifficulty selected) {
        if (!(level instanceof ServerLevel server) || !server.dimension().equals(TrialRealm.KEY)) return;
        if (server.getDifficulty() == Difficulty.PEACEFUL) {
            player.displayClientMessage(Component.translatable("message.until_eternity.trial.peaceful"), true);
            return;
        }
        TrialWaves.Config config = TrialWaves.current();
        if (config == null || config.waves().isEmpty()) {
            player.displayClientMessage(Component.translatable("message.until_eternity.trial.no_waves"), true);
            return;
        }
        if (!TrialRunEvents.reserve(server, worldPosition)) {
            player.displayClientMessage(Component.translatable("message.until_eternity.trial.too_close"), true);
            return;
        }
        difficulty = selected;
        snapshot = config;
        run = UUID.randomUUID();
        waveIndex = 0;
        phase = Phase.PREPARE;
        ticksLeft = config.waves().get(0).prepareTicks();
        waveLimit = ticksLeft;
        rewardIssued = false;
        mobs.clear();
        missingTicks.clear();
        mobTrace.clear();
        apollyonHandoff = null;
        outsideTicks = 0;
        bar = new ServerBossEvent(Component.translatable("title.until_eternity.trial.start"), BossEvent.BossBarColor.PURPLE,
                BossEvent.BossBarOverlay.PROGRESS);
        title(player, "title.until_eternity.trial.start");
        setChanged();
        if (ticksLeft == 0) spawnWave(server);
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
        if (server.getDifficulty() == Difficulty.PEACEFUL) { fail(server, "difficulty changed to peaceful"); return; }
        ServerPlayer player = server.getServer().getPlayerList().getPlayer(owner);
        if (player == null) { fail(server, "owner offline"); return; }
        if (player.isDeadOrDying()) { fail(server, "owner died"); return; }
        if (player.level() != server) { fail(server, "owner left dimension"); return; }
        if (player.distanceToSqr(Vec3.atCenterOf(worldPosition)) > 48 * 48) {
            if (++outsideTicks >= 40) { fail(server, "owner outside 48 blocks for 40 ticks"); return; }
        } else outsideTicks = 0;
        if (phase == Phase.ACTIVE) {
            if (!processApollyonHandoff(server)) return;
            for (UUID id : mobs) {
                if (apollyonHandoff != null && id.equals(apollyonHandoff.summonId)) continue;
                Entity entity = server.getEntity(id);
                if (entity == null) {
                    int missing = missingTicks.merge(id, 1, Integer::sum);
                    if (missing >= 20) { fail(server, "tracked mob absent uuid=" + id + " trace=" + mobTrace.get(id)
                            + " after=" + missing + " ticks wave=" + waveIndex); return; }
                    continue;
                }
                missingTicks.remove(id);
                if (entity.isRemoved() || !entity.isAlive()
                        || entity.distanceToSqr(Vec3.atCenterOf(worldPosition)) > 64 * 64
                        || !entity.getPersistentData().hasUUID(MOB_RUN)
                        || !entity.getPersistentData().getUUID(MOB_RUN).equals(run)) {
                    fail(server, "tracked mob invalid uuid=" + id + " type=" + entity.getType() + " pos=" + entity.blockPosition()
                            + " removed=" + entity.isRemoved() + " reason=" + entity.getRemovalReason()
                            + " alive=" + entity.isAlive() + " runTag=" + entity.getPersistentData().getString(MOB_RUN)); return;
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
            Entity created;
            try { created = spec.type().create(server); }
            catch (RuntimeException ex) {
                until_eternity.LOGGER.error("Trial mob factory failed: type={} wave={}", spec.type(), waveIndex, ex);
                fail(server, "mob factory exception type=" + spec.type());
                return;
            }
            boolean special = TrialApollyonSpawnContext.SUMMON_ID.equals(
                    net.minecraft.core.registries.BuiltInRegistries.ENTITY_TYPE.getKey(spec.type()));
            if (special && (spec.count() != 1 || apollyonHandoff != null)) {
                if (created != null) created.discard();
                fail(server, "Apollyon summon must occur exactly once in a wave");
                return;
            }
            if (created == null || (!(created instanceof Mob) && !(special
                    && TrialApollyonSpawnContext.isSummonApollyon(created))) || !spawnOne(server, created, special)) {
                if (created != null) created.discard();
                fail(server, "spawn failure entity=" + spec.type() + " index=" + i + " wave=" + waveIndex); return;
            }
        }
        phase = Phase.ACTIVE;
        ticksLeft = wave.timeTicks();
        waveLimit = ticksLeft;
        setChanged();
    }

    private boolean spawnOne(ServerLevel server, Entity entity, boolean apollyonSummon) {
        int invalidGround = 0, invalidBorder = 0, collisions = 0;
        for (int attempt = 0; attempt < 32; attempt++) {
            double angle = server.random.nextDouble() * Math.PI * 2;
            double radius = 7 + server.random.nextDouble() * 9;
            BlockPos horizontal = BlockPos.containing(worldPosition.getX() + 0.5 + Math.cos(angle) * radius,
                    worldPosition.getY(), worldPosition.getZ() + 0.5 + Math.sin(angle) * radius);
            BlockPos feet = server.getHeightmapPos(net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                    horizontal);
            if (!server.getWorldBorder().isWithinBounds(feet)) { invalidBorder++; continue; }
            if (Math.abs(feet.getY() - worldPosition.getY()) > 12
                    || !server.getBlockState(feet.below()).isFaceSturdy(server, feet.below(), Direction.UP)) {
                invalidGround++; continue;
            }
            entity.moveTo(feet.getX() + 0.5, feet.getY(), feet.getZ() + 0.5, server.random.nextFloat() * 360, 0);
            if (!server.noCollision(entity, entity.getBoundingBox())
                    || !server.getEntities(entity, entity.getBoundingBox()).isEmpty()) {
                collisions++; continue;
            }
            entity.getPersistentData().putUUID(MOB_RUN, run);
            entity.getPersistentData().putLong(MOB_MONUMENT, worldPosition.asLong());
            mobs.add(entity.getUUID());
            mobTrace.put(entity.getUUID(), "type=" + entity.getType() + " spawn=" + feet + " chunkLoaded=" + server.hasChunkAt(feet));
            if (apollyonSummon) apollyonHandoff = new TrialApollyonHandoff(
                    entity.getUUID(), run, worldPosition, waveIndex, entity.position());
            if (!server.addFreshEntity(entity)) {
                mobs.remove(entity.getUUID());
                mobTrace.remove(entity.getUUID());
                if (apollyonSummon) apollyonHandoff = null;
                until_eternity.LOGGER.warn("Trial mob insertion rejected: type={} uuid={} pos={}", entity.getType(), entity.getUUID(), feet);
                return false;
            }
            if (apollyonSummon) {
                until_eternity.LOGGER.info("Trial Apollyon summon created: summon={} run={} monument={} wave={} pos={}",
                        entity.getUUID(), run, worldPosition, waveIndex, entity.position());
            } else if (entity instanceof Mob mob) {
                if (mob instanceof Warden warden) {
                    warden.getBrain().setMemoryWithExpiry(MemoryModuleType.DIG_COOLDOWN, Unit.INSTANCE, Long.MAX_VALUE);
                    warden.setPose(Pose.EMERGING);
                    warden.getBrain().setMemoryWithExpiry(MemoryModuleType.IS_EMERGING, Unit.INSTANCE, WardenAi.EMERGE_DURATION);
                }
                if (!applyDifficultyHealth(mob)) return false;
            }

            server.sendParticles(ParticleTypes.REVERSE_PORTAL, entity.getX(), entity.getY() + 1, entity.getZ(),
                    20, 0.5, 0.7, 0.5, 0.05);
            server.playSound(null, feet, SoundEvents.ENDERMAN_TELEPORT, SoundSource.HOSTILE, 0.8f, 0.8f);
            return true;
        }
        until_eternity.LOGGER.warn("Trial spawn exhausted: type={} attempts=32 border={} ground={} collision={} monument={}",
                entity.getType(), invalidBorder, invalidGround, collisions, worldPosition);
        return false;
    }

    private boolean applyDifficultyHealth(Mob mob) {
        AttributeInstance health = mob.getAttribute(Attributes.MAX_HEALTH);
        if (health == null) return false;
        if (health.getModifier(HEALTH_MODIFIER) == null)
            health.addTransientModifier(new AttributeModifier(HEALTH_MODIFIER, "Trial difficulty health",
                    difficulty.healthMultiplier() - 1.0, AttributeModifier.Operation.MULTIPLY_TOTAL));
        mob.setHealth(mob.getMaxHealth());
        return true;
    }

    public boolean awaitingApollyon(Entity source) {
        return phase == Phase.ACTIVE && run != null && apollyonHandoff != null
                && source.level() == level && mobs.contains(source.getUUID())
                && source.getPersistentData().hasUUID(MOB_RUN)
                && run.equals(source.getPersistentData().getUUID(MOB_RUN))
                && apollyonHandoff.matchesSource(source.getUUID(), run, worldPosition, waveIndex);
    }

    public boolean offerApollyonCandidate(Entity source, Mob candidate, boolean isApollyon) {
        if (!awaitingApollyon(source) || !(level instanceof ServerLevel server)
                || candidate.level() != server || candidate.isRemoved()) return false;
        if (!apollyonHandoff.offer(source.getUUID(), run, worldPosition, waveIndex,
                candidate.getUUID(), candidate.position(), isApollyon, source.tickCount, server.getGameTime())) return false;
        candidate.getPersistentData().putUUID(MOB_RUN, run);
        candidate.getPersistentData().putLong(MOB_MONUMENT, worldPosition.asLong());
        until_eternity.LOGGER.info("Trial Apollyon candidate joined: summon={} boss={} run={} monument={}",
                source.getUUID(), candidate.getUUID(), run, worldPosition);
        setChanged();
        return true;
    }

    private boolean processApollyonHandoff(ServerLevel server) {
        TrialApollyonHandoff handoff = apollyonHandoff;
        if (handoff == null) return true;
        if (handoff.stage == TrialApollyonHandoff.Stage.SUMMONING) {
            Entity source = server.getEntity(handoff.summonId);
            if (source == null || source.isRemoved()) {
                int missing = missingTicks.merge(handoff.summonId, 1, Integer::sum);
                if (missing >= 20) {
                    fail(server, "Apollyon summon vanished before conversion uuid=" + handoff.summonId
                            + " missingTicks=" + missing + " trace=" + mobTrace.get(handoff.summonId));
                    return false;
                }
            } else {
                missingTicks.remove(handoff.summonId);
                if (!source.isAlive() || source.distanceToSqr(handoff.expectedPos) > 4.0D
                        || !TrialApollyonSpawnContext.isSummonApollyon(source)) {
                    fail(server, "Apollyon summon invalid uuid=" + handoff.summonId + " pos=" + source.position()
                            + " removed=" + source.isRemoved() + " reason=" + source.getRemovalReason());
                    return false;
                }
                if (source.tickCount > 470) {
                    fail(server, "Apollyon summon passed tick 450 without a valid boss uuid=" + handoff.summonId
                            + " age=" + source.tickCount);
                    return false;
                }
            }
            return true;
        }
        Entity candidate = server.getEntity(handoff.candidateId);
        if (candidate == null) {
            if (server.getGameTime() - handoff.candidateSince >= 20) {
                fail(server, "Apollyon candidate never entered world uuid=" + handoff.candidateId
                        + " summon=" + handoff.summonId);
                return false;
            }
            return true;
        }
        if (!(candidate instanceof Mob boss) || candidate.isRemoved() || !candidate.isAlive()
                || !TrialApollyonIdentity.isApollyon(candidate)
                || !handoff.isCandidate(candidate.getUUID())
                || !candidate.getPersistentData().hasUUID(MOB_RUN)
                || !handoff.runId.equals(candidate.getPersistentData().getUUID(MOB_RUN))
                || !candidate.getPersistentData().contains(MOB_MONUMENT)
                || candidate.getPersistentData().getLong(MOB_MONUMENT) != worldPosition.asLong()
                || candidate.distanceToSqr(handoff.expectedPos) > 4.0D) {
            fail(server, "Apollyon candidate invalid uuid=" + handoff.candidateId + " pos=" + candidate.position()
                    + " removed=" + candidate.isRemoved());
            return false;
        }
        if (!applyDifficultyHealth(boss)) {
            fail(server, "Apollyon boss lacks MAX_HEALTH uuid=" + boss.getUUID());
            return false;
        }
        mobs.remove(handoff.summonId);
        mobs.add(boss.getUUID());
        missingTicks.remove(handoff.summonId);
        mobTrace.remove(handoff.summonId);
        mobTrace.put(boss.getUUID(), "Apollyon from summon=" + handoff.summonId + " pos=" + boss.position());
        apollyonHandoff = null;
        until_eternity.LOGGER.info("Trial Apollyon handoff complete: summon={} boss={} run={} monument={} difficulty={}",
                handoff.summonId, boss.getUUID(), handoff.runId, worldPosition, difficulty);
        setChanged();
        return true;
    }

    public void onMobDeath(UUID mobId, UUID runId) {
        if (phase != Phase.ACTIVE || !runId.equals(run) || !mobs.remove(mobId)) return;
        if (level instanceof ServerLevel server) {
            if (snapshot == null) {
                fail(server, "tracked mob died before a restored run was invalidated");
                return;
            }
            ServerPlayer player = server.getServer().getPlayerList().getPlayer(owner);
            if (player == null || player.isDeadOrDying() || player.level() != server) {
                fail(server, "owner absent at mob death");
                return;
            }
        }
        missingTicks.remove(mobId);
        mobTrace.remove(mobId);
        if (mobs.isEmpty() && level instanceof ServerLevel server) {
            waveIndex++;
            if (waveIndex >= snapshot.waves().size()) finish(server);
            else {
                ServerPlayer player = server.getServer().getPlayerList().getPlayer(owner);
                if (player != null) player.displayClientMessage(
                        Component.translatable("message.until_eternity.trial.wave_complete", waveIndex), true);
                phase = Phase.PREPARE;
                ticksLeft = snapshot.waves().get(waveIndex).prepareTicks();
                waveLimit = ticksLeft;
                if (ticksLeft == 0) spawnWave(server);
            }
        }
        setChanged();
    }

    private void finish(ServerLevel server) {
        if (!canComplete(phase, pendingRock, rewardIssued)) return;
        phase = Phase.COMPLETE;
        pendingRock = true;
        setChanged();
        ServerPlayer player = server.getServer().getPlayerList().getPlayer(owner);
        if (player != null) title(player, "title.until_eternity.trial.complete");
        server.playSound(null, worldPosition, SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundSource.BLOCKS, 1, 1);
        server.sendParticles(ParticleTypes.TOTEM_OF_UNDYING, worldPosition.getX() + 0.5, worldPosition.getY() + 1,
                worldPosition.getZ() + 0.5, 70, 1, 1, 1, 0.1);
        closeRun(server);
        if (player != null) player.displayClientMessage(
                Component.translatable("message.until_eternity.trial.claim_reward"), false);
        setChanged();
    }

    static boolean canComplete(Phase phase, boolean pendingRock, boolean rewardIssued) {
        return phase == Phase.ACTIVE && !pendingRock && !rewardIssued;
    }

    static boolean canClaim(Phase phase, boolean pendingRock, boolean rewardIssued) {
        return phase == Phase.COMPLETE && pendingRock && !rewardIssued;
    }

    static boolean canSelect(Phase phase, boolean pendingRock, boolean rewardIssued) {
        return !pendingRock && (phase == Phase.IDLE || phase == Phase.FAILED
                || phase == Phase.COMPLETE && rewardIssued);
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
        Set<UUID> cleanup = new HashSet<>(mobs);
        if (apollyonHandoff != null) cleanup.addAll(apollyonHandoff.cleanupIds());
        for (UUID id : cleanup) {
            Entity entity = server.getEntity(id);
            if (entity != null && run != null && entity.getPersistentData().hasUUID(MOB_RUN)
                    && run.equals(entity.getPersistentData().getUUID(MOB_RUN))) entity.discard();
        }
        mobs.clear();
        missingTicks.clear();
        mobTrace.clear();
    }

    private void closeRun(ServerLevel server) {
        TrialRunEvents.release(server, worldPosition);
        if (bar != null) { bar.removeAllPlayers(); bar = null; }
        run = null;
        snapshot = null;
        apollyonHandoff = null;
    }

    private void deliverRock(ServerPlayer player) {
        if (!canClaim(phase, pendingRock, rewardIssued) || !player.getUUID().equals(owner)) return;
        Inventory inventory = player.getInventory();
        int count = difficulty.rewardCount();
        List<Integer> freeSlots = TrialRewardSlots.find(inventory.items.size(),
                slot -> inventory.items.get(slot).isEmpty(), count);
        if (freeSlots.isEmpty()) {
            player.displayClientMessage(Component.translatable("message.until_eternity.trial.inventory_full"), true);
            return;
        }
        for (int slot : freeSlots) inventory.setItem(slot, new ItemStack(ModItems.ROCK.get()));
        inventory.setChanged();
        player.inventoryMenu.broadcastChanges();
        if (player.containerMenu != player.inventoryMenu) player.containerMenu.broadcastChanges();
        pendingRock = false;
        rewardIssued = true;
        setChanged();
        player.displayClientMessage(Component.translatable("message.until_eternity.trial.reward_claimed", count), true);
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
        pendingRock = false;
        rewardIssued = true;
    }

    private boolean running() { return phase == Phase.PREPARE || phase == Phase.ACTIVE || phase == Phase.INTERMISSION; }
    public boolean hasRun(UUID runId) { return running() && run != null && run.equals(runId); }
    public boolean tracksDamageSource(UUID mobId, UUID runId) {
        return phase == Phase.ACTIVE && run != null && run.equals(runId) && mobs.contains(mobId);
    }
    public TrialDifficulty difficulty() { return difficulty; }
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
        tag.putBoolean("PendingRock", pendingRock);
        tag.putBoolean("RewardIssued", rewardIssued);
        tag.putString("TrialDifficulty", difficulty.name());
        if (apollyonHandoff != null) tag.put("ApollyonHandoff", apollyonHandoff.save());
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
        pendingRock = tag.getBoolean("PendingRock");
        rewardIssued = tag.getBoolean("RewardIssued");
        if (rewardIssued) pendingRock = false;
        difficulty = TrialDifficulty.fromSaved(tag.getString("TrialDifficulty"));
        apollyonHandoff = tag.contains("ApollyonHandoff", Tag.TAG_COMPOUND)
                ? TrialApollyonHandoff.load(tag.getCompound("ApollyonHandoff")) : null;
        checkOldRun = level == null && running();
    }
}
