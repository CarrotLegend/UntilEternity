package com.carrot123.until_eternity.item.curio;

import com.carrot123.until_eternity.registry.ModAttributes;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;
import java.util.List;

public class DarkCageItem extends BaseModCurioItem {
    static final double FOCUS_DAMAGE_AMOUNT = 0.13D;
    private static final ResourceLocation ITEM_ID =
            new ResourceLocation("until_eternity", "dark_cage");
    private static final ResourceLocation SPELL_POWER =
            new ResourceLocation("goety_revelation", "spell_power");

    public DarkCageItem() {
        super(
                new Properties().rarity(Rarity.EPIC).fireResistant(),
                ITEM_ID,
                List.of(
                        CurioAttributeSpec.slot(
                                "body",
                                "body_slots",
                                1.0D,
                                AttributeModifier.Operation.ADDITION),
                        CurioAttributeSpec.of(
                                () -> ForgeRegistries.ATTRIBUTES.getValue(SPELL_POWER),
                                "spell_power",
                                1.0D,
                                AttributeModifier.Operation.ADDITION),
                        CurioAttributeSpec.of(
                                ModAttributes.FOCUS_DAMAGE,
                                "dark_cage_focus_damage",
                                FOCUS_DAMAGE_AMOUNT,
                                AttributeModifier.Operation.ADDITION)
                )
        );
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
