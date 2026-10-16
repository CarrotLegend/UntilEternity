
package com.carrot123.until_eternity.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Pseudo
@Mixin(
        targets = "com.branders.spawnermod.gui.SpawnerConfigGui",
        remap = false
)
public abstract class SpawnerConfigGuiTextureMixin {

    @ModifyConstant(
            method = "<init>",
            constant = @Constant(
                    stringValue = "/textures/gui/spawner_config_screen.png"
            ),
            remap = false,
            require = 1
    )
    private String untilEternity$fixSpawnerConfigTexture(
            String original
    ) {
        return "textures/gui/spawner_config_screen.png";
    }
}
