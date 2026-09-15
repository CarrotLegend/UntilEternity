package com.carrot123.until_eternity.item.lore;

import com.carrot123.until_eternity.client.screen.book.LoreBookClient;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;

/** A fixed-content readable item. Pages live entirely in client resources. */
public final class LoreBookItem extends Item {
    private final Type type;

    public LoreBookItem(Type type) {
        super(new Item.Properties().stacksTo(1));
        this.type = type;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (level.isClientSide) {
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> LoreBookClient.open(type));
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }

    public enum Type {
        MINER_LOG,
        DAMP_DIARY,
        TORN_PAPER_1,
        TORN_PAPER_2,
        TORN_PAPER_3,
        TORN_PAPER_4,
        TORN_PAPER_5
    }
}
