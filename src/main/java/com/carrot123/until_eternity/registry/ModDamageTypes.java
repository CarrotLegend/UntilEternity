package com.carrot123.until_eternity.registry;

import com.carrot123.until_eternity.until_eternity;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.chat.Component;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;

public final class ModDamageTypes {
    public static final ResourceKey<DamageType> SACRIFICE = ResourceKey.create(
            Registries.DAMAGE_TYPE,
            new ResourceLocation(until_eternity.MODID, "sacrifice"));
    public static final ResourceKey<DamageType> BYPASS_ALL = ResourceKey.create(
            Registries.DAMAGE_TYPE,
            new ResourceLocation(until_eternity.MODID, "bypass_all"));

    private ModDamageTypes() {
    }

    public static DamageSource sacrifice(Level level) {
        Holder.Reference<DamageType> type = level.registryAccess()
                .registryOrThrow(Registries.DAMAGE_TYPE)
                .getHolderOrThrow(SACRIFICE);
        return new DamageSource(type);
    }

    public static DamageSource bypassAll(
            Level level,
            @Nullable Player attacker
    ) {
        Holder.Reference<DamageType> type = level.registryAccess()
                .registryOrThrow(Registries.DAMAGE_TYPE)
                .getHolderOrThrow(BYPASS_ALL);
        return new BypassAllDamageSource(type, attacker);
    }

    private static final class BypassAllDamageSource extends DamageSource {
        private BypassAllDamageSource(
                Holder<DamageType> type,
                @Nullable Player attacker
        ) {
            super(type, attacker, attacker);
        }

        @Override
        public Component getLocalizedDeathMessage(LivingEntity target) {
            if (getEntity() != null) {
                return Component.translatable(
                        "death.attack." + getMsgId() + ".player",
                        target.getDisplayName(),
                        getEntity().getDisplayName());
            }
            return Component.translatable(
                    "death.attack." + getMsgId(),
                    target.getDisplayName());
        }
    }
}
