package com.carrot123.until_eternity.event;

import com.carrot123.until_eternity.compat.eternalcareer.ChefRank;
import com.carrot123.until_eternity.compat.eternalcareer.ChefRankHelper;
import com.carrot123.until_eternity.until_eternity;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.AnvilUpdateEvent;
import net.minecraftforge.event.entity.player.AnvilRepairEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(
        modid = until_eternity.MODID,
        bus = Mod.EventBusSubscriber.Bus.FORGE
)
public final class ChefRankAnvilEvents {

    private static final int LEVEL_COST = 1;
    private static final int BADGE_COST = 1;

    private ChefRankAnvilEvents() {
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onAnvilUpdate(
            AnvilUpdateEvent event
    ) {
        ItemStack armor = event.getLeft();
        ItemStack badge = event.getRight();

        if (!ChefRankHelper.isChefArmor(armor)) {
            return;
        }

        ChefRank targetRank =
                ChefRankHelper.getRankForBadge(
                        badge
                );

        if (targetRank == ChefRank.NONE) {
            return;
        }

        ChefRank currentRank =
                ChefRankHelper.getRank(
                        armor
                );

        if (targetRank.id()
                != currentRank.id() + 1) {
            return;
        }

        ItemStack output = armor.copy();
        output.setCount(1);

        ChefRankHelper.setRank(
                output,
                targetRank
        );

        event.setMaterialCost(BADGE_COST);
        event.setCost(LEVEL_COST);
        event.setOutput(output);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onAnvilRepair(
            AnvilRepairEvent event
    ) {
        Player player = event.getEntity();

        if (!(player.containerMenu instanceof AnvilMenu menu)) {
            return;
        }

        ItemStack armor = event.getLeft();
        ItemStack badge = event.getRight();
        ItemStack output = event.getOutput();

        if (!isChefRankUpgradeResult(
                armor,
                badge,
                output
        )) {
            return;
        }

        menu.repairItemCountCost = BADGE_COST;
    }

    private static boolean isChefRankUpgradeResult(
            ItemStack armor,
            ItemStack badge,
            ItemStack output
    ) {

        if (!ChefRankHelper.isChefArmor(armor)) {
            return false;
        }

        if (output == null
                || output.isEmpty()) {
            return false;
        }

        if (output.getCount() != 1) {
            return false;
        }

        if (!ChefRankHelper.isChefArmor(output)) {
            return false;
        }

        if (!ItemStack.isSameItem(
                armor,
                output
        )) {
            return false;
        }

        ChefRank targetRank =
                ChefRankHelper.getRankForBadge(
                        badge
                );

        if (targetRank == ChefRank.NONE) {
            return false;
        }

        ChefRank currentRank =
                ChefRankHelper.getRank(
                        armor
                );

        if (targetRank.id()
                != currentRank.id() + 1) {
            return false;
        }

        return ChefRankHelper.getRank(output)
                == targetRank;
    }
}