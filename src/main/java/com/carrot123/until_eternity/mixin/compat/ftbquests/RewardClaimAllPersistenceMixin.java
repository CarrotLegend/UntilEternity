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

    private static final String CHOICE =
            "dev.ftb.mods.ftbquests.quest.reward.ChoiceReward";

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

        RewardClaimAllAccessor accessor =
                (RewardClaimAllAccessor) (Object) this;

        if (nbt.contains(NBT_KEY)) {
            accessor.untilEternity$setExcludeFromClaimAll(
                    nbt.getBoolean(NBT_KEY)
            );
        } else {
            accessor.untilEternity$setExcludeFromClaimAll(false);
        }
    }

    private static boolean untilEternity$isSupportedReward(Object reward) {
        if (untilEternity$isInstanceOf(reward, CHOICE)) {
            return false;
        }

        return untilEternity$isInstanceOf(reward, RANDOM);
    }

    private static boolean untilEternity$isInstanceOf(
            Object object,
            String targetClassName
    ) {
        Class<?> type = object.getClass();

        while (type != null) {
            if (targetClassName.equals(type.getName())) {
                return true;
            }

            type = type.getSuperclass();
        }

        return false;
    }
}