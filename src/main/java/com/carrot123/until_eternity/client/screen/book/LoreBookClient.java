package com.carrot123.until_eternity.client.screen.book;

import com.carrot123.until_eternity.item.lore.LoreBookItem;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public final class LoreBookClient {
    private LoreBookClient() {
    }

    public static void open(LoreBookItem.Type type) {
        Minecraft minecraft = Minecraft.getInstance();
        minecraft.setScreen(switch (type) {
            case MINER_LOG -> new MinerLogScreen();
            case DAMP_DIARY -> new DampDiaryScreen();
            case TORN_PAPER_1, TORN_PAPER_2, TORN_PAPER_3, TORN_PAPER_4, TORN_PAPER_5 ->
                    new TornPaperScreen(type);
        });
    }
}
