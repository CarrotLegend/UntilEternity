package com.carrot123.until_eternity.network;

import com.carrot123.until_eternity.entity.StormPriestessEntity;
import com.carrot123.until_eternity.item.ModItems;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraft.network.FriendlyByteBuf;

import java.util.function.Supplier;

public record SelectStormPriestessExchangeC2SPacket(int entityId, byte selection) {
    public static final byte ALTAR = 0;

    public static void encode(SelectStormPriestessExchangeC2SPacket packet, FriendlyByteBuf buffer) {
        buffer.writeVarInt(packet.entityId());
        buffer.writeByte(packet.selection());
    }

    public static SelectStormPriestessExchangeC2SPacket decode(FriendlyByteBuf buffer) {
        return new SelectStormPriestessExchangeC2SPacket(buffer.readVarInt(), buffer.readByte());
    }

    public static void handle(SelectStormPriestessExchangeC2SPacket packet, Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player == null || player.isSpectator()
                    || !(player.level().getEntity(packet.entityId()) instanceof StormPriestessEntity priestess)
                    || !priestess.isAlive() || player.distanceToSqr(priestess) > 64.0D) {
                return;
            }
            if (packet.selection() != ALTAR) {
                return;
            }

            ResourceLocation ingredientId = new ResourceLocation("cataclysm", "essence_of_the_storm");
            int amount = 1;
            Item ingredient = ForgeRegistries.ITEMS.getValue(ingredientId);
            if (ingredient == null || ingredient == Items.AIR || !ForgeRegistries.ITEMS.containsKey(ingredientId)
                    || countIngredient(player.getInventory(), ingredient) < amount) {
                sendResult(player, packet.entityId(), OpenStormPriestessDialogueS2CPacket.INSUFFICIENT);
                return;
            }

            consumeIngredient(player.getInventory(), ingredient, amount);
            Item result = ModItems.STORM_ALTAR.get();
            ItemStack reward = new ItemStack(result);
            player.getInventory().add(reward);
            if (!reward.isEmpty()) {
                player.drop(reward, false);
            }
            player.inventoryMenu.broadcastChanges();
            sendResult(player, packet.entityId(), OpenStormPriestessDialogueS2CPacket.SUCCESS);
        });
        context.setPacketHandled(true);
    }

    private static int countIngredient(Inventory inventory, Item ingredient) {
        int total = 0;
        for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
            ItemStack stack = inventory.getItem(slot);
            if (stack.is(ingredient)) {
                total += stack.getCount();
            }
        }
        return total;
    }

    private static void consumeIngredient(Inventory inventory, Item ingredient, int amount) {
        int remaining = amount;
        for (int slot = 0; slot < inventory.getContainerSize() && remaining > 0; slot++) {
            ItemStack stack = inventory.getItem(slot);
            if (stack.is(ingredient)) {
                int toRemove = Math.min(remaining, stack.getCount());
                inventory.removeItem(slot, toRemove);
                remaining -= toRemove;
            }
        }
        inventory.setChanged();
    }

    private static void sendResult(ServerPlayer player, int entityId, byte state) {
        ModNetworking.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player),
                new OpenStormPriestessDialogueS2CPacket(entityId, state));
    }
}
