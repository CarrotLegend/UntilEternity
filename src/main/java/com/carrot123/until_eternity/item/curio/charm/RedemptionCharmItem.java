package com.carrot123.until_eternity.item.curio.charm;

import com.carrot123.until_eternity.item.curio.BaseModCurioItem;
import com.carrot123.until_eternity.item.curio.CurioAttributeProfile;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.SlotContext;

public final class RedemptionCharmItem extends BaseModCurioItem {
    public static final String SLOT_ID = "charm";

    public RedemptionCharmItem(CurioAttributeProfile profile) {
        super(new Properties().stacksTo(1), profile.itemId(), profile.modifierSpecs());
    }

    @Override
    public boolean canEquip(SlotContext context, ItemStack stack) {
        return context != null
                && !context.cosmetic()
                && SLOT_ID.equals(context.identifier())
                && super.canEquip(context, stack);
    }

    @Override
    public boolean canEquipFromUse(SlotContext context, ItemStack stack) {
        return canEquip(context, stack) && super.canEquipFromUse(context, stack);
    }

    @Override
    protected boolean canApplyModifiers(SlotContext context, ItemStack stack) {
        return context != null
                && SLOT_ID.equals(context.identifier())
                && !context.cosmetic();
    }
}
