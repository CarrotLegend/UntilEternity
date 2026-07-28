package com.carrot123.until_eternity.compat;

import com.mojang.logging.LogUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraftforge.registries.ForgeRegistries;
import org.slf4j.Logger;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public final class PuffishAttributesCompat {
    public static final ResourceLocation MELEE_DAMAGE =
            new ResourceLocation("puffish_attributes", "melee_damage");
    public static final ResourceLocation HEALING =
            new ResourceLocation("puffish_attributes", "healing");
    public static final ResourceLocation KNOCKBACK =
            new ResourceLocation("puffish_attributes", "knockback");

    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Map<ResourceLocation, Attribute> CACHE = new HashMap<>();
    private static final Set<ResourceLocation> MISSING_WARNED = ConcurrentHashMap.newKeySet();

    private PuffishAttributesCompat() {
    }

    @Nullable
    public static Attribute resolve(ResourceLocation id) {
        synchronized (CACHE) {
            Attribute cached = CACHE.get(id);
            if (cached != null) {
                return cached;
            }

            Attribute attribute = ForgeRegistries.ATTRIBUTES.getValue(id);
            if (attribute != null) {
                CACHE.put(id, attribute);
                return attribute;
            }
        }

        if (MISSING_WARNED.add(id)) {
            LOGGER.error("Required Pufferfish attribute {} is not registered", id);
        }
        return null;
    }
}
