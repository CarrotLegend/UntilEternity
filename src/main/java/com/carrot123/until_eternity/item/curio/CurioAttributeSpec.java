package com.carrot123.until_eternity.item.curio;

import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import top.theillusivec4.curios.api.SlotAttribute;

import javax.annotation.Nullable;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * A slot-independent attribute modifier supplied by a Curio item.
 */
public record CurioAttributeSpec(
        Supplier<? extends Attribute> attribute,
        String modifierKey,
        double amount,
        AttributeModifier.Operation operation
) {
    public CurioAttributeSpec {
        Objects.requireNonNull(attribute, "attribute");
        Objects.requireNonNull(modifierKey, "modifierKey");
        Objects.requireNonNull(operation, "operation");
        if (modifierKey.isBlank()) {
            throw new IllegalArgumentException("modifierKey must not be blank");
        }
        if (!Double.isFinite(amount)) {
            throw new IllegalArgumentException("amount must be finite");
        }
    }

    public static CurioAttributeSpec of(
            Supplier<? extends Attribute> attribute,
            String modifierKey,
            double amount,
            AttributeModifier.Operation operation
    ) {
        return new CurioAttributeSpec(attribute, modifierKey, amount, operation);
    }

    public static CurioAttributeSpec slot(
            String slotIdentifier,
            String modifierKey,
            double amount,
            AttributeModifier.Operation operation
    ) {
        Objects.requireNonNull(slotIdentifier, "slotIdentifier");
        return of(
                () -> SlotAttribute.getOrCreate(slotIdentifier),
                modifierKey,
                amount,
                operation
        );
    }

    @Nullable
    Attribute resolveAttribute() {
        return attribute.get();
    }
}
