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

import java.util.Locale;

public final class InfinimealGrowthCompat {
    public static final ResourceLocation ENIGMATIC_BUSH =
            new ResourceLocation("enigmaticdelicacy", "enigmatic_bush");

    public static final ResourceLocation ASTRAL_SAPLING =
            new ResourceLocation("enigmaticdelicacy", "astral_sapling");

    public static final ResourceLocation ASTRAL_LEAVES =
            new ResourceLocation("enigmaticdelicacy", "astral_leaves");

    public static final ResourceLocation BLOSSOMING_ASTRAL_LEAVES =
            new ResourceLocation("enigmaticdelicacy", "blossoming_astral_leaves");

    private static final int ASTRAL_GROWTH_TICKS = 128;
    private static final int ASTRAL_FRUIT_SEARCH_DEPTH = 16;

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

        if (isAstralGrowthBlock(blockId, block)) {
            if (level.isClientSide) {
                return true;
            }

            if (level instanceof ServerLevel serverLevel) {
                growAstralPlant(serverLevel, pos);
            }

            return true;
        }

        return false;
    }

    private static void growAstralPlant(ServerLevel level, BlockPos startPos) {
        BlockPos currentPos = startPos;

        for (int i = 0; i < ASTRAL_GROWTH_TICKS; i++) {
            BlockPos fruitPos = findLowestAstralFruit(level, startPos);

            if (fruitPos != null) {
                currentPos = fruitPos;
            }

            BlockState state = level.getBlockState(currentPos);
            Block block = state.getBlock();
            ResourceLocation blockId = ForgeRegistries.BLOCKS.getKey(block);

            if (!isAstralGrowthBlock(blockId, block)) {
                return;
            }

            state.randomTick(level, currentPos, level.random);

            BlockPos newFruitPos = findLowestAstralFruit(level, startPos);

            if (newFruitPos != null) {
                currentPos = newFruitPos;
            } else {
                currentPos = startPos;
            }
        }
    }

    private static BlockPos findLowestAstralFruit(Level level, BlockPos origin) {
        BlockPos found = null;

        for (int offset = 0; offset <= ASTRAL_FRUIT_SEARCH_DEPTH; offset++) {
            BlockPos checkPos = origin.below(offset);
            BlockState state = level.getBlockState(checkPos);
            Block block = state.getBlock();
            ResourceLocation id = ForgeRegistries.BLOCKS.getKey(block);

            if (isAstralFruitBlock(id, block)) {
                found = checkPos;
            } else if (offset > 0 && found != null) {
                break;
            }
        }

        return found;
    }

    private static boolean isAstralGrowthBlock(ResourceLocation id, Block block) {
        if (id == null || !"enigmaticdelicacy".equals(id.getNamespace())) {
            return false;
        }

        if (ASTRAL_LEAVES.equals(id) || BLOSSOMING_ASTRAL_LEAVES.equals(id)) {
            return true;
        }

        return isAstralFruitBlock(id, block);
    }

    private static boolean isAstralFruitBlock(ResourceLocation id, Block block) {
        if (id == null || !"enigmaticdelicacy".equals(id.getNamespace())) {
            return false;
        }

        String path = id.getPath().toLowerCase(Locale.ROOT);
        String className = block.getClass().getSimpleName().toLowerCase(Locale.ROOT);

        boolean fruitLike =
                path.contains("fruit")
                        || path.contains("flower")
                        || className.contains("fruit")
                        || className.contains("flower");

        boolean astralLike =
                path.contains("astral")
                        || path.contains("celestial")
                        || path.contains("hanging")
                        || className.contains("astral")
                        || className.contains("celestial")
                        || className.contains("hanging");

        return fruitLike && astralLike;
    }
}