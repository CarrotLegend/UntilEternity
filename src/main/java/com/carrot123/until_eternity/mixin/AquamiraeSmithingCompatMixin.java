package com.carrot123.until_eternity.mixin;

import com.google.common.collect.Multimap;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.SmithingTransformRecipe;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Mixin(value = SmithingTransformRecipe.class, remap = false)
public abstract class AquamiraeSmithingCompatMixin {

    @Inject(
            method = {
                    "assemble(Lnet/minecraft/world/Container;Lnet/minecraft/core/RegistryAccess;)Lnet/minecraft/world/item/ItemStack;",
                    "m_5874_(Lnet/minecraft/world/Container;Lnet/minecraft/core/RegistryAccess;)Lnet/minecraft/world/item/ItemStack;"
            },
            at = @At("RETURN"),
            remap = false
    )
    private void untilEternity$fixAquamiraeTreasureArmor(
            Container container,
            RegistryAccess registryAccess,
            CallbackInfoReturnable<ItemStack> cir
    ) {
        ItemStack base = container.getItem(1);
        ItemStack result = cir.getReturnValue();

        if (base.isEmpty() || result.isEmpty()) {
            return;
        }

        if (base.getItem() == result.getItem()) {
            return;
        }

        if (!untilEternity$isAquamiraeTreasureArmor(base)) {
            return;
        }

        CompoundTag baseTag = base.getTag();

        if (baseTag == null) {
            return;
        }

        ListTag baseModifiers = baseTag.getList(
                "AttributeModifiers",
                Tag.TAG_COMPOUND
        );

        ListTag preservedBonuses = new ListTag();

        for (int i = 0; i < baseModifiers.size(); i++) {
            CompoundTag modifierTag = baseModifiers.getCompound(i);

            if ("base_bonus".equals(modifierTag.getString("Name"))) {
                preservedBonuses.add(modifierTag.copy());
            }
        }

        if (preservedBonuses.isEmpty()) {
            return;
        }

        ItemStack cleanResult = result.copy();
        CompoundTag cleanTag = cleanResult.getOrCreateTag();

        cleanTag.remove("AttributeModifiers");

        List<SlotModifier> defaultModifiers = new ArrayList<>();
        boolean hasArmorAttribute = false;

        for (EquipmentSlot slot : EquipmentSlot.values()) {
            Multimap<Attribute, AttributeModifier> modifiers =
                    cleanResult.getAttributeModifiers(slot);

            for (Map.Entry<Attribute, AttributeModifier> entry : modifiers.entries()) {
                defaultModifiers.add(
                        new SlotModifier(
                                slot,
                                entry.getKey(),
                                entry.getValue()
                        )
                );

                if (entry.getKey() == Attributes.ARMOR) {
                    hasArmorAttribute = true;
                }
            }
        }

        if (!hasArmorAttribute) {
            return;
        }

        ListTag rebuiltModifiers = new ListTag();

        for (SlotModifier entry : defaultModifiers) {
            ResourceLocation attributeId =
                    BuiltInRegistries.ATTRIBUTE.getKey(entry.attribute());

            if (attributeId == null) {
                continue;
            }

            CompoundTag modifierTag = entry.modifier().save();

            modifierTag.putString(
                    "AttributeName",
                    attributeId.toString()
            );

            modifierTag.putString(
                    "Slot",
                    entry.slot().getName()
            );

            rebuiltModifiers.add(modifierTag);
        }

        for (int i = 0; i < preservedBonuses.size(); i++) {
            rebuiltModifiers.add(
                    preservedBonuses.getCompound(i).copy()
            );
        }

        result.getOrCreateTag().put(
                "AttributeModifiers",
                rebuiltModifiers
        );
    }

    private static boolean untilEternity$isAquamiraeTreasureArmor(
            ItemStack stack
    ) {
        Item item = stack.getItem();

        if (!untilEternity$isTreasureBaseItem(item)) {
            return false;
        }

        CompoundTag tag = stack.getTag();

        if (tag == null
                || !tag.contains(
                        "AttributeModifiers",
                        Tag.TAG_LIST
                )) {
            return false;
        }

        ListTag modifiers = tag.getList(
                "AttributeModifiers",
                Tag.TAG_COMPOUND
        );

        boolean hasBaseArmor = false;
        boolean hasBaseBonus = false;

        for (int i = 0; i < modifiers.size(); i++) {
            CompoundTag modifier = modifiers.getCompound(i);

            String name = modifier.getString("Name");
            String attributeName =
                    modifier.getString("AttributeName");

            if ("base_armor".equals(name)
                    && "minecraft:generic.armor".equals(attributeName)) {
                hasBaseArmor = true;
            }

            if ("base_bonus".equals(name)) {
                hasBaseBonus = true;
            }
        }

        return hasBaseArmor && hasBaseBonus;
    }

    private static boolean untilEternity$isTreasureBaseItem(
            Item item
    ) {
        return item == Items.IRON_HELMET
                || item == Items.IRON_CHESTPLATE
                || item == Items.IRON_LEGGINGS
                || item == Items.IRON_BOOTS
                || item == Items.LEATHER_HELMET
                || item == Items.LEATHER_CHESTPLATE
                || item == Items.LEATHER_LEGGINGS
                || item == Items.LEATHER_BOOTS;
    }

    private record SlotModifier(
            EquipmentSlot slot,
            Attribute attribute,
            AttributeModifier modifier
    ) {
    }
}