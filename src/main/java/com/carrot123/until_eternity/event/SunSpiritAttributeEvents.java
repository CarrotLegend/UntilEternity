package com.carrot123.until_eternity.event;

import com.carrot123.until_eternity.until_eternity;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraftforge.event.entity.EntityAttributeModificationEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

@Mod.EventBusSubscriber(
        modid = until_eternity.MODID,
        bus = Mod.EventBusSubscriber.Bus.MOD
)
public final class SunSpiritAttributeEvents {

    private static final ResourceLocation SUN_SPIRIT_ID =
            new ResourceLocation(
                    "aether",
                    "sun_spirit"
            );

    private SunSpiritAttributeEvents() {
    }

    @SubscribeEvent
    @SuppressWarnings("unchecked")
    public static void modifyAttributes(
            EntityAttributeModificationEvent event
    ) {
        EntityType<?> rawType =
                ForgeRegistries.ENTITY_TYPES
                        .getValue(
                                SUN_SPIRIT_ID
                        );

        if (rawType == null) {
            return;
        }

        EntityType<? extends LivingEntity> type =
                (EntityType<? extends LivingEntity>) rawType;

        if (!event.has(
                type,
                Attributes.ARMOR
        )) {
            event.add(
                    type,
                    Attributes.ARMOR,
                    5.0D
            );
        }

        if (!event.has(
                type,
                Attributes.ATTACK_DAMAGE
        )) {
            event.add(
                    type,
                    Attributes.ATTACK_DAMAGE,
                    6.0D
            );
        }
    }
}