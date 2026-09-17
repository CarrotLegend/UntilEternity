package com.carrot123.until_eternity.mixin.compat.enigmaticdelicacy;

import com.carrot123.until_eternity.compat.enigmaticdelicacy.InfinimealGrowthCompat;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

@Pseudo
@Mixin(targets = "com.aizistral.enigmaticlegacy.items.Infinimeal", remap = false)
public abstract class InfinimealCompatMixin {
    @Inject(
            method = "applyVanillaBonemeal(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Ljava/util/Optional;Ljava/util/Optional;)Z",
            at = @At("HEAD"),
            cancellable = true,
            require = 1,
            remap = false)
    private static void untilEternity$growDelicacyPlants(
            ItemStack stack,
            Level level,
            BlockPos pos,
            Optional<Player> player,
            Optional<Direction> direction,
            CallbackInfoReturnable<Boolean> cir
    ) {
        if (InfinimealGrowthCompat.tryGrow(level, pos)) {
            cir.setReturnValue(true);
        }
    }
}
