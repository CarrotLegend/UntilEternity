package com.carrot123.until_eternity.mixin.compat.enigmaticaddons;

import com.carrot123.until_eternity.compat.enigmaticaddons.RedemptionRecognitionCompat;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Pseudo
@Mixin(
        targets = "auviotre.enigmatic.addon.handlers.AddonEventHandler",
        remap = false
)
public abstract class AddonEventHandlerRecognitionMixin {

    @Redirect(
            method = "onPlayerTick(Lnet/minecraftforge/event/TickEvent$PlayerTickEvent;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/aizistral/enigmaticlegacy/handlers/SuperpositionHandler;isTheCursedOne(Lnet/minecraft/world/entity/player/Player;)Z",
                    remap = false
            ),
            remap = false
    )
    private boolean untilEternity$allowNightScrollPhantomProtection(
            Player player
    ) {
        return RedemptionRecognitionCompat
                .canUseRecognizedRelic(player);
    }

    @Redirect(
            method = "onEntityDamage(Lnet/minecraftforge/event/entity/living/LivingDamageEvent;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/aizistral/enigmaticlegacy/handlers/SuperpositionHandler;isTheCursedOne(Lnet/minecraft/world/entity/player/Player;)Z",
                    ordinal = 2,
                    remap = false
            ),
            remap = false
    )
    private boolean untilEternity$allowNightScrollLifeSteal(
            Player player
    ) {
        return RedemptionRecognitionCompat
                .canUseRecognizedRelic(player);
    }

    @Redirect(
            method = "onEntityHurt(Lnet/minecraftforge/event/entity/living/LivingHurtEvent;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lauviotre/enigmatic/addon/contents/items/BlessRing$Helper;addBetrayal(Lnet/minecraft/world/entity/player/Player;I)V",
                    ordinal = 0,
                    remap = false
            ),
            remap = false
    )
    private void untilEternity$removeBerserkHellBladeBetrayal(
            Player player,
            int amount
    ) {
    }

    @Redirect(
            method = "onEntityHurt(Lnet/minecraftforge/event/entity/living/LivingHurtEvent;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lauviotre/enigmatic/addon/contents/items/BlessRing$Helper;addBetrayal(Lnet/minecraft/world/entity/player/Player;I)V",
                    ordinal = 1,
                    remap = false
            ),
            remap = false
    )
    private void untilEternity$removeBerserkPetBetrayal(
            Player player,
            int amount
    ) {
    }

    @Redirect(
            method = "onEntityDamage(Lnet/minecraftforge/event/entity/living/LivingDamageEvent;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lauviotre/enigmatic/addon/contents/items/BlessRing$Helper;addBetrayal(Lnet/minecraft/world/entity/player/Player;I)V",
                    ordinal = 0,
                    remap = false
            ),
            remap = false
    )
    private void untilEternity$removeBerserkDefenceBetrayal(
            Player player,
            int amount
    ) {
    }

    @Redirect(
            method = "onEntityDamage(Lnet/minecraftforge/event/entity/living/LivingDamageEvent;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lauviotre/enigmatic/addon/contents/items/BlessRing$Helper;addBetrayal(Lnet/minecraft/world/entity/player/Player;I)V",
                    ordinal = 1,
                    remap = false
            ),
            remap = false
    )
    private void untilEternity$removeBerserkAttackBetrayal(
            Player player,
            int amount
    ) {
    }
}