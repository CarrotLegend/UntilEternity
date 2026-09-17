package com.carrot123.until_eternity.compat.enigmaticdelicacy;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.SaplingBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.registries.ForgeRegistries;

public final class InfinimealGrowthCompat {
    public static final ResourceLocation ENIGMATIC_BUSH =
            new ResourceLocation("enigmaticdelicacy", "enigmatic_bush");
    public static final ResourceLocation ASTRAL_SAPLING =
            new ResourceLocation("enigmaticdelicacy", "astral_sapling");

    private InfinimealGrowthCompat() {
    }

    public static boolean tryGrow(Level level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        Block block = state.getBlock();
        ResourceLocation blockId = ForgeRegistries.BLOCKS.getKey(block);

        if (ENIGMATIC_BUSH.equals(blockId) && block instanceof CropBlock crop) {
            if (crop.isMaxAge(state)) {
                return false;
            }
            if (!level.isClientSide) {
                crop.growCrops(level, pos, state);
            }
            return true;
        }

        if (ASTRAL_SAPLING.equals(blockId) && block instanceof SaplingBlock sapling) {
            if (level instanceof ServerLevel serverLevel) {
                sapling.advanceTree(serverLevel, pos, state, serverLevel.random);
            }
            return true;
        }

        return false;
    }
}
