package com.carrot123.until_eternity.compat.ironsspellbooks;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraftforge.registries.ForgeRegistries;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Function;

public final class IronCurioAttributeCompat {
    private static final String SEED_NAMESPACE = "irons_spellbooks";

    private IronCurioAttributeCompat() {
    }

    public static Multimap<Attribute, AttributeModifier> rebuild(
            UUID slotUuid,
            Multimap<Attribute, AttributeModifier> original
    ) {
        return rebuild(slotUuid, original, IronCurioAttributeCompat::attributeKey);
    }

    static Multimap<Attribute, AttributeModifier> rebuild(
            UUID slotUuid,
            Multimap<Attribute, AttributeModifier> original,
            Function<Attribute, String> attributeKeyResolver
    ) {
        Objects.requireNonNull(slotUuid, "Curios slot UUID");
        Objects.requireNonNull(original, "Iron's Spells attribute modifiers");
        Objects.requireNonNull(attributeKeyResolver, "attribute key resolver");

        ImmutableMultimap.Builder<Attribute, AttributeModifier> result =
                ImmutableMultimap.builder();
        int ordinal = 0;

        for (Map.Entry<Attribute, AttributeModifier> entry : original.entries()) {
            Attribute attribute = entry.getKey();
            AttributeModifier modifier = entry.getValue();
            UUID modifierUuid = deriveModifierUuid(
                    slotUuid,
                    attributeKeyResolver.apply(attribute),
                    modifier.getId(),
                    modifier.getName(),
                    modifier.getOperation(),
                    ordinal++
            );

            result.put(attribute, new AttributeModifier(
                    modifierUuid,
                    modifier.getName(),
                    modifier.getAmount(),
                    modifier.getOperation()
            ));
        }

        return result.build();
    }

    static UUID deriveModifierUuid(
            UUID slotUuid,
            String attributeKey,
            UUID originalModifierUuid,
            String modifierName,
            AttributeModifier.Operation operation,
            int ordinal
    ) {
        String seed = slotUuid
                + "|" + SEED_NAMESPACE
                + "|" + attributeKey
                + "|" + originalModifierUuid
                + "|" + modifierName
                + "|" + operation.name()
                + "|" + ordinal;
        return UUID.nameUUIDFromBytes(seed.getBytes(StandardCharsets.UTF_8));
    }

    private static String attributeKey(Attribute attribute) {
        ResourceLocation attributeId = ForgeRegistries.ATTRIBUTES.getKey(attribute);
        return attributeId != null ? attributeId.toString() : attribute.getDescriptionId();
    }
}
