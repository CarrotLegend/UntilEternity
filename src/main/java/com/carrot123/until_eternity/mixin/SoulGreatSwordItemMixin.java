package com.carrot123.until_eternity.mixin;

import com.carrot123.until_eternity.compat.legendarymonsters.SoulGreatSwordCompat;
import net.miauczel.legendary_monsters.item.custom.SoulGreatSwordItem;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = SoulGreatSwordItem.class, remap = false)
public abstract class SoulGreatSwordItemMixin {
    @Inject(method = "use", at = @At("HEAD"), cancellable = true, remap = true)
    private void untilEternity$restoreSoulRage(Level level, Player player, InteractionHand hand,
                                               CallbackInfoReturnable<InteractionResultHolder<ItemStack>> callback) {
        ItemStack stack = player.getItemInHand(hand);
        if (level instanceof ServerLevel serverLevel) {
            SoulGreatSwordCompat.useSoulRage(serverLevel, player, stack);
        }
        callback.setReturnValue(InteractionResultHolder.sidedSuccess(stack, level.isClientSide));
    }

    @Inject(method = "onUseTick", at = @At("HEAD"), cancellable = true, remap = true)
    private void untilEternity$disableParryTick(Level level, LivingEntity entity, ItemStack stack,
                                                int remainingUseDuration, CallbackInfo callback) {
        callback.cancel();
    }

    @Inject(method = "releaseUsing", at = @At("HEAD"), cancellable = true, remap = true)
    private void untilEternity$disableDaggerRelease(ItemStack stack, Level level, LivingEntity entity,
                                                    int timeCharged, CallbackInfo callback) {
        callback.cancel();
    }
}
