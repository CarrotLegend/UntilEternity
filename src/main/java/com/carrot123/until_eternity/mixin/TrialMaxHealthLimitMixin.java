package com.carrot123.until_eternity.mixin;

import net.minecraft.world.entity.ai.attributes.Attributes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

/**
 * The vanilla 1024 health ceiling would clamp the trial's tenfold Warden and
 * Wither health. This only changes the MAX_HEALTH attribute's allowed range;
 * trial-specific modifiers are still applied by the monument itself.
 */
@Mixin(Attributes.class)
public abstract class TrialMaxHealthLimitMixin {
    @ModifyConstant(method = "<clinit>", constant = @Constant(doubleValue = 1024.0D, ordinal = 0))
    private static double untilEternity$raiseMaxHealthCeiling(double original) {
        return 1_000_000.0D;
    }
}
