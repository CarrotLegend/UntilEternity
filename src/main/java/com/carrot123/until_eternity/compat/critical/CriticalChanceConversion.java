package com.carrot123.until_eternity.compat.critical;

import net.minecraft.world.entity.ai.attributes.AttributeModifier;

import javax.annotation.Nullable;

final class CriticalChanceConversion {
    private CriticalChanceConversion() {
    }

    @Nullable
    static AttributeModifier toTerraAddition(AttributeModifier source) {
        AttributeModifier.Operation operation = source.getOperation();
        if (operation != AttributeModifier.Operation.MULTIPLY_BASE
                && operation != AttributeModifier.Operation.MULTIPLY_TOTAL) {
            return null;
        }
        return new AttributeModifier(
                source.getId(),
                source.getName(),
                source.getAmount(),
                AttributeModifier.Operation.ADDITION);
    }
}
