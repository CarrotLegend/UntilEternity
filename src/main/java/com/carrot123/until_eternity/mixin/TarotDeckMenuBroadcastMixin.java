package com.carrot123.until_eternity.mixin;

import com.carrot123.until_eternity.tarot.TarotDeckSyncAccess;
import net.minecraft.world.inventory.AbstractContainerMenu;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractContainerMenu.class)
public abstract class TarotDeckMenuBroadcastMixin {
    @Inject(method = "broadcastChanges", at = @At("TAIL"))
    private void untilEternity$syncDeckParent(CallbackInfo callback) {
        if (this instanceof TarotDeckSyncAccess deckMenu) {
            deckMenu.untilEternity$syncDeckIfChanged();
        }
    }
}
