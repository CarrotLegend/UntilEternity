
package com.carrot123.until_eternity.event;

import com.carrot123.until_eternity.compat.goetyrevelation.VenomousWhipUpgradeHelper;
import com.carrot123.until_eternity.until_eternity;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraftforge.event.ItemAttributeModifierEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

@Mod.EventBusSubscriber(
        modid = until_eternity.MODID,
        bus = Mod.EventBusSubscriber.Bus.FORGE
)
public final class VenomousWhipAttributeEvents {

    private static final ResourceLocation SERVANT_DAMAGE =
            new ResourceLocation(
                    "eternal_career",
                    "servant_damage"
            );

    private static final UUID MODIFIER_ID =
            UUID.nameUUIDFromBytes(
                    "until_eternity:venomous_whip/servant_damage"
                            .getBytes(StandardCharsets.UTF_8)
            );

    private VenomousWhipAttributeEvents() {
    }

    @SubscribeEvent
    public static void onItemAttributeModifiers(
            ItemAttributeModifierEvent event
    ) {
        if (event.getSlotType() != EquipmentSlot.MAINHAND) {
            return;
        }

        if (!VenomousWhipUpgradeHelper.isWhip(
                event.getItemStack()
        )) {
            return;
        }

        Attribute attribute =
                ForgeRegistries.ATTRIBUTES.getValue(
                        SERVANT_DAMAGE
                );

        if (attribute == null) {
            return;
        }

        double bonus =
                VenomousWhipUpgradeHelper.getBonus(
                        event.getItemStack()
                );

        event.addModifier(
                attribute,
                new AttributeModifier(
                        MODIFIER_ID,
                        "until_eternity.venomous_whip.servant_damage",
                        bonus,
                        AttributeModifier.Operation.MULTIPLY_BASE
                )
        );
    }
}
