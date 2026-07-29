package com.carrot123.until_eternity.item.curio;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurioItem;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

/**
 * Common Curios implementation for item-owned, slot-independent definitions.
 *
 * <p>Definitions do not depend on a slot name or index. Modifier instances use
 * the stable UUID supplied by Curios for the equipped slot plus the definition
 * key, allowing duplicate items in different slots to stack without collisions.</p>
 */
public class BaseModCurioItem extends Item implements ICurioItem {
    private final ResourceLocation itemId;
    private final List<CurioAttributeSpec> modifierSpecs;

    protected BaseModCurioItem(
            Properties properties,
            ResourceLocation itemId,
            Collection<CurioAttributeSpec> modifierSpecs
    ) {
        super(properties.stacksTo(1));
        this.itemId = Objects.requireNonNull(itemId, "itemId");
        this.modifierSpecs = List.copyOf(modifierSpecs);
        validateUniqueKeys(this.modifierSpecs);
    }

    @Override
    public final Multimap<Attribute, AttributeModifier> getAttributeModifiers(
            SlotContext slotContext,
            UUID slotUuid,
            ItemStack stack
    ) {
        if (slotContext != null && slotContext.cosmetic()) {
            return ImmutableMultimap.of();
        }
        ImmutableMultimap.Builder<Attribute, AttributeModifier> builder =
                ImmutableMultimap.builder();
        for (CurioAttributeSpec spec : modifierSpecs) {
            Attribute attribute = spec.resolveAttribute();
            if (attribute != null) {
                builder.put(attribute, new AttributeModifier(
                        createModifierUuid(slotUuid, spec.modifierKey()),
                        itemId + "/curio_attribute/" + spec.modifierKey(),
                        spec.amount(),
                        spec.operation()
                ));
            }
        }
        return builder.build();
    }

    public final ResourceLocation getCurioItemId() {
        return itemId;
    }

    public final List<CurioAttributeSpec> getModifierSpecs() {
        return modifierSpecs;
    }

    public static UUID createModifierUuid(
            UUID slotUuid,
            String modifierKey
    ) {
        return CurioModifierId.create(slotUuid, modifierKey);
    }

    private static void validateUniqueKeys(List<CurioAttributeSpec> specs) {
        Set<String> keys = new HashSet<>();
        for (CurioAttributeSpec spec : specs) {
            if (!keys.add(spec.modifierKey())) {
                throw new IllegalArgumentException(
                        "Duplicate Curio attribute modifier key: "
                                + spec.modifierKey());
            }
        }
    }
}
