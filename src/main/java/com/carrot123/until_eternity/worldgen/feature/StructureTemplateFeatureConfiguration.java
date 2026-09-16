package com.carrot123.until_eternity.worldgen.feature;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;

public record StructureTemplateFeatureConfiguration(
        ResourceLocation template,
        RotationMode rotation,
        Mirror mirror,
        BlockPos offset
) implements FeatureConfiguration {
    public static final Codec<StructureTemplateFeatureConfiguration> CODEC =
            RecordCodecBuilder.create(instance -> instance.group(
                    ResourceLocation.CODEC.fieldOf("template")
                            .forGetter(StructureTemplateFeatureConfiguration::template),
                    RotationMode.CODEC.fieldOf("rotation")
                            .forGetter(StructureTemplateFeatureConfiguration::rotation),
                    Mirror.CODEC.fieldOf("mirror")
                            .forGetter(StructureTemplateFeatureConfiguration::mirror),
                    BlockPos.CODEC.fieldOf("offset")
                            .forGetter(StructureTemplateFeatureConfiguration::offset)
            ).apply(instance, StructureTemplateFeatureConfiguration::new));

    public enum RotationMode implements StringRepresentable {
        NONE("none", Rotation.NONE),
        CLOCKWISE_90("clockwise_90", Rotation.CLOCKWISE_90),
        CLOCKWISE_180("180", Rotation.CLOCKWISE_180),
        COUNTERCLOCKWISE_90("counterclockwise_90", Rotation.COUNTERCLOCKWISE_90),
        RANDOM("random", null);

        public static final Codec<RotationMode> CODEC =
                StringRepresentable.fromEnum(RotationMode::values);
        private static final Rotation[] RANDOM_ROTATIONS = Rotation.values();

        private final String serializedName;
        private final Rotation fixedRotation;

        RotationMode(String serializedName, Rotation fixedRotation) {
            this.serializedName = serializedName;
            this.fixedRotation = fixedRotation;
        }

        public Rotation resolve(RandomSource random) {
            if (this == RANDOM) {
                return RANDOM_ROTATIONS[random.nextInt(RANDOM_ROTATIONS.length)];
            }
            return fixedRotation;
        }

        @Override
        public String getSerializedName() {
            return serializedName;
        }
    }
}
