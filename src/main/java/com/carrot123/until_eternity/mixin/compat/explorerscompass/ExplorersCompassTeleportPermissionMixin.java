package com.carrot123.until_eternity.mixin.compat.explorerscompass;

import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@Mixin(
        targets = "com.chaosthedude.explorerscompass.util.PlayerUtils",
        remap = false
)
public abstract class ExplorersCompassTeleportPermissionMixin {

    @Inject(
            method = "canTeleport(Lnet/minecraft/server/MinecraftServer;Lnet/minecraft/world/entity/player/Player;)Z",
            at = @At("HEAD"),
            cancellable = true,
            require = 1,
            remap = false
    )
    private static void untilEternity$allowAllPlayersToTeleport(
            MinecraftServer server,
            Player player,
            CallbackInfoReturnable<Boolean> cir
    ) {
        cir.setReturnValue(true);
    }
}
