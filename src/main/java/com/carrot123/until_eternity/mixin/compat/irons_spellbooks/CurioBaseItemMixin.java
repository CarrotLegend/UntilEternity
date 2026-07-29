package com.carrot123.until_eternity.mixin.compat.irons_spellbooks;

import com.carrot123.until_eternity.compat.ironsspellbooks.IronCurioAttributeCompat;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import io.redspace.ironsspellbooks.item.curios.CurioBaseItem;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import top.theillusivec4.curios.api.SlotContext;

import java.util.UUID;
import java.util.function.Function;

@Mixin(value = CurioBaseItem.class, remap = false)
public abstract class CurioBaseItemMixin {
    @Shadow
    @Nullable
    Function<Integer, Multimap<Attribute, AttributeModifier>> attributes;

    @Inject(
            method = "getAttributeModifiers(Ltop/theillusivec4/curios/api/SlotContext;Ljava/util/UUID;Lnet/minecraft/world/item/ItemStack;)Lcom/google/common/collect/Multimap;",
            at = @At("HEAD"),
            cancellable = true,
            require = 1,
            remap = false
    )
    private void untilEternity$useCuriosSlotUuid(
            SlotContext slotContext,
            UUID slotUuid,
            ItemStack stack,
            CallbackInfoReturnable<Multimap<Attribute, AttributeModifier>> callback
    ) {
        if (this.attributes == null) {
            return;
        }
        if (slotContext.cosmetic()) {
            callback.setReturnValue(ImmutableMultimap.of());
            return;
        }

        Multimap<Attribute, AttributeModifier> original =
                this.attributes.apply(slotContext.index());
        callback.setReturnValue(IronCurioAttributeCompat.rebuild(slotUuid, original));
    }
}
