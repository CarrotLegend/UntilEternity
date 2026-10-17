package com.carrot123.until_eternity.network;

import com.carrot123.until_eternity.client.dialogue.StormPriestessDialogueClient;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record OpenStormPriestessDialogueS2CPacket(int entityId, byte state) {
    public static final byte OPEN = 0;
    public static final byte INSUFFICIENT = 1;
    public static final byte SUCCESS = 2;

    public static void encode(OpenStormPriestessDialogueS2CPacket packet, FriendlyByteBuf buffer) {
        buffer.writeVarInt(packet.entityId());
        buffer.writeByte(packet.state());
    }

    public static OpenStormPriestessDialogueS2CPacket decode(FriendlyByteBuf buffer) {
        return new OpenStormPriestessDialogueS2CPacket(buffer.readVarInt(), buffer.readByte());
    }

    public static void handle(OpenStormPriestessDialogueS2CPacket packet,
                              Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT,
                () -> () -> StormPriestessDialogueClient.handle(packet.entityId(), packet.state())));
        context.setPacketHandled(true);
    }
}
