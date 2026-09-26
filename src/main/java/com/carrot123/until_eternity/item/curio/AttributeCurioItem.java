package com.carrot123.until_eternity.item.curio;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.SlotContext;

public class AttributeCurioItem extends BaseModCurioItem {
    private final String allowedSlot;

    public AttributeCurioItem(Properties properties, CurioAttributeProfile attributeProfile) {
        this(properties, attributeProfile, null);
    }

    public AttributeCurioItem(Properties properties, CurioAttributeProfile attributeProfile,
                              String allowedSlot) {
        super(properties, attributeProfile.itemId(), attributeProfile.modifierSpecs());
        this.allowedSlot = allowedSlot;
    }

    @Override
    public boolean canEquip(SlotContext slotContext, ItemStack stack) {
        return isAllowedSlot(slotContext)
                && super.canEquip(slotContext, stack)
                && CurioMutualExclusionHandler.canEquip(slotContext, stack);
    }

    @Override
    public boolean canEquipFromUse(SlotContext slotContext, ItemStack stack) {
        return isAllowedSlot(slotContext)
                && super.canEquipFromUse(slotContext, stack);
    }

    @Override
    protected boolean canApplyModifiers(SlotContext slotContext, ItemStack stack) {
        return isAllowedSlot(slotContext);
    }

    private boolean isAllowedSlot(SlotContext slotContext) {
        return allowedSlot == null || (slotContext != null
                && !slotContext.cosmetic()
                && allowedSlot.equals(slotContext.identifier()));
    }
}
