package com.carrot123.until_eternity.mixin.compat.goetyrevelation;

import net.minecraft.world.item.Item;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Pseudo
@Mixin(targets = "com.mega.revelationfix.common.init.GRItems", remap = false)
public abstract class QuietusStarPropertiesMixin {
    @ModifyArg(
            method = "lambda$init$0()Lnet/minecraft/world/item/Item;",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/item/Item;<init>(Lnet/minecraft/world/item/Item$Properties;)V",
                    remap = true),
            index = 0,
            require = 1,
            remap = false)
    private static Item.Properties untilEternity$limitQuietusStarStack(
            Item.Properties properties
    ) {
        return properties.stacksTo(1);
    }
}
