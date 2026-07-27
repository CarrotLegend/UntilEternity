package com.carrot123.until_eternity.item.curio;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraftforge.registries.ForgeRegistries;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurioItem;

import java.util.UUID;

public class MithrilGlovesItem extends Item implements ICurioItem {

    private static final UUID MAGIC_DAMAGE_UUID = UUID.fromString("b3c4d5e6-7f80-9a01-3456-789012cdef01");
    private static final UUID MAGIC_RING_SLOT_UUID = UUID.fromString("c4d5e6f7-8a90-0b12-4567-890123def012");

    public MithrilGlovesItem() {
        super(new Properties().stacksTo(1).rarity(Rarity.EPIC).fireResistant());
    }

    @Override
    public Multimap<Attribute, AttributeModifier> getAttributeModifiers(SlotContext slotContext, UUID uuid, ItemStack stack) {
        Multimap<Attribute, AttributeModifier> modifiers = ArrayListMultimap.create();

        // +10% 魔法伤害
        Attribute spellPotency = ForgeRegistries.ATTRIBUTES.getValue(
                new ResourceLocation("goety", "spell_potency"));
        if (spellPotency != null) {
            modifiers.put(spellPotency, new AttributeModifier(
                    MAGIC_DAMAGE_UUID, "Mithril gloves magic damage",
                    0.10, AttributeModifier.Operation.MULTIPLY_BASE));
        }

        // +8 法戒槽位
        CuriosApi.addSlotModifier(modifiers, "magic_ring", uuid, 8, AttributeModifier.Operation.ADDITION);

        return modifiers;
    }
}
