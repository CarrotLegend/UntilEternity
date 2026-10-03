package com.carrot123.until_eternity.mixin.compat;

import com.carrot123.until_eternity.tarot.TarotSingleCardPresence;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import shiroroku.tarotcards.Registry.ItemRegistry;

import java.util.function.Consumer;

@Mixin(value = ItemStack.class, remap = false)
public abstract class TarotMagicianItemStackMixin {

    @Inject(
            method = {
                    "hurtAndBreak(ILnet/minecraft/world/entity/LivingEntity;Ljava/util/function/Consumer;)V",
                    "m_41622_(ILnet/minecraft/world/entity/LivingEntity;Ljava/util/function/Consumer;)V"
            },
            at = @At("HEAD"),
            cancellable = true,
            remap = false
    )
    private <T extends LivingEntity> void untilEternity$preventDurabilityLoss(
            int amount,
            T entity,
            Consumer<T> breakCallback,
            CallbackInfo ci
    ) {
        if (amount <= 0) {
            return;
        }

        if (!(entity instanceof Player player)) {
            return;
        }

        if (entity.level().isClientSide) {
            return;
        }

        ItemStack stack =
                (ItemStack) (Object) this;

        if (!stack.isDamageableItem()) {
            return;
        }

        if (!TarotSingleCardPresence.has(
                player,
                ItemRegistry.the_magician.get()
        )) {
            return;
        }

        ci.cancel();
    }
}