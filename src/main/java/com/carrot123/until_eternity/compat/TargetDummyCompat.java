package com.carrot123.until_eternity.compat;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.registries.ForgeRegistries;

public final class TargetDummyCompat {

    private static final ResourceLocation DUMMMMMMY_TARGET_DUMMY =
            new ResourceLocation("dummmmmmy", "target_dummy");

    private TargetDummyCompat() {
    }

    public static boolean isTargetDummy(Entity entity) {
        if (entity == null) {
            return false;
        }

        ResourceLocation id =
                ForgeRegistries.ENTITY_TYPES.getKey(entity.getType());

        return DUMMMMMMY_TARGET_DUMMY.equals(id);
    }
}