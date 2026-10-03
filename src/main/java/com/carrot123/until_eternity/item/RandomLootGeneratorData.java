package com.carrot123.until_eternity.item;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;

import java.util.List;

final class RandomLootGeneratorData {

    static final List<ResourceLocation> LOOT_TABLE_IDS = List.of(
            chest("abandoned_mineshaft"),
            chest("village/village_armorer"),
            chest("village/village_butcher"),
            chest("village/village_cartographer"),
            chest("village/village_desert_house"),
            chest("village/village_fisher"),
            chest("village/village_fletcher"),
            chest("village/village_mason"),
            chest("village/village_plains_house"),
            chest("village/village_savanna_house"),
            chest("village/village_shepherd"),
            chest("village/village_snowy_house"),
            chest("village/village_taiga_house"),
            chest("village/village_tannery"),
            chest("village/village_temple"),
            chest("village/village_toolsmith"),
            chest("village/village_weaponsmith"),
            chest("ancient_city"),
            chest("simple_dungeon"),
            chest("stronghold_corridor"),
            chest("stronghold_crossing"),
            chest("stronghold_library"),
            chest("ruined_portal"),
            chest("shipwreck_map"),
            chest("shipwreck_supply"),
            chest("shipwreck_treasure")
    );

    private RandomLootGeneratorData() {
    }

    static ResourceLocation getRandomLootTable(RandomSource random) {
        return LOOT_TABLE_IDS.get(
                random.nextInt(LOOT_TABLE_IDS.size())
        );
    }

    private static ResourceLocation chest(String path) {
        return new ResourceLocation(
                "minecraft",
                "chests/" + path
        );
    }
}