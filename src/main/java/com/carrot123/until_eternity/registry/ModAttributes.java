package com.carrot123.until_eternity.registry;

import com.carrot123.until_eternity.until_eternity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;
import net.minecraftforge.event.entity.EntityAttributeModificationEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModAttributes {
    public static final String FOCUS_DAMAGE_DESCRIPTION_ID =
            "attribute.name.until_eternity.focus_damage";

    public static final DeferredRegister<Attribute> ATTRIBUTES =
            DeferredRegister.create(ForgeRegistries.ATTRIBUTES,
                    until_eternity.MODID);

    public static final RegistryObject<Attribute> FOCUS_DAMAGE =
            ATTRIBUTES.register("focus_damage", () -> new RangedAttribute(
                    FOCUS_DAMAGE_DESCRIPTION_ID,
                    1.0D,
                    0.0D,
                    32767.0D).setSyncable(true));

    private ModAttributes() {
    }

    public static void register(IEventBus modEventBus) {
        ATTRIBUTES.register(modEventBus);
    }

    @Mod.EventBusSubscriber(
            modid = until_eternity.MODID,
            bus = Mod.EventBusSubscriber.Bus.MOD)
    public static final class PlayerAttributes {
        private PlayerAttributes() {
        }

        @SubscribeEvent
        public static void addPlayerAttributes(
                EntityAttributeModificationEvent event
        ) {
            event.add(EntityType.PLAYER, FOCUS_DAMAGE.get());
        }
    }
}
