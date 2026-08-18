package com.carrot123.until_eternity.item;

import com.carrot123.until_eternity.until_eternity;
import com.carrot123.until_eternity.registry.ModPotions;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

@SuppressWarnings("null")
public class ModCreativeModeTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
        DeferredRegister.create(Registries.CREATIVE_MODE_TAB, until_eternity.MODID);

    public static final RegistryObject<CreativeModeTab> UNTIL_ETERNITY_TAB =
        CREATIVE_MODE_TABS.register("until_eternity_tab", () -> CreativeModeTab.builder()
            .icon(() -> new ItemStack(ModItems.FINALITE_INGOT.get()))
            .title(Component.translatable("itemgroup.until_eternity_tab"))
            .displayItems((itemDisplayParameters, output) -> {
                output.accept(ModItems.BLOOD_COPPER_INGOT.get());
                output.accept(ModItems.DRAGONBREATH_INGOT.get());
                output.accept(ModItems.ELEMENTAL_CORE.get());
                output.accept(ModItems.FINALITE_INGOT.get());
                output.accept(ModItems.FINAL_KEY.get());
                output.accept(ModItems.FINAL_KEY_MOLD.get());
                output.accept(ModItems.FINAL_KEY_CASTING_FLUID.get());
                output.accept(ModItems.UNIVERSAL_SMITHING_TEMPLATE.get());
                output.accept(ModItems.IMMORTAL_ALTAR.get());
                output.accept(ModItems.SATURATED_EARTH_PARTICLE.get());
                output.accept(ModItems.SATURATED_FIRE_PARTICLE.get());
                output.accept(ModItems.SATURATED_GOLD_PARTICLE.get());
                output.accept(ModItems.SATURATED_WATER_PARTICLE.get());
                output.accept(ModItems.SATURATED_WOOD_PARTICLE.get());
                output.accept(ModItems.ELEMENTAL_GAUNTLET.get());
                output.accept(ModItems.REAPER_TOOTH_NECKLACE.get());
                output.accept(ModItems.SAND_SHARK_TOOTH_NECKLACE.get());
                output.accept(ModItems.REGENERATOR.get());
                output.accept(ModItems.GUTTERING_CANDLE.get());
                output.accept(ModItems.COSMIC_AEGIS.get());
                output.accept(ModItems.EMPOWERED_SHIELD.get());
                output.accept(ModItems.PROOF_OF_SPURNER.get());
                output.accept(ModItems.CRYSTAL_OF_DRAWN_BOW.get());
                output.accept(ModItems.SWORD_WRAITH.get());
                output.accept(ModItems.WROUGHT_IRON.get());
                output.accept(ModItems.IMMORTAL_HEART.get());
                output.accept(ModItems.IMMORTAL_ESSENCE.get());
                output.accept(ModItems.TRUE_CHEFS_KNIFE.get());
                output.accept(ModItems.CALAMITY_DAGGER.get());
                output.accept(ModItems.ANCIENT_NETHERITE_BLADE.get());
                output.accept(ModItems.TATTERED_CLOAK.get());
                output.accept(ModItems.WITHER_FRAGMENT.get());
                output.accept(ModItems.BROKEN_CROWN.get());
                output.accept(ModItems.BROKEN_NOSERING.get());
                output.accept(ModItems.MONSTERS_SCYTHE.get());
                output.accept(ModItems.VIBRANT_AMETHYST.get());
                output.accept(ModItems.SNOW_SPEAR.get());
                output.accept(ModItems.FINAL_INGOT_PICKAXE.get());
                output.accept(ModItems.PUMPKIN_NUGGET.get());
                output.accept(ModItems.PUMPKIN_INGOT.get());
                output.accept(ModItems.IMFULL.get());
                output.accept(ModItems.EMPTY_POTTERY_SHARD.get());
                output.accept(ModItems.GODS_RECOGNITION.get());
                output.accept(ModItems.INERT_FINALITE_INGOT.get());
                output.accept(ModItems.OMINOUS_TOTEM.get());
                output.accept(ModItems.CHAOS_ELIXIR.get());
                output.accept(ModItems.SPAWNER_FRAGMENT.get());
                output.accept(ModItems.DARK_CAGE.get());
                output.accept(ModItems.MITHRIL_GLOVES.get());
                output.accept(ModItems.PEWTER_GLOVES.get());
                output.accept(ModItems.DIVINE_SOUL_LAMP.get());
                output.accept(ModItems.DYING_FURY.get());
                output.accept(ModItems.HORROR_HUNT.get());
                output.accept(ModItems.EMPOWERED_RING.get());
                output.accept(ModItems.ADVANCED_EMPOWERED_RING.get());
                output.accept(ModItems.AETHERLIGHT_RING.get());
                output.accept(ModItems.RESONANCE_ARMOR.get());
                output.accept(ModItems.GREATER_ARCANE_RING.get());
                output.accept(ModItems.RING_OF_WARPED_MAGIC.get());
                output.accept(ModItems.ADVANCED_RING_OF_WARPED_MAGIC.get());
                output.accept(ModItems.RING_OF_SOUL_CRAVING.get());
                output.accept(ModItems.RING_OF_PURITY.get());
                output.accept(ModItems.RING_OF_WARPED_CHANTING.get());
                output.accept(ModItems.RING_OF_WARPED_COOLING.get());
                output.accept(ModItems.VOID_RING.get());
                output.accept(ModItems.ROCK.get());
                output.accept(PotionUtils.setPotion(
                    new ItemStack(Items.POTION),
                    ModPotions.MANA_ERUPTION_LONG.get()));
                output.accept(PotionUtils.setPotion(
                    new ItemStack(Items.POTION),
                    ModPotions.MANA_ERUPTION_STRONG.get()));
            }).build());

    public static void register(IEventBus eventBus) {
        CREATIVE_MODE_TABS.register(eventBus);
    }
}
