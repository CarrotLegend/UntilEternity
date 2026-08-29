package com.carrot123.until_eternity.event;

import com.carrot123.until_eternity.recipe.ChefRankAnvilRecipe;
import com.carrot123.until_eternity.recipe.ModRecipeTypes;
import com.carrot123.until_eternity.until_eternity;

import net.minecraft.network.chat.Component;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import net.minecraftforge.event.AnvilUpdateEvent;
import net.minecraftforge.event.entity.player.AnvilRepairEvent;
import net.minecraftforge.eventbus.api.EventPriority;
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

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onAnvilUpdate(
            AnvilUpdateEvent event
    ) {
        Player player = event.getPlayer();

        if (player == null) {
            return;
        }

        Level level = player.level();

        Optional<ChefRankAnvilRecipe> recipeOptional =
                findRecipe(
                        level,
                        event.getLeft(),
                        event.getRight()
                );

        if (recipeOptional.isEmpty()) {
            return;
        }

        ChefRankAnvilRecipe recipe =
                recipeOptional.get();

        SimpleContainer input =
                createInput(
                        event.getLeft(),
                        event.getRight()
                );

        ItemStack output =
                recipe.assemble(
                        input,
                        level.registryAccess()
                );

        if (output.isEmpty()) {
            return;
        }

        String name = event.getName();

        if (name != null) {
            if (name.isEmpty()) {
                output.resetHoverName();
            } else {
                output.setHoverName(
                        Component.literal(name)
                );
            }
        }

        event.setCost(
                recipe.getLevelCost()
        );

        event.setMaterialCost(
                recipe.getMaterialCost()
        );

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

        Level level = player.level();

        Optional<ChefRankAnvilRecipe> recipeOptional =
                findRecipe(
                        level,
                        event.getLeft(),
                        event.getRight()
                );

        if (recipeOptional.isEmpty()) {
            return;
        }

        ChefRankAnvilRecipe recipe =
                recipeOptional.get();

        menu.repairItemCountCost =
                recipe.getMaterialCost();
    }

    private static Optional<ChefRankAnvilRecipe> findRecipe(
            Level level,
            ItemStack left,
            ItemStack right
    ) {
        SimpleContainer input =
                createInput(
                        left,
                        right
                );

        return level.getRecipeManager()
                .getRecipeFor(
                        ModRecipeTypes.CHEF_RANK_ANVIL.get(),
                        input,
                        level
                );
    }

    private static SimpleContainer createInput(
            ItemStack left,
            ItemStack right
    ) {
        SimpleContainer input =
                new SimpleContainer(2);

        input.setItem(
                0,
                left.copy()
        );

        input.setItem(
                1,
                right.copy()
        );

        return input;
    }
}