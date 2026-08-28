package com.carrot123.until_eternity.mixin.compat.thirst;

import dev.ghen.thirst.foundation.util.ReflectionUtil;
import net.minecraft.core.BlockSource;
import net.minecraft.core.dispenser.DefaultDispenseItemBehavior;
import net.minecraft.core.dispenser.DispenseItemBehavior;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.lang.reflect.Method;

@Mixin(value = ReflectionUtil.class, remap = false)
public abstract class ThirstReflectionCompatMixin {
    @Inject(
            method = "fuckYouReflections(Ljava/lang/reflect/Method;Ljava/lang/Object;[Ljava/lang/Object;)Ljava/lang/Object;",
            at = @At("HEAD"),
            cancellable = true,
            remap = false,
            require = 1
    )
    private static void untilEternity$dispatchWrappedDispenserBehavior(
            Method method,
            Object receiver,
            Object[] args,
            CallbackInfoReturnable<Object> cir
    ) {
        if (method != null
                && receiver instanceof DispenseItemBehavior behavior
                && method.getDeclaringClass() == DefaultDispenseItemBehavior.class
                && !method.getDeclaringClass().isInstance(receiver)
                && args != null
                && args.length == 2
                && args[0] instanceof BlockSource source
                && args[1] instanceof ItemStack stack) {
            cir.setReturnValue(behavior.dispense(source, stack));
        }
    }
}
