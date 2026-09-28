package com.carrot123.until_eternity.loot;

import com.carrot123.until_eternity.until_eternity;
import java.util.Set;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.entries.LootTableReference;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraftforge.event.LootTableLoadEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(
        modid = until_eternity.MODID,
        bus = Mod.EventBusSubscriber.Bus.FORGE
)
public final class AccessoryLootInjection {

    private static final float INJECTION_CHANCE = 0.75F;

    private static final ResourceLocation ACCESSORY_LOOT_TABLE =
            new ResourceLocation(
                    until_eternity.MODID,
                    "chests/accessory_cache"
            );

    private static final Set<ResourceLocation> TARGET_TABLES = Set.of(
            new ResourceLocation(
                    "minecraft",
                    "chests/simple_dungeon"
            ),
            new ResourceLocation(
                    "minecraft",
                    "chests/abandoned_mineshaft"
            ),
            new ResourceLocation(
                    "minecraft",
                    "chests/ancient_city"
            ),
            new ResourceLocation(
                    "minecraft",
                    "chests/ancient_city_ice_box"
            ),
            new ResourceLocation(
                    "minecraft",
                    "chests/stronghold_corridor"
            ),
            new ResourceLocation(
                    "minecraft",
                    "chests/stronghold_crossing"
            ),
            new ResourceLocation(
                    "minecraft",
                    "chests/stronghold_library"
            )
    );

    private AccessoryLootInjection() {
    }

    @SubscribeEvent
    public static void onLootTableLoad(
            LootTableLoadEvent event
    ) {
        if (!TARGET_TABLES.contains(event.getName())) {
            return;
        }

        LootPool pool = LootPool.lootPool()
                .name("until_eternity_accessory_cache")
                .setRolls(ConstantValue.exactly(1.0F))
                .when(
                        LootItemRandomChanceCondition.randomChance(
                                INJECTION_CHANCE
                        )
                )
                .add(
                        LootTableReference.lootTableReference(
                                ACCESSORY_LOOT_TABLE
                        )
                )
                .build();

        event.getTable().addPool(pool);
    }
}