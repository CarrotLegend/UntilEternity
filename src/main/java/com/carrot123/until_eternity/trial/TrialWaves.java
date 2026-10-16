package com.carrot123.until_eternity.trial;

import com.carrot123.until_eternity.until_eternity;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
            JsonArray array = raw.getAsJsonObject().getAsJsonArray("waves");
            if (array == null || array.isEmpty()) throw new IllegalArgumentException("waves must not be empty");
            List<Wave> waves = new ArrayList<>();
            for (JsonElement waveRaw : array) {
                JsonObject object = waveRaw.getAsJsonObject();
                int time = object.get("time_ticks").getAsInt();
                if (time <= 0 || time > 72000) throw new IllegalArgumentException("invalid time_ticks: " + time);
                JsonArray mobsRaw = object.getAsJsonArray("mobs");
                if (mobsRaw == null || mobsRaw.isEmpty()) throw new IllegalArgumentException("wave needs mobs");
                List<MobSpec> mobs = new ArrayList<>();
                for (JsonElement mobRaw : mobsRaw) {
                    JsonObject mobObject = mobRaw.getAsJsonObject();
                    ResourceLocation mobId = requireId(mobObject.get("id").getAsString());
                    int count = mobObject.get("count").getAsInt();
                    EntityType<?> entityType = BuiltInRegistries.ENTITY_TYPE.getOptional(mobId)
                            .orElseThrow(() -> new IllegalArgumentException("unknown entity " + mobId));
                    if (count <= 0 || count > 64) throw new IllegalArgumentException("invalid mob count " + count);
                    mobs.add(new MobSpec(entityType, count));
                }
                JsonObject rewardRaw = object.getAsJsonObject("reward");
                ResourceLocation rewardId = requireId(rewardRaw.get("id").getAsString());
                Item reward = BuiltInRegistries.ITEM.getOptional(rewardId)
                        .orElseThrow(() -> new IllegalArgumentException("unknown item " + rewardId));
                int rewardCount = rewardRaw.get("count").getAsInt();
                if (rewardCount <= 0 || rewardCount > reward.getMaxStackSize())
                    throw new IllegalArgumentException("invalid reward count " + rewardCount);
                waves.add(new Wave(time, List.copyOf(mobs), reward, rewardCount));
            }
            current = new Config(List.copyOf(waves));
            until_eternity.LOGGER.info("Loaded {} trial waves", waves.size());
        } catch (RuntimeException e) {
            until_eternity.LOGGER.error("Invalid trial wave definition {}. Keeping previous valid configuration.", ID, e);
        }
    }

    private static ResourceLocation requireId(String raw) {
        ResourceLocation id = ResourceLocation.tryParse(raw);
        if (id == null) throw new IllegalArgumentException("invalid resource ID " + raw);
        return id;
    }
    public record Config(List<Wave> waves) {}
    public record Wave(int timeTicks, List<MobSpec> mobs, Item reward, int rewardCount) {}
    public record MobSpec(EntityType<?> type, int count) {}
}
