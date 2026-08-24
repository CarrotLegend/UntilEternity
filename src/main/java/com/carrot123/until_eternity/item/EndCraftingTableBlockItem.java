package com.carrot123.until_eternity.item;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;

public final class EndCraftingTableBlockItem extends BlockItem {
    public EndCraftingTableBlockItem(Block block, Properties properties) {
        super(block, properties);
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return true;
    }
}
