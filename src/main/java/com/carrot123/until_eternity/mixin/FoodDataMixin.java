package com.carrot123.until_eternity.mixin;

import com.carrot123.until_eternity.compat.ImFullConsumptionGuard;
import com.carrot123.until_eternity.item.ImFullInventoryHelper;
import com.carrot123.until_eternity.util.NaturalHealingMath;
import com.github.L_Ender.cataclysm.init.ModAttribute;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(FoodData.class)
public abstract class FoodDataMixin {

    @WrapOperation(
            method = "tick(Lnet/minecraft/world/entity/player/Player;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/player/Player;heal(F)V"
            ),
            require = 2
    )
    private void untilEternity$applyNatureHeal(
            Player player,
            float originalAmount,
            Operation<Void> original
    ) {
        if (player.level().isClientSide) {
            original.call(player, originalAmount);
            return;
        }

        float modifiedAmount = NaturalHealingMath.apply(
                originalAmount,
                player.getAttributeValue(ModAttribute.NATURE_HEAL.get())
        );

        original.call(player, modifiedAmount);
    }

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
