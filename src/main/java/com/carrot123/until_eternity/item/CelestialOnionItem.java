package com.carrot123.until_eternity.item;

import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.type.inventory.ICurioStacksHandler;

public class CelestialOnionItem extends Item {

    private static final String ACCESSORY_SLOT = "accessory";

    private static final int REQUIRED_SLOTS = 7;
    private static final int MAX_SLOTS = 8;

    public CelestialOnionItem() {
        super(new Item.Properties()
                .stacksTo(1)
                .rarity(Rarity.EPIC)
                .fireResistant()
        );
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(
            @NotNull Level level,
            @NotNull Player player,
            @NotNull InteractionHand usedHand
    ) {
        ItemStack stack = player.getItemInHand(usedHand);

        if (level.isClientSide) {
            return InteractionResultHolder.success(stack);
        }

        boolean[] success = {false};

        CuriosApi.getCuriosInventory(player).ifPresent(curiosInventory -> {

            ICurioStacksHandler accessoryHandler =
                    curiosInventory.getCurios().get(ACCESSORY_SLOT);

            if (accessoryHandler == null) {
                return;
            }

            int currentSlots = accessoryHandler.getSlots();

            if (currentSlots >= REQUIRED_SLOTS
                    && currentSlots < MAX_SLOTS) {

                accessoryHandler.grow(1);

                stack.shrink(1);

                success[0] = true;
            }
        });

        if (success[0]) {
            return InteractionResultHolder.consume(stack);
        }

        player.displayClientMessage(
                Component.translatable(
                        "message.until_eternity.celestial_onion.cannot_use"
                ),
                false
        );

        return InteractionResultHolder.fail(stack);
    }

    @Override
    public void appendHoverText(
            @NotNull ItemStack stack,
            @Nullable Level level,
            @NotNull List<Component> tooltip,
            @NotNull TooltipFlag flag
    ) {
        tooltip.add(
                Component.translatable(
                                "tooltip.until_eternity.celestial_onion.line1"
                        )
                        .withStyle(
                                ChatFormatting.GRAY,
                                ChatFormatting.ITALIC
                        )
        );

        tooltip.add(
                Component.translatable(
                                "tooltip.until_eternity.celestial_onion.line2"
                        )
                        .withStyle(ChatFormatting.GOLD)
        );
    }
}