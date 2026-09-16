package com.carrot123.until_eternity.item.curio;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import top.theillusivec4.curios.api.SlotContext;

import javax.annotation.Nullable;
import java.util.List;

public final class VoidGripItem extends AttributeCurioItem {
    public VoidGripItem(Properties properties) {
        super(properties, CurioAttributeProfile.VOID_GRIP);
    }

    @Override
    public boolean canEquip(SlotContext slotContext, ItemStack stack) {
        return super.canEquip(slotContext, stack)
                && (slotContext == null
                || slotContext.entity() == null
                || CurioEquipmentHelper.countEquippedExcept(
                        slotContext.entity(), this, stack) < 1);
    }

    @Override
    public void appendHoverText(
            ItemStack stack,
            @Nullable Level level,
            List<Component> tooltip,
            TooltipFlag flag
    ) {
        super.appendHoverText(stack, level, tooltip, flag);
        tooltip.add(Component.translatable(
                "tooltip.until_eternity.void_grip.apply")
                .withStyle(ChatFormatting.DARK_PURPLE));
        tooltip.add(Component.translatable(
                "tooltip.until_eternity.void_grip.corrosion")
                .withStyle(ChatFormatting.GRAY));
    }
}
