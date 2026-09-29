package com.carrot123.until_eternity.item;

import com.carrot123.until_eternity.client.ClientBookOpener;
import com.carrot123.until_eternity.client.DampDiaryBookContent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public final class DampDiaryItem extends Item {
    public DampDiaryItem() {
        super(new Item.Properties().stacksTo(1));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (level.isClientSide) {
            ClientBookOpener.open(DampDiaryBookContent.createReadableBook());
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }
}
