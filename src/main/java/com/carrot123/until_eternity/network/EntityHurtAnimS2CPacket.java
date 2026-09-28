package com.carrot123.until_eternity.network;

import java.util.function.Supplier;

import com.carrot123.until_eternity.client.EntityHurtAnimClientHandler;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

public record EntityHurtAnimS2CPacket(int entityId) {

    public static void encode(
            EntityHurtAnimS2CPacket packet,
            FriendlyByteBuf buffer
    ) {
        buffer.writeVarInt(packet.entityId);
    }

    public static EntityHurtAnimS2CPacket decode(
            FriendlyByteBuf buffer
    ) {
        return new EntityHurtAnimS2CPacket(
                buffer.readVarInt()
        );
    }

    public static void handle(
            EntityHurtAnimS2CPacket packet,
            Supplier<NetworkEvent.Context> contextSupplier
    ) {
        NetworkEvent.Context context =
                contextSupplier.get();

        context.enqueueWork(
                () -> DistExecutor.unsafeRunWhenOn(
                        Dist.CLIENT,
                        () -> () ->
                                EntityHurtAnimClientHandler.handle(
                                        packet.entityId
                                )
                )
        );

        context.setPacketHandled(true);
    }
}