package com.carrot123.until_eternity.loot;

import com.carrot123.until_eternity.until_eternity;
import com.mojang.serialization.Codec;
import net.minecraftforge.common.loot.IGlobalLootModifier;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModLootModifiers {
    public static final DeferredRegister<Codec<? extends IGlobalLootModifier>>
            LOOT_MODIFIER_SERIALIZERS = DeferredRegister.create(
                    ForgeRegistries.Keys.GLOBAL_LOOT_MODIFIER_SERIALIZERS,
                    until_eternity.MODID);

    public static final RegistryObject<Codec<? extends IGlobalLootModifier>>
            PLENITUDE_LOOT = LOOT_MODIFIER_SERIALIZERS.register(
                    "plenitude_loot",
                    () -> PlenitudeLootModifier.CODEC);

    private ModLootModifiers() {
    }

    public static void register(IEventBus eventBus) {
        LOOT_MODIFIER_SERIALIZERS.register(eventBus);
    }
}
