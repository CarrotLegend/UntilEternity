package com.carrot123.until_eternity.mixin.compat.enigmaticaddons;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.registries.ForgeRegistries;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.type.inventory.IDynamicStackHandler;

@Pseudo
@Mixin(
        targets = "auviotre.enigmatic.addon.handlers.AddonEventHandler",
        remap = false
)
public abstract class InsigniaMiningRestrictionMixin {

    @Unique
    private static final ResourceLocation UNTIL_ETERNITY$ADVENTURE_CHARM =
            new ResourceLocation("enigmaticaddons", "adventure_charm");

    @Unique
    private static final ResourceLocation UNTIL_ETERNITY$DESPAIR_INSIGNIA =
            new ResourceLocation("enigmaticaddons", "despair_insignia");

    @Unique
    private static final ThreadLocal<Boolean> UNTIL_ETERNITY$WAS_CANCELLED =
            ThreadLocal.withInitial(() -> false);

    @Inject(
            method = "miningStuff(Lnet/minecraftforge/event/entity/player/PlayerEvent$BreakSpeed;)V",
            at = @At("HEAD"),
            remap = false
    )
    private void untilEternity$rememberCancellationState(
            PlayerEvent.BreakSpeed event,
            CallbackInfo ci
    ) {
        UNTIL_ETERNITY$WAS_CANCELLED.set(event.isCanceled());
    }

    @Inject(
            method = "miningStuff(Lnet/minecraftforge/event/entity/player/PlayerEvent$BreakSpeed;)V",
            at = @At("TAIL"),
            remap = false
    )
    private void untilEternity$removeInsigniaMiningRestriction(
            PlayerEvent.BreakSpeed event,
            CallbackInfo ci
    ) {
        try {
            boolean wasCancelled = UNTIL_ETERNITY$WAS_CANCELLED.get();

            if (wasCancelled || !event.isCanceled()) {
                return;
            }

            Player player = event.getEntity();

            if (untilEternity$hasEquipped(
                    player,
                    UNTIL_ETERNITY$ADVENTURE_CHARM
            ) || untilEternity$hasEquipped(
                    player,
                    UNTIL_ETERNITY$DESPAIR_INSIGNIA
            )) {
                event.setCanceled(false);
            }
        } finally {
            UNTIL_ETERNITY$WAS_CANCELLED.remove();
        }
    }

    @Unique
    private static boolean untilEternity$hasEquipped(
            Player player,
            ResourceLocation itemId
    ) {
        Item target = ForgeRegistries.ITEMS.getValue(itemId);

        if (target == null) {
            return false;
        }

        return CuriosApi.getCuriosInventory(player)
                .map(handler -> handler.getCurios()
                        .values()
                        .stream()
                        .anyMatch(stacksHandler ->
                                untilEternity$containsItem(
                                        stacksHandler.getStacks(),
                                        target
                                )
                        )
                )
                .orElse(false);
    }

    @Unique
    private static boolean untilEternity$containsItem(
            IDynamicStackHandler stacks,
            Item target
    ) {
        for (int slot = 0; slot < stacks.getSlots(); slot++) {
            if (stacks.getStackInSlot(slot).is(target)) {
                return true;
            }
        }

        return false;
    }
}