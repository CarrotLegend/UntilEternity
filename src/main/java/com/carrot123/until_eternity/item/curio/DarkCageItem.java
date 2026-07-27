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

    private static final UUID SPELL_POTENCY_UUID = UUID.fromString("f1a2b3c4-5d6e-7f80-1234-567890abcdef");
    private static final UUID MAGIC_DAMAGE_UUID = UUID.fromString("a2b3c4d5-6e7f-8a90-2345-678901bcdef0");

    public DarkCageItem() {
        super(new Properties().stacksTo(1).rarity(Rarity.EPIC).fireResistant());
    }

    @Override
    public Multimap<Attribute, AttributeModifier> getAttributeModifiers(SlotContext slotContext, UUID uuid, ItemStack stack) {
        Multimap<Attribute, AttributeModifier> modifiers = ArrayListMultimap.create();

        // +1 胸饰栏位
        CuriosApi.addSlotModifier(modifiers, "chest", uuid, 1, AttributeModifier.Operation.ADDITION);

        // +1 巫法强度
        Attribute spellPotency = ForgeRegistries.ATTRIBUTES.getValue(
                new ResourceLocation("goety", "spell_potency"));
        if (spellPotency != null) {
            modifiers.put(spellPotency, new AttributeModifier(
                    SPELL_POTENCY_UUID, "Dark cage spell potency",
                    1.0, AttributeModifier.Operation.ADDITION));
        }

        // +5% 魔法伤害
        Attribute magicDamage = ForgeRegistries.ATTRIBUTES.getValue(
                new ResourceLocation("goety", "spell_potency"));
        if (magicDamage != null) {
            modifiers.put(magicDamage, new AttributeModifier(
                    MAGIC_DAMAGE_UUID, "Dark cage magic damage",
                    0.05, AttributeModifier.Operation.MULTIPLY_BASE));
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
