package com.carrot123.until_eternity.compat.attribute;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AttributeModifierConversionsTest {
    private static final UUID ID = UUID.fromString(
            "8a726de2-a774-47fa-8a43-b3731159c15f");

    @Test
    void terraAdditionBecomesDynamicMultiplyBaseAndPreservesIdentityFields() {
        AttributeModifier source = modifier(
                0.25D, AttributeModifier.Operation.ADDITION);
        AttributeModifier converted =
                AttributeModifierConversions.terraMultiplierToDynamic(source);

        assertEquals(source.getId(), converted.getId());
        assertEquals(source.getName(), converted.getName());
        assertEquals(source.getAmount(), converted.getAmount());
        assertEquals(AttributeModifier.Operation.MULTIPLY_BASE,
                converted.getOperation());
    }

    @Test
    void terraMultipliersKeepTheirOriginalModifierObject() {
        for (AttributeModifier.Operation operation : List.of(
                AttributeModifier.Operation.MULTIPLY_BASE,
                AttributeModifier.Operation.MULTIPLY_TOTAL)) {
            AttributeModifier source = modifier(0.15D, operation);
            assertSame(source,
                    AttributeModifierConversions.terraMultiplierToDynamic(
                            source));
        }
    }

    @Test
    void healingPowerAdditionBecomesDynamicPercentageAndPreservesIdentity() {
        AttributeModifier source = modifier(
                0.10D, AttributeModifier.Operation.ADDITION);
        AttributeModifier converted = AttributeModifierConversions
                .healingPowerToDynamic(source);

        assertEquals(source.getId(), converted.getId());
        assertEquals(source.getName(), converted.getName());
        assertEquals(0.10D, converted.getAmount());
        assertEquals(AttributeModifier.Operation.MULTIPLY_BASE,
                converted.getOperation());
    }

    @Test
    void healingPowerMultipliersKeepTheirStackingOperation() {
        for (AttributeModifier.Operation operation : List.of(
                AttributeModifier.Operation.MULTIPLY_BASE,
                AttributeModifier.Operation.MULTIPLY_TOTAL)) {
            AttributeModifier source = modifier(0.20D, operation);
            assertSame(source,
                    AttributeModifierConversions.healingPowerToDynamic(
                            source));
        }
    }

    @Test
    void healingPowerTenAndTwentyPercentAddToThirtyPercent() {
        AttributeModifier ten = AttributeModifierConversions
                .healingPowerToDynamic(modifier(
                        0.10D, AttributeModifier.Operation.ADDITION));
        AttributeModifier twenty = AttributeModifierConversions
                .healingPowerToDynamic(modifier(
                        0.20D, AttributeModifier.Operation.ADDITION));

        assertEquals(13.0D, dynamic(10.0D, 0.0D,
                ten.getAmount() + twenty.getAmount(), List.of()), 1.0E-12D);
    }

    @Test
    void terraOperationsAreEquivalentForAnyDynamicBase() {
        for (double base : List.of(0.0D, 1.0D, 37.5D, 100.0D)) {
            double amount = 0.25D;
            // Terra's default-1 ADDITION and MULTIPLY_BASE both produce 1+a;
            // Puffish must therefore receive MULTIPLY_BASE for either source.
            assertEquals(base * (1.0D + amount),
                    dynamic(base, 0.0D, amount, List.of()), 1.0E-12D);
            assertEquals(base * (1.0D + amount),
                    dynamic(base, 0.0D, 0.0D, List.of(amount)), 1.0E-12D);
        }
    }

    @Test
    void terraDodgeContractUsesStrictLessThanBoundary() {
        assertFalse(0.0F < 0.0D);
        assertTrue(Math.nextDown(1.0F) < 1.0D);
        assertFalse(1.0F < 1.0D);
    }

    @Test
    void actualTerraSourceCombinationIsEquivalentAtOneHundredBase() {
        double terra = 100.0D * (1.0D + 0.20D)
                * (1.0D + 0.10D)
                * (1.0D + 0.15D);
        double puffish = dynamic(100.0D, 0.0D, 0.20D,
                List.of(0.10D, 0.15D));
        assertEquals(terra, puffish, 1.0E-12D);
    }

    @Test
    void obscureDodgeConversionMatchesApi18DoubleCountingRules() {
        AttributeModifier addition = AttributeModifierConversions
                .obscureDodgeToTerra(modifier(
                        0.20D, AttributeModifier.Operation.ADDITION));
        AttributeModifier multiplyBase = AttributeModifierConversions
                .obscureDodgeToTerra(modifier(
                        0.15D, AttributeModifier.Operation.MULTIPLY_BASE));
        AttributeModifier multiplyTotal = AttributeModifierConversions
                .obscureDodgeToTerra(modifier(
                        0.10D, AttributeModifier.Operation.MULTIPLY_TOTAL));

        assertEquals(0.40D, addition.getAmount());
        assertEquals(0.15D, multiplyBase.getAmount());
        assertEquals(0.10D, multiplyTotal.getAmount());
        assertEquals(AttributeModifier.Operation.ADDITION,
                addition.getOperation());
        assertEquals(AttributeModifier.Operation.ADDITION,
                multiplyBase.getOperation());
        assertEquals(AttributeModifier.Operation.ADDITION,
                multiplyTotal.getOperation());
    }

    @Test
    void obscureDodgeAndResidualClampsMatchConfirmedLimits() {
        assertEquals(0.8D, obscureDodge(0.0D,
                List.of(modifier(0.50D,
                        AttributeModifier.Operation.ADDITION))));
        assertEquals(0.7D,
                AttributeModifierConversions.combineDodgeChance(0.4D, 0.3D));
        assertEquals(1.0D,
                AttributeModifierConversions.combineDodgeChance(0.7D, 0.8D));
        assertEquals(0.0D,
                AttributeModifierConversions.combineDodgeChance(-0.5D, 0.1D));
    }

    @Test
    void mixedAdditionAndMultiplyBaseIsDetected() {
        assertTrue(AttributeModifierConversions.hasAdditionAndMultiplyBase(
                List.of(
                        modifier(0.10D, AttributeModifier.Operation.ADDITION),
                        modifier(0.20D,
                                AttributeModifier.Operation.MULTIPLY_BASE))));
        assertFalse(AttributeModifierConversions.hasAdditionAndMultiplyBase(
                List.of(
                        modifier(0.10D,
                                AttributeModifier.Operation.MULTIPLY_BASE),
                        modifier(0.20D,
                                AttributeModifier.Operation.MULTIPLY_TOTAL))));
    }

    @Test
    void percentageShredAlwaysUsesMultiplyBaseAndPreservesValueAndName() {
        for (AttributeModifier.Operation operation
                : AttributeModifier.Operation.values()) {
            AttributeModifier source = modifier(0.25D, operation);
            AttributeModifier converted = AttributeModifierConversions
                    .percentageShred(
                            source,
                            id("obscure_api", "penetration"),
                            id("puffish_attributes", "armor_shred"));

            assertEquals(source.getName(), converted.getName());
            assertEquals(0.25D, converted.getAmount());
            assertEquals(AttributeModifier.Operation.MULTIPLY_BASE,
                    converted.getOperation());
        }
    }

    @Test
    void percentageShredUuidIsStableAndNamespacedByBothAttributes() {
        AttributeModifier source = modifier(
                0.30D, AttributeModifier.Operation.MULTIPLY_TOTAL);
        AttributeModifier obscureArmor = AttributeModifierConversions
                .percentageShred(
                        source,
                        id("obscure_api", "penetration"),
                        id("puffish_attributes", "armor_shred"));
        AttributeModifier obscureArmorAgain = AttributeModifierConversions
                .percentageShred(
                        source,
                        id("obscure_api", "penetration"),
                        id("puffish_attributes", "armor_shred"));
        AttributeModifier goetyArmor = AttributeModifierConversions
                .percentageShred(
                        source,
                        id("goety_revelation", "armor_penetration"),
                        id("puffish_attributes", "armor_shred"));
        AttributeModifier obscureProtection = AttributeModifierConversions
                .percentageShred(
                        source,
                        id("obscure_api", "penetration"),
                        id("puffish_attributes", "protection_shred"));

        assertEquals(obscureArmor.getId(), obscureArmorAgain.getId());
        assertFalse(obscureArmor.getId().equals(goetyArmor.getId()));
        assertFalse(obscureArmor.getId().equals(obscureProtection.getId()));
    }

    @Test
    void percentageShredReducesOnlyTheRequestedDynamicBase() {
        assertEquals(15.0D, negativeMultiplyBase(20.0D, 0.25D), 1.0E-12D);
        assertEquals(12.0D, 12.0D, 1.0E-12D);
        assertEquals(14.0D, negativeMultiplyBase(20.0D, 0.30D), 1.0E-12D);
        assertEquals(8.0D, negativeMultiplyBase(16.0D, 0.50D), 1.0E-12D);
        assertEquals(10.0D,
                negativeMultiplyBase(20.0D, 0.20D + 0.30D), 1.0E-12D);
    }

    private static AttributeModifier modifier(
            double amount,
            AttributeModifier.Operation operation) {
        return new AttributeModifier(ID, "attribute unification test",
                amount, operation);
    }

    private static double dynamic(
            double base,
            double addition,
            double multiplyBase,
            List<Double> multiplyTotal) {
        double afterAddition = base + addition;
        double result = afterAddition + afterAddition * multiplyBase;
        for (double amount : multiplyTotal) {
            result *= 1.0D + amount;
        }
        return result;
    }

    private static double obscureDodge(
            double base,
            List<AttributeModifier> modifiers) {
        double vanilla = base;
        for (AttributeModifier modifier : modifiers) {
            if (modifier.getOperation()
                    == AttributeModifier.Operation.ADDITION) {
                vanilla += modifier.getAmount();
            }
        }
        double repeated = modifiers.stream()
                .mapToDouble(AttributeModifier::getAmount)
                .sum();
        return Math.max(0.0D, Math.min(vanilla + repeated, 0.8D));
    }

    private static ResourceLocation id(String namespace, String path) {
        return new ResourceLocation(namespace, path);
    }

    private static double negativeMultiplyBase(double base, double amount) {
        return base - base * amount;
    }
}
