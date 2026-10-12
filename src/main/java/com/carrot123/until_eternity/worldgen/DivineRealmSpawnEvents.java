package com.carrot123.until_eternity.worldgen;

import com.carrot123.until_eternity.until_eternity;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.entity.living.MobSpawnEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = until_eternity.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class DivineRealmSpawnEvents {
    public static final ResourceKey<Level> DIVINE_REALM = ResourceKey.create(
            Registries.DIMENSION,
            new ResourceLocation(until_eternity.MODID, "divine_realm")
    );

    private DivineRealmSpawnEvents() {
    }

    @SubscribeEvent
    public static void onSpawnPlacementCheck(MobSpawnEvent.SpawnPlacementCheck event) {
        if (!DIVINE_REALM.equals(event.getLevel().getLevel().dimension())) {
            return;
        }

        MobSpawnType type = event.getSpawnType();
        if (type == MobSpawnType.NATURAL
                || type == MobSpawnType.CHUNK_GENERATION
                || type == MobSpawnType.PATROL
                || type == MobSpawnType.PHANTOM) {
            event.setResult(Event.Result.DENY);
        }
    }
}
