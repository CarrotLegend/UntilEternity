package com.carrot123.until_eternity.item;

import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class SnowSpear extends SwordItem {
    public static final float BASE_ATTACK_DAMAGE = 500.0F;
    private static final float PLAYER_BASE_ATTACK_DAMAGE = 1.0F;
    public static final ResourceKey<DamageType> FROST_BITTEN_KEY =
            ResourceKey.create(Registries.DAMAGE_TYPE,
                    new ResourceLocation("until_eternity", "frost_bitten"));

    public SnowSpear(
            Tier tier,
            float attackSpeedModifier,
            Properties properties) {
        super(tier, attackDamageModifier(tier), attackSpeedModifier, properties);
    }

    private static int attackDamageModifier(Tier tier) {
        float modifier = BASE_ATTACK_DAMAGE
                - PLAYER_BASE_ATTACK_DAMAGE
                - tier.getAttackDamageBonus();
        if (modifier != Math.rint(modifier)) {
            throw new IllegalArgumentException(
                    "Snow Spear requires an integral tier attack damage bonus");
        }
        return (int) modifier;
    }

    public static DamageSource frostDamageSource(
            Level level,
            LivingEntity attacker) {
        Holder.Reference<DamageType> frostType = level.registryAccess()
                .registryOrThrow(Registries.DAMAGE_TYPE)
                .getHolderOrThrow(FROST_BITTEN_KEY);
        return new DamageSource(frostType, attacker, attacker);
    }

    @Override
    public boolean hurtEnemy(
            @Nonnull ItemStack stack,
            @Nonnull LivingEntity target,
            @Nonnull LivingEntity attacker) {
        if (!attacker.level().isClientSide) {
            attacker.level().playSound(
                    null,
                    target.getX(),
                    target.getY(),
                    target.getZ(),
                    SoundEvents.SNOW_BREAK,
                    SoundSource.PLAYERS,
                    1.0F,
                    1.0F);
        }
        return super.hurtEnemy(stack, target, attacker);
    }

    @Override
    public void appendHoverText(
            @Nonnull ItemStack stack,
            @Nullable Level worldIn,
            @Nonnull List<Component> tooltip,
            @Nonnull TooltipFlag flagIn) {
        tooltip.add(Component.translatable(
                        "item.until_eternity.snow_spear.desc",
                        Math.round(BASE_ATTACK_DAMAGE))
                .withStyle(ChatFormatting.AQUA));
        super.appendHoverText(stack, worldIn, tooltip, flagIn);
    }

    @Override
    public boolean isEnchantable(@Nonnull ItemStack stack) {
        return true;
    }
}
