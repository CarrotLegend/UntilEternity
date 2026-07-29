package com.carrot123.until_eternity.item.curio;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.SlotContext;

public class AttributeCurioItem extends BaseModCurioItem {
    public AttributeCurioItem(Properties properties, CurioAttributeProfile attributeProfile) {
        super(properties, attributeProfile.itemId(), attributeProfile.modifierSpecs());
    }

    @Override
    public boolean canEquip(SlotContext slotContext, ItemStack stack) {
        return super.canEquip(slotContext, stack)
                && CurioMutualExclusionHandler.canEquip(slotContext, stack);
    }
}
