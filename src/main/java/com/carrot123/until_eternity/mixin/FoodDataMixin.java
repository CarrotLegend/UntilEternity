package com.carrot123.until_eternity.mixin;

import com.carrot123.until_eternity.compat.ImFullConsumptionGuard;
import com.carrot123.until_eternity.item.ImFullInventoryHelper;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodData;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(FoodData.class)
public abstract class FoodDataMixin {
    @WrapMethod(
            method = "tick(Lnet/minecraft/world/entity/player/Player;)V",
            require = 1
    )
    private void untilEternity$preserveFoodWhileCarryingImFull(
            Player player,
            Operation<Void> original
    ) {
        FoodData foodData = (FoodData) (Object) this;
        if (player.level().isClientSide
                || !ImFullInventoryHelper.hasImFullItem(player)) {
            original.call(player);
            return;
        }
        ImFullConsumptionGuard.FoodSnapshot before =
                ImFullConsumptionGuard.snapshot(foodData);
        original.call(player);
        ImFullConsumptionGuard.restoreFoodDecreases(foodData, before);
    }
}
