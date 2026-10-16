package com.carrot123.until_eternity.trial;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;

/** Loader-independent validation so malformed datapacks cannot reach runtime state. */
final class TrialWaveDocument {
    private TrialWaveDocument() {}

    static Definition parse(JsonElement raw) {
        JsonObject root = raw.getAsJsonObject();
        JsonArray array = root.getAsJsonArray("waves");
        if (array == null || array.isEmpty() || array.size() > 64)
            throw new IllegalArgumentException("waves must contain 1..64 definitions");
        int waveCount = root.get("wave_count").getAsInt();
        if (waveCount < 1 || waveCount > array.size()) throw new IllegalArgumentException("invalid wave_count " + waveCount);
        List<Wave> waves = new ArrayList<>();
        for (JsonElement waveRaw : array) {
            JsonObject object = waveRaw.getAsJsonObject();
            if (object.has("reward")) throw new IllegalArgumentException("per-wave reward is no longer supported");
            int prepare = object.get("prepare_ticks").getAsInt();
            int time = object.get("time_ticks").getAsInt();
            if (prepare < 0 || prepare > 72000) throw new IllegalArgumentException("invalid prepare_ticks " + prepare);
            if (time <= 0 || time > 72000) throw new IllegalArgumentException("invalid time_ticks " + time);
            JsonArray mobsRaw = object.getAsJsonArray("mobs");
            if (mobsRaw == null || mobsRaw.isEmpty()) throw new IllegalArgumentException("wave needs mobs");
            List<Mob> mobs = new ArrayList<>();
            int total = 0;
            for (JsonElement mobRaw : mobsRaw) {
                JsonObject entry = mobRaw.getAsJsonObject();
                String id = entry.get("id").getAsString();
                if (!id.matches("[a-z0-9_.-]+:[a-z0-9_./-]+")) throw new IllegalArgumentException("invalid entity ID " + id);
                int count = entry.get("count").getAsInt();
                if (count <= 0 || count > 64) throw new IllegalArgumentException("invalid mob count " + count);
                total += count;
                if (total > 128) throw new IllegalArgumentException("too many mobs in one wave");
                mobs.add(new Mob(id, count));
            }
            waves.add(new Wave(prepare, time, List.copyOf(mobs)));
        }
        return new Definition(waveCount, List.copyOf(waves));
    }

    record Definition(int waveCount, List<Wave> waves) {}
    record Wave(int prepareTicks, int timeTicks, List<Mob> mobs) {}
    record Mob(String id, int count) {}
}
