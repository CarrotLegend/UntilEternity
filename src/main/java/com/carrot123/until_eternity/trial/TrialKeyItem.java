package com.carrot123.until_eternity.trial;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

public final class TrialKeyItem extends Item {
    public TrialKeyItem() { super(new Item.Properties().stacksTo(1).rarity(Rarity.RARE)); }
    @Override public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        boolean success = level.isClientSide || TrialPortalPlacement.create(level, player, hand);
        return new InteractionResultHolder<>(success ? InteractionResult.SUCCESS : InteractionResult.FAIL, player.getItemInHand(hand));
    }
    @Override public InteractionResult useOn(UseOnContext context) {
        return context.getLevel().isClientSide ? InteractionResult.SUCCESS
                : TrialPortalPlacement.create(context.getLevel(), context.getPlayer(), context.getHand()) ? InteractionResult.CONSUME : InteractionResult.FAIL;
    }
}
