package com.carrot123.until_eternity.event;

import com.carrot123.until_eternity.compat.ironsspellbooks.StaffAffixHelper;
import com.carrot123.until_eternity.compat.ironsspellbooks.StaffUpgradeHelper;
import com.carrot123.until_eternity.recipe.ModRecipeSerializers;
import com.carrot123.until_eternity.until_eternity;
import net.minecraft.ChatFormatting;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(
        modid = until_eternity.MODID,
        bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class StaffAffixEvents {
    private StaffAffixEvents() {
    }

    @SubscribeEvent
    public static void onItemCrafted(PlayerEvent.ItemCraftedEvent event) {
        if (event.getEntity().level().isClientSide) {
            return;
        }
        ItemStack crafted = event.getCrafting();
        if (!StaffUpgradeHelper.isUpgradeableStaff(crafted)) {
            return;
        }

        Recipe<?> recipe = findCraftingRecipe(event.getInventory(), event);
        if (recipe == null) {
            return;
        }
        if (recipe.getSerializer() == ModRecipeSerializers.STAFF_AFFIX_REROLL.get()) {
            StaffAffixHelper.roll(crafted, event.getEntity().getRandom());
            return;
        }
        if (recipe.getSerializer() == ModRecipeSerializers.STAFF_UPGRADE.get()
                || containsUpgradeableStaff(event.getInventory())
                || StaffAffixHelper.getAffix(crafted).isPresent()) {
            return;
        }
        StaffAffixHelper.roll(crafted, event.getEntity().getRandom());
    }

    @SubscribeEvent
    public static void onTooltip(ItemTooltipEvent event) {
        StaffAffixHelper.getAffix(event.getItemStack()).ifPresent(affix ->
                event.getToolTip().add(1,
                        net.minecraft.network.chat.Component.translatable(
                                        affix.translationKey())
                                .withStyle(ChatFormatting.DARK_PURPLE)));
    }

    private static Recipe<?> findCraftingRecipe(
            Container inventory,
            PlayerEvent.ItemCraftedEvent event
    ) {
        if (!(inventory instanceof CraftingContainer craftingContainer)) {
            return null;
        }
        return event.getEntity().level().getRecipeManager()
                .getRecipeFor(RecipeType.CRAFTING, craftingContainer,
                        event.getEntity().level())
                .orElse(null);
    }

    private static boolean containsUpgradeableStaff(Container inventory) {
        for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
            if (StaffUpgradeHelper.isUpgradeableStaff(
                    inventory.getItem(slot))) {
                return true;
            }
        }
        return false;
    }
}
