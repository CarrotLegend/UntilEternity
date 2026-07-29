package com.carrot123.until_eternity.item.curio;

import com.carrot123.until_eternity.compat.GoetyRevelationAttributesCompat;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import top.theillusivec4.curios.api.SlotContext;

import java.util.List;

public class WarpedRingItem extends BaseModCurioItem {
    public WarpedRingItem(
                          ResourceLocation itemId,
                          ResourceLocation attributeId,
                          String modifierKey,
                          double amount,
                          AttributeModifier.Operation operation,
                          int maxEquipped) {
        super(
                new Properties().rarity(Rarity.RARE).fireResistant(),
                itemId,
                List.of(CurioAttributeSpec.of(
                        () -> GoetyRevelationAttributesCompat.resolve(attributeId),
                        modifierKey,
                        amount,
                        operation))
        );
        if (maxEquipped < 1) {
            throw new IllegalArgumentException("maxEquipped must be positive");
        }
        this.maxEquipped = maxEquipped;
    }

    private final int maxEquipped;

    int maxEquipped() {
        return maxEquipped;
    }

    @Override
    public boolean canEquip(SlotContext slotContext, ItemStack stack) {
        return slotContext == null
                || slotContext.entity() == null
                || CurioEquipmentHelper.countEquipped(
                        slotContext.entity(), this) < maxEquipped;
    }
}
