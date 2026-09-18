package com.carrot123.until_eternity.client;

import com.carrot123.until_eternity.item.ModItems;

import net.minecraft.client.Minecraft;
import net.minecraft.world.item.ItemStack;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public final class TrueBedrockClientActivationHandler {

    public static void showTrueBedrock() {

        Minecraft.getInstance()
                .gameRenderer
                .displayItemActivation(
                        new ItemStack(
                                ModItems.TRUE_BEDROCK.get()
                        )
                );
    }

    private TrueBedrockClientActivationHandler() {
    }
}