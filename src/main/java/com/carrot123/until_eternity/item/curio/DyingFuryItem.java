package com.carrot123.until_eternity.item.curio;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nullable;
import java.util.List;

public final class DyingFuryItem extends BaseModCurioItem {
    public DyingFuryItem() {
        super(
                new Properties().rarity(Rarity.EPIC).fireResistant(),
                new ResourceLocation("until_eternity", "dying_fury"),
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
                "tooltip.until_eternity.dying_fury.effect"));
    }
}
