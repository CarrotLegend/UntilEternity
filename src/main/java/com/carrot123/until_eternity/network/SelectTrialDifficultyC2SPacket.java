package com.carrot123.until_eternity.network;

import com.carrot123.until_eternity.trial.TrialDifficulty;
import com.carrot123.until_eternity.trial.TrialMonumentBlockEntity;
import com.carrot123.until_eternity.trial.TrialRealm;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record SelectTrialDifficultyC2SPacket(BlockPos pos, int difficultyId) {
    public static void encode(SelectTrialDifficultyC2SPacket packet, FriendlyByteBuf buffer) {
        buffer.writeBlockPos(packet.pos);
        buffer.writeVarInt(packet.difficultyId);
    }
    public static SelectTrialDifficultyC2SPacket decode(FriendlyByteBuf buffer) {
        return new SelectTrialDifficultyC2SPacket(buffer.readBlockPos(), buffer.readVarInt());
    }
    public static void handle(SelectTrialDifficultyC2SPacket packet, Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            TrialDifficulty difficulty = TrialDifficulty.byNetworkId(packet.difficultyId);
            if (player == null || difficulty == null || !player.level().dimension().equals(TrialRealm.KEY)
                    || !player.level().hasChunkAt(packet.pos)
                    || player.distanceToSqr(packet.pos.getX() + .5, packet.pos.getY() + .5, packet.pos.getZ() + .5) > 64) return;
            if (player.level().getBlockEntity(packet.pos) instanceof TrialMonumentBlockEntity monument)
                monument.selectDifficulty(player, difficulty);
        });
        context.setPacketHandled(true);
    }
}
