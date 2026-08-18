package com.carrot123.until_eternity.compat.attribute;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

final class AttributeModifierConversions {
    private AttributeModifierConversions() {
    }

    static AttributeModifier terraMultiplierToDynamic(
            AttributeModifier source) {
        if (source.getOperation() != AttributeModifier.Operation.ADDITION) {
            return source;
        }
        return copyWith(source, source.getAmount(),
                AttributeModifier.Operation.MULTIPLY_BASE);
    }

    static AttributeModifier healingPowerToDynamic(
            AttributeModifier source) {
        return terraMultiplierToDynamic(source);
    }

    static AttributeModifier obscureDodgeToTerra(
            AttributeModifier source) {
        double amount = source.getAmount();
        if (source.getOperation() == AttributeModifier.Operation.ADDITION) {
            amount *= 2.0D;
        }
        return copyWith(source, amount, AttributeModifier.Operation.ADDITION);
    }

    static AttributeModifier percentageShred(
            AttributeModifier source,
            ResourceLocation sourceAttributeId,
            ResourceLocation targetAttributeId) {
        String identity = "until_eternity:attribute_remap|"
                + sourceAttributeId + "|" + source.getId() + "|"
                + targetAttributeId;
        UUID targetId = UUID.nameUUIDFromBytes(
                identity.getBytes(StandardCharsets.UTF_8));
        return new AttributeModifier(
                targetId,
                source.getName(),
                source.getAmount(),
                AttributeModifier.Operation.MULTIPLY_BASE);
    }

    static double combineDodgeChance(
            double terraChance,
            double obscureResidual) {
        return Math.max(0.0D, Math.min(terraChance + obscureResidual, 1.0D));
    }

    static boolean hasAdditionAndMultiplyBase(
            Iterable<AttributeModifier> modifiers) {
        boolean addition = false;
        boolean multiplyBase = false;
        for (AttributeModifier modifier : modifiers) {
            addition |= modifier.getOperation()
                    == AttributeModifier.Operation.ADDITION;
            multiplyBase |= modifier.getOperation()
                    == AttributeModifier.Operation.MULTIPLY_BASE;
        }
        return addition && multiplyBase;
    }

    private static AttributeModifier copyWith(
            AttributeModifier source,
            double amount,
            AttributeModifier.Operation operation) {
        return new AttributeModifier(
                source.getId(),
                source.getName(),
                amount,
                operation);
    }
}
