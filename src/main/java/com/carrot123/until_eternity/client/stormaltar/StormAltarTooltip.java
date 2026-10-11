package com.carrot123.until_eternity.client.stormaltar;

import com.carrot123.until_eternity.recipe.StormAltarRecipe;
import com.carrot123.until_eternity.until_eternity;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

@Mod.EventBusSubscriber(modid = until_eternity.MODID, value = Dist.CLIENT)
public final class StormAltarTooltip {
    private StormAltarTooltip() {}
    @SubscribeEvent public static void onTooltip(ItemTooltipEvent event) {
        ResourceLocation id = ForgeRegistries.ITEMS.getKey(event.getItemStack().getItem());
        if (id != null && id.equals(new ResourceLocation("eternal_career", "generic_left_boot_blessing"))
                && event.getItemStack().hasTag()
                && event.getItemStack().getTag().contains(StormAltarRecipe.OVERRIDES_KEY, 9)) {
            ListTag overrides = event.getItemStack().getTag().getList(StormAltarRecipe.OVERRIDES_KEY, 10);
            for (int i = 0; i < overrides.size(); i++) {
                var data = overrides.getCompound(i);
                if (data.getString("Attribute").equals("until_eternity:all_damage")
                        && data.getDouble("Amount") == 0.04
                        && data.getString("Operation").equals("multiply_base")) {
                    event.getToolTip().add(Component.translatable("tooltip.until_eternity.storm_altar.enhanced")
                            .withStyle(ChatFormatting.AQUA));
                    break;
                }
            }
        }
    }
}
