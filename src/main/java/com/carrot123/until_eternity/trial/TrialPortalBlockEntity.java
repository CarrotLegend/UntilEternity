package com.carrot123.until_eternity.trial;

import com.carrot123.until_eternity.block.entity.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public final class TrialPortalBlockEntity extends BlockEntity {
    private long expiresAt = -1;
    private UUID portalId;
    private BlockPos anchor;
    private int shapeVersion;

    public TrialPortalBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.TRIAL_PORTAL.get(), pos, state);
    }

    public void setExpiresAt(long value) {
        expiresAt = value;
        setChanged();
    }

    public void configure(UUID id, BlockPos origin, long expiry) {
        portalId = id;
        anchor = origin.immutable();
        expiresAt = expiry;
        shapeVersion = 2;
        setChanged();
        if (level != null) level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
    }

    public UUID portalId() { return portalId; }
    public BlockPos anchor() { return anchor; }
    public long expiresAt() { return expiresAt; }
    public int shapeVersion() { return shapeVersion; }

    public static void tick(Level level, BlockPos pos, BlockState state, TrialPortalBlockEntity portal) {
        if (portal.shapeVersion < 2 || (portal.expiresAt < 0 && level.dimension().equals(TrialRealm.KEY))
                || (portal.expiresAt >= 0 && level.getGameTime() >= portal.expiresAt)) {
            if (level instanceof net.minecraft.server.level.ServerLevel server)
                TrialPortalPlacement.remove(server, pos, portal);
        }
    }

    @Override protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putLong("ExpiresAt", expiresAt);
        tag.putInt("ShapeVersion", shapeVersion);
        if (portalId != null) tag.putUUID("PortalId", portalId);
        if (anchor != null) tag.putLong("Anchor", anchor.asLong());
    }

    @Override public void load(CompoundTag tag) {
        super.load(tag);
        expiresAt = tag.contains("ExpiresAt") ? tag.getLong("ExpiresAt") : -1;
        shapeVersion = tag.getInt("ShapeVersion");
        portalId = tag.hasUUID("PortalId") ? tag.getUUID("PortalId") : null;
        anchor = tag.contains("Anchor") ? BlockPos.of(tag.getLong("Anchor")) : null;
    }

    @Override public CompoundTag getUpdateTag() { return saveWithoutMetadata(); }
    @Nullable @Override public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
    @Override public void onDataPacket(Connection connection, ClientboundBlockEntityDataPacket packet) {
        if (packet.getTag() != null) load(packet.getTag());
    }
}
