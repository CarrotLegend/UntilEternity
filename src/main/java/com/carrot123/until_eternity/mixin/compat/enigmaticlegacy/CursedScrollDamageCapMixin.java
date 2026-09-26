package com.carrot123.until_eternity.mixin.compat.enigmaticlegacy;

import com.aizistral.enigmaticlegacy.handlers.SuperpositionHandler;
import com.aizistral.enigmaticlegacy.items.CursedScroll;
import com.carrot123.until_eternity.compat.enigmaticlegacy.CursedScrollDamageCap;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.Slice;

@Pseudo
@Mixin(
        targets = "com.aizistral.enigmaticlegacy.handlers.EnigmaticEventHandler",
        remap = false
)
public abstract class CursedScrollDamageCapMixin {
    @Redirect(
            method = "onEntityHurt(Lnet/minecraftforge/event/entity/living/LivingHurtEvent;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraftforge/event/entity/living/LivingHurtEvent;setAmount(F)V",
                    ordinal = 0,
                    remap = false
            ),
            slice = @Slice(
                    from = @At(
                            value = "INVOKE",
                            target = "Lcom/aizistral/enigmaticlegacy/handlers/SuperpositionHandler;getCurseAmount(Lnet/minecraft/world/entity/player/Player;)I",
                            remap = false
                    )
            ),
            require = 1,
            remap = false
    )
    private void untilEternity$capCursedScrollDamage(
            LivingHurtEvent event,
            float newAmount
    ) {
        if (!(event.getSource().getEntity() instanceof Player player)) {
            event.setAmount(newAmount);
            return;
        }

        if (CursedScroll.damageBoost == null) {
            event.setAmount(newAmount);
            return;
        }

        ItemStack scroll =
                CursedScrollDamageCap.getEquippedScroll(player);

        if (scroll.isEmpty()) {
            event.setAmount(newAmount);
            return;
        }

        double perCurse =
                CursedScroll.damageBoost
                        .getValue()
                        .asModifier();

        int curseCount =
                SuperpositionHandler.getCurseAmount(player);

        if (!Double.isFinite(perCurse)
                || perCurse <= 0.0D
                || curseCount <= 0) {
            event.setAmount(newAmount);
            return;
        }

        double rawBoost =
                perCurse * curseCount;

        double effectiveBoost =
                CursedScrollDamageCap.clampBoost(
                        scroll,
                        rawBoost
                );

        double excessBoost =
                rawBoost - effectiveBoost;

        if (!Double.isFinite(excessBoost)
                || excessBoost <= 0.0D) {
            event.setAmount(newAmount);
            return;
        }

        float baseAmount =
                event.getAmount();

        double correctedAmount =
                newAmount
                        - baseAmount * excessBoost;

        if (!Double.isFinite(correctedAmount)) {
            event.setAmount(newAmount);
            return;
        }

        event.setAmount(
                Math.max(
                        0.0F,
                        (float) correctedAmount
                )
        );
    }
}