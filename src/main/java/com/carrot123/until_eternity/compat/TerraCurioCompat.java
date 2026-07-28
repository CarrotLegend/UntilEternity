package com.carrot123.until_eternity.compat;

import com.mojang.logging.LogUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraftforge.registries.ForgeRegistries;
import org.slf4j.Logger;

import javax.annotation.Nullable;
import java.util.concurrent.atomic.AtomicBoolean;

public final class TerraCurioCompat {
    public static final ResourceLocation ARMOR_PASS =
            new ResourceLocation("terra_curio", "armor_pass");
    public static final ResourceLocation ANKH_SHIELD =
            new ResourceLocation("terra_curio", "ankh_shield");
    public static final ResourceLocation SHARK_TOOTH_NECKLACE =
            new ResourceLocation("terra_curio", "shark_tooth_necklace");

    private static final Logger LOGGER = LogUtils.getLogger();
    private static final AtomicBoolean MISSING_WARNED = new AtomicBoolean();
    private static volatile Attribute cachedArmorPass;

    private TerraCurioCompat() {
    }

    @Nullable
    public static Attribute armorPass() {
        Attribute cached = cachedArmorPass;
        if (cached != null) {
            return cached;
        }

        synchronized (TerraCurioCompat.class) {
            if (cachedArmorPass == null) {
                cachedArmorPass = ForgeRegistries.ATTRIBUTES.getValue(ARMOR_PASS);
            }
            cached = cachedArmorPass;
        }

        if (cached == null && MISSING_WARNED.compareAndSet(false, true)) {
            LOGGER.error("Required Terra Curio attribute {} is not registered", ARMOR_PASS);
        }
        return cached;
    }
}
