package com.carrot123.until_eternity.compat;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.registries.ForgeRegistries;

@SuppressWarnings("null")
public final class SummoningRitualsCompat {

    private static final String MOD_ID = "summoningrituals";

    private static final ResourceLocation ALTAR_ID =
            new ResourceLocation(
                    MOD_ID,
                    "altar"
            );

    private static final ResourceLocation INDESTRUCTIBLE_ALTAR_ID =
            new ResourceLocation(
                    MOD_ID,
                    "indestructible_altar"
            );

    private static final double DROP_SEARCH_RADIUS = 3.0D;

    private SummoningRitualsCompat() {
    }

    public static boolean isIndestructibleAltar(
            BlockState state
    ) {
        if (state == null || state.isAir()) {
            return false;
        }

        ResourceLocation blockId =
                ForgeRegistries.BLOCKS.getKey(
                        state.getBlock()
                );

        return INDESTRUCTIBLE_ALTAR_ID.equals(blockId);
    }

    public static Set<UUID> snapshotNearbyItemEntities(
            ServerLevel level,
            BlockPos pos
    ) {
        Set<UUID> result = new HashSet<>();

        AABB searchBox = new AABB(pos)
                .inflate(DROP_SEARCH_RADIUS);

        for (ItemEntity itemEntity :
                level.getEntitiesOfClass(
                        ItemEntity.class,
                        searchBox
                )) {

            result.add(itemEntity.getUUID());
        }

        return result;
    }

    public static void replaceNormalAltarDropWithIndestructibleAltar(
            ServerLevel level,
            BlockPos pos,
            Set<UUID> previousItemEntities
    ) {
        Item indestructibleAltarItem =
                getRegisteredItem(
                        INDESTRUCTIBLE_ALTAR_ID
                );

        if (indestructibleAltarItem == null
                || indestructibleAltarItem == Items.AIR) {
            return;
        }

        Item normalAltarItem =
                getRegisteredItem(
                        ALTAR_ID
                );

        if (normalAltarItem != null
                && normalAltarItem != Items.AIR) {

            removeOneNewNormalAltarDrop(
                    level,
                    pos,
                    normalAltarItem,
                    previousItemEntities
            );
        }

        Block.popResource(
                level,
                pos,
                new ItemStack(indestructibleAltarItem)
        );
    }
    private static void removeOneNewNormalAltarDrop(
            ServerLevel level,
            BlockPos pos,
            Item normalAltarItem,
            Set<UUID> previousItemEntities
    ) {
        AABB searchBox = new AABB(pos)
                .inflate(DROP_SEARCH_RADIUS);

        for (ItemEntity itemEntity :
                level.getEntitiesOfClass(
                        ItemEntity.class,
                        searchBox
                )) {

            if (previousItemEntities.contains(
                    itemEntity.getUUID()
            )) {
                continue;
            }

            ItemStack stack = itemEntity.getItem();

            if (!stack.is(normalAltarItem)) {
                continue;
            }
            if (stack.getCount() <= 1) {
                itemEntity.discard();
            } else {
                stack.shrink(1);
                itemEntity.setItem(stack);
            }
            return;
        }
    }

    private static Item getRegisteredItem(
            ResourceLocation id
    ) {
        if (!ForgeRegistries.ITEMS.containsKey(id)) {
            return null;
        }

        return ForgeRegistries.ITEMS.getValue(id);
    }
}