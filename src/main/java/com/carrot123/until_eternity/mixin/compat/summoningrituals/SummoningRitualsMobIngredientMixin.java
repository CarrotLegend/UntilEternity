package com.carrot123.until_eternity.mixin.compat.summoningrituals;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraftforge.common.ForgeSpawnEggItem;

@Pseudo
@Mixin(
        targets = "com.almostreliable.summoningrituals.compat.viewer.common.MobIngredient",
        remap = false
)
public abstract class SummoningRitualsMobIngredientMixin {

    @Shadow
    @Final
    private EntityType<?> mob;

    @Inject(
            method = "getEgg",
            at = @At("HEAD"),
            cancellable = true,
            remap = false
    )
    private void untilEternity$findForgeSpawnEgg(
            CallbackInfoReturnable<SpawnEggItem> cir
    ) {
        SpawnEggItem forgeEgg = ForgeSpawnEggItem.fromEntityType(this.mob);

        if (forgeEgg != null) {
            cir.setReturnValue(forgeEgg);
        }
    }
}
