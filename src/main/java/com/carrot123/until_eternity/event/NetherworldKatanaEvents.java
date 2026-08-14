package com.carrot123.until_eternity.event;

import com.carrot123.until_eternity.compat.eeeabsmobs.ImmortalScarCombatLogic;
import com.carrot123.until_eternity.registry.ModMobEffects;
import com.carrot123.until_eternity.until_eternity;
import com.eeeab.eeeabsmobs.sever.init.ItemInit;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.ItemAttributeModifierEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.confluence.terra_curio.misc.ModAttributes;

import java.util.UUID;

@Mod.EventBusSubscriber(
        modid = until_eternity.MODID,
        bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class NetherworldKatanaEvents {
    public static final UUID CRITICAL_CHANCE_UUID = UUID.fromString(
            "5d151384-9131-3e77-9d04-d5612e819a6f");
    public static final double CRITICAL_CHANCE_AMOUNT = 0.25D;

    private NetherworldKatanaEvents() {
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onLivingDamage(LivingDamageEvent event) {
        LivingEntity target = event.getEntity();
        float amount = event.getAmount();
        if (target.level().isClientSide
                || !Float.isFinite(amount)
                || amount <= 0.0F
                || !(event.getSource().getEntity() instanceof Player player)
                || event.getSource().getDirectEntity() != player
                || target == player
                || !isNetherworldKatana(player.getMainHandItem())) {
            return;
        }

        boolean wasScarred = target.hasEffect(ModMobEffects.IMMORTAL_SCAR.get());
        if (wasScarred) {
            event.setAmount(ImmortalScarCombatLogic.doubleDamage(amount));
        }

        if (ImmortalScarCombatLogic.shouldApply(
                player.getRandom().nextFloat())) {
            target.addEffect(new MobEffectInstance(
                    ModMobEffects.IMMORTAL_SCAR.get(),
                    ImmortalScarCombatLogic.DURATION_TICKS,
                    ImmortalScarCombatLogic.AMPLIFIER,
                    false,
                    false,
                    true), player);
        }
    }

    @SubscribeEvent
    public static void onItemAttributeModifiers(
            ItemAttributeModifierEvent event) {
        if (event.getSlotType() != EquipmentSlot.MAINHAND
                || !isNetherworldKatana(event.getItemStack())) {
            return;
        }

        event.addModifier(
                ModAttributes.getCriticalChance(),
                new AttributeModifier(
                        CRITICAL_CHANCE_UUID,
                        "until_eternity.netherworld_katana.critical_chance",
                        CRITICAL_CHANCE_AMOUNT,
                        AttributeModifier.Operation.ADDITION));
    }

    public static boolean isNetherworldKatana(
            net.minecraft.world.item.ItemStack stack) {
        return stack.is(ItemInit.THE_NETHERWORLD_KATANA.get());
    }
}
