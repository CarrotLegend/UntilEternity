package com.carrot123.until_eternity.item.curio;

import com.carrot123.until_eternity.compat.GoetyRevelationAttributesCompat;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.resources.ResourceLocation;
import top.theillusivec4.curios.api.SlotContext;

import java.util.List;

public final class VoidRingItem extends BaseModCurioItem {
    static final double SPELL_POWER_AMOUNT = 2.0D;
    static final double SPELL_POWER_MULTIPLIER_AMOUNT = 1.0D;
    static final int MAX_EQUIPPED = 1;
    private static final ResourceLocation ITEM_ID =
            new ResourceLocation("until_eternity", "void_ring");

    public VoidRingItem() {
        super(
                new Properties().rarity(Rarity.RARE).fireResistant(),
                ITEM_ID,
                List.of(
                        CurioAttributeSpec.of(
                                () -> GoetyRevelationAttributesCompat.resolve(
                                        GoetyRevelationAttributesCompat.SPELL_POWER),
                                "spell_power",
                                SPELL_POWER_AMOUNT,
                                AttributeModifier.Operation.ADDITION),
                        CurioAttributeSpec.of(
                                () -> GoetyRevelationAttributesCompat.resolve(
                                        GoetyRevelationAttributesCompat.SPELL_POWER_MULTIPLIER),
                                "spell_power_multiplier",
                                SPELL_POWER_MULTIPLIER_AMOUNT,
                                AttributeModifier.Operation.ADDITION)
                )
        );
    }

    @Override
    public boolean canEquip(SlotContext slotContext, ItemStack stack) {
        return slotContext == null
                || slotContext.entity() == null
                || CurioEquipmentHelper.countEquipped(
                        slotContext.entity(), this) < MAX_EQUIPPED;
    }
}
