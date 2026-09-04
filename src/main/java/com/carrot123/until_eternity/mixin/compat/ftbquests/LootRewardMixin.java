package com.carrot123.until_eternity.mixin.compat.ftbquests;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@Mixin(
        targets = "dev.ftb.mods.ftbquests.quest.reward.LootReward",
        remap = false
)
public abstract class LootRewardMixin {
    @Inject(
            method = "getExcludeFromClaimAll()Z",
            at = @At("HEAD"),
            cancellable = true
    )
    private void untilEternity$getConfiguredExcludeFromClaimAll(
            CallbackInfoReturnable<Boolean> cir
    ) {
        boolean value =
                ((RewardClaimAllAccessor) (Object) this)
                        .untilEternity$getExcludeFromClaimAll();

        cir.setReturnValue(value);
    }
}