package com.carrot123.until_eternity.registry;

import com.carrot123.until_eternity.until_eternity;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.level.Level;

public final class ModDamageTypes {
    public static final ResourceKey<DamageType> SACRIFICE = ResourceKey.create(
            Registries.DAMAGE_TYPE,
            new ResourceLocation(until_eternity.MODID, "sacrifice"));

    private ModDamageTypes() {
    }

    public static DamageSource sacrifice(Level level) {
        Holder.Reference<DamageType> type = level.registryAccess()
                .registryOrThrow(Registries.DAMAGE_TYPE)
                .getHolderOrThrow(SACRIFICE);
        return new DamageSource(type);
    }
}
