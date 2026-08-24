package com.carrot123.until_eternity.item;

import java.util.function.Supplier;

import net.minecraft.world.item.Tier;
import net.minecraft.world.item.crafting.Ingredient;

public enum ModTiers implements Tier {
    TRUE_CHEFS_KNIFE(5, 2000, 10.0F, 998.0F, 25, () -> Ingredient.EMPTY),
    ANCIENT_NETHERITE_BLADE(5, 2031, 9.0F, 31.0F, 15, () -> Ingredient.EMPTY),
    CALAMITY_DAGGER(4, 1561, 8.0F, 69.0F, 22, () -> Ingredient.EMPTY),
    MONSTERS_SCYTHE(5, 2000, 8.0F, 1079.0F, 25, () -> Ingredient.EMPTY),
    FINAL_INGOT(7, 4096, 12.0F, 24.0F, 30, () -> Ingredient.of(ModItems.FINALITE_INGOT.get()));

    private final int level;
    private final int uses;
    private final float speed;
    private final float damage;
    private final int enchantmentValue;
    private final Supplier<Ingredient> repairIngredient;

    ModTiers(int level, int uses, float speed, float damage, int enchantmentValue, Supplier<Ingredient> repairIngredient) {
        this.level = level;
        this.uses = uses;
        this.speed = speed;
        this.damage = damage;
        this.enchantmentValue = enchantmentValue;
        this.repairIngredient = repairIngredient;
    }

    @Override
    public int getUses() {
        return this.uses;
    }

    @Override
    public float getSpeed() {
        return this.speed;
    }

    @Override
    public float getAttackDamageBonus() {
        return this.damage;
    }

    @Override
    public int getLevel() {
        return this.level;
    }

    @Override
    public int getEnchantmentValue() {
        return this.enchantmentValue;
    }

    @Override
    public Ingredient getRepairIngredient() {
        return this.repairIngredient.get();
    }
}
