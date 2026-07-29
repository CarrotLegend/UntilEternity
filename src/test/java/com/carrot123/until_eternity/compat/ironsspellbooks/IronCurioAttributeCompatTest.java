package com.carrot123.until_eternity.compat.ironsspellbooks;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;

class IronCurioAttributeCompatTest {
    private static final UUID SLOT_ZERO =
            UUID.fromString("11223344-5566-7788-99aa-bbccddeeff00");
    private static final UUID SLOT_ONE =
            UUID.fromString("00ffeedd-ccbb-aa99-8877-665544332211");
    private static final UUID ORIGINAL_ADDITION =
            UUID.fromString("aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee");
    private static final UUID ORIGINAL_MULTIPLY =
            UUID.fromString("11111111-2222-3333-4444-555555555555");
    private static final Attribute TEST_ATTRIBUTE = new RangedAttribute(
            "attribute.until_eternity.irons_compat_test",
            0.0D,
            -1024.0D,
            1024.0D
    );

    @Test
    void rebuildPreservesAttributeNameAmountAndOperation() {
        AttributeModifier originalModifier = new AttributeModifier(
                ORIGINAL_ADDITION,
                "irons_spellbooks:test_modifier",
                25.0D,
                AttributeModifier.Operation.ADDITION
        );
        Multimap<Attribute, AttributeModifier> rebuilt =
                IronCurioAttributeCompat.rebuild(
                        SLOT_ZERO,
                        ImmutableMultimap.of(TEST_ATTRIBUTE, originalModifier),
                        Attribute::getDescriptionId
                );

        AttributeModifier rebuiltModifier =
                rebuilt.get(TEST_ATTRIBUTE).iterator().next();
        assertSame(TEST_ATTRIBUTE, rebuilt.keySet().iterator().next());
        assertNotSame(originalModifier, rebuiltModifier);
        assertNotEquals(originalModifier.getId(), rebuiltModifier.getId());
        assertEquals(originalModifier.getName(), rebuiltModifier.getName());
        assertEquals(originalModifier.getAmount(), rebuiltModifier.getAmount());
        assertEquals(originalModifier.getOperation(), rebuiltModifier.getOperation());
    }

    @Test
    void sameSlotAndOriginalEntriesProduceStableUuids() {
        Multimap<Attribute, AttributeModifier> original = twoModifiers();

        var first = new ArrayList<>(
                IronCurioAttributeCompat.rebuild(
                                SLOT_ZERO, original, Attribute::getDescriptionId)
                        .get(TEST_ATTRIBUTE));
        var second = new ArrayList<>(
                IronCurioAttributeCompat.rebuild(
                                SLOT_ZERO, original, Attribute::getDescriptionId)
                        .get(TEST_ATTRIBUTE));

        assertEquals(first.get(0).getId(), second.get(0).getId());
        assertEquals(first.get(1).getId(), second.get(1).getId());
        assertNotEquals(first.get(0).getId(), first.get(1).getId());
    }

    @Test
    void differentCuriosSlotUuidsCannotCollideAtTheSameIndex() {
        Multimap<Attribute, AttributeModifier> original = twoModifiers();
        var firstSlot = new ArrayList<>(
                IronCurioAttributeCompat.rebuild(
                                SLOT_ZERO, original, Attribute::getDescriptionId)
                        .get(TEST_ATTRIBUTE));
        var secondSlot = new ArrayList<>(
                IronCurioAttributeCompat.rebuild(
                                SLOT_ONE, original, Attribute::getDescriptionId)
                        .get(TEST_ATTRIBUTE));

        assertNotEquals(firstSlot.get(0).getId(), secondSlot.get(0).getId());
        assertNotEquals(firstSlot.get(1).getId(), secondSlot.get(1).getId());
    }

    @Test
    void rebuiltMapIsImmutable() {
        Multimap<Attribute, AttributeModifier> rebuilt =
                IronCurioAttributeCompat.rebuild(
                        SLOT_ZERO,
                        twoModifiers(),
                        Attribute::getDescriptionId
                );

        assertFalse(rebuilt.isEmpty());
        org.junit.jupiter.api.Assertions.assertThrows(
                UnsupportedOperationException.class,
                rebuilt::clear
        );
    }

    private static Multimap<Attribute, AttributeModifier> twoModifiers() {
        return ImmutableMultimap.<Attribute, AttributeModifier>builder()
                .put(TEST_ATTRIBUTE, new AttributeModifier(
                        ORIGINAL_ADDITION,
                        "irons_spellbooks:addition",
                        25.0D,
                        AttributeModifier.Operation.ADDITION
                ))
                .put(TEST_ATTRIBUTE, new AttributeModifier(
                        ORIGINAL_MULTIPLY,
                        "irons_spellbooks:multiply",
                        0.15D,
                        AttributeModifier.Operation.MULTIPLY_BASE
                ))
                .build();
    }
}
