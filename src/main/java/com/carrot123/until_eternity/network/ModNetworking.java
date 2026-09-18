package com.carrot123.until_eternity.network;

import com.carrot123.until_eternity.until_eternity;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

public final class ModNetworking {

    private static final String PROTOCOL_VERSION = "1";

    public static final SimpleChannel CHANNEL =
            NetworkRegistry.newSimpleChannel(
                    new ResourceLocation(until_eternity.MODID, "main"),
                    () -> PROTOCOL_VERSION,
                    PROTOCOL_VERSION::equals,
                    PROTOCOL_VERSION::equals
            );

    private static int packetId = 0;
    private static boolean registered = false;

    private ModNetworking() {
    }

    public static void register() {
        if (registered) {
            return;
        }

        registered = true;

        CHANNEL.registerMessage(
                packetId++,
                TrueBedrockActivationS2CPacket.class,
                TrueBedrockActivationS2CPacket::encode,
                TrueBedrockActivationS2CPacket::decode,
                TrueBedrockActivationS2CPacket::handle
        );
    }
}