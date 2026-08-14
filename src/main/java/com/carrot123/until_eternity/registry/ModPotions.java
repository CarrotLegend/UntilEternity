package com.carrot123.until_eternity.registry;

import com.carrot123.until_eternity.until_eternity;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModPotions {
    public static final String DISPLAY_NAME = "until_eternity.mana_eruption";
    public static final int LONG_DURATION = 20 * 60 * 10;
    public static final int STRONG_DURATION = 20 * 60 * 5;

    public static final DeferredRegister<Potion> POTIONS =
            DeferredRegister.create(ForgeRegistries.POTIONS, until_eternity.MODID);

    public static final RegistryObject<Potion> MANA_ERUPTION_LONG =
            POTIONS.register(
                    "mana_eruption_long",
                    () -> new Potion(
                            DISPLAY_NAME,
                            new MobEffectInstance(
                                    ModMobEffects.MANA_ERUPTION.get(),
                                    LONG_DURATION,
                                    0)));

    public static final RegistryObject<Potion> MANA_ERUPTION_STRONG =
            POTIONS.register(
                    "mana_eruption_strong",
                    () -> new Potion(
                            DISPLAY_NAME,
                            new MobEffectInstance(
                                    ModMobEffects.MANA_ERUPTION.get(),
                                    STRONG_DURATION,
                                    1)));

    private ModPotions() {
    }

    public static void register(IEventBus eventBus) {
        POTIONS.register(eventBus);
    }
}
