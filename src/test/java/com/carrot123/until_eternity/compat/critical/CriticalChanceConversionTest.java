package com.carrot123.until_eternity.compat.critical;

import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CriticalChanceConversionTest {
    private static final UUID MODIFIER_ID =
            UUID.fromString("c555e5cf-d4ee-489a-9843-21c7020f3410");

    @Test
    void terraRollUsesStrictLessThanBoundary() {
        assertFalse(UnifiedCriticalHitHandler.shouldCriticalHit(0.0F, 0.0D));
        assertFalse(UnifiedCriticalHitHandler.shouldCriticalHit(0.5F, 0.5D));
        assertTrue(UnifiedCriticalHitHandler.shouldCriticalHit(0.999999F, 1.0D));
    }

    @Test
    void multiplyBaseAndTotalBecomeEquivalentTerraAdditions() {
        assertConverted(AttributeModifier.Operation.MULTIPLY_BASE, 0.15D);
        assertConverted(AttributeModifier.Operation.MULTIPLY_TOTAL, 0.01D);
    }

    @Test
    void unknownAdditionIsPreservedRatherThanSilentlyReinterpreted() {
        AttributeModifier source = modifier(
                AttributeModifier.Operation.ADDITION, 0.25D);
        assertNull(CriticalChanceConversion.toTerraAddition(source));
    }

    @Test
    void currentAquamiraeAmountsKeepTheSameProbabilityAfterConversion() {
        assertEquals(oldObscureChance(List.of(0.01D)), terraChance(List.of(0.01D)));
        assertEquals(oldObscureChance(List.of(0.15D)), terraChance(List.of(0.15D)));
        assertEquals(oldObscureChance(List.of(0.10D, 0.50D, 0.05D)),
                terraChance(List.of(0.10D, 0.50D, 0.05D)));
        assertEquals(1.0D, oldObscureChance(List.of(0.50D, 0.50D, 0.15D)));
        assertEquals(1.0D, terraChance(List.of(0.50D, 0.50D, 0.15D)));
    }

    @Test
    void obscureCriticalDamageDefaultsAndClampMatchApi18Contract() {
        assertEquals(2.0D, obscureCriticalDamage(2.0D, List.of()));
        assertEquals(4.0D, obscureCriticalDamage(3.0D, List.of(1.0D)));
        assertEquals(10.0D, obscureCriticalDamage(12.0D, List.of(10.0D)));
    }

    private static void assertConverted(
            AttributeModifier.Operation operation,
            double amount
    ) {
        AttributeModifier source = modifier(operation, amount);
        AttributeModifier converted =
                CriticalChanceConversion.toTerraAddition(source);
        assertEquals(source.getId(), converted.getId());
        assertEquals(source.getName(), converted.getName());
        assertEquals(source.getAmount(), converted.getAmount());
        assertEquals(AttributeModifier.Operation.ADDITION,
                converted.getOperation());
    }

    private static AttributeModifier modifier(
            AttributeModifier.Operation operation,
            double amount
    ) {
        return new AttributeModifier(MODIFIER_ID, "Aquamirae critical hit",
                amount, operation);
    }

    private static double oldObscureChance(List<Double> rawAmounts) {
        return Math.min(rawAmounts.stream().mapToDouble(Double::doubleValue).sum(),
                1.0D);
    }

    private static double terraChance(List<Double> additions) {
        return Math.min(additions.stream().mapToDouble(Double::doubleValue).sum(),
                1.0D);
    }

    private static double obscureCriticalDamage(
            double vanillaValue,
            List<Double> rawAmounts
    ) {
        double repeatedModifiers = rawAmounts.stream()
                .mapToDouble(Double::doubleValue).sum();
        return Math.max(0.0D,
                Math.min(vanillaValue + repeatedModifiers, 10.0D));
    }
}
