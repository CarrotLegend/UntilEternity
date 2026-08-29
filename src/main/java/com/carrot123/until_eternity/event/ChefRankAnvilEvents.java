package com.carrot123.until_eternity.event;

import com.Polarice3.Goety.client.inventory.container.DarkAnvilMenu;

import com.carrot123.until_eternity.recipe.ChefRankAnvilRecipe;
import com.carrot123.until_eternity.recipe.ModRecipeTypes;
import com.carrot123.until_eternity.until_eternity;

import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import net.minecraftforge.event.AnvilUpdateEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Optional;

@Mod.EventBusSubscriber(
        modid = until_eternity.MODID,
        bus = Mod.EventBusSubscriber.Bus.FORGE
)
public final class ChefRankAnvilEvents {

    private ChefRankAnvilEvents() {
    }

    @SubscribeEvent
    public static void onAnvilUpdate(AnvilUpdateEvent event) {
        Player player = event.getPlayer();

        if (player == null) {
            return;
        }

        Level level = player.level();

        SimpleContainer input = new SimpleContainer(2);
        input.setItem(0, event.getLeft());
        input.setItem(1, event.getRight());

        Optional<ChefRankAnvilRecipe> recipeOptional =
                level.getRecipeManager().getRecipeFor(
                        ModRecipeTypes.CHEF_RANK_ANVIL.get(),
                        input,
                        level
                );

        if (recipeOptional.isEmpty()) {
            return;
        }

        ChefRankAnvilRecipe recipe =
                recipeOptional.get();

        ItemStack output =
                recipe.assemble(
                        input,
                        level.registryAccess()
                );

        event.setOutput(output);
        event.setCost(recipe.getLevelCost());
        event.setMaterialCost(recipe.getMaterialCost());

        if (player.containerMenu instanceof DarkAnvilMenu darkAnvilMenu) {
            darkAnvilMenu.repairItemCountCost =
                    recipe.getMaterialCost();
        }
    }
}