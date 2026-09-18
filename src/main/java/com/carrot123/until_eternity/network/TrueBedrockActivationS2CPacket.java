package com.carrot123.until_eternity.network;

import com.carrot123.until_eternity.client.TrueBedrockClientActivationHandler;

import net.minecraft.network.FriendlyByteBuf;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public final class TrueBedrockActivationS2CPacket {

    public static void encode(
            TrueBedrockActivationS2CPacket packet,
            FriendlyByteBuf buffer
    ) {
    }

    public static TrueBedrockActivationS2CPacket decode(
            FriendlyByteBuf buffer
    ) {
        return new TrueBedrockActivationS2CPacket();
    }

    public static void handle(
            TrueBedrockActivationS2CPacket packet,
            Supplier<NetworkEvent.Context> contextSupplier
    ) {
        NetworkEvent.Context context =
                contextSupplier.get();

        context.enqueueWork(
                () -> DistExecutor.unsafeRunWhenOn(
                        Dist.CLIENT,
                        () ->
                                TrueBedrockClientActivationHandler
                                        ::showTrueBedrock
                )
        );

        context.setPacketHandled(true);
    }
}