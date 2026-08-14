package com.carrot123.until_eternity.compat;

import dev.ghen.thirst.foundation.common.capability.IThirst;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodData;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ImFullConsumptionGuardTest {
    @Test
    void foodDecreasesAreRestoredWithoutFillingOrBlockingIncreases() {
        FoodData food = new FoodData();
        food.setFoodLevel(12);
        food.setSaturation(3.5F);
        ImFullConsumptionGuard.FoodSnapshot before =
                ImFullConsumptionGuard.snapshot(food);

        food.setFoodLevel(11);
        food.setSaturation(2.5F);
        ImFullConsumptionGuard.restoreFoodDecreases(food, before);
        assertEquals(12, food.getFoodLevel());
        assertEquals(3.5F, food.getSaturationLevel());

        food.setFoodLevel(14);
        food.setSaturation(5.0F);
        ImFullConsumptionGuard.restoreFoodDecreases(food, before);
        assertEquals(14, food.getFoodLevel());
        assertEquals(5.0F, food.getSaturationLevel());
    }

    @Test
    void thirstAndQuenchedDecreasesAreRestoredButIncreasesRemain() {
        TestThirst thirst = new TestThirst(8, 4);
        ImFullConsumptionGuard.ThirstSnapshot before =
                ImFullConsumptionGuard.snapshot(thirst);

        thirst.setThirst(7);
        thirst.setQuenched(3);
        assertTrue(ImFullConsumptionGuard.restoreThirstDecreases(
                thirst, before));
        assertEquals(8, thirst.getThirst());
        assertEquals(4, thirst.getQuenched());

        thirst.setThirst(10);
        thirst.setQuenched(6);
        assertFalse(ImFullConsumptionGuard.restoreThirstDecreases(
                thirst, before));
        assertEquals(10, thirst.getThirst());
        assertEquals(6, thirst.getQuenched());
    }

    private static final class TestThirst implements IThirst {
        private int thirst;
        private int quenched;
        private float exhaustion;
        private boolean shouldTick = true;

        private TestThirst(int thirst, int quenched) {
            this.thirst = thirst;
            this.quenched = quenched;
        }

        @Override public int getThirst() { return thirst; }
        @Override public void setThirst(int value) { thirst = value; }
        @Override public int getQuenched() { return quenched; }
        @Override public void setQuenched(int value) { quenched = value; }
        @Override public float getExhaustion() { return exhaustion; }
        @Override public void setExhaustion(float value) { exhaustion = value; }
        @Override public void addExhaustion(Player player, float value) {
            exhaustion += value;
        }
        @Override public void tick(Player player) { }
        @Override public void drink(Player player, int thirst, int quenched) {
            this.thirst += thirst;
            this.quenched += quenched;
        }
        @Override public void updateThirstData(Player player) { }
        @Override public void setJustHealed() { }
        @Override public void ExhaustionRecalculate() { }
        @Override public void setShouldTickThirst(boolean value) {
            shouldTick = value;
        }
        @Override public boolean getShouldTickThirst() { return shouldTick; }
        @Override public void copy(IThirst other) {
            thirst = other.getThirst();
            quenched = other.getQuenched();
        }
        @Override public CompoundTag serializeNBT() { return new CompoundTag(); }
        @Override public void deserializeNBT(CompoundTag tag) { }
    }
}
