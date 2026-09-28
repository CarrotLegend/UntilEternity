package com.carrot123.until_eternity.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.BookViewScreen;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public final class ClientBookOpener {
    private ClientBookOpener() {
    }

    public static void open(ItemStack stack) {
        Minecraft.getInstance().setScreen(
                new BookViewScreen(
                        new BookViewScreen.WrittenBookAccess(stack)
                )
        );
    }
}