package com.carrot123.until_eternity.event;

import com.carrot123.until_eternity.until_eternity;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.ItemAttributeModifierEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.UUID;

@Mod.EventBusSubscriber(modid = until_eternity.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class GoetyCompatEvents {

    private static final UUID FOCUS_BAG_UUID = UUID.fromString("c3d4e5f6-a7b8-9012-cdef-123456789003");
    private static final UUID FOCUS_PACK_UUID = UUID.fromString("d4e5f6a7-b8c9-0123-defa-234567890014");

    @SubscribeEvent
    public static void onItemAttributeModifier(ItemAttributeModifierEvent event) {
        ItemStack stack = event.getItemStack();
        ResourceLocation id = ForgeRegistries.ITEMS.getKey(stack.getItem());
        if (id == null) return;

        // 聚晶包: +1 巫法强度乘数 (Goety Revelation)
        if (id.equals(new ResourceLocation("goety", "focus_bag"))) {
            addSpellPowerMultiplier(event, FOCUS_BAG_UUID, 1.0);
        }
        // 多晶大袋: +1.5 巫法强度乘数 (Goety Revelation)
        else if (id.equals(new ResourceLocation("goety", "focus_pack"))) {
            addSpellPowerMultiplier(event, FOCUS_PACK_UUID, 1.5);
        }
    }

    private static void addSpellPowerMultiplier(ItemAttributeModifierEvent event, UUID uuid, double amount) {
        Attribute spellPower = ForgeRegistries.ATTRIBUTES.getValue(
                new ResourceLocation("goety_revelation", "spell_power"));
        if (spellPower != null) {
            event.addModifier(spellPower, new AttributeModifier(
                    uuid, "Goety compat spell power multiplier",
                    amount, AttributeModifier.Operation.MULTIPLY_BASE));
        }
    }
}
