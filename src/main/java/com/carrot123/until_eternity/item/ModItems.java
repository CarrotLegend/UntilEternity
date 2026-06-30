package com.carrot123.until_eternity.item;

import com.carrot123.until_eternity.item.curio.ImmuneCurioItem;
import com.carrot123.until_eternity.item.curio.LifeCapItem;
import com.carrot123.until_eternity.until_eternity;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tiers;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

@SuppressWarnings("null")
public class ModItems {
    public static final DeferredRegister<Item> ITEMS =
        DeferredRegister.create(ForgeRegistries.ITEMS, until_eternity.MODID);
    public static final RegistryObject<Item> DRAGONBREATH_INGOT = ITEMS.register("dragonbreath_ingot", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> BLOOD_COPPER_INGOT = ITEMS.register("blood_copper_ingot", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> ELEMENTAL_CORE = ITEMS.register("elemental_core", () -> new Item(new Item.Properties().rarity(Rarity.RARE).fireResistant()));
    public static final RegistryObject<Item> SATURATED_GOLD_PARTICLE = ITEMS.register("saturated_gold_particle", () -> new Item(new Item.Properties().rarity(Rarity.RARE)));
    public static final RegistryObject<Item> SATURATED_WOOD_PARTICLE = ITEMS.register("saturated_wood_particle", () -> new Item(new Item.Properties().rarity(Rarity.RARE)));
    public static final RegistryObject<Item> SATURATED_WATER_PARTICLE = ITEMS.register("saturated_water_particle", () -> new Item(new Item.Properties().rarity(Rarity.RARE)));
    public static final RegistryObject<Item> SATURATED_FIRE_PARTICLE = ITEMS.register("saturated_fire_particle", () -> new Item(new Item.Properties().rarity(Rarity.RARE)));
    public static final RegistryObject<Item> SATURATED_EARTH_PARTICLE = ITEMS.register("saturated_earth_particle", () -> new Item(new Item.Properties().rarity(Rarity.RARE)));
    public static final RegistryObject<Item> FINALITE_INGOT = ITEMS.register("finalite_ingot", () -> new Item(new Item.Properties().rarity(Rarity.EPIC).fireResistant()));
    public static final RegistryObject<Item> ELEMENTAL_GAUNTLET = ITEMS.register("elemental_gauntlet", () -> new Item(new Item.Properties().stacksTo(1).rarity(Rarity.EPIC).fireResistant()));
    public static final RegistryObject<Item> REAPER_TOOTH_NECKLACE = ITEMS.register("reaper_tooth_necklace", () -> new Item(new Item.Properties().stacksTo(1).rarity(Rarity.EPIC)));
    public static final RegistryObject<Item> SAND_SHARK_TOOTH_NECKLACE = ITEMS.register("sand_shark_tooth_necklace", () -> new Item(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> REGENERATOR = ITEMS.register("regenerator", () -> new LifeCapItem(new Item.Properties().stacksTo(1), 0.5f, false));
    public static final RegistryObject<Item> GUTTERING_CANDLE = ITEMS.register("guttering_candle", () -> new LifeCapItem(new Item.Properties().stacksTo(1), 1.0f, true));
    public static final RegistryObject<Item> COSMIC_AEGIS = ITEMS.register("cosmic_aegis", () -> new ImmuneCurioItem(new Item.Properties().stacksTo(1).rarity(Rarity.EPIC).fireResistant(), ImmuneCurioItem.CurioType.ALL));
    public static final RegistryObject<Item> EMPOWERED_SHIELD = ITEMS.register("empowered_shield", () -> new ImmuneCurioItem(new Item.Properties().stacksTo(1).fireResistant(), ImmuneCurioItem.CurioType.LIMITED));
    public static final RegistryObject<Item> PROOF_OF_SPURNER = ITEMS.register("proof_of_spurner", () -> new ImmuneCurioItem(new Item.Properties().stacksTo(1).fireResistant(), ImmuneCurioItem.CurioType.ALL));
    public static final RegistryObject<Item> CRYSTAL_OF_DRAWN_BOW = ITEMS.register("crystal_of_drawn_bow", () -> new Item(new Item.Properties().fireResistant()));
    public static final RegistryObject<Item> SWORD_WRAITH = ITEMS.register("sword_wraith", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> WROUGHT_IRON = ITEMS.register("wrought_iron", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> IMMORTAL_HEART = ITEMS.register("immortal_heart", () -> new Item(new Item.Properties().rarity(Rarity.RARE).fireResistant()));
    public static final RegistryObject<Item> IMMORTAL_ESSENCE = ITEMS.register("immortal_essence", () -> new Item(new Item.Properties().fireResistant()));
    public static final RegistryObject<Item> TRUE_CHEFS_KNIFE = ITEMS.register("true_chefs_knife", () -> new SwordItem(ModTiers.TRUE_CHEFS_KNIFE, 0, -1.8F, new Item.Properties().rarity(Rarity.EPIC).fireResistant()));
    public static final RegistryObject<Item> ANCIENT_NETHERITE_BLADE = ITEMS.register("ancient_netherite_blade", () -> new SwordItem(ModTiers.ANCIENT_NETHERITE_BLADE, 0, -1.8F, new Item.Properties().fireResistant()));
    public static final RegistryObject<Item> CALAMITY_DAGGER = ITEMS.register("calamity_dagger", () -> new SwordItem(ModTiers.CALAMITY_DAGGER, 0, -1.8F, new Item.Properties().fireResistant()));
    public static final RegistryObject<Item> TATTERED_CLOAK = ITEMS.register("tattered_cloak", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> WITHER_FRAGMENT = ITEMS.register("wither_fragment", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> BROKEN_CROWN = ITEMS.register("broken_crown", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> BROKEN_NOSERING = ITEMS.register("broken_nosering", () -> new Item(new Item.Properties().fireResistant()));
    public static final RegistryObject<Item> VIBRANT_AMETHYST = ITEMS.register("vibrant_amethyst", () -> new Item(new Item.Properties().fireResistant()));
    public static final RegistryObject<Item> MONSTERS_SCYTHE = ITEMS.register("monsters_scythe", () -> new MonstersScythe(ModTiers.MONSTERS_SCYTHE, 0, -3.2F, new Item.Properties().durability(3270).fireResistant()));
    public static final RegistryObject<Item> SNOW_SPEAR = ITEMS.register("snow_spear", () -> new SnowSpear(Tiers.NETHERITE, 0, -2.5F, 400.0F, new Item.Properties().durability(2031).rarity(Rarity.RARE).fireResistant()));
    public static final RegistryObject<Item> FINAL_INGOT_PICKAXE = ITEMS.register("final_ingot_pickaxe", () -> new FinalIngotPickaxe(ModTiers.FINAL_INGOT, 0, -2.8F, new Item.Properties().durability(4096).rarity(Rarity.EPIC).fireResistant()));
    public static final RegistryObject<Item> PUMPKIN_NUGGET = ITEMS.register("pumpkin_nugget", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> PUMPKIN_INGOT = ITEMS.register("pumpkin_ingot", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> IMFULL = ITEMS.register("imfull", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> EMPTY_POTTERY_SHARD = ITEMS.register("empty_pottery_shard", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> GODS_RECOGNITION = ITEMS.register("gods_recognition", () -> new Item(new Item.Properties().fireResistant()));
    public static final RegistryObject<Item> INERT_FINALITE_INGOT = ITEMS.register("inert_finalite_ingot", () -> new Item(new Item.Properties().fireResistant()));
    public static final RegistryObject<Item> OMINOUS_TOTEM = ITEMS.register("ominous_totem", () -> new OminousTotem(new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON)));
    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}