package com.carrot123.until_eternity.compat.goety;

import com.Polarice3.Goety.common.blocks.entities.ArcaBlockEntity;
import com.Polarice3.Goety.common.capabilities.soulenergy.ISoulEnergy;
import com.Polarice3.Goety.utils.SEHelper;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(
        modid = "until_eternity",
        bus = Mod.EventBusSubscriber.Bus.FORGE
)
public final class ArcaCrossDimensionFix {

    private ArcaCrossDimensionFix() {
    }

    @SubscribeEvent
    public static void onChangedDimension(
            PlayerEvent.PlayerChangedDimensionEvent event
    ) {
        if (event.getEntity() instanceof ServerPlayer player) {
            refreshArca(player);
        }
    }

    @SubscribeEvent
    public static void onLogin(
            PlayerEvent.PlayerLoggedInEvent event
    ) {
        if (event.getEntity() instanceof ServerPlayer player) {
            refreshArca(player);
        }
    }

    @SubscribeEvent
    public static void onRespawn(
            PlayerEvent.PlayerRespawnEvent event
    ) {
        if (event.getEntity() instanceof ServerPlayer player) {
            refreshArca(player);
        }
    }

    private static void refreshArca(
            ServerPlayer player
    ) {
        ISoulEnergy soulEnergy =
                SEHelper.getCapability(player);

        BlockPos arcaPos =
                soulEnergy.getArcaBlock();

        ResourceKey<Level> arcaDimension =
                soulEnergy.getArcaBlockDimension();

        if (arcaPos == null || arcaDimension == null) {
            return;
        }

        MinecraftServer server =
                player.getServer();

        if (server == null) {
            return;
        }

        ServerLevel arcaLevel =
                server.getLevel(arcaDimension);

        if (arcaLevel == null) {
            return;
        }

        arcaLevel.getChunkAt(arcaPos);

        BlockEntity blockEntity =
                arcaLevel.getBlockEntity(arcaPos);

        boolean valid =
                blockEntity instanceof ArcaBlockEntity arca
                        && player.getUUID().equals(
                        arca.getOwnerUUID()
                );

        if (valid) {
            if (!soulEnergy.getSEActive()) {
                soulEnergy.setSEActive(true);
                SEHelper.sendSEUpdatePacket(player);
            }
        } else {
            if (soulEnergy.getSEActive()) {
                soulEnergy.setSEActive(false);
                SEHelper.sendSEUpdatePacket(player);
            }
        }
    }
}
