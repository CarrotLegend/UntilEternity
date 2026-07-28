package com.carrot123.until_eternity.item.curio;

import com.carrot123.until_eternity.compat.TerraCurioCompat;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class CurioMutualExclusionHandlerTest {
    @Test
    void shieldGroupIncludesBothLocalShieldsAndTerraAnkhShield() {
        Set<ResourceLocation> group =
                CurioMutualExclusionHandler.findGroup(id("empowered_shield"));

        assertEquals(Set.of(
                id("empowered_shield"),
                id("cosmic_aegis"),
                TerraCurioCompat.ANKH_SHIELD
        ), group);
    }

    @Test
    void sharkGroupIncludesBothLocalNecklacesAndTerraNecklace() {
        Set<ResourceLocation> group =
                CurioMutualExclusionHandler.findGroup(id("reaper_tooth_necklace"));

        assertEquals(Set.of(
                id("reaper_tooth_necklace"),
                id("sand_shark_tooth_necklace"),
                TerraCurioCompat.SHARK_TOOTH_NECKLACE
        ), group);
    }

    @Test
    void lifeGroupIsSeparateFromShieldAndSharkGroups() {
        Set<ResourceLocation> life =
                CurioMutualExclusionHandler.findGroup(id("regenerator"));
        Set<ResourceLocation> shield =
                CurioMutualExclusionHandler.findGroup(id("cosmic_aegis"));
        Set<ResourceLocation> shark =
                CurioMutualExclusionHandler.findGroup(id("sand_shark_tooth_necklace"));

        assertEquals(Set.of(id("regenerator"), id("guttering_candle")), life);
        assertNotEquals(life, shield);
        assertNotEquals(life, shark);
        assertNull(CurioMutualExclusionHandler.findGroup(id("elemental_gauntlet")));
    }

    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath("until_eternity", path);
    }
}
