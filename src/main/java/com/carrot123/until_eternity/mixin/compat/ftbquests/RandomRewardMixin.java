package com.carrot123.until_eternity.mixin.compat.ftbquests;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@Mixin(
        targets = "dev.ftb.mods.ftbquests.quest.reward.RandomReward",
        remap = false
)
public abstract class RandomRewardMixin {

    private static final String RANDOM =
            "dev.ftb.mods.ftbquests.quest.reward.RandomReward";
    @Inject(
            method = "<init>",
            at = @At("TAIL")
    )
    private void untilEternity$fixRandomRewardDefault(
            CallbackInfo ci
    ) {
        if (RANDOM.equals(this.getClass().getName())) {
            ((RewardClaimAllAccessor) (Object) this)
                    .untilEternity$setExcludeFromClaimAll(false);
        }
    }
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
    @Inject(
            method = "isClaimAllHardcoded()Z",
            at = @At("HEAD"),
            cancellable = true
    )
    private void untilEternity$unlockClaimAllOption(
            CallbackInfoReturnable<Boolean> cir
    ) {
        cir.setReturnValue(false);
    }
}