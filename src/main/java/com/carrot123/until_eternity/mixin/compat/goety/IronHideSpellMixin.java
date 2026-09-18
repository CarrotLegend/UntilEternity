package com.carrot123.until_eternity.mixin.compat.goety;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import com.Polarice3.Goety.common.magic.SpellStat;
import com.Polarice3.Goety.common.magic.spells.IronHideSpell;
import com.Polarice3.Goety.utils.WandUtil;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

@Mixin(value = IronHideSpell.class, remap = false)
public abstract class IronHideSpellMixin {

    @Redirect(
            method =
                    "SpellResult("
                    + "Lnet/minecraft/server/level/ServerLevel;"
                    + "Lnet/minecraft/world/entity/LivingEntity;"
                    + "Lnet/minecraft/world/item/ItemStack;"
                    + "Lcom/Polarice3/Goety/common/magic/SpellStat;"
                    + ")V",
            at = @At(
                    value = "INVOKE",
                    target =
                            "Lcom/Polarice3/Goety/utils/WandUtil;"
                            + "getPotencyLevel("
                            + "Lnet/minecraft/world/entity/LivingEntity;"
                            + ")I",
                    remap = false
            ),
            require = 1,
            remap = false
    )
    private int untilEternity$preserveIronHideSpellPower(
            LivingEntity focusCaster,
            ServerLevel worldIn,
            LivingEntity caster,
            ItemStack staff,
            SpellStat spellStat
    ) {

        int basePotency = spellStat.getPotency();
        int enchantmentPotency =
                WandUtil.getPotencyLevel(focusCaster);

        return basePotency + enchantmentPotency;
    }
}