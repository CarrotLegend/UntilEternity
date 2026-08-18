package com.carrot123.until_eternity.compat.critical;

import com.carrot123.until_eternity.until_eternity;
import com.obscuria.obscureapi.registry.ObscureAPIAttributes;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.confluence.terra_curio.misc.ModAttributes;

@Mod.EventBusSubscriber(
        modid = until_eternity.MODID,
        bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class UnifiedCriticalHitHandler {
    static final float DEFAULT_CRITICAL_DAMAGE = 2.0F;

    private UnifiedCriticalHitHandler() {
    }

    @SubscribeEvent(priority = EventPriority.NORMAL)
    public static void onLivingDamage(LivingDamageEvent event) {
        if (event.getEntity().level().isClientSide
                || event.getSource().is(DamageTypes.FELL_OUT_OF_WORLD)
                || event.getSource().is(DamageTypes.GENERIC_KILL)
                || event.getAmount() <= 0.0F) {
            return;
        }

        Entity sourceEntity = event.getSource().getEntity();
        if (!(sourceEntity instanceof LivingEntity attacker)) {
            return;
        }
        AttributeInstance criticalChance =
                attacker.getAttribute(ModAttributes.getCriticalChance());
        if (criticalChance == null
                || !shouldCriticalHit(attacker.getRandom().nextFloat(),
                criticalChance.getValue())) {
            return;
        }

        event.setAmount(event.getAmount() * getCriticalDamage(attacker));
    }

    static boolean shouldCriticalHit(float roll, double chance) {
        return roll < chance;
    }

    private static float getCriticalDamage(LivingEntity attacker) {
        try {
            return ObscureAPIAttributes.getCriticalDamage(attacker);
        } catch (RuntimeException | LinkageError ignored) {
            return DEFAULT_CRITICAL_DAMAGE;
        }
    }
}
