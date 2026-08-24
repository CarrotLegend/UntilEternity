package com.carrot123.until_eternity.item;

import com.carrot123.until_eternity.registry.ModMobEffects;
import com.carrot123.until_eternity.combat.CookingFrenzyProgression;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;

public final class TrueChefsKnifeItem extends SwordItem {
    public TrueChefsKnifeItem(Properties properties) {
        super(ModTiers.TRUE_CHEFS_KNIFE, 0, -1.8F, properties);
    }

    public static void ensureUnbreakable(ItemStack stack) {
        ensureUnbreakable(stack.getOrCreateTag());
    }

    static void ensureUnbreakable(CompoundTag tag) {
        UnbreakableStackData.apply(tag);
    }

    @Override
    public ItemStack getDefaultInstance() {
        ItemStack stack = super.getDefaultInstance();
        ensureUnbreakable(stack);
        return stack;
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean selected) {
        ensureUnbreakable(stack);
        super.inventoryTick(stack, level, entity, slot, selected);
    }

    @Override
    public void onCraftedBy(ItemStack stack, Level level, Player player) {
        ensureUnbreakable(stack);
        super.onCraftedBy(stack, level, player);
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        ensureUnbreakable(stack);
        boolean result = super.hurtEnemy(stack, target, attacker);
        if (!attacker.level().isClientSide
                && attacker instanceof Player player
                && player.getMainHandItem() == stack) {
            MobEffectInstance current = player.getEffect(ModMobEffects.COOKING_FRENZY.get());
            int amplifier = CookingFrenzyProgression.nextAmplifier(
                    current == null ? -1 : current.getAmplifier());
            player.addEffect(new MobEffectInstance(
                    ModMobEffects.COOKING_FRENZY.get(),
                    CookingFrenzyProgression.DURATION_TICKS,
                    amplifier));
        }
        return result;
    }

    @Override
    public boolean mineBlock(ItemStack stack, Level level, BlockState state, BlockPos pos, LivingEntity miner) {
        ensureUnbreakable(stack);
        return super.mineBlock(stack, level, state, pos, miner);
    }

}
