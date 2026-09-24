package com.carrot123.until_eternity.mixin.client;

import com.carrot123.until_eternity.network.ModNetworking;
import com.carrot123.until_eternity.network.TarotCardFlipC2SPacket;
import com.carrot123.until_eternity.tarot.TarotCardHelper;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.world.inventory.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import shiroroku.tarotcards.Item.TarotDeck.TarotDeckScreen;

@Mixin(AbstractContainerScreen.class)
public abstract class TarotScreenClickMixin {
    @Invoker("findSlot")
    protected abstract Slot untilEternity$findSlot(double mouseX, double mouseY);

    @Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true)
    private void untilEternity$flipCard(double mouseX, double mouseY, int button,
            CallbackInfoReturnable<Boolean> callback) {
        Slot hovered = untilEternity$findSlot(mouseX, mouseY);
        if (button != 1 || !Screen.hasShiftDown() || hovered == null
                || !TarotCardHelper.isTarotCard(hovered.getItem())) {
            return;
        }
        Object screen = this;
        boolean allowed = screen instanceof InventoryScreen
                && net.minecraft.client.Minecraft.getInstance().player != null
                && hovered.container == net.minecraft.client.Minecraft.getInstance()
                        .player.getInventory()
                || screen instanceof TarotDeckScreen && hovered.index < 58;
        if (!allowed) {
            return;
        }
        ModNetworking.CHANNEL.sendToServer(
                new TarotCardFlipC2SPacket(((AbstractContainerScreen<?>) (Object) this).getMenu()
                        .containerId, hovered.index));
        callback.setReturnValue(true);
    }
}
