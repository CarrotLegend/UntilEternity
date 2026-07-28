package com.carrot123.until_eternity.item.curio;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.ForgeRegistries;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurioItem;

import javax.annotation.Nullable;
import java.util.List;
import java.util.UUID;

public class DarkCageItem extends Item implements ICurioItem {

    private static final UUID BODY_SLOT_UUID = UUID.fromString("a0b1c2d3-e4f5-6789-abcd-ef0123456700");
    private static final UUID SPELL_POWER_UUID = UUID.fromString("a1b2c3d4-e5f6-7890-abcd-ef0123456701");
    private static final UUID SPELL_POTENCY_UUID = UUID.fromString("b2c3d4e5-f6a7-8901-bcde-f01234567802");

    public DarkCageItem() {
        super(new Properties().stacksTo(1).rarity(Rarity.EPIC).fireResistant());
    }

    @Override
    public Multimap<Attribute, AttributeModifier> getAttributeModifiers(SlotContext slotContext, UUID uuid, ItemStack stack) {
        return buildModifiers(uuid);
    }

    @Override
    public Multimap<Attribute, AttributeModifier> getAttributeModifiers(String identifier, ItemStack stack) {
        return buildModifiers(BODY_SLOT_UUID);
    }

    private Multimap<Attribute, AttributeModifier> buildModifiers(UUID slotUuid) {
        Multimap<Attribute, AttributeModifier> modifiers = ArrayListMultimap.create();

        // +1 胸饰栏位
        CuriosApi.addSlotModifier(modifiers, "body", slotUuid, 1, AttributeModifier.Operation.ADDITION);

        // +1 巫法强度 (Goety Revelation)
        Attribute spellPower = ForgeRegistries.ATTRIBUTES.getValue(
                new ResourceLocation("goety_revelation", "spell_power"));
        if (spellPower != null) {
            modifiers.put(spellPower, new AttributeModifier(
                    SPELL_POWER_UUID, "Dark cage spell power",
                    1.0, AttributeModifier.Operation.ADDITION));
        }

        // +15% 法术强效 (Goety)
        Attribute spellPotency = ForgeRegistries.ATTRIBUTES.getValue(
                new ResourceLocation("goety", "spell_potency"));
        if (spellPotency != null) {
            modifiers.put(spellPotency, new AttributeModifier(
                    SPELL_POTENCY_UUID, "Dark cage spell potency",
                    0.15, AttributeModifier.Operation.MULTIPLY_BASE));
        }

        return modifiers;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level,
                                List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("tooltip.until_eternity.dark_cage.line1")
                .withStyle(ChatFormatting.GOLD));
        tooltip.add(Component.translatable("tooltip.until_eternity.dark_cage.line2")
                .withStyle(ChatFormatting.GRAY));
    }
}
