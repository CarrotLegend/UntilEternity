
package com.carrot123.until_eternity.mixin.compat.soulcurse;

import com.carrot123.until_eternity.compat.soulcurse.SoulCurioReloadGuard;
import java.util.function.Function;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.inventory.IDynamicStackHandler;

@Mixin(
        targets = "top.theillusivec4.curios.common.inventory.DynamicStackHandler",
        remap = false
)
public abstract class DynamicStackHandlerSoulReloadMixin {

    @Shadow(remap = false)
    protected Function<Integer, SlotContext> ctxBuilder;

    @Inject(
            method = {
                    "isItemValid",
                    "isItemValid(ILnet/minecraft/world/item/ItemStack;)Z"
            },
            at = @At(value = "HEAD", remap = false),
            cancellable = true,
            remap = false,
            require = 1
    )
    private void untilEternity$restoreSoulCurio(
            int slot,
            ItemStack stack,
            CallbackInfoReturnable<Boolean> cir
    ) {
        if (!SoulCurioReloadGuard.isRestoring()
                || stack.isEmpty()) {
            return;
        }

        ResourceLocation itemId =
                ForgeRegistries.ITEMS.getKey(stack.getItem());

        if (itemId == null
                || !"eternal_career".equals(itemId.getNamespace())) {
            return;
        }

        String requiredSlot = switch (itemId.getPath()) {
            case "soul_core",
                    "blood_hunter_core",
                    "hyena_core" -> "ring";

            case "soul_box" -> "scroll";

            default -> null;
        };

        if (requiredSlot == null) {
            return;
        }

        IDynamicStackHandler handler =
                (IDynamicStackHandler) (Object) this;

        if (slot < 0 || slot >= handler.getSlots()) {
            cir.setReturnValue(false);
            return;
        }

        SlotContext context =
                this.ctxBuilder.apply(slot);

        cir.setReturnValue(
                context != null
                        && context.entity() instanceof Player
                        && !context.cosmetic()
                        && requiredSlot.equals(context.identifier())
        );
    }
}
