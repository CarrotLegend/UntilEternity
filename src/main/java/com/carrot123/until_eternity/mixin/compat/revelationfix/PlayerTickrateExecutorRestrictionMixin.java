package com.carrot123.until_eternity.mixin.compat.revelationfix;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.util.LazyOptional;
import top.theillusivec4.curios.api.type.capability.ICuriosItemHandler;

@Pseudo
@Mixin(
        targets = "com.mega.revelationfix.common.apollyon.common.PlayerTickrateExecutor",
        remap = false
)
public abstract class PlayerTickrateExecutorRestrictionMixin {

    @Redirect(
            method = "playerTick(Lnet/minecraft/world/entity/player/Player;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/player/Player;m_5810_()V",
                    remap = false
            ),
            require = 1,
            remap = false
    )
    private static void untilEternity$allowRightClick(
            Player player
    ) {
    }

    @Redirect(
            method = "playerTick(Lnet/minecraft/world/entity/player/Player;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Ltop/theillusivec4/curios/api/CuriosApi;getCuriosInventory(Lnet/minecraft/world/entity/LivingEntity;)Lnet/minecraftforge/common/util/LazyOptional;",
                    remap = false
            ),
            require = 1,
            remap = false
    )
    private static LazyOptional<ICuriosItemHandler> untilEternity$preventCurioConfiscation(
            LivingEntity entity
    ) {
        return LazyOptional.empty();
    }
}