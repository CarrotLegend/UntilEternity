package com.carrot123.until_eternity.event;

import com.carrot123.until_eternity.combat.NetherworldKatanaAttackContext;
import com.carrot123.until_eternity.combat.TrueChefsKnifeAbsoluteDamageContext;
import com.carrot123.until_eternity.effect.VoidCorrosionDamageContext;
import com.carrot123.until_eternity.registry.ModAttributes;
import com.carrot123.until_eternity.tarot.TarotSetEffectManager;
import com.carrot123.until_eternity.until_eternity;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(
        modid = until_eternity.MODID,
        bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class AllDamageCombatEvents {
    private AllDamageCombatEvents() {
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onLivingHurt(LivingHurtEvent event) {
        LivingEntity target = event.getEntity();
        DamageSource source = event.getSource();
        if (TrueChefsKnifeAbsoluteDamageContext.wasAllDamageAmplified(
                target, source)
                || NetherworldKatanaAttackContext.wasAllDamageAmplified(
                        target, source)) {
            return;
        }
        float amount = event.getAmount();
        float modified = amplify(target, source, amount);
        if (Float.compare(modified, amount) != 0) {
            event.setAmount(modified);
        }
    }

    public static float amplify(
            LivingEntity target,
            DamageSource source,
            float amount
    ) {
        if (target.level().isClientSide
                || amount <= 0.0F
                || !Float.isFinite(amount)
                || VoidCorrosionDamageContext.isOwnPeriodicDamage(
                        target, source)) {
            return amount;
        }
        ServerPlayer attacker = PlayerDamageAttackerResolver.resolve(source);
        if (attacker == null || attacker == target) {
            return amount;
        }
        DyingFuryCombatEvents.syncModifier(attacker);
        TarotSetEffectManager.syncBeforeAllDamage(attacker);
        double multiplier = attacker.getAttributeValue(
                ModAttributes.ALL_DAMAGE.get());
        if (!Double.isFinite(multiplier)) {
            return amount;
        }
        double modified = (double) amount * multiplier;
        if (!Double.isFinite(modified)) {
            return amount;
        }
        if (modified >= Float.MAX_VALUE) {
            return Float.MAX_VALUE;
        }
        return (float) modified;
    }
}
