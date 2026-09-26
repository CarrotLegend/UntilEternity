package com.carrot123.until_eternity.worldgen.structure;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.LegacyRandomSource;
import net.minecraft.world.level.levelgen.WorldGenerationContext;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.heightproviders.HeightProvider;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.TerrainAdjustment;
import net.minecraft.world.level.levelgen.structure.pools.EmptyPoolElement;
import net.minecraft.world.level.levelgen.structure.pools.JigsawPlacement;
import net.minecraft.world.level.levelgen.structure.pools.StructurePoolElement;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;

public final class StableEndJigsawStructure extends Structure {
    public static final Codec<StableEndJigsawStructure> CODEC = ExtraCodecs.validate(
            RecordCodecBuilder.mapCodec(instance -> instance.group(
                    settingsCodec(instance),
                    StructureTemplatePool.CODEC.fieldOf("start_pool").forGetter(structure -> structure.startPool),
                    ResourceLocation.CODEC.optionalFieldOf("start_jigsaw_name").forGetter(structure -> structure.startJigsawName),
                    Codec.intRange(0, 7).fieldOf("size").forGetter(structure -> structure.maxDepth),
                    HeightProvider.CODEC.fieldOf("start_height").forGetter(structure -> structure.startHeight),
                    Codec.BOOL.fieldOf("use_expansion_hack").forGetter(structure -> structure.useExpansionHack),
                    Heightmap.Types.CODEC.optionalFieldOf("project_start_to_heightmap").forGetter(structure -> structure.projectStartToHeightmap),
                    Codec.intRange(1, 128).fieldOf("max_distance_from_center").forGetter(structure -> structure.maxDistanceFromCenter)
            ).apply(instance, StableEndJigsawStructure::new)),
            StableEndJigsawStructure::verifyRange
    ).codec();

    private final Holder<StructureTemplatePool> startPool;
    private final Optional<ResourceLocation> startJigsawName;
    private final int maxDepth;
    private final HeightProvider startHeight;
    private final boolean useExpansionHack;
    private final Optional<Heightmap.Types> projectStartToHeightmap;
    private final int maxDistanceFromCenter;

    private StableEndJigsawStructure(StructureSettings settings,
                                     Holder<StructureTemplatePool> startPool,
                                     Optional<ResourceLocation> startJigsawName,
                                     int maxDepth,
                                     HeightProvider startHeight,
                                     boolean useExpansionHack,
                                     Optional<Heightmap.Types> projectStartToHeightmap,
                                     int maxDistanceFromCenter) {
        super(settings);
        this.startPool = startPool;
        this.startJigsawName = startJigsawName;
        this.maxDepth = maxDepth;
        this.startHeight = startHeight;
        this.useExpansionHack = useExpansionHack;
        this.projectStartToHeightmap = projectStartToHeightmap;
        this.maxDistanceFromCenter = maxDistanceFromCenter;
    }

    private static DataResult<StableEndJigsawStructure> verifyRange(StableEndJigsawStructure structure) {
        int margin = structure.terrainAdaptation() == TerrainAdjustment.NONE ? 0 : 12;
        return structure.maxDistanceFromCenter + margin > 128
                ? DataResult.error(() -> "Structure size including terrain adaptation must not exceed 128")
                : DataResult.success(structure);
    }

    @Override
    protected Optional<GenerationStub> findGenerationPoint(GenerationContext context) {
        ChunkPos chunk = context.chunkPos();
        WorldgenRandom previewRandom = new WorldgenRandom(new LegacyRandomSource(0L));
        previewRandom.setLargeFeatureSeed(context.seed(), chunk.x, chunk.z);
        int previewY = startHeight.sample(previewRandom,
                new WorldGenerationContext(context.chunkGenerator(), context.heightAccessor()));
        Rotation rotation = Rotation.getRandom(previewRandom);
        StructurePoolElement element = startPool.value().getRandomTemplate(previewRandom);
        if (element == EmptyPoolElement.INSTANCE) {
            return Optional.empty();
        }
        BlockPos start = new BlockPos(chunk.getMinBlockX(), previewY, chunk.getMinBlockZ());
        BoundingBox footprint = element.getBoundingBox(context.structureTemplateManager(), start, rotation);

        if (!EndStructureTerrainValidator.hasStableEndTerrain(context, footprint, startPool)) {
            return Optional.empty();
        }

        int startY = startHeight.sample(context.random(),
                new WorldGenerationContext(context.chunkGenerator(), context.heightAccessor()));
        BlockPos actualStart = new BlockPos(chunk.getMinBlockX(), startY, chunk.getMinBlockZ());
        return JigsawPlacement.addPieces(context, startPool, startJigsawName, maxDepth,
                actualStart, useExpansionHack, projectStartToHeightmap, maxDistanceFromCenter);
    }

    @Override
    public StructureType<?> type() {
        return ModStructureTypes.STABLE_END_JIGSAW.get();
    }
}
