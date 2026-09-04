package com.carrot123.until_eternity.mixin.compat.ftbquests;

import net.minecraft.nbt.CompoundTag;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(
        targets = "dev.ftb.mods.ftbquests.quest.reward.Reward",
        remap = false
)
public abstract class RewardClaimAllPersistenceMixin {

    private static final String RANDOM =
            "dev.ftb.mods.ftbquests.quest.reward.RandomReward";

    private static final String LOOT =
            "dev.ftb.mods.ftbquests.quest.reward.LootReward";

    private static final String NBT_KEY =
            "exclude_from_claim_all";

    @Inject(
            method = "writeData(Lnet/minecraft/nbt/CompoundTag;)V",
            at = @At("TAIL")
    )
    private void untilEternity$writeClaimAllSetting(
            CompoundTag nbt,
            CallbackInfo ci
    ) {
        if (!untilEternity$isSupportedReward(this)) {
            return;
        }

        RewardClaimAllAccessor accessor =
                (RewardClaimAllAccessor) (Object) this;
        nbt.putBoolean(
                NBT_KEY,
                accessor.untilEternity$getExcludeFromClaimAll()
        );
    }

    @Inject(
            method = "readData(Lnet/minecraft/nbt/CompoundTag;)V",
            at = @At("TAIL")
    )
    private void untilEternity$readClaimAllSetting(
            CompoundTag nbt,
            CallbackInfo ci
    ) {
        if (!untilEternity$isSupportedReward(this)) {
            return;
        }
        if (nbt.contains(NBT_KEY)) {
            return;
        }
        String className = this.getClass().getName();

        boolean defaultValue = !RANDOM.equals(className);

        RewardClaimAllAccessor accessor =
                (RewardClaimAllAccessor) (Object) this;

        accessor.untilEternity$setExcludeFromClaimAll(defaultValue);
    }

    private static boolean untilEternity$isSupportedReward(Object reward) {
        String className = reward.getClass().getName();

        return RANDOM.equals(className) || LOOT.equals(className);
    }
}
