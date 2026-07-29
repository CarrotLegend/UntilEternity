package com.carrot123.until_eternity.item.curio;

import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.SlotContext;

public class ImmuneCurioItem extends BaseModCurioItem {

    public enum CurioType {
        LIMITED,   // 仅免疫指定原版负面效果 + 火焰/岩浆块伤害
        ALL        // 免疫所有有害效果（含模组） + 火焰/岩浆块伤害
    }

    private final CurioType curioType;
    public ImmuneCurioItem(
            Properties properties,
            CurioType curioType,
            CurioAttributeProfile attributeProfile
    ) {
        super(properties, attributeProfile.itemId(), attributeProfile.modifierSpecs());
        this.curioType = curioType;
    }

    public CurioType getCurioType() {
        return curioType;
    }

    @Override
    public boolean canEquip(SlotContext slotContext, ItemStack stack) {
        return super.canEquip(slotContext, stack)
                && CurioMutualExclusionHandler.canEquip(slotContext, stack);
    }
}
