package com.carrot123.until_eternity.mixin.compat.enigmaticlegacy;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Pseudo
@Mixin(targets = "com.aizistral.enigmaticlegacy.items.CursedScroll", remap = false)
public abstract class CursedScrollConfigMixin {
    @ModifyConstant(
            method = "onConfig(Lcom/aizistral/omniconfig/wrappers/OmniconfigWrapper;)V",
            constant = @Constant(stringValue = "Mining speed increase provided by Scroll of a Thousand Curses for each curse, as percentage."),
            require = 1,
            remap = false)
    private static String untilEternity$describeAttackSpeed(String original) {
        return "Attack speed increase provided by Scroll of a Thousand Curses for each curse, as percentage.";
    }
}
