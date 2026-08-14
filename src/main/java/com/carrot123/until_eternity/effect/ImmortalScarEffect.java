package com.carrot123.until_eternity.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

public final class ImmortalScarEffect extends MobEffect {
    public static final int COLOR = 0x4B143F;

    public ImmortalScarEffect() {
        super(MobEffectCategory.HARMFUL, COLOR);
    }
}
