package com.carrot123.until_eternity.mixin;

import com.carrot123.until_eternity.util.InfinityArrowHelper;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArrowItem;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(BowItem.class)
public abstract class BowItemInfinityMixin {

    @WrapOperation(
            method = {
                    "releaseUsing(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/LivingEntity;I)V",
                    "m_5551_(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/LivingEntity;I)V"
            },
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/item/ArrowItem;isInfinite(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/entity/player/Player;)Z",
                    remap = false
            ),
            remap = false,
            require = 1,
            expect = 1,
            allow = 1
    )
    private boolean untilEternity$allowAnyArrowItem(
            ArrowItem arrow,
            ItemStack ammo,
            ItemStack bow,
            Player player,
            Operation<Boolean> original
    ) {
        boolean originalResult = original.call(
                arrow,
                ammo,
                bow,
                player
        );

        return originalResult
                || InfinityArrowHelper.isInfinite(
                        ammo,
                        bow
                );
    }
}