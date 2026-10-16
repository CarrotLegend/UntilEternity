package com.carrot123.until_eternity.mixin.compat.goetyrevelation;

import com.carrot123.until_eternity.trial.TrialApollyonSpawnContext;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;

@Pseudo
@Mixin(targets = "com.Polarice3.Goety.common.entities.util.SummonApostle", remap = false, priority = 900)
public abstract class TrialSummonApostleTickMixin {
    @WrapMethod(method = {"tick()V", "m_8119_()V"}, remap = false, require = 1)
    private void untilEternity$trackApollyonSummon(Operation<Void> original) {
        TrialApollyonSpawnContext.duringTick((Entity) (Object) this, original::call);
    }
}
