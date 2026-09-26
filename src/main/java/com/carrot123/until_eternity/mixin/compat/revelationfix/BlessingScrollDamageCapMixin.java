package com.carrot123.until_eternity.mixin.compat.revelationfix;

import com.carrot123.until_eternity.compat.goetyrevelation.BlessingScrollDamageCap;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Pseudo
@Mixin(targets = "com.mega.revelationfix.common.item.curios.enigmtic_legacy.BlessingScroll$CuriosHandler", remap = false)
public abstract class BlessingScrollDamageCapMixin {
    @Redirect(
            method = "livingHurt(Lnet/minecraftforge/event/entity/living/LivingHurtEvent;)V",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraftforge/event/entity/living/LivingHurtEvent;setAmount(F)V",
                    remap = false),
            require = 1,
            remap = false
    )
    private static void untilEternity$capBlessingDamage(LivingHurtEvent event,
                                                        float originalAfter) {
        if (!(event.getSource().getEntity() instanceof Player player)) {
            event.setAmount(originalAfter);
            return;
        }
        ItemStack scroll = BlessingScrollDamageCap.getEquippedScroll(player);
        if (scroll.isEmpty()) {
            event.setAmount(originalAfter);
            return;
        }
        double corrected = BlessingScrollDamageCap.capOriginalAmount(
                scroll, event.getAmount(), originalAfter);
        event.setAmount((float) corrected);
    }
}
