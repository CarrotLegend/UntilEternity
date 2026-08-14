package com.carrot123.until_eternity.compat;

import dev.ghen.thirst.foundation.common.capability.IThirst;
import net.minecraft.world.food.FoodData;

public final class ImFullConsumptionGuard {
    private ImFullConsumptionGuard() {
    }

    public static FoodSnapshot snapshot(FoodData foodData) {
        return new FoodSnapshot(
                foodData.getFoodLevel(),
                foodData.getSaturationLevel());
    }

    public static void restoreFoodDecreases(
            FoodData foodData,
            FoodSnapshot before
    ) {
        if (foodData.getFoodLevel() < before.foodLevel()) {
            foodData.setFoodLevel(before.foodLevel());
        }
        if (foodData.getSaturationLevel() < before.saturationLevel()) {
            foodData.setSaturation(before.saturationLevel());
        }
    }

    public static ThirstSnapshot snapshot(IThirst thirst) {
        return new ThirstSnapshot(thirst.getThirst(), thirst.getQuenched());
    }

    public static boolean restoreThirstDecreases(
            IThirst thirst,
            ThirstSnapshot before
    ) {
        boolean changed = false;
        if (thirst.getThirst() < before.thirst()) {
            thirst.setThirst(before.thirst());
            changed = true;
        }
        if (thirst.getQuenched() < before.quenched()) {
            thirst.setQuenched(before.quenched());
            changed = true;
        }
        return changed;
    }

    public record FoodSnapshot(int foodLevel, float saturationLevel) {
    }

    public record ThirstSnapshot(int thirst, int quenched) {
    }
}
