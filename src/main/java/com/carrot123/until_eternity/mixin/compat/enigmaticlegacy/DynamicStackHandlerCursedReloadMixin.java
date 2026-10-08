package com.carrot123.until_eternity.mixin.compat.enigmaticlegacy;

import com.aizistral.enigmaticlegacy.api.items.ICursed;
import com.carrot123.until_eternity.compat.enigmaticlegacy.CursedCurioReloadGuard;
import java.util.function.Function;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.Event;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.event.CurioEquipEvent;
import top.theillusivec4.curios.api.type.inventory.IDynamicStackHandler;

@Mixin(
        targets =
                "top.theillusivec4.curios.common.inventory.DynamicStackHandler",
        remap = false
)
public abstract class DynamicStackHandlerCursedReloadMixin {

    @Shadow(remap = false)
    protected Function<Integer, SlotContext> ctxBuilder;

    @Inject(
            method = {
                    "isItemValid",
                    "isItemValid(ILnet/minecraft/world/item/ItemStack;)Z"
            },
            at = @At(
                    value = "HEAD",
                    remap = false
            ),
            cancellable = true,
            remap = false,
            require = 1
    )
    private void untilEternity$restoreCursedCurio(
            int slot,
            ItemStack stack,
            CallbackInfoReturnable<Boolean> cir
    ) {
        if (!CursedCurioReloadGuard.isRestoring()) {
            return;
        }

        if (!(stack.getItem() instanceof ICursed)) {
            return;
        }

        IDynamicStackHandler handler =
                (IDynamicStackHandler) (Object) this;

        if (slot < 0
                || slot >= handler.getSlots()) {
            cir.setReturnValue(false);
            return;
        }

        SlotContext context =
                this.ctxBuilder.apply(slot);

        CurioEquipEvent equipEvent =
                new CurioEquipEvent(
                        stack,
                        context
                );

        MinecraftForge.EVENT_BUS.post(
                equipEvent
        );

        Event.Result result =
                equipEvent.getResult();

        if (result == Event.Result.DENY) {
            cir.setReturnValue(false);
            return;
        }

        if (result == Event.Result.ALLOW) {
            cir.setReturnValue(true);
            return;
        }

        cir.setReturnValue(
                CuriosApi.isStackValid(
                        context,
                        stack
                )
        );
    }
}