package com.carrot123.until_eternity.mixin.compat.irons_spellbooks;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import io.redspace.ironsspellbooks.damage.DamageSources;
import io.redspace.ironsspellbooks.entity.spells.EchoingStrikeEntity;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraftforge.registries.ForgeRegistries;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(value = DamageSources.class, remap = false)
public abstract class DamageSourcesEchoingStrikeVoidEchoMixin {
    private static final ResourceLocation VOID_ECHO =
            new ResourceLocation("kaleidoscope_end", "void_echo");

    @WrapMethod(
            method = "applyDamage"
                    + "(Lnet/minecraft/world/entity/Entity;"
                    + "FLnet/minecraft/world/damagesource/DamageSource;)Z"
    )
    private static boolean untilEternity$preventEchoingStrikeVoidEcho(
            Entity target,
            float amount,
            DamageSource source,
            Operation<Boolean> original
    ) {
        if (!(source.getDirectEntity() instanceof EchoingStrikeEntity)) {
            return original.call(target, amount, source);
        }

        if (!(source.getEntity() instanceof LivingEntity attacker)) {
            return original.call(target, amount, source);
        }

        ItemStack weapon = attacker.getMainHandItem();

        if (weapon.isEmpty()) {
            return original.call(target, amount, source);
        }

        Enchantment voidEcho =
                ForgeRegistries.ENCHANTMENTS.getValue(VOID_ECHO);

        if (voidEcho == null) {
            return original.call(target, amount, source);
        }

        int level = weapon.getEnchantmentLevel(voidEcho);

        if (level <= 0) {
            return original.call(target, amount, source);
        }

        Map<Enchantment, Integer> enchantments =
                new HashMap<>(EnchantmentHelper.getEnchantments(weapon));

        enchantments.remove(voidEcho);
        EnchantmentHelper.setEnchantments(enchantments, weapon);

        try {
            return original.call(target, amount, source);
        } finally {
            Map<Enchantment, Integer> current =
                    new HashMap<>(EnchantmentHelper.getEnchantments(weapon));

            current.put(voidEcho, level);
            EnchantmentHelper.setEnchantments(current, weapon);
        }
    }
}