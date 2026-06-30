package com.carrot123.until_eternity.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.Optional;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class SnowSpear extends SwordItem {
    private final float frozenDamage;
    public static final ResourceKey<DamageType> FROST_BITTEN_KEY =
            ResourceKey.create(Registries.DAMAGE_TYPE,
                    new ResourceLocation("until_eternity", "frost_bitten"));
    public SnowSpear(Tier tier, int attackDamageModifier, float attackSpeedModifier, float frozenDamage, Properties properties) {
        super(tier, 0, attackSpeedModifier, properties);
        this.frozenDamage = frozenDamage;
    }
    @SuppressWarnings("null")
    @Override
    public boolean hurtEnemy(@Nonnull ItemStack stack, @Nonnull LivingEntity target, @Nonnull LivingEntity attacker) {
        if (attacker.level().isClientSide) return false;
        Optional<Holder.Reference<DamageType>> holderRef = attacker.level().registryAccess()
                .registryOrThrow(Registries.DAMAGE_TYPE)
                .getHolder(FROST_BITTEN_KEY);
        if (holderRef.isPresent()) {
            DamageSource frostSource = new DamageSource(holderRef.get());
            target.hurt(frostSource, frozenDamage);
        }
        // 3. 雪破碎音效
        attacker.level().playSound(null, target.getX(), target.getY(), target.getZ(),
                SoundEvents.SNOW_BREAK, SoundSource.PLAYERS, 1.0F, 1.0F);
        // 4. 耐久消耗
        stack.hurtAndBreak(1, attacker, (p) -> p.broadcastBreakEvent(InteractionHand.MAIN_HAND));
        return true;
    }

    @Override
    public void appendHoverText(@Nonnull ItemStack stack, @Nullable Level worldIn, @Nonnull List<Component> tooltip, @Nonnull TooltipFlag flagIn) {
        tooltip.add(Component.translatable("item.until_eternity.snow_spear.desc", frozenDamage)
                .withStyle(ChatFormatting.AQUA));
        super.appendHoverText(stack, worldIn, tooltip, flagIn);
    }
    @Override
    public boolean isEnchantable(@Nonnull ItemStack stack) {
        return true;
    }
}