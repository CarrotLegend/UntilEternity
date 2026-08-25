package com.carrot123.until_eternity.mixin.compat.goety;

import com.Polarice3.Goety.common.blocks.entities.IceBouquetTrapBlockEntity;
import com.Polarice3.Goety.common.entities.projectiles.IceBouquet;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = IceBouquetTrapBlockEntity.class, remap = false)
public abstract class IceBouquetTrapBlockEntityMixin {
    @Redirect(
            method = "tick()V",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/Polarice3/Goety/common/entities/projectiles/IceBouquet;setSoulEating(Z)V",
                    remap = false
            ),
            remap = false,
            require = 1
    )
    private void untilEternity$markTrapIceBouquet(
            IceBouquet iceBouquet,
            boolean soulEating
    ) {
        iceBouquet.setSoulEating(soulEating);
        iceBouquet.getPersistentData().putBoolean(
                "until_eternity:ice_bouquet_trap",
                true
        );
    }
}