package com.carrot123.until_eternity.trial;

import com.carrot123.until_eternity.block.entity.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public final class TrialPortalBlockEntity extends BlockEntity {
    private long expiresAt = -1;

    public TrialPortalBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.TRIAL_PORTAL.get(), pos, state);
    }

    public void setExpiresAt(long value) {
        expiresAt = value;
        setChanged();
    }

    public static void tick(Level level, BlockPos pos, BlockState state, TrialPortalBlockEntity portal) {
        if (portal.expiresAt >= 0 && level.getGameTime() >= portal.expiresAt) {
            level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
        }
    }

    @Override protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putLong("ExpiresAt", expiresAt);
    }

    @Override public void load(CompoundTag tag) {
        super.load(tag);
        expiresAt = tag.contains("ExpiresAt") ? tag.getLong("ExpiresAt") : -1;
    }
}
