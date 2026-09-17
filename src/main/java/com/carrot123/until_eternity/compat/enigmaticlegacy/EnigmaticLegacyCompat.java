package com.carrot123.until_eternity.compat.enigmaticlegacy;

import net.minecraftforge.fml.ModList;

public final class EnigmaticLegacyCompat {
    private EnigmaticLegacyCompat() {
    }

    public static void registerIfLoaded() {
        if (ModList.get().isLoaded("enigmaticlegacy")) {
            CursedScrollAttackSpeedEvents.register();
        }
    }
}
