package com.carrot123.until_eternity.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.InclusiveRange;
import net.minecraft.world.level.BaseSpawner;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BaseSpawner.class)
public abstract class BaseSpawnerMixin {

    private static final ResourceLocation UNTIL_ETERNITY$CURSED_INFUSER =
            new ResourceLocation("goety", "cursed_infuser");

    @Inject(
            method = {
                    "serverTick",
                    "m_151311_"
            },
            at = @At("HEAD"),
            cancellable = true,
            remap = false
    )
    private void untilEternity$disableUnderCursedInfuser(
            ServerLevel level,
            BlockPos pos,
            CallbackInfo ci
    ) {
        if (untilEternity$hasCursedInfuserAbove(level, pos)) {
            ci.cancel();
        }
    }

    @Inject(
            method = {
                    "clientTick",
                    "m_151319_"
            },
            at = @At("HEAD"),
            cancellable = true,
            remap = false
    )
    private void untilEternity$disableClientUnderCursedInfuser(
            Level level,
            BlockPos pos,
            CallbackInfo ci
    ) {
        if (untilEternity$hasCursedInfuserAbove(level, pos)) {
            ci.cancel();
        }
    }

    @Redirect(
            method = {
                    "serverTick",
                    "m_151311_"
            },
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/util/InclusiveRange;isValueInRange(Ljava/lang/Comparable;)Z",
                    remap = false
            ),
            remap = false,
            require = 0
    )
    private boolean untilEternity$ignoreCustomSpawnerLightRulesMojmap(
            InclusiveRange<?> range,
            Comparable<?> value
    ) {
        return true;
    }

    @Redirect(
            method = {
                    "serverTick",
                    "m_151311_"
            },
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/util/InclusiveRange;m_184578_(Ljava/lang/Comparable;)Z",
                    remap = false
            ),
            remap = false,
            require = 0
    )
    private boolean untilEternity$ignoreCustomSpawnerLightRulesSrg(
            InclusiveRange<?> range,
            Comparable<?> value
    ) {
        return true;
    }

    private static boolean untilEternity$hasCursedInfuserAbove(
            Level level,
            BlockPos spawnerPos
    ) {
        ResourceLocation id = BuiltInRegistries.BLOCK.getKey(
                level.getBlockState(spawnerPos.above()).getBlock()
        );

        return UNTIL_ETERNITY$CURSED_INFUSER.equals(id);
    }
}