package com.carrot123.until_eternity.mixin.compat.ftbquests;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.gen.Accessor;

@Pseudo
@Mixin(
        targets = "dev.ftb.mods.ftbquests.quest.reward.Reward",
        remap = false
)
public interface RewardClaimAllAccessor {

    @Accessor("excludeFromClaimAll")
    boolean untilEternity$getExcludeFromClaimAll();

    @Accessor("excludeFromClaimAll")
    void untilEternity$setExcludeFromClaimAll(boolean value);
}