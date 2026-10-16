package com.carrot123.until_eternity.trial;

import com.carrot123.until_eternity.until_eternity;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.EntityType;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

@Mod.EventBusSubscriber(modid = until_eternity.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class TrialWaves extends SimpleJsonResourceReloadListener {
    private static final ResourceLocation ID = new ResourceLocation(until_eternity.MODID, "boss_rush");
    private static volatile Config current;
    private TrialWaves() { super(new Gson(), "trial_waves"); }
    public static Config current() { return current; }

    @SubscribeEvent public static void onReloadRegistration(AddReloadListenerEvent event) {
        event.addListener(new TrialWaves());
    }

    @Override protected void apply(Map<ResourceLocation, JsonElement> files, ResourceManager manager, ProfilerFiller profiler) {
        JsonElement raw = files.get(ID);
        if (raw == null) {
            until_eternity.LOGGER.error("Missing trial wave definition {}. Keeping previous valid configuration.", ID);
            return;
        }
        try {
            current = parse(raw, id -> BuiltInRegistries.ENTITY_TYPE.getOptional(id)
                    .orElseThrow(() -> new IllegalArgumentException("unknown entity " + id)));
            until_eternity.LOGGER.info("Loaded {} of {} trial waves", current.waves().size(), current.definedCount());
        } catch (RuntimeException e) {
            until_eternity.LOGGER.error("Invalid trial wave definition {}. Keeping previous valid configuration.", ID, e);
        }
    }

    static Config parse(JsonElement raw, Function<ResourceLocation, EntityType<?>> resolver) {
            TrialWaveDocument.Definition document = TrialWaveDocument.parse(raw);
            List<Wave> waves = new ArrayList<>();
            for (TrialWaveDocument.Wave source : document.waves()) {
                List<MobSpec> mobs = new ArrayList<>();
                for (TrialWaveDocument.Mob mob : source.mobs()) {
                    ResourceLocation mobId = requireId(mob.id());
                    EntityType<?> entityType = resolver.apply(mobId);
                    if (entityType == null || !entityType.canSummon())
                        throw new IllegalArgumentException("entity is not a summonable mob " + mobId);
                    mobs.add(new MobSpec(entityType, mob.count()));
                }
                waves.add(new Wave(source.prepareTicks(), source.timeTicks(), List.copyOf(mobs)));
            }
            return new Config(List.copyOf(waves.subList(0, document.waveCount())), waves.size());
    }

    private static ResourceLocation requireId(String raw) {
        ResourceLocation id = ResourceLocation.tryParse(raw);
        if (id == null) throw new IllegalArgumentException("invalid resource ID " + raw);
        return id;
    }
    public record Config(List<Wave> waves, int definedCount) {}
    public record Wave(int prepareTicks, int timeTicks, List<MobSpec> mobs) {}
    public record MobSpec(EntityType<?> type, int count) {}
}
