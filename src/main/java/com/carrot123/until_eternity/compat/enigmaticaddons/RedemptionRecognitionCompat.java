package com.carrot123.until_eternity.compat.enigmaticaddons;

import com.aizistral.enigmaticlegacy.handlers.SuperpositionHandler;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.ForgeRegistries;

public final class RedemptionRecognitionCompat {

    private static final ResourceLocation BLESS_RING =
            new ResourceLocation("enigmaticaddons", "bless_ring");

    private static final String BLESS_SPAWN =
            "BlessNextSpawn";

    private RedemptionRecognitionCompat() {
    }

    public static boolean hasBlessStatus(Player player) {
        if (player == null) {
            return false;
        }

        if (player.getPersistentData().getBoolean(BLESS_SPAWN)) {
            return true;
        }

        Item blessRing = ForgeRegistries.ITEMS.getValue(BLESS_RING);

        return blessRing != null
                && SuperpositionHandler.hasCurio(player, blessRing);
    }

    public static boolean canUseRecognizedRelic(Player player) {
        return player != null
                && (
                hasBlessStatus(player)
                        || SuperpositionHandler.isTheCursedOne(player)
        );
    }
}