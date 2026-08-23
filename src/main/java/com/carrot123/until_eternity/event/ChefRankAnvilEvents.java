package com.carrot123.until_eternity.event;

import com.carrot123.until_eternity.compat.eternalcareer.ChefRank;
import com.carrot123.until_eternity.compat.eternalcareer.ChefRankHelper;
import com.carrot123.until_eternity.until_eternity;

import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.AnvilUpdateEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(
        modid = until_eternity.MODID,
        bus = Mod.EventBusSubscriber.Bus.FORGE
)
public final class ChefRankAnvilEvents {

    private static final int LEVEL_COST = 1;

    private ChefRankAnvilEvents() {
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onAnvilUpdate(
            AnvilUpdateEvent event
    ) {
        ItemStack left = event.getLeft();
        ItemStack right = event.getRight();

        if (!ChefRankHelper.isChefArmor(left)) {
            return;
        }

        ChefRank targetRank =
                ChefRankHelper.getRankForBadge(right);

        if (targetRank == ChefRank.NONE) {
            return;
        }

        ChefRank currentRank =
                ChefRankHelper.getRank(left);

        if (targetRank.id() != currentRank.id() + 1) {
            return;
        }

        ItemStack output = left.copy();
        output.setCount(1);

        ChefRankHelper.setRank(
                output,
                targetRank
        );

        event.setMaterialCost(1);
        event.setCost(LEVEL_COST);
        event.setOutput(output);
    }
}