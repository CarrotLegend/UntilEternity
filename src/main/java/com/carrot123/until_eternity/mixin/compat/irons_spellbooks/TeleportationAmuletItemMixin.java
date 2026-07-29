package com.carrot123.until_eternity.mixin.compat.irons_spellbooks;

import io.redspace.ironsspellbooks.item.curios.TeleportationAmuletItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.common.util.NonNullConsumer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICuriosItemHandler;

@Mixin(value = TeleportationAmuletItem.class, remap = false)
public abstract class TeleportationAmuletItemMixin {
    @Invoker("createItemEntity")
    protected abstract void untilEternity$createItemEntity(
            Level level,
            ItemStack stack,
            Vec3 position
    );

    @Redirect(
            method = "handleCurse(Ltop/theillusivec4/curios/api/SlotContext;Lnet/minecraft/world/item/ItemStack;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraftforge/common/util/LazyOptional;ifPresent(Lnet/minecraftforge/common/util/NonNullConsumer;)V",
                    remap = false
            ),
            require = 1,
            remap = false
    )
    private void untilEternity$useActualCurioSlot(
            LazyOptional<ICuriosItemHandler> optional,
            NonNullConsumer<? super ICuriosItemHandler> originalConsumer,
            SlotContext slotContext,
            ItemStack cursedStack
    ) {
        optional.ifPresent(handler -> {
            var stacksHandler = handler.getCurios().get(slotContext.identifier());
            if (stacksHandler == null) {
                return;
            }

            ItemStack equipped = stacksHandler.getStacks()
                    .getStackInSlot(slotContext.index());
            if (!ItemStack.matches(cursedStack, equipped)) {
                return;
            }

            handler.setEquippedCurio(
                    slotContext.identifier(),
                    slotContext.index(),
                    ItemStack.EMPTY
            );
            this.untilEternity$createItemEntity(
                    slotContext.entity().level(),
                    cursedStack,
                    slotContext.entity().position()
            );
        });
    }
}
