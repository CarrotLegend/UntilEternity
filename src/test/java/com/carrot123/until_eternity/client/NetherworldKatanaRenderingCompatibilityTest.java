package com.carrot123.until_eternity.client;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.IntInsnNode;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.List;
import java.util.jar.JarFile;
import java.util.stream.StreamSupport;

import static org.junit.jupiter.api.Assertions.*;

class NetherworldKatanaRenderingCompatibilityTest {
    private static final String ROOT = "com/carrot123/until_eternity/";
    private static final String MIXIN =
            "client.eeeabsmobs.EMItemStackRenderMixin";
    private static final String TEXTURE =
            "assets/until_eternity/textures/entity/netherworld_katana_remaster.png";
    private static final String TEXTURE_SHA256 =
            "f7f15f5c465226aeb4d427e7e602156d6f07b5aec50b5ca5a35bd8bd5495fbca";

    @Test
    void importedModelAndTexturePreserveTheAuthoredContract() throws Exception {
        assertFalse(Files.exists(Path.of(
                "src/main/resources/assets/netherworld_katana_remaster.java")));
        assertFalse(Files.exists(Path.of(
                "src/main/resources/assets/netherworld_katana_remaster.png")));

        try (InputStream input = resource(TEXTURE)) {
            assertNotNull(input);
            byte[] bytes = input.readAllBytes();
            assertEquals(TEXTURE_SHA256, HexFormat.of().formatHex(
                    MessageDigest.getInstance("SHA-256").digest(bytes)));
            BufferedImage image = ImageIO.read(new java.io.ByteArrayInputStream(bytes));
            assertNotNull(image);
            assertEquals(256, image.getWidth());
            assertEquals(256, image.getHeight());
        }

        ClassNode model = own("client/model/NetherworldKatanaReplacementModel");
        MethodNode layer = method(model, "createBodyLayer");
        assertTrue(numbers(layer, 64.0) >= 2, "logical texture size must remain 64x64");
        assertTrue(numbers(layer, 58.0) >= 1, "main blade geometry is missing");
        assertTrue(numbers(layer, 80.9) >= 1, "blade-tip pivot is missing");
        assertTrue(numbers(layer, 0.0) >= 1, "zero-thickness geometry is missing");
        assertEquals(List.of("root", "bladeRoot", "tsubaRoot", "handleRoot", "skeHead"),
                model.fields.stream()
                        .filter(field -> field.desc.endsWith("ModelPart;"))
                        .map(field -> field.name).toList());
    }

    @Test
    void rendererCachesItsLayerAndPreservesOriginalRenderInputs() throws Exception {
        ClassNode renderer = own(
                "client/render/NetherworldKatanaReplacementRenderer");
        assertEquals(1, allCalls(renderer, "bakeLayer"));
        assertEquals(1, allCalls(renderer, "getArmorFoilBuffer"));
        assertEquals(1, allCalls(renderer, "armorCutoutNoCull"));
        assertEquals(1, allCalls(renderer, "hasFoil"));
        assertEquals(1, constants(renderer, "textures/entity/netherworld_katana_remaster.png"));
        assertEquals(0, constants(renderer,
                "textures/entity/immortal/immortal_boss/immortal_netherworld_katana.png"));
        assertEquals(0, constants(renderer, "THE_NETHERWORLD_KATANA"));

        ClassNode events = own("client/render/NetherworldKatanaClientEvents");
        assertEquals(1, allCalls(events, "registerLayerDefinition"));
        assertEquals(1, allCalls(events, "invalidateModel"));
    }

    @Test
    void clientMixinReplacesOnlyFourHandContexts() throws Exception {
        JsonObject config;
        try (InputStream input = resource("until_eternity.mixins.json")) {
            assertNotNull(input);
            config = JsonParser.parseReader(new InputStreamReader(
                    input, StandardCharsets.UTF_8)).getAsJsonObject();
        }
        JsonArray client = config.getAsJsonArray("client");
        assertTrue(StreamSupport.stream(client.spliterator(), false)
                .anyMatch(value -> MIXIN.equals(value.getAsString())));
        assertFalse(StreamSupport.stream(config.getAsJsonArray("mixins").spliterator(), false)
                .anyMatch(value -> MIXIN.equals(value.getAsString())));

        ClassNode mixin = own("mixin/client/eeeabsmobs/EMItemStackRenderMixin");
        AnnotationNode target = mixin.invisibleAnnotations.stream()
                .filter(value -> value.desc.endsWith("Mixin;"))
                .findFirst().orElseThrow();
        assertTrue(target.values.toString().contains(
                "com/eeeab/eeeabsmobs/client/render/util/EMItemStackRender"));
        MethodNode hook = method(mixin, "untilEternity$renderNetherworldKatana");
        assertEquals(1, calls(hook, "render"));
        assertEquals(1, calls(hook, "cancel"));
        assertEquals(1, fieldNamed(mixin, "THE_NETHERWORLD_KATANA"));
        assertEquals(4, fieldConstants(mixin, "net/minecraft/world/item/ItemDisplayContext"));
    }

