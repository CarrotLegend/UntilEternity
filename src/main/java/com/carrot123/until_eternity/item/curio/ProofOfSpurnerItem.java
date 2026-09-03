package com.carrot123.until_eternity.item.curio;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public final class ProofOfSpurnerItem extends ImmuneCurioItem {

    public ProofOfSpurnerItem() {
        super(
                new Item.Properties()
                        .stacksTo(1)
                        .fireResistant(),
                ImmuneCurioItem.CurioType.ALL,
                CurioAttributeProfile.PROOF_OF_SPURNER
        );
    }

    @Override
    public List<Component> getAttributesTooltip(
            List<Component> tooltips,
            ItemStack stack
    ) {
        tooltips.clear();
        return tooltips;
    }
}