package com.carrot123.until_eternity.registry;

import com.carrot123.until_eternity.effect.ManaEruptionEffect;
import com.carrot123.until_eternity.effect.ImmortalScarEffect;
import com.carrot123.until_eternity.until_eternity;
import net.minecraft.world.effect.MobEffect;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModMobEffects {
    public static final DeferredRegister<MobEffect> MOB_EFFECTS =
            DeferredRegister.create(ForgeRegistries.MOB_EFFECTS, until_eternity.MODID);

    public static final RegistryObject<MobEffect> MANA_ERUPTION =
            MOB_EFFECTS.register("mana_eruption", ManaEruptionEffect::new);

    public static final RegistryObject<MobEffect> IMMORTAL_SCAR =
            MOB_EFFECTS.register("immortal_scar", ImmortalScarEffect::new);

    private ModMobEffects() {
    }

    public static void register(IEventBus eventBus) {
        MOB_EFFECTS.register(eventBus);
    }
}
