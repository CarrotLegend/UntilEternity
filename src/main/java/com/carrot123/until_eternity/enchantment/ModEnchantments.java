package com.carrot123.until_eternity.enchantment;

import com.carrot123.until_eternity.until_eternity;

import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

@SuppressWarnings("null")
public class ModEnchantments {

    // 自定义附魔类别：适用于所有最大堆叠为1的物品（武器/工具/护甲/饰品等）
    public static final EnchantmentCategory CURSE_EQUIPPABLE =
            EnchantmentCategory.create("until_eternity_curse_equippable", item -> item.getMaxStackSize() == 1);
    public static final DeferredRegister<Enchantment> ENCHANTMENTS = DeferredRegister.create(ForgeRegistries.ENCHANTMENTS, until_eternity.MODID);
    public static final RegistryObject<Enchantment> POWER = ENCHANTMENTS.register("power", PowerEnchantment::new);
    public static final RegistryObject<Enchantment> CURSE_OF_HUNGER = ENCHANTMENTS.register("curse_of_hunger", CurseOfHungerEnchantment::new);
    public static final RegistryObject<Enchantment> CURSE_OF_SHORT_LIFE = ENCHANTMENTS.register("curse_of_short_life", CurseOfShortLifeEnchantment::new);

    public static void register(IEventBus eventBus) {
        ENCHANTMENTS.register(eventBus);
    }
}
