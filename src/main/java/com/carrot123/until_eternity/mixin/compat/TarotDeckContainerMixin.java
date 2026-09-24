package com.carrot123.until_eternity.mixin.compat;

import com.carrot123.until_eternity.network.ModNetworking;
import com.carrot123.until_eternity.network.SyncTarotDeckS2CPacket;
import com.carrot123.until_eternity.tarot.TarotDeckSyncAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.network.PacketDistributor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import shiroroku.tarotcards.Item.TarotDeck.TarotDeckContainer;

@Mixin(value = TarotDeckContainer.class, remap = false)
public abstract class TarotDeckContainerMixin implements TarotDeckSyncAccess {
    @Shadow ItemStack deck;

    @Unique private ServerPlayer untilEternity$owner;
    @Unique private CompoundTag untilEternity$lastContents;

    @Inject(method = "<init>", at = @At("RETURN"), remap = false)
    private void untilEternity$rememberOwner(int id, Inventory inventory, Player player,
            CallbackInfo callback) {
        if (player instanceof ServerPlayer serverPlayer) {
            untilEternity$owner = serverPlayer;
        }
    }

    @Override
    public void untilEternity$syncDeckIfChanged() {
        ServerPlayer player = untilEternity$owner;
        if (player == null || player.containerMenu != (Object) this) {
            return;
        }
        int inventorySlot;
        if (deck == player.getMainHandItem()) {
            inventorySlot = player.getInventory().selected;
        } else if (deck == player.getOffhandItem()) {
            inventorySlot = 40;
        } else {
            return;
        }
        deck.getCapability(ForgeCapabilities.ITEM_HANDLER).ifPresent(handler -> {
            if (!(handler instanceof ItemStackHandler stackHandler) || handler.getSlots() != 22) {
                return;
            }
            CompoundTag contents = stackHandler.serializeNBT();
            if (contents.equals(untilEternity$lastContents)) {
                return;
            }
            untilEternity$lastContents = contents.copy();
            player.getInventory().setChanged();
            if (inventorySlot == 40) {
                player.inventoryMenu.getSlot(45).setChanged();
            } else {
                ((AbstractContainerMenu) (Object) this).getSlot(inventorySlot).setChanged();
            }
            player.inventoryMenu.broadcastChanges();
            ModNetworking.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player),
                    new SyncTarotDeckS2CPacket(
                            ((AbstractContainerMenu) (Object) this).containerId,
                            inventorySlot, contents));
        });
    }
}
