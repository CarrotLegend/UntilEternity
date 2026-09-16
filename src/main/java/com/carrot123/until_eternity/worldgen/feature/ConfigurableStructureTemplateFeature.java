package com.carrot123.until_eternity.worldgen.feature;

import java.util.Optional;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

/** Places one complete structure-template NBT as a configured feature. */
public final class ConfigurableStructureTemplateFeature
        extends Feature<StructureTemplateFeatureConfiguration> {
    public ConfigurableStructureTemplateFeature() {
        super(StructureTemplateFeatureConfiguration.CODEC);
    }

    @Override
    public boolean place(
            FeaturePlaceContext<StructureTemplateFeatureConfiguration> context
    ) {
        WorldGenLevel level = context.level();
        StructureTemplateFeatureConfiguration configuration = context.config();
        Optional<StructureTemplate> optionalTemplate = level.getLevel()
                .getStructureManager()
                .get(configuration.template());
        if (optionalTemplate.isEmpty()) {
            return false;
        }

        BlockPos anchor = context.origin().offset(configuration.offset());
        BlockPos support = anchor.below();
        if (!level.getBlockState(support)
                .isFaceSturdy(level, support, Direction.UP)) {
            return false;
        }

        StructureTemplate template = optionalTemplate.get();
        Vec3i size = template.getSize();
        BlockPos pivot = new BlockPos(
                (size.getX() - 1) / 2,
                0,
                (size.getZ() - 1) / 2
        );
        BlockPos templateOrigin = anchor.subtract(pivot);
        Rotation rotation = configuration.rotation().resolve(context.random());
        StructurePlaceSettings settings = new StructurePlaceSettings()
                .setMirror(configuration.mirror())
                .setRotation(rotation)
                .setRotationPivot(pivot);

        return template.placeInWorld(
                level,
                templateOrigin,
                templateOrigin,
                settings,
                context.random(),
                Block.UPDATE_CLIENTS
        );
    }
}
