package com.carrot123.until_eternity.trial;

import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;

public final class TrialMonumentItem extends BlockItem {
    public TrialMonumentItem(Block block, Properties properties) { super(block, properties); }
    @Override public InteractionResult place(BlockPlaceContext context) {
        return context.getLevel().dimension().equals(TrialRealm.KEY)
                ? super.place(context) : InteractionResult.FAIL;
    }
}
