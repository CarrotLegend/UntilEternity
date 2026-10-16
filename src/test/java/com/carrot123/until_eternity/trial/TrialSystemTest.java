package com.carrot123.until_eternity.trial;

import com.google.gson.JsonParser;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.AABB;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TrialSystemTest {
    @Test void circleRejectsTransparentCornersAndAcceptsCenter() {
        assertFalse(TrialPortalGeometry.intersectsDisk(1.8, 1.9, 1.8, 1.9));
        assertTrue(TrialPortalGeometry.intersectsDisk(-0.3, 0.3, -0.9, 0.9));
        assertTrue(TrialPortalGeometry.intersectsDisk(2.0, 2.1, 0, 0.1));
        assertFalse(TrialPortalGeometry.intersectsDisk(2.01, 2.1, 0, 0.1));
        BlockPos anchor = new BlockPos(0, 65, 0);
        assertFalse(TrialPortalGeometry.intersects(anchor, net.minecraft.core.Direction.Axis.X,
                new AABB(3.8, 69.1, 0.4, 3.9, 69.2, 0.6)));
        assertTrue(TrialPortalGeometry.intersects(anchor, net.minecraft.core.Direction.Axis.X,
                new AABB(1.7, 66, 0.4, 2.3, 67.8, 0.6)));
    }

    @Test void waveCountAndPrepareAreDataDriven() {
        for (int count : new int[]{1, 3, 5}) {
            TrialWaveDocument.Definition config = TrialWaveDocument.parse(JsonParser.parseString(config(count, 100)));
            assertEquals(count, config.waveCount());
            assertEquals(5, config.waves().size());
            assertEquals(60, config.waves().get(0).prepareTicks());
            if (count > 1) assertEquals(100, config.waves().get(1).prepareTicks());
        }
        TrialWaveDocument.Definition zero = TrialWaveDocument.parse(JsonParser.parseString(config(2, 0)));
        assertEquals(0, zero.waves().get(1).prepareTicks());
    }

    @Test void invalidConfigurationsAreRejected() {
        assertThrows(RuntimeException.class, () -> TrialWaveDocument.parse(JsonParser.parseString(config(0, 100))));
        assertThrows(RuntimeException.class, () -> TrialWaveDocument.parse(JsonParser.parseString(config(6, 100))));
        assertThrows(RuntimeException.class, () -> TrialWaveDocument.parse(JsonParser.parseString(config(2, -1))));
        assertThrows(RuntimeException.class, () -> TrialWaveDocument.parse(JsonParser.parseString(config(2, 100).replace("\"time_ticks\":2400", "\"time_ticks\":0"))));
        assertThrows(RuntimeException.class, () -> TrialWaveDocument.parse(JsonParser.parseString(config(2, 100).replace("\"count\":1", "\"count\":65"))));
        assertThrows(RuntimeException.class, () -> TrialWaveDocument.parse(JsonParser.parseString(config(2, 100).replace("minecraft:zombie", "Invalid:mob"))));
        assertThrows(RuntimeException.class, () -> TrialWaveDocument.parse(JsonParser.parseString(config(2, 100).replace("\"mobs\"", "\"reward\":{},\"mobs\""))));
    }

    @Test void completionCanOnlySettleOnce() {
        assertTrue(TrialMonumentBlockEntity.canComplete(TrialMonumentBlockEntity.Phase.ACTIVE, false, false));
        assertFalse(TrialMonumentBlockEntity.canComplete(TrialMonumentBlockEntity.Phase.COMPLETE, false, true));
        assertFalse(TrialMonumentBlockEntity.canComplete(TrialMonumentBlockEntity.Phase.ACTIVE, true, false));
        assertFalse(TrialMonumentBlockEntity.canComplete(TrialMonumentBlockEntity.Phase.ACTIVE, false, true));
    }

    @Test void difficultyMultipliersAndLegacyNameAreStable() {
        assertEquals(2.0, TrialDifficulty.EASY.healthMultiplier());
        assertEquals(4.0, TrialDifficulty.NORMAL.healthMultiplier());
        assertEquals(6.0, TrialDifficulty.HARD.healthMultiplier());
        assertEquals(10.0, TrialDifficulty.MALICE.healthMultiplier());
        assertEquals(1.0, TrialDifficulty.EASY.damageMultiplier());
        assertEquals(1.0, TrialDifficulty.NORMAL.damageMultiplier());
        assertEquals(1.5, TrialDifficulty.HARD.damageMultiplier());
        assertEquals(3.0, TrialDifficulty.MALICE.damageMultiplier());
        assertEquals(1, TrialDifficulty.EASY.rewardCount());
        assertEquals(2, TrialDifficulty.NORMAL.rewardCount());
        assertEquals(3, TrialDifficulty.HARD.rewardCount());
        assertEquals(4, TrialDifficulty.MALICE.rewardCount());
        assertNull(TrialDifficulty.byNetworkId(-1));
        assertNull(TrialDifficulty.byNetworkId(4));
        assertEquals(TrialDifficulty.NORMAL, TrialDifficulty.fromSaved(""));
        assertEquals(2, TrialDifficulty.fromSaved("").rewardCount());
        assertEquals(TrialDifficulty.MALICE, TrialDifficulty.fromSaved("MALICE"));
    }

    @Test void pendingRewardMustBeClaimedBeforeAnotherRun() {
        assertTrue(TrialMonumentBlockEntity.canSelect(TrialMonumentBlockEntity.Phase.IDLE, false, false));
        assertTrue(TrialMonumentBlockEntity.canSelect(TrialMonumentBlockEntity.Phase.FAILED, false, false));
        assertFalse(TrialMonumentBlockEntity.canSelect(TrialMonumentBlockEntity.Phase.PREPARE, false, false));
        assertFalse(TrialMonumentBlockEntity.canSelect(TrialMonumentBlockEntity.Phase.ACTIVE, false, false));
        assertFalse(TrialMonumentBlockEntity.canSelect(TrialMonumentBlockEntity.Phase.COMPLETE, true, false));
        assertTrue(TrialMonumentBlockEntity.canClaim(TrialMonumentBlockEntity.Phase.COMPLETE, true, false));
        assertFalse(TrialMonumentBlockEntity.canClaim(TrialMonumentBlockEntity.Phase.COMPLETE, false, true));
        assertTrue(TrialMonumentBlockEntity.canSelect(TrialMonumentBlockEntity.Phase.COMPLETE, false, true));
    }

    private static String config(int count, int secondPrepare) {
        StringBuilder waves = new StringBuilder();
        for (int i = 0; i < 5; i++) {
            if (i != 0) waves.append(',');
            waves.append("{\"prepare_ticks\":").append(i == 0 ? 60 : secondPrepare)
                    .append(",\"time_ticks\":2400,\"mobs\":[{\"id\":\"minecraft:zombie\",\"count\":1}]}");
        }
        return "{\"wave_count\":" + count + ",\"waves\":[" + waves + "]}";
    }
}
