package com.carrot123.until_eternity.worldgen.feature;

import java.util.Optional;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

public class StructureTemplateFeature
        extends Feature<NoneFeatureConfiguration> {

    private final ResourceLocation templateId;

    public StructureTemplateFeature(ResourceLocation templateId) {
        super(NoneFeatureConfiguration.CODEC);
        this.templateId = templateId;
    }

    @Override
    public boolean place(
            FeaturePlaceContext<NoneFeatureConfiguration> context
    ) {
        WorldGenLevel level = context.level();
        BlockPos origin = context.origin();

        Optional<StructureTemplate> optionalTemplate =
                level.getLevel()
                        .getStructureManager()
                        .get(templateId);

        if (optionalTemplate.isEmpty()) {
            return false;
        }

        StructureTemplate template = optionalTemplate.get();

        Rotation rotation = Rotation.values()[
                context.random().nextInt(4)
        ];

        StructurePlaceSettings settings =
                new StructurePlaceSettings()
                        .setRotation(rotation);

        return template.placeInWorld(
                level,
                origin,
                origin,
                settings,
                context.random(),
                Block.UPDATE_CLIENTS
        );
    }
}