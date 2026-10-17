package com.carrot123.until_eternity.client.dialogue;

import com.carrot123.until_eternity.entity.StormPriestessEntity;
import com.carrot123.until_eternity.network.OpenStormPriestessDialogueS2CPacket;
import net.minecraft.client.Minecraft;

public class StormPriestessDialogueClient {
    public static void handle(int entityId, byte state) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null) {
            return;
        }
        if (state == OpenStormPriestessDialogueS2CPacket.OPEN) {
            if (minecraft.level.getEntity(entityId) instanceof StormPriestessEntity) {
                minecraft.setScreen(new StormPriestessDialogueScreen(entityId));
            }
            return;
        }
        if (minecraft.screen instanceof StormPriestessDialogueScreen screen && screen.getPriestessId() == entityId) {
            if (state == OpenStormPriestessDialogueS2CPacket.INSUFFICIENT) {
                screen.showInsufficient();
            } else if (state == OpenStormPriestessDialogueS2CPacket.SUCCESS) {
                minecraft.setScreen(null);
            }
        }
    }
}
