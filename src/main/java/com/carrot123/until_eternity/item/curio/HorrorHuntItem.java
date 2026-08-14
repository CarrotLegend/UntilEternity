package com.carrot123.until_eternity.item.curio;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.List;

public final class HorrorHuntItem extends BaseModCurioItem {
    public HorrorHuntItem() {
        super(
                new Properties().rarity(Rarity.EPIC).fireResistant(),
                new ResourceLocation("until_eternity", "horror_hunt"),
                List.of()
        );
    }

    @Override
    public void appendHoverText(
            ItemStack stack,
            @Nullable Level level,
            List<Component> tooltip,
            TooltipFlag flag
    ) {
        tooltip.add(Component.translatable(
                "tooltip.until_eternity.horror_hunt.effect"));
    }
}
