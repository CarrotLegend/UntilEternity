package com.carrot123.until_eternity.mixin.compat;

import com.carrot123.until_eternity.tarot.TarotCardHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import shiroroku.tarotcards.Item.TarotItem;

@Mixin(value = TarotItem.class, remap = false)
public abstract class TarotItemMixin {
    @Inject(method = "hasTarot", at = @At("HEAD"), cancellable = true, remap = false)
    private static void untilEternity$disableOriginalCards(Player player, Item item,
            CallbackInfoReturnable<Boolean> callback) {
        callback.setReturnValue(false);
    }

    @Inject(method = "isActivated", at = @At("HEAD"), cancellable = true, remap = false)
    private static void untilEternity$ignoreLegacyActivation(ItemStack stack,
            CallbackInfoReturnable<Boolean> callback) {
        callback.setReturnValue(false);
    }

    @Inject(method = "use", at = @At("HEAD"), cancellable = true, remap = true)
    private void untilEternity$ignoreOldActivation(Level level, Player player,
            InteractionHand hand, CallbackInfoReturnable<InteractionResultHolder<ItemStack>> callback) {
        ItemStack stack = player.getItemInHand(hand);
        if (hand == InteractionHand.MAIN_HAND && player.isShiftKeyDown()) {
            if (!level.isClientSide) {
                TarotCardHelper.toggle(stack);
                player.getInventory().setChanged();
                player.inventoryMenu.broadcastChanges();
                if (player.containerMenu != player.inventoryMenu) {
                    player.containerMenu.broadcastChanges();
                }
            }
            callback.setReturnValue(InteractionResultHolder.sidedSuccess(stack, level.isClientSide));
        } else {
            callback.setReturnValue(InteractionResultHolder.pass(stack));
        }
    }

    @Inject(method = "isFoil", at = @At("HEAD"), cancellable = true, remap = true)
    private void untilEternity$alwaysGlintTaggedCards(ItemStack stack,
            CallbackInfoReturnable<Boolean> callback) {
        callback.setReturnValue(TarotCardHelper.isTaggedTarotCard(stack));
    }

}
