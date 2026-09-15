package com.carrot123.until_eternity.compat;

import org.junit.jupiter.api.Test;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.jar.JarFile;
import java.util.stream.StreamSupport;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/** Bytecode contract tests, without loading Aether or bootstrapping a game client. */
class AetherCompatibilityTest {
    private static final String ROOT = "com/carrot123/until_eternity/";
    private static final String CAPABILITY = "com/aetherteam/aether/capability/player/AetherPlayerCapability";

    @Test
    void scopedAttackRestoresOuterValueAndClearsAfterException() {
        ScopedValueStack<String> scope = new ScopedValueStack<>();
        assertNull(scope.current(null));
        scope.withValue("outer", () -> {
            assertThrows(IllegalStateException.class, () -> scope.withValue("inner", () -> {
                assertEquals("inner", scope.current(null));
                throw new IllegalStateException("failed hurt");
            }));
            assertEquals("outer", scope.current(null));
            return false;
        });
        assertNull(scope.current(null));
    }

    @Test
    void multiplierExistsOnlyInFinalDamageHandler() throws Exception {
        MethodNode handler = method(own("event/GravititePickaxeDamageEvents"), "onLivingDamage");
        assertEquals(1, constants(handler, 2.5F));
        assertEquals(1, calls(handler, "claim"));
        assertEquals(1, calls(handler, "setAmount"));
        assertEquals(0, calls(handler, "hurt"));
        ClassNode wrapper = own("mixin/compat/aether/GravititePickaxePlayerAttackMixin");
        assertTrue(wrapper.methods.stream().allMatch(m -> constants(m, 2.5F) == 0));
    }

    @Test
    void attackScopeChecksExactIdsAndDirectPlayerDamageAndClaimsOnce() throws Exception {
        ClassNode context = own("combat/GravititePickaxeAttackContext");
        MethodNode initializer = method(context, "<clinit>");
        assertEquals(1, constants(initializer, "gravitite_pickaxe"));
        assertEquals(1, constants(initializer, "slider"));
        MethodNode capture = method(context, "withAttack");
        assertEquals(1, calls(capture, "getMainHandItem"));
        assertEquals(1, calls(capture, "getEntity"));
        assertEquals(1, calls(capture, "getDirectEntity"));
        assertTrue(StreamSupport.stream(capture.instructions.spliterator(), false)
                .anyMatch(i -> i instanceof FieldInsnNode f && f.name.equals("PLAYER_ATTACK")));
        // withValue is unconditional, so ineligible nested attacks mask the outer scope too.
        assertEquals(1, calls(capture, "withValue"));
        MethodNode claim = method(context, "claim");
        int read = -1;
        int write = -1;
        for (int i = 0; i < claim.instructions.size(); i++) {
            if (claim.instructions.get(i) instanceof FieldInsnNode f && f.name.equals("claimed")) {
                if (f.getOpcode() == Opcodes.GETFIELD) read = i;
                if (f.getOpcode() == Opcodes.PUTFIELD) write = i;
            }
        }
        assertTrue(read >= 0 && write > read, "An already-claimed hit must be checked before consumption");
    }

    @Test
    void lifeShardInjectionHasOneExactUnmappedTarget() throws Exception {
        ClassNode mixin = own("mixin/compat/aether/AetherPlayerLifeShardMixin");
        MethodNode handler = method(mixin, "untilEternity$lifeShardHealthPerUse");
        AnnotationNode annotation = handler.visibleAnnotations.stream()
                .filter(a -> a.desc.endsWith("/ModifyConstant;")).findFirst().orElseThrow();
        assertEquals(List.of("getLifeShardHealthAttributeModifier()Lnet/minecraft/world/entity/ai/attributes/AttributeModifier;"),
                value(annotation, "method"));
        assertEquals(false, value(annotation, "remap"));
        assertEquals(1, value(annotation, "require"));
        assertEquals(1, value(annotation, "allow"));
        List<?> constant = (List<?>) value(annotation, "constant");
        assertEquals(2.0F, value((AnnotationNode) constant.get(0), "floatValue"));
        assertEquals(1, constants(handler, 20.0F));
    }

