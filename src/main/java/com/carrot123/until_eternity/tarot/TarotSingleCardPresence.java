package com.carrot123.until_eternity.tarot;

import com.google.common.collect.ImmutableList;
import net.minecraft.core.NonNullList;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.fml.ModList;
import shiroroku.tarotcards.Configuration;
import shiroroku.tarotcards.CuriosCompat;
import shiroroku.tarotcards.Registry.ItemRegistry;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public final class TarotSingleCardPresence {

    private TarotSingleCardPresence() {
    }

    public static boolean has(
            Player player,
            Item tarot
    ) {
        if (player == null || tarot == null) {
            return false;
        }

        ItemStack deck = null;

        Inventory inventory =
                player.getInventory();

        List<NonNullList<ItemStack>> fullInventory =
                ImmutableList.of(
                        inventory.items,
                        inventory.armor,
                        inventory.offhand
                );

        if (ModList.get().isLoaded("curios")) {
            ItemStack singleCard =
                    CuriosCompat.getTarotCardCurio(
                            player,
                            tarot
                    );

            if (singleCard != null
                    && !singleCard.isEmpty()) {
                return true;
            }

            deck =
                    CuriosCompat.getTarotDeckCurio(
                            player
                    );
        }

        if (!Configuration.require_card_in_curio.get()) {
            for (List<ItemStack> compartment
                    : fullInventory) {

                for (ItemStack stack
                        : compartment) {

                    if (stack.is(tarot)) {
                        return true;
                    }

                    if (stack.getItem()
                            == ItemRegistry.tarot_deck.get()) {
                        deck = stack;
                    }
                }
            }
        }

        if (deck == null || deck.isEmpty()) {
            return false;
        }

        if (!Configuration.tarot_deck_applies_effects.get()) {
            return false;
        }

        AtomicBoolean found =
                new AtomicBoolean(false);

        deck.getCapability(
                ForgeCapabilities.ITEM_HANDLER
        ).ifPresent(handler -> {
            for (int slot = 0;
                 slot < handler.getSlots();
                 slot++) {

                if (handler.getStackInSlot(slot)
                        .is(tarot)) {
                    found.set(true);
                    break;
                }
            }
        });

        return found.get();
    }
}