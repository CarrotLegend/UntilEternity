package com.carrot123.until_eternity.item.curio;

import net.minecraft.world.item.Item;

public class LifeCapItem extends Item {
    private final float maxHealthFraction;   // 比例值，如 0.5
    private final boolean useAbsolute;       // true 表示固定数值（1）

    /**
     * @param maxFraction 当 useAbsolute=false 时，为最大生命值的比例（0~1）；
     *                    当 useAbsolute=true 时，为固定生命值（如 1）。
     */
    public LifeCapItem(Properties properties, float maxFraction, boolean absolute) {
        super(properties);
        this.maxHealthFraction = maxFraction;
        this.useAbsolute = absolute;
    }

    public float getMaxHealthFraction() {
        return maxHealthFraction;
    }

    public boolean isAbsolute() {
        return useAbsolute;
    }
}