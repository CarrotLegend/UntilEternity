package com.carrot123.until_eternity.tarot;

import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import shiroroku.tarotcards.Item.TarotDeck.TarotDeckItem;

public final class TarotDeckScanner {
    private TarotDeckScanner() {
    }

    public static boolean isDeck(ItemStack stack) {
        return !stack.isEmpty() && stack.getItem() instanceof TarotDeckItem;
    }

    public static TarotDeckSnapshot scan(ItemStack deck) {
        Map<ResourceLocation, TarotOrientation> cards = new LinkedHashMap<>();
        if (!isDeck(deck)) {
            return new TarotDeckSnapshot(cards);
        }
        deck.getCapability(ForgeCapabilities.ITEM_HANDLER).ifPresent(handler -> {
            for (int slot = 0; slot < handler.getSlots(); slot++) {
                ItemStack card = handler.getStackInSlot(slot);
                ResourceLocation id = TarotCardHelper.cardId(card);
                if (id != null) {
                    cards.putIfAbsent(id, TarotCardHelper.orientation(card));
                }
            }
        });
        return new TarotDeckSnapshot(cards);
    }
}
