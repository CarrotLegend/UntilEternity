package com.carrot123.until_eternity.event;

import com.carrot123.until_eternity.item.ModItems;
import com.carrot123.until_eternity.item.FinalIngotPickaxe;
import com.carrot123.until_eternity.until_eternity;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = until_eternity.MODID)
public final class FinalIngotPickaxeInteractionEvents {
    private FinalIngotPickaxeInteractionEvents() {
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        if (event.getHand() != InteractionHand.MAIN_HAND) {
            return;
        }

        Player player = event.getEntity();
        if (!player.isShiftKeyDown()) {
            return;
        }

        ItemStack stack = event.getItemStack();
        if (!stack.is(ModItems.FINAL_INGOT_PICKAXE.get())) {
            return;
        }

        Level level = event.getLevel();
        BlockState state = level.getBlockState(event.getPos());
        if (state.isAir() || state.getDestroySpeed(level, event.getPos()) >= 0.0F) {
            return;
        }

        boolean destroyed = false;
        if (level.isClientSide) {
            destroyed = !player.getCooldowns().isOnCooldown(stack.getItem());
        } else if (stack.getItem() instanceof FinalIngotPickaxe pickaxe) {
            destroyed = pickaxe.tryBreakUnbreakableBlock(level, event.getPos(), player);
        }

        event.setCanceled(true);
        event.setCancellationResult(destroyed
                ? InteractionResult.sidedSuccess(level.isClientSide)
                : InteractionResult.FAIL);
    }
}
