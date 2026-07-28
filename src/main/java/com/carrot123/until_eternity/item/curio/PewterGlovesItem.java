package com.carrot123.until_eternity.item.curio;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurioItem;

import javax.annotation.Nullable;
import java.util.List;
import java.util.UUID;

public class PewterGlovesItem extends Item implements ICurioItem {

    private static final UUID WARPED_RING_SLOT_UUID = UUID.fromString("e5f6a7b8-c9d0-1234-ef56-789012abcd34");

    public PewterGlovesItem() {
        super(new Properties().stacksTo(1).rarity(Rarity.EPIC).fireResistant());
    }

    @Override
    public Multimap<Attribute, AttributeModifier> getAttributeModifiers(
            SlotContext slotContext, UUID uuid, ItemStack stack) {
        return buildModifiers(uuid);
    }

    @Override
    public Multimap<Attribute, AttributeModifier> getAttributeModifiers(
            String identifier, ItemStack stack) {
        return buildModifiers(WARPED_RING_SLOT_UUID);
    }

    private Multimap<Attribute, AttributeModifier> buildModifiers(UUID slotUuid) {
        Multimap<Attribute, AttributeModifier> modifiers = ArrayListMultimap.create();
        CuriosApi.addSlotModifier(modifiers, "warped_ring", slotUuid,
                8, AttributeModifier.Operation.ADDITION);
        return modifiers;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level,
                                List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("tooltip.until_eternity.pewter_gloves.line1")
                .withStyle(ChatFormatting.DARK_PURPLE));
        tooltip.add(Component.translatable("tooltip.until_eternity.pewter_gloves.line2")
                .withStyle(ChatFormatting.GRAY));
    }
}
