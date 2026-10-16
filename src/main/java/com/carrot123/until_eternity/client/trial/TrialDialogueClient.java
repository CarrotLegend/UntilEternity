package com.carrot123.until_eternity.client.trial;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;

public final class TrialDialogueClient {
    private TrialDialogueClient() {}
    public static void open(BlockPos pos) {
        if (Minecraft.getInstance().level != null) Minecraft.getInstance().setScreen(new TrialDialogueScreen(pos));
    }
}
