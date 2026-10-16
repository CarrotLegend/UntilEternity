package com.carrot123.until_eternity.compat.goetyrevelation;

import com.Polarice3.Goety.common.entities.ModEntityType;
import com.Polarice3.Goety.common.entities.boss.Apostle;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.MobSpawnType;
import z1gned.goetyrevelation.config.ModConfig;
import z1gned.goetyrevelation.util.ApollyonAbilityHelper;

/** Mirrors the boss initialization in Revelation's summon, without its intermediate entity. */
public final class TrialApollyonFactory {
    private TrialApollyonFactory() {}

    public static Apostle create(ServerLevel level) {
        return new Apostle(ModEntityType.APOSTLE.get(), level);
    }

    public static void initialize(Apostle apostle, ServerLevel level) {
        ((ApollyonAbilityHelper) apostle).allTitlesApostle_1_20_1$setApollyon(true);
        apostle.setHealth(ModConfig.APOLLYON_HEALTH.get().floatValue());
        apostle.finalizeSpawn(level, level.getCurrentDifficultyAt(apostle.blockPosition()),
                MobSpawnType.MOB_SUMMONED, null, null);
    }
}
