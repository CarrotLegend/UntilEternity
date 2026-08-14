package com.carrot123.until_eternity.event;

import com.carrot123.until_eternity.until_eternity;
import com.carrot123.until_eternity.worldgen.ImmortalDimensions;
import com.eeeab.eeeabsmobs.sever.init.EntityInit;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.monster.Monster;
import net.minecraftforge.event.entity.living.MobSpawnEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.EnumSet;
import java.util.Set;

@Mod.EventBusSubscriber(modid = until_eternity.MODID,
        bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class ImmortalSpawnEvents {
    private static final Set<MobSpawnType> NATURAL_ECOLOGY_TYPES =
            EnumSet.of(
                    MobSpawnType.NATURAL,
                    MobSpawnType.CHUNK_GENERATION,
                    MobSpawnType.STRUCTURE,
                    MobSpawnType.PATROL,
                    MobSpawnType.REINFORCEMENT,
                    MobSpawnType.JOCKEY,
                    MobSpawnType.EVENT,
                    MobSpawnType.TRIGGERED
            );

    private ImmortalSpawnEvents() {
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onSpawnPlacement(
            MobSpawnEvent.SpawnPlacementCheck event) {
        if (!isImmortalDimension(event.getLevel())) {
            return;
        }

        if (!isNaturalEcology(event.getSpawnType())) {
            return;
        }

        if (!isAllowedEntity(event.getEntityType())) {
            event.setResult(Event.Result.DENY);
            return;
        }

        boolean validWithoutLight = checkAnyLightMonsterRules(
                event.getEntityType(), event);
        event.setResult(validWithoutLight
                ? Event.Result.ALLOW
                : Event.Result.DENY);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onFinalizeSpawn(MobSpawnEvent.FinalizeSpawn event) {
        if (isImmortalDimension(event.getLevel())
                && isNaturalEcology(event.getSpawnType())
                && !isAllowedEntity(event.getEntity().getType())) {
            event.setSpawnCancelled(true);
        }
    }

    static boolean isNaturalEcology(MobSpawnType spawnType) {
        return NATURAL_ECOLOGY_TYPES.contains(spawnType);
    }

    static boolean isAllowedEntity(EntityType<?> entityType) {
        return entityType == EntityInit.IMMORTAL_SKELETON.get()
                || entityType == EntityInit.IMMORTAL_KNIGHT.get()
                || entityType == EntityInit.IMMORTAL_EXECUTIONER.get();
    }

    private static boolean isImmortalDimension(
            net.minecraft.world.level.ServerLevelAccessor level) {
        return level.getLevel().dimension()
                .equals(ImmortalDimensions.IMMORTAL_DIMENSION);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static boolean checkAnyLightMonsterRules(
            EntityType<?> entityType,
            MobSpawnEvent.SpawnPlacementCheck event) {
        return Monster.checkAnyLightMonsterSpawnRules(
                (EntityType) entityType,
                event.getLevel(),
                event.getSpawnType(),
                event.getPos(),
                event.getRandom());
    }
}
