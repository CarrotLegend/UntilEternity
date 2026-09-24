package com.carrot123.until_eternity.network;

import com.carrot123.until_eternity.tarot.TarotCardHelper;
import com.carrot123.until_eternity.tarot.TarotSetManager;
import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;
import shiroroku.tarotcards.Item.TarotDeck.TarotDeckContainer;

public record TarotCardFlipC2SPacket(int containerId, int slotId) {
    public static void encode(TarotCardFlipC2SPacket packet, FriendlyByteBuf buffer) {
        buffer.writeVarInt(packet.containerId);
        buffer.writeVarInt(packet.slotId);
    }

    public static TarotCardFlipC2SPacket decode(FriendlyByteBuf buffer) {
        return new TarotCardFlipC2SPacket(buffer.readVarInt(), buffer.readVarInt());
    }

    public static void handle(TarotCardFlipC2SPacket packet,
            Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player == null) {
                return;
            }
            AbstractContainerMenu menu = player.containerMenu;
            if (menu.containerId != packet.containerId || !menu.stillValid(player)
                    || packet.slotId < 0 || packet.slotId >= menu.slots.size()) {
                return;
            }
            Slot slot = menu.getSlot(packet.slotId);
            boolean playerSlot = slot.container == player.getInventory();
            boolean allowed = menu instanceof InventoryMenu && playerSlot
                    || menu instanceof TarotDeckContainer
                    && packet.slotId < 58;
            if (!allowed || !slot.isActive() || !TarotCardHelper.isTarotCard(slot.getItem())) {
                return;
            }
            ItemStack changed = slot.getItem().copy();
            TarotCardHelper.toggle(changed);
            slot.set(changed);
            slot.setChanged();
            menu.broadcastChanges();
            TarotSetManager.markDirty(player);
        });
        context.setPacketHandled(true);
    }
}
