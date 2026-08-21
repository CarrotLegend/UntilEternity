package com.carrot123.until_eternity.event;

import com.carrot123.until_eternity.compat.goety.FocusDamageMath;
import com.carrot123.until_eternity.compat.goety.GoetyFocusDamageResolver;
import com.carrot123.until_eternity.registry.ModAttributes;
import com.carrot123.until_eternity.until_eternity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(
        modid = until_eternity.MODID,
        bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class FocusDamageEvents {
    private FocusDamageEvents() {
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onLivingHurt(LivingHurtEvent event) {
        float originalDamage = event.getAmount();
        if (!(originalDamage > 0.0F)
                || !Float.isFinite(originalDamage)
                || !(event.getEntity().level() instanceof ServerLevel level)) {
            return;
        }

        ServerPlayer caster = GoetyFocusDamageResolver.resolveCaster(
                level, event.getSource());
        if (caster == null) {
            return;
        }
        double focusDamage = caster.getAttributeValue(
                ModAttributes.FOCUS_DAMAGE.get());
        float modifiedDamage = FocusDamageMath.apply(
                originalDamage, focusDamage);
        if (Float.compare(modifiedDamage, originalDamage) != 0) {
            event.setAmount(modifiedDamage);
        }
    }
}
