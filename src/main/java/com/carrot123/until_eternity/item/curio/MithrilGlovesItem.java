package com.carrot123.until_eternity.item.curio;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nullable;
import java.util.List;

public class MithrilGlovesItem extends BaseModCurioItem {
    private static final ResourceLocation ITEM_ID =
            new ResourceLocation("until_eternity", "mithril_gloves");

    public MithrilGlovesItem() {
        super(
                new Properties().rarity(Rarity.EPIC).fireResistant(),
                ITEM_ID,
                List.of(CurioAttributeSpec.slot(
                        "magic_ring",
                        "magic_ring_slots",
                        8.0D,
                        AttributeModifier.Operation.ADDITION))
        );
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level,
                                List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("tooltip.until_eternity.mithril_gloves.line1")
                .withStyle(ChatFormatting.DARK_AQUA));
        tooltip.add(Component.translatable("tooltip.until_eternity.mithril_gloves.line2")
                .withStyle(ChatFormatting.GRAY));
    }
}
