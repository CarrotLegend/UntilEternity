package com.carrot123.until_eternity.item;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import net.minecraftforge.registries.ForgeRegistries;

import org.jetbrains.annotations.Nullable;

import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurioItem;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class TrueBedrockItem extends Item implements ICurioItem {

    private static final ResourceLocation REVELATION_RESISTANCE =
            new ResourceLocation(
                    "goety_revelation",
                    "resistance"
            );

    public TrueBedrockItem(Properties properties) {
        super(properties);
    }

    @Override
    public Multimap<Attribute, AttributeModifier> getAttributeModifiers(
            SlotContext slotContext,
            UUID uuid,
            ItemStack stack
    ) {
        ImmutableMultimap.Builder<Attribute, AttributeModifier> builder =
                ImmutableMultimap.builder();

        builder.put(
                Attributes.MAX_HEALTH,
                new AttributeModifier(
                        uuid,
                        "until_eternity.true_bedrock.max_health",
                        1000.0D,
                        AttributeModifier.Operation.ADDITION
                )
        );

        builder.put(
                Attributes.ARMOR,
                new AttributeModifier(
                        uuid,
                        "until_eternity.true_bedrock.armor",
                        12.0D,
                        AttributeModifier.Operation.ADDITION
                )
        );

        builder.put(
                Attributes.ARMOR_TOUGHNESS,
                new AttributeModifier(
                        uuid,
                        "until_eternity.true_bedrock.armor_toughness",
                        6.0D,
                        AttributeModifier.Operation.ADDITION
                )
        );

        Attribute resistance =
                ForgeRegistries.ATTRIBUTES.getValue(
                        REVELATION_RESISTANCE
                );

        if (resistance != null) {
            builder.put(
                    resistance,
                    new AttributeModifier(
                            uuid,
                            "until_eternity.true_bedrock.resistance",
                            0.90D,
                            AttributeModifier.Operation.ADDITION
                    )
            );
        }

        return builder.build();
    }

    @Override
    public List<Component> getAttributesTooltip(
            List<Component> tooltips,
            ItemStack stack
    ) {
        return new ArrayList<>();
    }

    @Override
    public void appendHoverText(
            ItemStack stack,
            @Nullable Level level,
            List<Component> tooltip,
            TooltipFlag flag
    ) {
        super.appendHoverText(
                stack,
                level,
                tooltip,
                flag
        );

        tooltip.add(
                Component.translatable(
                                "tooltip.until_eternity.true_bedrock.flavor"
                        )
                        .withStyle(
                                ChatFormatting.GRAY,
                                ChatFormatting.ITALIC
                        )
        );

        if (!Screen.hasShiftDown()) {

            tooltip.add(
                    Component.translatable(
                                    "tooltip.until_eternity.true_bedrock.hold_shift"
                            )
                            .withStyle(
                                    ChatFormatting.DARK_GRAY
                            )
            );

        } else {

            tooltip.add(
                    Component.translatable(
                                    "tooltip.until_eternity.true_bedrock.immunity"
                            )
                            .withStyle(
                                    ChatFormatting.GRAY
                            )
            );

            tooltip.add(
                    Component.translatable(
                                    "tooltip.until_eternity.true_bedrock.revive"
                            )
                            .withStyle(
                                    ChatFormatting.GRAY
                            )
            );
        }

        tooltip.add(
                Component.empty()
        );

        tooltip.add(
                Component.translatable(
                                "tooltip.until_eternity.true_bedrock.equipped"
                        )
                        .withStyle(
                                ChatFormatting.GRAY
                        )
        );

        tooltip.add(
                Component.literal(" ")
                        .append(
                                Component.translatable(
                                                "tooltip.until_eternity.true_bedrock.max_health"
                                        )
                                        .withStyle(
                                                ChatFormatting.BLUE
                                        )
                        )
        );

        tooltip.add(
                Component.literal(" ")
                        .append(
                                Component.translatable(
                                                "tooltip.until_eternity.true_bedrock.armor"
                                        )
                                        .withStyle(
                                                ChatFormatting.BLUE
                                        )
                        )
        );

        tooltip.add(
                Component.literal(" ")
                        .append(
                                Component.translatable(
                                                "tooltip.until_eternity.true_bedrock.armor_toughness"
                                        )
                                        .withStyle(
                                                ChatFormatting.BLUE
                                        )
                        )
        );

        tooltip.add(
                Component.literal(" ")
                        .append(
                                Component.translatable(
                                                "tooltip.until_eternity.true_bedrock.resistance"
                                        )
                                        .withStyle(
                                                ChatFormatting.BLUE
                                        )
                        )
        );
    }
}