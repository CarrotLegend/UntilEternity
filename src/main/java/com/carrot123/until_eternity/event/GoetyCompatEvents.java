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

@Mod.EventBusSubscriber(modid = until_eternity.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class GoetyCompatEvents {

    private static final UUID FOCUS_BAG_UUID = UUID.fromString("d5e6f7a8-9b01-1c23-5678-901234ef0123");
    private static final UUID FOCUS_PACK_UUID = UUID.fromString("e6f7a8b9-0c12-2d34-6789-012345f01234");

    @SubscribeEvent
    public static void onItemAttributeModifier(ItemAttributeModifierEvent event) {
        ItemStack stack = event.getItemStack();
        ResourceLocation id = ForgeRegistries.ITEMS.getKey(stack.getItem());
        if (id == null) return;

        // 聚晶包: +1 巫法强度乘数
        if (id.equals(new ResourceLocation("goety", "focus_bag"))) {
            addSpellPotencyMultiplier(event, FOCUS_BAG_UUID, 1.0);
        }
        // 多晶大袋: +1.5 巫法强度乘数
        else if (id.equals(new ResourceLocation("goety", "focus_pack"))) {
            addSpellPotencyMultiplier(event, FOCUS_PACK_UUID, 1.5);
        }
    }

    private static void addSpellPotencyMultiplier(ItemAttributeModifierEvent event, UUID uuid, double amount) {
        Attribute spellPotency = ForgeRegistries.ATTRIBUTES.getValue(
                new ResourceLocation("goety", "spell_potency"));
        if (spellPotency != null) {
            event.addModifier(spellPotency, new AttributeModifier(
                    uuid, "Goety compat spell potency multiplier",
                    amount, AttributeModifier.Operation.MULTIPLY_BASE));
        }
    }
}
