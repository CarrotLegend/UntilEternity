package com.carrot123.until_eternity.item;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FinalIngotPickaxeInteractionResourceTest {
    private static final Path JAVA = Path.of("src/main/java/com/carrot123/until_eternity");

    @Test
    void rightClickHandlerOwnsOnlyTheExactUnbreakableMainHandInteraction() throws Exception {
        String event = source("event/FinalIngotPickaxeInteractionEvents.java");

        assertTrue(event.contains("event.getHand() != InteractionHand.MAIN_HAND"));
        assertTrue(event.contains("!player.isShiftKeyDown()"));
        assertTrue(event.contains("!stack.is(ModItems.FINAL_INGOT_PICKAXE.get())"));
        assertTrue(event.contains("state.isAir() || state.getDestroySpeed(level, event.getPos()) >= 0.0F"));
        assertTrue(event.contains("if (level.isClientSide)"));
        assertTrue(event.contains("isOnCooldown(stack.getItem())"));
        assertEquals(1, count(event, "pickaxe.tryBreakUnbreakableBlock("));
        assertEquals(1, count(event, "event.setCanceled(true)"));
        assertTrue(event.contains("InteractionResult.sidedSuccess(level.isClientSide)"));
        assertTrue(event.contains("InteractionResult.FAIL"));
        assertFalse(event.contains("setUseBlock("));
        assertFalse(event.contains("setUseItem("));
        assertFalse(event.contains("Event.Result.ALLOW"));
    }

    @Test
    void onePublicHelperOwnsDestructionCooldownAndAltarReplacement() throws Exception {
        String item = source("item/FinalIngotPickaxe.java");

        assertTrue(item.contains("public boolean tryBreakUnbreakableBlock("));
        assertTrue(item.contains("level instanceof ServerLevel serverLevel"));
        assertTrue(item.contains("player instanceof ServerPlayer serverPlayer"));
        assertTrue(item.contains("player.getCooldowns().isOnCooldown(this)"));
        assertTrue(item.contains("originalState.isAir()"));
        assertTrue(item.contains("originalState.getDestroySpeed(serverLevel, pos) >= 0.0F"));
        assertEquals(1, count(item, "serverPlayer.gameMode.destroyBlock(pos)"));
        assertEquals(1, count(item, "snapshotNearbyItemEntities("));
        assertEquals(1, count(item, "replaceNormalAltarDropWithIndestructibleAltar("));
        assertEquals(1, count(item, "addCooldown("));
        assertEquals(3, count(item, "tryBreakUnbreakableBlock("));
        assertFalse(item.contains("breakUnbreakableBlock("));
    }

    private static String source(String relative) throws Exception {
        return Files.readString(JAVA.resolve(relative));
    }

    private static int count(String text, String needle) {
        return (text.length() - text.replace(needle, "").length()) / needle.length();
    }
}
