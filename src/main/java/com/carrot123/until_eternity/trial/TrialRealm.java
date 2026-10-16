package com.carrot123.until_eternity.trial;

import com.carrot123.until_eternity.until_eternity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

public final class TrialRealm {
    public static final ResourceKey<Level> KEY = ResourceKey.create(Registries.DIMENSION,
            new ResourceLocation(until_eternity.MODID, "trial_realm"));
    public static final BlockPos ENTRY = new BlockPos(0, 65, 0);
    public static final BlockPos RETURN_PORTAL = new BlockPos(5, 65, 0);
    private TrialRealm() {}
}
