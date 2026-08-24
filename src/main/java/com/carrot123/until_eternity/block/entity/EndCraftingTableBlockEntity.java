package com.carrot123.until_eternity.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * A render anchor only. End-crafting inputs remain transient menu state.
 */
public final class EndCraftingTableBlockEntity extends BlockEntity {
    public EndCraftingTableBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.END_CRAFTING_TABLE.get(), pos, state);
    }
}
