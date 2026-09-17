package com.carrot123.until_eternity.event;

import com.carrot123.until_eternity.combat.TrueChefsKnifeAbsoluteDamageContext;
import com.carrot123.until_eternity.enchantment.ActualEnchantmentLevel;
import com.carrot123.until_eternity.enchantment.ArmorRendDamageLogic;
import com.carrot123.until_eternity.enchantment.ModEnchantments;
import com.carrot123.until_eternity.until_eternity;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = until_eternity.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class ArmorRendCombatEvents {
    private ArmorRendCombatEvents() {
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onLivingHurt(LivingHurtEvent event) {
        if (event.getEntity().level().isClientSide
                || !event.getSource().is(DamageTypes.PLAYER_ATTACK)
                || !(event.getSource().getEntity() instanceof Player player)
                || event.getSource().getDirectEntity() != player
                || event.getEntity() == player
                || TrueChefsKnifeAbsoluteDamageContext.wasArmorRendAmplified(
                        event.getEntity(), event.getSource())) {
            return;
        }
        int level = ActualEnchantmentLevel.read(
                ModEnchantments.ARMOR_REND.get(), player.getMainHandItem());
        event.setAmount(ArmorRendDamageLogic.apply(
                event.getAmount(), event.getEntity().getArmorValue(), level));
    }
}
