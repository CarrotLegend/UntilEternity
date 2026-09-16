package com.carrot123.until_eternity.event;

import com.carrot123.until_eternity.combat.TrueChefsKnifeAbsoluteDamageContext;
import com.carrot123.until_eternity.effect.VoidCorrosionDamageContext;
import com.carrot123.until_eternity.effect.VoidCorrosionDamageLogic;
import com.carrot123.until_eternity.effect.VoidCorrosionEffectApplier;
import com.carrot123.until_eternity.item.ModItems;
import com.carrot123.until_eternity.item.curio.CurioEquipmentHelper;
import com.carrot123.until_eternity.registry.ModMobEffects;
import com.carrot123.until_eternity.until_eternity;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(
        modid = until_eternity.MODID,
        bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class VoidCorrosionCombatEvents {
    public static final String ACCESSORY_SLOT = "accessory";

    private VoidCorrosionCombatEvents() {
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onLivingHurt(LivingHurtEvent event) {
        LivingEntity target = event.getEntity();
        if (target.level().isClientSide
                || !target.hasEffect(ModMobEffects.VOID_CORROSION.get())
                || VoidCorrosionDamageContext.isOwnPeriodicDamage(
                        target, event.getSource())
                || TrueChefsKnifeAbsoluteDamageContext
                .wasVoidCorrosionAmplified(target, event.getSource())) {
            return;
        }
        event.setAmount(VoidCorrosionDamageLogic.amplifyIncomingDamage(
                event.getAmount()));
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onLivingDamage(LivingDamageEvent event) {
        LivingEntity target = event.getEntity();
        if (target.level().isClientSide
                || !event.getSource().is(DamageTypes.PLAYER_ATTACK)
                || !(event.getSource().getEntity() instanceof Player player)
                || event.getSource().getDirectEntity() != player
                || target == player
                || !CurioEquipmentHelper.isEquippedInSlot(
                        player, ModItems.VOID_GRIP.get(), ACCESSORY_SLOT)) {
            return;
        }
        VoidCorrosionEffectApplier.forceApply(target, player);
    }
}
