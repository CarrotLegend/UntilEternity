package com.carrot123.until_eternity.mixin.compat.thirst;

import com.carrot123.until_eternity.compat.ImFullConsumptionGuard;
import com.carrot123.until_eternity.item.ImFullInventoryHelper;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import dev.ghen.thirst.content.thirst.PlayerThirst;
import dev.ghen.thirst.foundation.common.capability.IThirst;
import dev.ghen.thirst.foundation.common.capability.ModCapabilities;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(value = PlayerThirst.class, remap = false)
public abstract class PlayerThirstMixin {
    @WrapMethod(
            method = "tick(Lnet/minecraft/world/entity/player/Player;)V",
            remap = false,
            require = 1
    )
    private void untilEternity$preserveThirstWhileCarryingImFull(
            Player player,
            Operation<Void> original
    ) {
        if (player.level().isClientSide
                || !ImFullInventoryHelper.hasImFullItem(player)) {
            original.call(player);
            return;
        }
        IThirst thirst = player.getCapability(ModCapabilities.PLAYER_THIRST)
                .resolve()
                .orElse(null);
        if (thirst == null) {
            original.call(player);
            return;
        }
        ImFullConsumptionGuard.ThirstSnapshot before =
                ImFullConsumptionGuard.snapshot(thirst);
        original.call(player);
        if (ImFullConsumptionGuard.restoreThirstDecreases(thirst, before)) {
            thirst.updateThirstData(player);
        }
    }
}
