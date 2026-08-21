package com.carrot123.until_eternity.mixin.compat.goety;

import com.Polarice3.Goety.api.magic.ISpell;
import com.Polarice3.Goety.common.items.magic.DarkWand;
import com.carrot123.until_eternity.compat.goety.GoetyFocusCastContext;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(value = DarkWand.class, remap = false)
public abstract class DarkWandFocusCastMixin {

    @WrapMethod(
            method = {
                    "onUseTick("
                            + "Lnet/minecraft/world/level/Level;"
                            + "Lnet/minecraft/world/entity/LivingEntity;"
                            + "Lnet/minecraft/world/item/ItemStack;"
                            + "I)V",

                    "m_5929_("
                            + "Lnet/minecraft/world/level/Level;"
                            + "Lnet/minecraft/world/entity/LivingEntity;"
                            + "Lnet/minecraft/world/item/ItemStack;"
                            + "I)V"
            },
            remap = false,
            require = 1
    )
    private void untilEternity$trackUseTick(
            Level level,
            LivingEntity caster,
            ItemStack stack,
            int count,
            Operation<Void> original
    ) {
        ISpell spell = untilEternity$getSpell(stack);

        if (spell == null) {
            original.call(
                    level,
                    caster,
                    stack,
                    count
            );
            return;
        }

        GoetyFocusCastContext.withPlayerCast(
                caster,
                spell,
                () -> original.call(
                        level,
                        caster,
                        stack,
                        count
                )
        );
    }

    @WrapMethod(
            method = {
                    "releaseUsing("
                            + "Lnet/minecraft/world/item/ItemStack;"
                            + "Lnet/minecraft/world/level/Level;"
                            + "Lnet/minecraft/world/entity/LivingEntity;"
                            + "I)V",

                    "m_5551_("
                            + "Lnet/minecraft/world/item/ItemStack;"
                            + "Lnet/minecraft/world/level/Level;"
                            + "Lnet/minecraft/world/entity/LivingEntity;"
                            + "I)V"
            },
            remap = false,
            require = 1
    )
    private void untilEternity$trackReleaseUsing(
            ItemStack stack,
            Level level,
            LivingEntity caster,
            int timeRemaining,
            Operation<Void> original
    ) {
        ISpell spell = untilEternity$getSpell(stack);

        if (spell == null) {
            original.call(
                    stack,
                    level,
                    caster,
                    timeRemaining
            );
            return;
        }

        GoetyFocusCastContext.withPlayerCast(
                caster,
                spell,
                () -> original.call(
                        stack,
                        level,
                        caster,
                        timeRemaining
                )
        );
    }

    @WrapMethod(
            method =
                    "MagicResults("
                            + "Lnet/minecraft/world/item/ItemStack;"
                            + "Lnet/minecraft/world/level/Level;"
                            + "Lnet/minecraft/world/entity/LivingEntity;"
                            + "Lcom/Polarice3/Goety/api/magic/ISpell;"
                            + ")V",
            remap = false,
            require = 1
    )
    private void untilEternity$trackMagicResults(
            ItemStack stack,
            Level level,
            LivingEntity caster,
            ISpell spell,
            Operation<Void> original
    ) {
        if (spell == null) {
            original.call(
                    stack,
                    level,
                    caster,
                    null
            );
            return;
        }

        GoetyFocusCastContext.withPlayerCast(
                caster,
                spell,
                () -> original.call(
                        stack,
                        level,
                        caster,
                        spell
                )
        );
    }

    private ISpell untilEternity$getSpell(ItemStack stack) {
        return ((DarkWand) (Object) this).getSpell(stack);
    }
}