    @Test
    void lockedEeeabRendererStillMatchesTheInjectionContract() throws Exception {
        Path eeeab = auditJarWith(
                "com/eeeab/eeeabsmobs/client/render/util/EMItemStackRender.class");
        try (JarFile jar = new JarFile(eeeab.toFile());
             InputStream input = jar.getInputStream(jar.getJarEntry(
                     "com/eeeab/eeeabsmobs/client/render/util/EMItemStackRender.class"))) {
            ClassNode renderer = read(input);
            MethodNode method = renderer.methods.stream()
                    .filter(value -> (value.name.equals("renderByItem")
                            || value.name.equals("m_108829_"))
                            && value.desc.equals("(Lnet/minecraft/world/item/ItemStack;"
                            + "Lnet/minecraft/world/item/ItemDisplayContext;"
                            + "Lcom/mojang/blaze3d/vertex/PoseStack;"
                            + "Lnet/minecraft/client/renderer/MultiBufferSource;II)V"))
                    .findFirst().orElseThrow();
            assertEquals(3, calls(method,
                    "net/minecraft/client/renderer/entity/ItemRenderer",
                    "m_115184_"));
            assertTrue(constants(method, 0.5F) >= 1);
        }
    }

    private static Path auditJarWith(String entry) throws Exception {
        String classpath = System.getProperty(
                "untilEternity.productionMixinAuditClasspath", "");
        for (String value : classpath.split(java.io.File.pathSeparator)) {
            Path path = Path.of(value);
            if (!Files.isRegularFile(path)) continue;
            try (JarFile jar = new JarFile(path.toFile())) {
                if (jar.getEntry(entry) != null) return path;
            }
        }
        throw new AssertionError("No audit jar contains " + entry);
    }

    private static ClassNode own(String name) throws Exception {
        try (InputStream input = resource(ROOT + name + ".class")) {
            assertNotNull(input, name);
            return read(input);
        }
    }

    private static ClassNode read(InputStream input) throws Exception {
        ClassNode node = new ClassNode();
        new ClassReader(input).accept(node, 0);
        return node;
    }

    private static InputStream resource(String name) {
        return NetherworldKatanaRenderingCompatibilityTest.class
                .getClassLoader().getResourceAsStream(name);
    }

    private static MethodNode method(ClassNode node, String name) {
        return node.methods.stream().filter(value -> value.name.equals(name))
                .findFirst().orElseThrow();
    }

    private static long allCalls(ClassNode node, String name) {
        return node.methods.stream().mapToLong(value -> calls(value, name)).sum();
    }

    private static long calls(MethodNode method, String name) {
        return StreamSupport.stream(method.instructions.spliterator(), false)
                .filter(value -> value instanceof MethodInsnNode call
                        && call.name.equals(name)).count();
    }

    private static long calls(MethodNode method, String owner, String name) {
        return StreamSupport.stream(method.instructions.spliterator(), false)
                .filter(value -> value instanceof MethodInsnNode call
                        && call.owner.equals(owner) && call.name.equals(name)).count();
    }

    private static long constants(ClassNode node, Object constant) {
        return node.methods.stream().mapToLong(method -> constants(method, constant)).sum();
    }

    private static long constants(MethodNode method, Object constant) {
        return StreamSupport.stream(method.instructions.spliterator(), false)
                .filter(value -> value instanceof LdcInsnNode item
                        && constant.equals(item.cst)).count();
    }

    private static long numbers(MethodNode method, double expected) {
        return StreamSupport.stream(method.instructions.spliterator(), false)
                .filter(value -> {
                    if (value instanceof LdcInsnNode item
                            && item.cst instanceof Number number) {
                        return Math.abs(number.doubleValue() - expected) < 0.00001;
                    }
                    if (value instanceof IntInsnNode item) {
                        return item.operand == expected;
                    }
                    if (value instanceof InsnNode) {
                        int opcode = value.getOpcode();
                        if (opcode >= org.objectweb.asm.Opcodes.ICONST_M1
                                && opcode <= org.objectweb.asm.Opcodes.ICONST_5) {
                            return opcode - org.objectweb.asm.Opcodes.ICONST_0 == expected;
                        }
                        return (opcode == org.objectweb.asm.Opcodes.FCONST_0
                                || opcode == org.objectweb.asm.Opcodes.DCONST_0)
                                && expected == 0.0;
                    }
                    return false;
                }).count();
    }

    private static long fieldConstants(ClassNode node, String owner) {
        return node.methods.stream().flatMap(method ->
                        StreamSupport.stream(method.instructions.spliterator(), false))
                .filter(value -> value instanceof org.objectweb.asm.tree.FieldInsnNode field
                        && field.owner.equals(owner)).count();
    }

    private static long fieldNamed(ClassNode node, String name) {
        return node.methods.stream().flatMap(method ->
                        StreamSupport.stream(method.instructions.spliterator(), false))
                .filter(value -> value instanceof org.objectweb.asm.tree.FieldInsnNode field
                        && field.name.equals(name)).count();
    }
}
