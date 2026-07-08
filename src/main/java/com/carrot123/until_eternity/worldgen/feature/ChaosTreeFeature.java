package com.carrot123.until_eternity.worldgen.feature;

import com.mojang.serialization.Codec;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

/**
 * 枯萎深色橡树 — 深色橡木树干，分支，无树叶
 */
public class ChaosTreeFeature extends Feature<NoneFeatureConfiguration> {

    private static final BlockState LOG = Blocks.DARK_OAK_LOG.defaultBlockState();

    public ChaosTreeFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    @SuppressWarnings("null")
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        BlockPos origin = context.origin();
        RandomSource random = context.random();

        BlockPos ground = origin.below();
        if (!level.getBlockState(ground).is(Blocks.GRASS_BLOCK)
                && !level.getBlockState(ground).is(Blocks.DIRT)) {
            return false;
        }

        int trunkHeight = 4 + random.nextInt(4); 
        int branchCount = 2 + random.nextInt(3); 

        // 树干
        for (int y = 0; y < trunkHeight; y++) {
            BlockPos pos = origin.above(y);
            if (!canPlaceAt(level, pos)) return false;
            level.setBlock(pos, LOG, 3);
        }

        // 分支
        BlockPos top = origin.above(trunkHeight - 1);
        for (int b = 0; b < branchCount; b++) {
            Direction dir = Direction.Plane.HORIZONTAL.getRandomDirection(random);
            int branchLength = 1 + random.nextInt(3); // 1-3 long

            BlockPos branchStart = top.relative(dir);
            if (canPlaceAt(level, branchStart)) {
                level.setBlock(branchStart, LOG, 3);
            }
            BlockPos current = branchStart;
            for (int i = 1; i < branchLength; i++) {
                BlockPos next;
                if (random.nextBoolean()) {
                    next = current.relative(dir);
                } else {
                    next = current.above();
                }
                if (canPlaceAt(level, next)) {
                    level.setBlock(next, LOG, 3);
                    current = next;
                } else {
                    break;
                }
            }
        }

        return true;
    }

    @SuppressWarnings("null")
    private boolean canPlaceAt(WorldGenLevel level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        return state.isAir() || state.canBeReplaced();
    }
}
