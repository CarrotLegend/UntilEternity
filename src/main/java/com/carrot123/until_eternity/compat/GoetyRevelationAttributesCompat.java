package com.carrot123.until_eternity.compat;

import com.mojang.logging.LogUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.registries.ForgeRegistries;
import org.slf4j.Logger;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

public final class GoetyRevelationAttributesCompat {
    public static final ResourceLocation DAMAGE_RESISTANCE =
            new ResourceLocation("goety_revelation", "resistance");
    public static final ResourceLocation ARMOR_PENETRATION =
            new ResourceLocation("goety_revelation", "armor_penetration");
    public static final ResourceLocation ENCHANTMENT_PIERCING =
            new ResourceLocation("goety_revelation", "enchantment_piercing");
    public static final ResourceLocation SPELL_COOLDOWN =
            new ResourceLocation("goety_revelation", "spell_cooldown");
    public static final ResourceLocation SPELL_POWER =
            new ResourceLocation("goety_revelation", "spell_power");
    public static final ResourceLocation SPELL_POWER_MULTIPLIER =
            new ResourceLocation("goety_revelation", "spell_power_multiplier");
    public static final ResourceLocation SOUL_DECREASE_REDUCTION =
            new ResourceLocation("goety_revelation", "soul_decrease_reduction");
    public static final ResourceLocation SOUL_INCREASE_EFFICIENCY =
            new ResourceLocation("goety_revelation", "soul_increase_efficiency");

    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Map<ResourceLocation, Attribute> CACHE = new HashMap<>();
    private static final Set<ResourceLocation> MISSING_WARNED = ConcurrentHashMap.newKeySet();
    private static final AtomicBoolean MISSING_MODS_WARNED = new AtomicBoolean();

    private GoetyRevelationAttributesCompat() {
    }

    @Nullable
    public static Attribute resolve(ResourceLocation id) {
        if (!ModList.get().isLoaded("goety_revelation")
                || !ModList.get().isLoaded("revelationfix")) {
            if (MISSING_MODS_WARNED.compareAndSet(false, true)) {
                LOGGER.warn("Goety Revelation attribute integration is disabled because "
                        + "Goety Revelation or RevelationFix is not loaded");
            }
            return null;
        }

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
            LOGGER.error("Goety Revelation attribute {} is not registered", id);
        }
        return null;
    }
}
