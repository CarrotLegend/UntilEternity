package com.carrot123.until_eternity.event;

import com.carrot123.until_eternity.compat.ironsspellbooks.StaffUpgradeBonuses;
import com.carrot123.until_eternity.compat.ironsspellbooks.StaffUpgradeHelper;
import com.carrot123.until_eternity.compat.ironsspellbooks.StaffUpgradeModifierIds;
import com.carrot123.until_eternity.compat.ironsspellbooks.StaffAffix;
import com.carrot123.until_eternity.compat.ironsspellbooks.StaffAffixHelper;
import com.carrot123.until_eternity.compat.ironsspellbooks.StaffAffixModifierIds;
import com.carrot123.until_eternity.until_eternity;
import io.redspace.ironsspellbooks.api.registry.AttributeRegistry;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraftforge.event.ItemAttributeModifierEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(
        modid = until_eternity.MODID,
        bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class StaffUpgradeAttributeHandler {
    private StaffUpgradeAttributeHandler() {
    }

    @SubscribeEvent
    public static void onItemAttributeModifiers(
            ItemAttributeModifierEvent event
    ) {
        if (event.getSlotType() != EquipmentSlot.MAINHAND) {
            return;
        }

        StaffAffixHelper.getAffix(event.getItemStack())
                .ifPresent(affix -> addAffixModifiers(event, affix));

        int level = StaffUpgradeHelper.getValidLevel(event.getItemStack());
        if (level == 0) {
            return;
        }
        StaffUpgradeBonuses bonuses = StaffUpgradeBonuses.forLevel(level);

        event.addModifier(
                AttributeRegistry.SPELL_POWER.get(),
                new AttributeModifier(
                        StaffUpgradeModifierIds.SPELL_POWER,
                        "until_eternity.staff_upgrade.spell_power",
                        bonuses.spellPower(),
                        AttributeModifier.Operation.ADDITION));
        event.addModifier(
                AttributeRegistry.COOLDOWN_REDUCTION.get(),
                new AttributeModifier(
                        StaffUpgradeModifierIds.COOLDOWN_REDUCTION,
                        "until_eternity.staff_upgrade.cooldown_reduction",
                        bonuses.cooldownReduction(),
                        AttributeModifier.Operation.ADDITION));
        event.addModifier(
                AttributeRegistry.CAST_TIME_REDUCTION.get(),
                new AttributeModifier(
                        StaffUpgradeModifierIds.CAST_TIME_REDUCTION,
                        "until_eternity.staff_upgrade.cast_time_reduction",
                        bonuses.castTimeReduction(),
                        AttributeModifier.Operation.ADDITION));
    }

    private static void addAffixModifiers(
            ItemAttributeModifierEvent event,
            StaffAffix affix
    ) {
        addAffixModifier(event, AttributeRegistry.SPELL_POWER.get(),
                StaffAffixModifierIds.SPELL_POWER, "spell_power",
                affix.spellPower());
        addAffixModifier(event, AttributeRegistry.CAST_TIME_REDUCTION.get(),
                StaffAffixModifierIds.CAST_TIME_REDUCTION,
                "cast_time_reduction", affix.castTimeReduction());
        addAffixModifier(event, AttributeRegistry.COOLDOWN_REDUCTION.get(),
                StaffAffixModifierIds.COOLDOWN_REDUCTION,
                "cooldown_reduction", affix.cooldownReduction());
    }

    private static void addAffixModifier(
            ItemAttributeModifierEvent event,
            net.minecraft.world.entity.ai.attributes.Attribute attribute,
            java.util.UUID uuid,
            String key,
            double amount
    ) {
        if (amount == 0.0D) {
            return;
        }
        event.addModifier(attribute, new AttributeModifier(
                uuid,
                "until_eternity.staff_affix." + key,
                amount,
                AttributeModifier.Operation.MULTIPLY_BASE));
    }
}
