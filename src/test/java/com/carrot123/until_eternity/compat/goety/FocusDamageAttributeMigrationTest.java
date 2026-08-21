package com.carrot123.until_eternity.compat.goety;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FocusDamageAttributeMigrationTest {
    @Test
    void onlyTheLegacyZeroBaseRequiresMigration() {
        assertTrue(FocusDamageAttributeMigration.shouldMigrate(0.0D));
        assertTrue(FocusDamageAttributeMigration.shouldMigrate(-0.0D));
        assertFalse(FocusDamageAttributeMigration.shouldMigrate(1.0D));
        assertFalse(FocusDamageAttributeMigration.shouldMigrate(0.5D));
        assertFalse(FocusDamageAttributeMigration.shouldMigrate(Double.NaN));
    }

    @Test
    void loginMigrationUsesPersistedVersionMarkerAndNoModifier()
            throws Exception {
        String source = Files.readString(Path.of(
                "src", "main", "java", "com", "carrot123",
                "until_eternity", "compat", "goety",
                "FocusDamageAttributeMigration.java"));
        assertTrue(source.contains("PlayerLoggedInEvent"));
        assertTrue(source.contains("Player.PERSISTED_NBT_TAG"));
        assertTrue(source.contains("focus_damage_multiplier_v1"));
        assertTrue(source.contains("focusDamage.setBaseValue(1.0D)"));
        assertFalse(source.contains("addPermanentModifier"));
        assertFalse(source.contains("addTransientModifier"));
    }
}
