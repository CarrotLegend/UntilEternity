package com.carrot123.until_eternity.item.curio;

import com.google.common.collect.Multimap;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurioItem;

import java.util.UUID;

public class ImmuneCurioItem extends Item implements ICurioItem {

    public enum CurioType {
        LIMITED,   // 仅免疫指定原版负面效果 + 火焰/岩浆块伤害
        ALL        // 免疫所有有害效果（含模组） + 火焰/岩浆块伤害
    }

    private final CurioType curioType;
    private final CurioAttributeProfile attributeProfile;

    public ImmuneCurioItem(
            Properties properties,
            CurioType curioType,
            CurioAttributeProfile attributeProfile
    ) {
        super(properties);
        this.curioType = curioType;
        this.attributeProfile = attributeProfile;
    }

    public CurioType getCurioType() {
        return curioType;
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
