package com.carrot123.until_eternity.item;

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

public class MonstersScythe extends SwordItem {
    public MonstersScythe(Tier tier, int attackDamageModifier, float attackSpeedModifier, Properties props) {
        super(tier, attackDamageModifier, attackSpeedModifier, props);
    }
    @Override
    public boolean canBeDepleted() {
        return false;
    }
    @Override
    public boolean isBarVisible(@Nonnull ItemStack stack) {
        return false;
    }
    @Override
    public void appendHoverText(@Nonnull ItemStack stack, @Nullable Level level, @Nonnull List<Component> tooltip, @Nonnull TooltipFlag flag) {
        tooltip.add(Component.translatable("item.unbreakable").withStyle(ChatFormatting.BLUE));
        super.appendHoverText(stack, level, tooltip, flag);
    }
    @Override
    public boolean isEnchantable(@Nonnull ItemStack stack) {
        return true;
    }
}