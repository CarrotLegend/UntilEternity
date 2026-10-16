package com.carrot123.until_eternity.network;

import com.carrot123.until_eternity.client.trial.TrialDialogueClient;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record OpenTrialDialogueS2CPacket(BlockPos pos) {
    public static void encode(OpenTrialDialogueS2CPacket packet, FriendlyByteBuf buffer) { buffer.writeBlockPos(packet.pos); }
    public static OpenTrialDialogueS2CPacket decode(FriendlyByteBuf buffer) { return new OpenTrialDialogueS2CPacket(buffer.readBlockPos()); }
    public static void handle(OpenTrialDialogueS2CPacket packet, Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> TrialDialogueClient.open(packet.pos)));
        context.setPacketHandled(true);
    }
}
