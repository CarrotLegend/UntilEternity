package com.carrot123.until_eternity.client.portal;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PortalVisualTrackerTest {
    @AfterEach
    void resetTracker() {
        PortalVisualTracker.reset();
    }

    @Test
    void customTypesReplaceEachOther() {
        PortalVisualTracker.mark(PortalVisualType.CHAOS);
        assertEquals(PortalVisualType.CHAOS, PortalVisualTracker.currentType());

        PortalVisualTracker.mark(PortalVisualType.IMMORTAL);
        assertEquals(PortalVisualType.IMMORTAL, PortalVisualTracker.currentType());
    }

    @Test
    void firstZeroIntensityObservationKeepsPendingActivation() {
        PortalVisualTracker.mark(PortalVisualType.CHAOS);

        PortalVisualTracker.updateFromVanillaEffect(0.0F, 0.0F);

        assertEquals(PortalVisualType.CHAOS, PortalVisualTracker.currentType());
        PortalVisualTracker.updateFromVanillaEffect(0.0F, 0.0F);
        assertEquals(PortalVisualType.NONE, PortalVisualTracker.currentType());
    }

    @Test
    void eitherVanillaIntensityKeepsTheLastCustomType() {
        PortalVisualTracker.mark(PortalVisualType.IMMORTAL);

        PortalVisualTracker.updateFromVanillaEffect(0.25F, 0.0F);
        PortalVisualTracker.updateFromVanillaEffect(0.0F, 0.25F);

        assertEquals(PortalVisualType.IMMORTAL, PortalVisualTracker.currentType());
        PortalVisualTracker.updateFromVanillaEffect(0.0F, 0.0F);
        assertEquals(PortalVisualType.NONE, PortalVisualTracker.currentType());
    }

    @Test
    void explicitResetRestoresVanillaVisualType() {
        PortalVisualTracker.mark(PortalVisualType.CHAOS);

        PortalVisualTracker.reset();

        assertEquals(PortalVisualType.NONE, PortalVisualTracker.currentType());
    }
}
