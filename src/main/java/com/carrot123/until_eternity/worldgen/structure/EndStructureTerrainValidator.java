package com.carrot123.until_eternity.worldgen.structure;

import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.NoiseColumn;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;

final class EndStructureTerrainValidator {
    private static final ResourceLocation MINING_POOL =
            new ResourceLocation("until_eternity", "end_mining_center/start_pool");

    private EndStructureTerrainValidator() {
    }

    static boolean hasStableEndTerrain(Structure.GenerationContext context,
                                       BoundingBox footprint,
                                       Holder<StructureTemplatePool> startPool) {
        boolean miningCenter = startPool.unwrapKey()
                .map(key -> key.location().equals(MINING_POOL)).orElse(false);
        int depth = miningCenter ? 8 : 7;
        int minimumStone = miningCenter ? 6 : 5;
        int maximumHeightDifference = miningCenter ? 8 : 10;
        int radiusX = (footprint.maxX() - footprint.minX() + 2) / 2 + 2;
        int radiusZ = (footprint.maxZ() - footprint.minZ() + 2) / 2 + 2;
        int centerX = (footprint.minX() + footprint.maxX()) / 2;
        int centerZ = (footprint.minZ() + footprint.maxZ()) / 2;
        int minimumY = Integer.MAX_VALUE;
        int maximumY = Integer.MIN_VALUE;
        ChunkGenerator generator = context.chunkGenerator();

        for (int dx : new int[]{-radiusX, 0, radiusX}) {
            for (int dz : new int[]{-radiusZ, 0, radiusZ}) {
                int x = centerX + dx;
                int z = centerZ + dz;
                int surfaceY = generator.getFirstOccupiedHeight(x, z, Heightmap.Types.WORLD_SURFACE_WG,
                        context.heightAccessor(), context.randomState());
                if (surfaceY <= context.heightAccessor().getMinBuildHeight() + depth) {
                    return false;
                }

                NoiseColumn column = generator.getBaseColumn(x, z,
                        context.heightAccessor(), context.randomState());
                boolean stoneNearSurface = false;
                int stoneCount = 0;
                for (int offset = 0; offset < depth; offset++) {
                    if (column.getBlock(surfaceY - offset).is(Blocks.END_STONE)) {
                        stoneCount++;
                        if (offset <= 3) {
                            stoneNearSurface = true;
                        }
                    }
                }
                if (!stoneNearSurface || stoneCount < minimumStone) {
                    return false;
                }

                minimumY = Math.min(minimumY, surfaceY);
                maximumY = Math.max(maximumY, surfaceY);
                if (maximumY - minimumY > maximumHeightDifference) {
                    return false;
                }
            }
        }
        return true;
    }
}
