package com.carrot123.until_eternity.item.curio;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import top.theillusivec4.curios.api.SlotContext;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.List;

public class IronAttributeCurioItem extends BaseModCurioItem {
    private final int maxEquipped;
    private final List<String> tooltipKeys;

    public IronAttributeCurioItem(
            Properties properties,
            ResourceLocation itemId,
            Collection<CurioAttributeSpec> modifierSpecs,
            int maxEquipped,
            String... tooltipKeys
    ) {
        super(properties, itemId, modifierSpecs);
        if (maxEquipped < 1) {
            throw new IllegalArgumentException("maxEquipped must be positive");
        }
        this.maxEquipped = maxEquipped;
        this.tooltipKeys = List.of(tooltipKeys);
    }

    public int maxEquipped() {
        return maxEquipped;
    }

    @Override
    public boolean canEquip(SlotContext slotContext, ItemStack stack) {
        if (slotContext == null || slotContext.entity() == null) {
            return true;
        }
        return CurioEquipmentHelper.countEquippedExcept(
                slotContext.entity(), this, stack) < maxEquipped;
    }

    @Override
    public void appendHoverText(
            ItemStack stack,
            @Nullable Level level,
            List<Component> tooltip,
            TooltipFlag flag
    ) {
        super.appendHoverText(stack, level, tooltip, flag);
        for (String tooltipKey : tooltipKeys) {
            tooltip.add(Component.translatable(tooltipKey)
                    .withStyle(ChatFormatting.GRAY));
        }
    }
}
