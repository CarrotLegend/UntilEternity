package com.carrot123.until_eternity.compat.goetyrevelation;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

@Mod.EventBusSubscriber(
        modid = "until_eternity",
        bus = Mod.EventBusSubscriber.Bus.FORGE
)
public final class BowOfRevelationEffectEvents {

    public static final String REVELATION_ARROW_TAG =
            "UntilEternityBowOfRevelation";

    private static final ResourceLocation BOW_OF_REVELATION =
            new ResourceLocation(
                    "goety_revelation",
                    "bow_of_revelation"
            );

    private BowOfRevelationEffectEvents() {
    }

    @SubscribeEvent
    public static void onEntityJoin(
            EntityJoinLevelEvent event
    ) {
        if (event.getLevel().isClientSide()) {
            return;
        }

        if (!(event.getEntity() instanceof AbstractArrow arrow)) {
            return;
        }

        Entity owner = arrow.getOwner();

        if (!(owner instanceof Player player)) {
            return;
        }

        if (!isUsingBowOfRevelation(player)) {
            return;
        }

        arrow.getPersistentData().putBoolean(
                REVELATION_ARROW_TAG,
                true
        );
    }

    private static boolean isUsingBowOfRevelation(
            Player player
    ) {
        if (isBowOfRevelation(player.getUseItem())) {
            return true;
        }

        if (isBowOfRevelation(player.getMainHandItem())) {
            return true;
        }

        return isBowOfRevelation(
                player.getOffhandItem()
        );
    }

    private static boolean isBowOfRevelation(
            ItemStack stack
    ) {
        if (stack.isEmpty()) {
            return false;
        }

        ResourceLocation id =
                ForgeRegistries.ITEMS.getKey(
                        stack.getItem()
                );

        return BOW_OF_REVELATION.equals(id);
    }
}