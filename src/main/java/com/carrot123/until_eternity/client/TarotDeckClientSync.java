package com.carrot123.until_eternity.client;

import com.carrot123.until_eternity.network.SyncTarotDeckS2CPacket;
import com.carrot123.until_eternity.tarot.TarotDeckScanner;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.items.ItemStackHandler;

public final class TarotDeckClientSync {
    private TarotDeckClientSync() {
    }

    public static void apply(SyncTarotDeckS2CPacket packet) {
        var player = Minecraft.getInstance().player;
        CompoundTag contents = packet.contents();
        if (player == null || player.containerMenu.containerId != packet.containerId()
                || contents == null || contents.getInt("Size") != 22) {
            return;
        }
        int slot = packet.inventorySlot();
        if (slot != 40 && (slot < 0 || slot > 8 || player.getInventory().selected != slot)) {
            return;
        }
        ItemStack deck = player.getInventory().getItem(slot);
        if (!TarotDeckScanner.isDeck(deck)) {
            return;
        }
        deck.getCapability(ForgeCapabilities.ITEM_HANDLER).ifPresent(handler -> {
            if (handler instanceof ItemStackHandler stackHandler && handler.getSlots() == 22) {
                // Keep the existing stack and its menu reference; replace only the capability state.
                stackHandler.deserializeNBT(contents.copy());
            }
        });
    }
}
