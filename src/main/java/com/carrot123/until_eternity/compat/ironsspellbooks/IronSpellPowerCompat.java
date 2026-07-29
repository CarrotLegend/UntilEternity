package com.carrot123.until_eternity.compat.ironsspellbooks;

import com.carrot123.until_eternity.enchantment.EmpowermentLevel;
import com.carrot123.until_eternity.item.ModItems;
import com.carrot123.until_eternity.item.curio.CurioEquipmentHelper;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.registry.AttributeRegistry;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.item.ItemStack;

public final class IronSpellPowerCompat {
    public static final double RESONANCE_POWER_PER_MANA = 0.001D;

    private IronSpellPowerCompat() {
    }

    public static double adjustSpellPowerAttribute(
            LivingEntity caster,
            Attribute attribute,
            double originalValue
    ) {
        if (caster == null || caster.level().isClientSide
                || attribute != AttributeRegistry.SPELL_POWER.get()) {
            return originalValue;
        }

        MagicData magicData = MagicData.getPlayerMagicData(caster);
        ItemStack castingStack = magicData.getPlayerCastingItem();
        int empowermentLevel = EmpowermentLevel.read(castingStack);
        boolean hasResonanceArmor =
                CurioEquipmentHelper.countEquipped(
                        caster, ModItems.RESONANCE_ARMOR.get()) > 0;
        return calculate(
                originalValue,
                empowermentLevel,
                magicData.getMana(),
                hasResonanceArmor);
    }

    public static double calculate(
            double originalValue,
            int empowermentLevel,
            float currentMana,
            boolean hasResonanceArmor
    ) {
        if (!Double.isFinite(originalValue)) {
            return originalValue;
        }
        double result = originalValue
                + EmpowermentLevel.bonusForLevel(empowermentLevel);
        if (hasResonanceArmor && Float.isFinite(currentMana)
                && currentMana > 0.0F) {
            result += currentMana * RESONANCE_POWER_PER_MANA;
        }
        return Double.isFinite(result) ? result : originalValue;
    }
}
