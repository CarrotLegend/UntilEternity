package com.carrot123.until_eternity.item;

import com.carrot123.until_eternity.worldgen.PortalShape;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

import java.util.Optional;

public class ChaosElixirItem extends Item {

    public ChaosElixirItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        BlockPos insidePos = pos.relative(context.getClickedFace());
        ItemStack stack = context.getItemInHand();

        // Determine portal axis from player's facing direction
        Direction.Axis axis = context.getHorizontalDirection().getAxis();

        // Try to find a valid portal shape
        Optional<PortalShape> shapeOpt = PortalShape.findEmptyPortalShape(level, insidePos, axis);

        if (shapeOpt.isPresent()) {
            PortalShape shape = shapeOpt.get();
            if (!level.isClientSide) {
                shape.createPortalBlocks();
                // Play fire activation sound (like flint & steel)
                level.playSound(null, pos, SoundEvents.FLINTANDSTEEL_USE, SoundSource.BLOCKS,
                        1.0F, level.random.nextFloat() * 0.4F + 0.8F);
            }
            // Consume one elixir
            stack.shrink(1);
            return InteractionResult.sidedSuccess(level.isClientSide);
        }

        return InteractionResult.PASS;
    }
}
