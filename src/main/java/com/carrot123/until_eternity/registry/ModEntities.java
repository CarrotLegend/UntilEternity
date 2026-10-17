package com.carrot123.until_eternity.registry;

import com.carrot123.until_eternity.entity.StormPriestessEntity;
import com.carrot123.until_eternity.until_eternity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

@Mod.EventBusSubscriber(modid = until_eternity.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModEntities {
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, until_eternity.MODID);

    public static final RegistryObject<EntityType<StormPriestessEntity>> STORM_PRIESTESS =
            ENTITY_TYPES.register("storm_priestess", () -> EntityType.Builder
                    .of(StormPriestessEntity::new, MobCategory.CREATURE)
                    .sized(0.6F, 1.95F)
                    .clientTrackingRange(10)
                    .build(until_eternity.MODID + ":storm_priestess"));

    public static void register(IEventBus eventBus) {
        ENTITY_TYPES.register(eventBus);
    }

    @SubscribeEvent
    public static void onCreateAttributes(EntityAttributeCreationEvent event) {
        event.put(STORM_PRIESTESS.get(), StormPriestessEntity.createAttributes().build());
    }
}
