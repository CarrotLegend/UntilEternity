package com.carrot123.until_eternity.event;

import com.carrot123.until_eternity.item.ModItems;
import com.carrot123.until_eternity.until_eternity;

import io.redspace.ironsspellbooks.damage.SpellDamageSource;

import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.CombatRules;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;

import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import top.theillusivec4.curios.api.CuriosApi;

@Mod.EventBusSubscriber(
        modid = until_eternity.MODID,
        bus = Mod.EventBusSubscriber.Bus.FORGE
)
public final class MageCrystalSpellPenetrationEvents {

    private static final float BASIC_PENETRATION = 0.10F;
    private static final float ADVANCED_PENETRATION = 0.20F;

    private MageCrystalSpellPenetrationEvents() {
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onLivingHurt(
            LivingHurtEvent event
    ) {
        if (!(event.getSource()
                instanceof SpellDamageSource)) {
            return;
        }

        if (event.getSource().is(
                DamageTypeTags.BYPASSES_ARMOR
        )) {
            return;
        }

        Entity attacker =
                event.getSource()
                        .getEntity();

        if (!(attacker instanceof Player player)) {
            return;
        }

        float penetration =
                getPenetration(
                        player
                );

        if (penetration <= 0.0F) {
            return;
        }

        LivingEntity target =
                event.getEntity();

        float damage =
                event.getAmount();

        if (damage <= 0.0F
                || !Float.isFinite(damage)) {
            return;
        }

        float armor =
                (float) target.getArmorValue();

        if (armor <= 0.0F) {
            return;
        }

        float toughness =
                (float) target.getAttributeValue(
                        Attributes.ARMOR_TOUGHNESS
                );

        float reducedArmor =
                armor * (1.0F - penetration);

        float normalAfterArmor =
                CombatRules.getDamageAfterAbsorb(
                        damage,
                        armor,
                        toughness
                );

        float penetratedAfterArmor =
                CombatRules.getDamageAfterAbsorb(
                        damage,
                        reducedArmor,
                        toughness
                );

        if (normalAfterArmor <= 0.0F
                || !Float.isFinite(normalAfterArmor)
                || !Float.isFinite(penetratedAfterArmor)) {
            return;
        }

        float compensation =
                penetratedAfterArmor
                        / normalAfterArmor;

        float modified =
                damage * compensation;

        if (!Float.isFinite(modified)) {
            return;
        }

        event.setAmount(
                modified
        );
    }

    private static float getPenetration(
            Player player
    ) {
        float penetration =
                0.0F;

        if (isEquippedInAccessory(
                player,
                ModItems.BASIC_MAGE_CRYSTAL.get()
        )) {
            penetration +=
                    BASIC_PENETRATION;
        }

        if (isEquippedInAccessory(
                player,
                ModItems.ADVANCED_MAGE_CRYSTAL.get()
        )) {
            penetration +=
                    ADVANCED_PENETRATION;
        }

        return Math.min(
                1.0F,
                penetration
        );
    }

    private static boolean isEquippedInAccessory(
            Player player,
            Item item
    ) {
        return CuriosApi
                .getCuriosInventory(player)
                .resolve()
                .map(handler ->
                        handler.findCurios(item)
                                .stream()
                                .anyMatch(result ->
                                        !result.slotContext()
                                                .cosmetic()
                                                && "accessory"
                                                .equals(
                                                        result.slotContext()
                                                                .identifier()
                                                )
                                )
                )
                .orElse(false);
    }
}