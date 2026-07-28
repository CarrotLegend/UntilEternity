package com.carrot123.until_eternity.item.curio;

import com.google.common.collect.Multimap;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurioItem;

import java.util.UUID;

public class AttributeCurioItem extends Item implements ICurioItem {
    private final CurioAttributeProfile attributeProfile;

    public AttributeCurioItem(Properties properties, CurioAttributeProfile attributeProfile) {
        super(properties);
        this.attributeProfile = attributeProfile;
    }

    @Override
    public Multimap<Attribute, AttributeModifier> getAttributeModifiers(
            SlotContext slotContext,
            UUID slotUuid,
            ItemStack stack
    ) {
        return attributeProfile.getModifiers(slotContext, slotUuid);
    }

    @Override
    public boolean canEquip(SlotContext slotContext, ItemStack stack) {
        return CurioMutualExclusionHandler.canEquip(slotContext, stack);
    }
}
