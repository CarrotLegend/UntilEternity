package com.carrot123.until_eternity.item.curio;

import com.carrot123.until_eternity.item.ModItems;
import com.carrot123.until_eternity.until_eternity;
import com.carrot123.until_eternity.util.CurioTooltipHelper;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(
        modid = until_eternity.MODID,
        value = Dist.CLIENT,
        bus = Mod.EventBusSubscriber.Bus.FORGE
)
public final class ProofOfSpurnerTooltipEvents {

    private ProofOfSpurnerTooltipEvents() {
    }

    @SubscribeEvent
    public static void onItemTooltip(ItemTooltipEvent event) {
        if (!event.getItemStack().is(ModItems.PROOF_OF_SPURNER.get())) {
            return;
        }

        CurioTooltipHelper.addLocalizedString(
                event.getToolTip(),
                "tooltip.until_eternity.proof_of_spurner.lore1"
        );

        CurioTooltipHelper.addLocalizedString(
                event.getToolTip(),
                "tooltip.until_eternity.proof_of_spurner.lore2"
        );

        CurioTooltipHelper.addBlank(event.getToolTip());

        CurioTooltipHelper.addLocalizedString(
                event.getToolTip(),
                "tooltip.until_eternity.proof_of_spurner.attributes_header"
        );

        CurioTooltipHelper.addLocalizedString(
                event.getToolTip(),
                "tooltip.until_eternity.proof_of_spurner.all_damage"
        );

        CurioTooltipHelper.addLocalizedString(
                event.getToolTip(),
                "tooltip.until_eternity.proof_of_spurner.max_health"
        );

        CurioTooltipHelper.addLocalizedString(
                event.getToolTip(),
                "tooltip.until_eternity.proof_of_spurner.armor"
        );

        CurioTooltipHelper.addLocalizedString(
                event.getToolTip(),
                "tooltip.until_eternity.proof_of_spurner.armor_toughness"
        );

        CurioTooltipHelper.addLocalizedString(
                event.getToolTip(),
                "tooltip.until_eternity.proof_of_spurner.attack_speed"
        );

        CurioTooltipHelper.addLocalizedString(
                event.getToolTip(),
                "tooltip.until_eternity.proof_of_spurner.knockback"
        );

        CurioTooltipHelper.addLocalizedString(
                event.getToolTip(),
                "tooltip.until_eternity.proof_of_spurner.damage_resistance"
        );

        CurioTooltipHelper.addLocalizedString(
                event.getToolTip(),
                "tooltip.until_eternity.proof_of_spurner.armor_shred"
        );

        CurioTooltipHelper.addLocalizedString(
                event.getToolTip(),
                "tooltip.until_eternity.proof_of_spurner.protection_shred"
        );
    }
}