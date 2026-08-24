package com.carrot123.until_eternity.menu;

import com.carrot123.until_eternity.until_eternity;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModMenuTypes {
    private static final DeferredRegister<MenuType<?>> MENUS =
            DeferredRegister.create(ForgeRegistries.MENU_TYPES, until_eternity.MODID);

    public static final RegistryObject<MenuType<EndCraftingTableMenu>> END_CRAFTING_TABLE =
            MENUS.register("end_crafting_table", () -> IForgeMenuType.create(
                    (containerId, inventory, buffer) ->
                            new EndCraftingTableMenu(containerId, inventory, buffer.readBlockPos())));

    private ModMenuTypes() { }
    public static void register(IEventBus bus) { MENUS.register(bus); }
}
