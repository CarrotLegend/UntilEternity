package com.carrot123.until_eternity.item.curio.charm;

import com.carrot123.until_eternity.compat.goetyrevelation.DivineSoulLampAttributeCompat;
import com.google.common.collect.Multimap;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurioItem;

import java.util.UUID;

public final class DivineSoulLampItem extends Item implements ICurioItem {
    public DivineSoulLampItem() {
        super(new Properties().stacksTo(1).rarity(Rarity.COMMON));
    }

    @Override
    public Multimap<Attribute, AttributeModifier> getAttributeModifiers(
            SlotContext slotContext,
            UUID slotUuid,
            ItemStack stack
    ) {
        return DivineSoulLampAttributeCompat.getAttributeModifiers(slotContext, slotUuid);
    }
}