    @Test
    void cachedAetherUsesCountTimesTwoAndReplacesItsOwnModifier() throws Exception {
        ClassNode capability = aether(CAPABILITY);
        MethodNode getter = method(capability, "getLifeShardHealthAttributeModifier");
        assertEquals(1, constants(getter, 2.0F));
        assertEquals(1, calls(getter, "getLifeShardCount"));
        assertEquals(1, StreamSupport.stream(getter.instructions.spliterator(), false)
                .filter(i -> i.getOpcode() == Opcodes.FMUL).count());
        assertTrue(StreamSupport.stream(getter.instructions.spliterator(), false)
                .anyMatch(i -> i instanceof FieldInsnNode f && f.name.equals("LIFE_SHARD_HEALTH_ID")));
        MethodNode update = method(capability, "handleLifeShardModifier");
        assertEquals(1, calls(update, "removeModifier"));
        assertEquals(1, calls(update, "addTransientModifier"));
        assertTrue(capability.methods.stream().anyMatch(m -> constants(m, "LifeShardCount") > 0));
    }

    @Test
    void cachedSliderChecksLegalityBeforeCallingSuperHurt() throws Exception {
        MethodNode hurt = method(aether("com/aetherteam/aether/entity/monster/dungeon/boss/Slider"), "hurt");
        int gate = -1;
        for (int i = 0; i < hurt.instructions.size(); i++) {
            if (hurt.instructions.get(i) instanceof MethodInsnNode call) {
                if (call.name.equals("canDamageSlider")) gate = i;
                if (call.name.equals("hurt") && call.getOpcode() == Opcodes.INVOKESPECIAL) {
                    assertTrue(gate >= 0 && gate < i);
                }
            }
        }
        assertTrue(gate >= 0);
    }

    @Test
    void cachedSilverHeartsUseActualModifierAmount() throws Exception {
        ClassNode overlays = aether("com/aetherteam/aether/client/renderer/AetherOverlays");
        assertTrue(overlays.methods.stream().anyMatch(m -> calls(m, "getLifeShardHealthAttributeModifier") > 0
                && calls(m, "getAmount") > 0));
    }

    private static ClassNode own(String name) throws Exception {
        try (InputStream stream = AetherCompatibilityTest.class.getClassLoader().getResourceAsStream(ROOT + name + ".class")) {
            assertNotNull(stream);
            return read(stream);
        }
    }

    private static ClassNode aether(String name) throws Exception {
        Path path = Path.of("build/aether-audit/aether.jar");
        assumeTrue(Files.isRegularFile(path), "Optional local Aether audit jar is absent; see docs/aether-compatibility.md");
        try (JarFile jar = new JarFile(path.toFile()); InputStream stream = jar.getInputStream(jar.getJarEntry(name + ".class"))) {
            assertEquals("1.20.1-1.5.2-neoforge", jar.getManifest().getMainAttributes().getValue("Implementation-Version"));
            return read(stream);
        }
    }

    private static ClassNode read(InputStream stream) throws Exception {
        ClassNode node = new ClassNode();
        new ClassReader(stream).accept(node, 0);
        return node;
    }

    private static MethodNode method(ClassNode node, String name) {
        return node.methods.stream().filter(m -> m.name.equals(name)).findFirst().orElseThrow();
    }

    private static long calls(MethodNode method, String name) {
        return StreamSupport.stream(method.instructions.spliterator(), false)
                .filter(i -> i instanceof MethodInsnNode call && call.name.equals(name)).count();
    }

    private static long constants(MethodNode method, Object value) {
        return StreamSupport.stream(method.instructions.spliterator(), false).filter(i ->
                i instanceof LdcInsnNode ldc && value.equals(ldc.cst)
                        || value.equals(2.0F) && i.getOpcode() == Opcodes.FCONST_2).count();
    }

    private static Object value(AnnotationNode annotation, String name) {
        int index = annotation.values.indexOf(name);
        assertTrue(index >= 0, name);
        return annotation.values.get(index + 1);
    }
}
