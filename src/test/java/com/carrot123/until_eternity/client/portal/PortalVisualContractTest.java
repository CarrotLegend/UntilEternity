package com.carrot123.until_eternity.client.portal;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PortalVisualContractTest {
    private static final Path ROOT = Path.of(System.getProperty("user.dir"));

    @Test
    void guiMixinRedirectsOnlyTheVanillaParticleSpriteLookup() throws IOException {
        String source = read("src/main/java/com/carrot123/until_eternity/mixin/client/GuiPortalOverlayMixin.java");

        assertTrue(source.contains("@Redirect("));
        assertTrue(source.contains(
                "renderPortalOverlay(Lnet/minecraft/client/gui/GuiGraphics;F)V"));
        assertTrue(source.contains(
                "BlockModelShaper;getParticleIcon(Lnet/minecraft/world/level/block/state/BlockState;)"));
        assertTrue(source.contains("require = 1"));
        assertFalse(source.contains("cancellable"));
        assertFalse(source.contains("@Inject"));
    }

    @Test
    void portalEntrancesMarkBeforeUsingTheVanillaStateMachine() throws IOException {
        String chaos = read("src/main/java/com/carrot123/until_eternity/block/ChaosPortalBlock.java");
        String immortal = read(
                "src/main/java/com/carrot123/until_eternity/mixin/compat/eeeabsmobs/BlockErosionPortalMixin.java");

        assertBefore(chaos, "PortalVisualType.CHAOS", "entity.handleInsidePortal(pos)");
        assertBefore(immortal, "PortalVisualType.IMMORTAL", "entity.handleInsidePortal(pos)");
        assertTrue(chaos.contains("player.isLocalPlayer")
                || read("src/main/java/com/carrot123/until_eternity/client/portal/PortalVisualTracker.java")
                .contains("player.isLocalPlayer()"));
    }

    @Test
    void customPortalModelsExposeTheirAnimatedTextureAsParticleSprite() throws IOException {
        String chaosNorthSouth = read(
                "src/main/resources/assets/until_eternity/models/block/chaos_portal_ns.json");
        String chaosEastWest = read(
                "src/main/resources/assets/until_eternity/models/block/chaos_portal_ew.json");
        assertTrue(chaosNorthSouth.contains(
                "\"particle\": \"until_eternity:block/chaos_portal\""));
        assertTrue(chaosEastWest.contains(
                "\"particle\": \"until_eternity:block/chaos_portal\""));

        assertEeeabModelParticle("assets/eeeabsmobs/models/block/erosion_portal_ns.json");
        assertEeeabModelParticle("assets/eeeabsmobs/models/block/erosion_portal_ew.json");
    }

    @Test
    void mixinConfigRegistersBothClientHooksAndManualEffectsStayDeleted() throws IOException {
        String config = read("src/main/resources/until_eternity.mixins.json");
        assertTrue(config.contains("\"client.GuiPortalOverlayMixin\""));
        assertTrue(config.contains("\"client.NetherPortalVisualMixin\""));
        assertFalse(Files.exists(ROOT.resolve(
                "src/main/java/com/carrot123/until_eternity/event/ChaosPortalClientEvents.java")));

        String tracker = read(
                "src/main/java/com/carrot123/until_eternity/client/portal/PortalVisualTracker.java");
        String events = read(
                "src/main/java/com/carrot123/until_eternity/client/portal/PortalVisualClientEvents.java");
        for (String forbidden : new String[]{
                "portalAnimTime", "RISE", "DECAY", "ComputeFov",
                "ComputeCameraAngles", "setYaw", "setPitch", "setRoll"}) {
            assertFalse(tracker.contains(forbidden));
            assertFalse(events.contains(forbidden));
        }
    }

    private static void assertEeeabModelParticle(String entryName) throws IOException {
        String classpath = System.getProperty("untilEternity.productionMixinAuditClasspath", "");
        for (String element : classpath.split(Pattern.quote(File.pathSeparator))) {
            if (element.isBlank()) {
                continue;
            }
            try (ZipFile archive = new ZipFile(element)) {
                ZipEntry entry = archive.getEntry(entryName);
                if (entry != null) {
                    String model = new String(
                            archive.getInputStream(entry).readAllBytes(), StandardCharsets.UTF_8);
                    assertTrue(model.contains(
                            "\"particle\": \"eeeabsmobs:block/erosion_portal\""));
                    return;
                }
            }
        }
        throw new AssertionError("Locked EEEAB artifact is missing " + entryName);
    }

    private static void assertBefore(String source, String first, String second) {
        int firstIndex = source.indexOf(first);
        int secondIndex = source.indexOf(second);
        assertTrue(firstIndex >= 0 && secondIndex > firstIndex,
                () -> first + " must appear before " + second);
    }

    private static String read(String relativePath) throws IOException {
        return Files.readString(ROOT.resolve(relativePath), StandardCharsets.UTF_8);
    }
}
