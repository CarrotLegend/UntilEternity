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
import shiroroku.tarotcards.Registry.ItemRegistry;

@Mixin(value = TarotItem.class, remap = false)
public abstract class TarotItemMixin {

    @Inject(
            method = "hasTarot(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/item/Item;)Z",
            at = @At("HEAD"),
            cancellable = true,
            remap = false
    )
    private static void untilEternity$disableReworkedOriginalEffects(
            Player player,
            Item item,
            CallbackInfoReturnable<Boolean> cir
    ) {
        if (item == ItemRegistry.justice.get()
                || item == ItemRegistry.strength.get()
                || item == ItemRegistry.the_magician.get()
                || item == ItemRegistry.the_star.get()) {
            cir.setReturnValue(false);
        }
    }

    @Inject(
            method = "isActivated(Lnet/minecraft/world/item/ItemStack;)Z",
            at = @At("HEAD"),
            cancellable = true,
            remap = false
    )
    private static void untilEternity$alwaysActivate(
            ItemStack stack,
            CallbackInfoReturnable<Boolean> cir
    ) {
        cir.setReturnValue(true);
    }

    @Inject(
            method = {
                    "use(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/InteractionHand;)Lnet/minecraft/world/InteractionResultHolder;",
                    "m_7203_(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/InteractionHand;)Lnet/minecraft/world/InteractionResultHolder;"
            },
            at = @At("HEAD"),
            cancellable = true,
            remap = false
    )
    private void untilEternity$orientation(
            Level level,
            Player player,
            InteractionHand hand,
            CallbackInfoReturnable<InteractionResultHolder<ItemStack>> cir
    ) {
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

            cir.setReturnValue(
                    InteractionResultHolder.sidedSuccess(
                            stack,
                            level.isClientSide
                    )
            );

            return;
        }

        cir.setReturnValue(
                InteractionResultHolder.pass(stack)
        );
    }

    @Inject(
            method = {
                    "isFoil(Lnet/minecraft/world/item/ItemStack;)Z",
                    "m_5812_(Lnet/minecraft/world/item/ItemStack;)Z"
            },
            at = @At("HEAD"),
            cancellable = true,
            remap = false
    )
    private void untilEternity$foil(
            ItemStack stack,
            CallbackInfoReturnable<Boolean> cir
    ) {
        cir.setReturnValue(
                TarotCardHelper.isTaggedTarotCard(stack)
        );
    }
}