package com.carrot123.until_eternity.network;

import com.carrot123.until_eternity.client.TarotDeckClientSync;
import java.util.function.Supplier;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

/** Full server-owned snapshot of the deck's 22-slot Forge item handler. */
public record SyncTarotDeckS2CPacket(int containerId, int inventorySlot, CompoundTag contents) {
    public static void encode(SyncTarotDeckS2CPacket packet, FriendlyByteBuf buffer) {
        buffer.writeVarInt(packet.containerId);
        buffer.writeVarInt(packet.inventorySlot);
        buffer.writeNbt(packet.contents);
    }

    public static SyncTarotDeckS2CPacket decode(FriendlyByteBuf buffer) {
        return new SyncTarotDeckS2CPacket(buffer.readVarInt(), buffer.readVarInt(),
                buffer.readNbt());
    }

    public static void handle(SyncTarotDeckS2CPacket packet,
            Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT,
                () -> () -> TarotDeckClientSync.apply(packet)));
        context.setPacketHandled(true);
    }
}
