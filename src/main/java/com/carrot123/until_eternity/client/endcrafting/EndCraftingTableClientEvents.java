package com.carrot123.until_eternity.client.endcrafting;

import com.carrot123.until_eternity.block.entity.ModBlockEntities;
import com.carrot123.until_eternity.until_eternity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(
        modid = until_eternity.MODID,
        bus = Mod.EventBusSubscriber.Bus.MOD,
        value = Dist.CLIENT)
public final class EndCraftingTableClientEvents {
    private EndCraftingTableClientEvents() {
    }

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(
                ModBlockEntities.END_CRAFTING_TABLE.get(),
                EndCraftingTableBlockEntityRenderer::new);
    }
}
