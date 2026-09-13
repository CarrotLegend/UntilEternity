package com.carrot123.until_eternity.compat.goetyrevelation;

import java.lang.reflect.Field;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import com.carrot123.until_eternity.until_eternity;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.ForgeRegistries;

public final class GoetyRevelationPuzzleWhitelist {
    private static final String PUZZLE_ITEMS_CLASS =
            "com.mega.revelationfix.common.odamane.common.TheEndPuzzleItems";
    private static final int REQUIRED_PUZZLE_COUNT = 4;

    public static final Set<ResourceLocation> ALLOWED_PUZZLES = Set.of(
            new ResourceLocation("minecraft", "chorus_flower"),
            new ResourceLocation("goety", "blazing_horn"),
            new ResourceLocation("goety", "shadow_essence"),
            new ResourceLocation("minecraft", "totem_of_undying"),
            new ResourceLocation("goety", "withered_manuscript"),
            new ResourceLocation("minecraft:sniffer_egg"),
            new ResourceLocation("goety:philosophers_stone"),
            new ResourceLocation("minecraft:end_stone"),
            new ResourceLocation("goety:night_beacon"),
            new ResourceLocation("goety:pithos"),
            new ResourceLocation("minecraft:echo_shard"),
            new ResourceLocation("minecraft:bed"),
            new ResourceLocation("goety:arca"),
            new ResourceLocation("goety:animation_core"),
            new ResourceLocation("minecraft:glow_lichen"),
            new ResourceLocation("goety:totem_of_souls"),
            new ResourceLocation("minecraft:beacon"),
            new ResourceLocation("goety:forbidden_piece"),
            new ResourceLocation("minecraft:end_crystal"),
            new ResourceLocation("minecraft:bone"),
            new ResourceLocation("minecraft:iron_ingot"),
            new ResourceLocation("minecraft:red_flower"),
            new ResourceLocation("goety:shriek_obelisk"),
            new ResourceLocation("goety:animator"),
            new ResourceLocation("goety:ectoplasm"),
            new ResourceLocation("goety:tunnel_focus"),
            new ResourceLocation("goety:black_book"),
            new ResourceLocation("goety:infernal_tome"),
            new ResourceLocation("goety:raging_matter"),
            new ResourceLocation("minecraft:deadbush"),
            new ResourceLocation("minecraft:recovery_compass"),
            new ResourceLocation("irons_spellbooks:tarnished_helmet"),
            new ResourceLocation("irons_spellbooks:permafrost_shard"),
            new ResourceLocation("irons_spellbooks:invisibility_ring"),
            new ResourceLocation("irons_spellbooks:arcane_debris"),
            new ResourceLocation("irons_spellbooks:ruined_book"),
            new ResourceLocation("cataclysm:ignitium_block"),
            new ResourceLocation("cataclysm:aptrgangr_head"),
            new ResourceLocation("cataclysm:void_lantern_block"),
            new ResourceLocation("aquamirae:rune_of_the_storm"),
            new ResourceLocation("enigmaticlegacy:cosmic_cake")
    );

    private GoetyRevelationPuzzleWhitelist() {
    }

    public static void filterBakedPuzzlePool() {
        try {
            Class<?> puzzleItemsClass = Class.forName(PUZZLE_ITEMS_CLASS);
            Field puzzleItemsField = puzzleItemsClass.getField("puzzleItems");
            Object fieldValue = puzzleItemsField.get(null);
            if (!(fieldValue instanceof Map<?, ?> puzzleItems)) {
                until_eternity.LOGGER.error(
                        "Goety Revelation puzzleItems field is not a Map; puzzle whitelist was not applied");
                return;
            }

            Set<ResourceLocation> availableAllowedIds = new LinkedHashSet<>();
            for (Object key : puzzleItems.keySet()) {
                if (key instanceof Item item) {
                    ResourceLocation id = ForgeRegistries.ITEMS.getKey(item);
                    if (id != null && ALLOWED_PUZZLES.contains(id)) {
                        availableAllowedIds.add(id);
                    }
                }
            }

            if (availableAllowedIds.size() < REQUIRED_PUZZLE_COUNT) {
                Set<ResourceLocation> unavailableIds =
                        new LinkedHashSet<>(ALLOWED_PUZZLES);
                unavailableIds.removeAll(availableAllowedIds);
                until_eternity.LOGGER.error(
                        "Goety Revelation puzzle whitelist has only {} valid entries; at least {} are required. "
                                + "The original puzzle pool will be used. Missing or unavailable entries: {}",
                        availableAllowedIds.size(),
                        REQUIRED_PUZZLE_COUNT,
                        unavailableIds);
                return;
            }

            puzzleItems.keySet().removeIf(key -> {
                if (!(key instanceof Item item)) {
                    return true;
                }
                ResourceLocation id = ForgeRegistries.ITEMS.getKey(item);
                return id == null || !availableAllowedIds.contains(id);
            });

            until_eternity.LOGGER.info(
                    "Applied Goety Revelation puzzle whitelist with {} entries: {}",
                    puzzleItems.size(),
                    availableAllowedIds);
        } catch (ReflectiveOperationException | RuntimeException exception) {
            until_eternity.LOGGER.error(
                    "Failed to apply the Goety Revelation puzzle whitelist; the original puzzle pool will be used",
                    exception);
        }
    }
}
