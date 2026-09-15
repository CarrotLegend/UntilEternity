package com.carrot123.until_eternity.compat;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.jar.JarFile;
import java.util.jar.JarInputStream;
import java.util.stream.StreamSupport;

import static org.junit.jupiter.api.Assertions.*;

class AbsoluteWeaponCompatibilityTest {
    private static final String ROOT = "com/carrot123/until_eternity/";

    @Test
    void contextsAreIsolatedAndRequireDirectPlayerAttack() throws Exception {
        ClassNode knife = own("combat/TrueChefsKnifeAbsoluteDamageContext");
        assertEquals(1, constants(knife, "true_chefs_knife"));
        assertEquals(0, constants(knife, "netherworld_katana"));
        verifyContext(knife, true);
        ClassNode katana = own("combat/NetherworldKatanaAttackContext");
        assertEquals(1, constants(katana, "netherworld_katana"));
        assertEquals(0, constants(katana, "true_chefs_knife"));
        verifyContext(katana, false);

        ClassNode playerMixin = own("mixin/AbsoluteWeaponPlayerAttackMixin");
        MethodNode wrapper = method(playerMixin,
                "untilEternity$trackAbsoluteWeaponAttack");
        assertEquals(1, calls(wrapper, "withAttack"));
        assertEquals(2, allCalls(playerMixin, "withAttack"));
        assertEquals(0, calls(wrapper, "hurt"));
    }

    @Test
    void onlyHurtHandlersSplitAndWriteHealthOrAmount() throws Exception {
        for (String name : List.of(
                "compat/revelationfix/TrueChefsKnifeDamageHandler",
                "compat/revelationfix/NetherworldKatanaDamageHandler")) {
            ClassNode handler = own(name);
            MethodNode hurt = method(handler, "onLivingHurt");
            assertEquals(1, calls(hurt, "setHealth"), name);
            assertEquals(1, calls(hurt, "setAmount"), name);
            assertEquals(1, calls(hurt, "claimSplit"), name);
            assertTrue(firstCall(hurt, "setAmount") < firstCall(hurt, "lockAmountUp"));
            for (String phase : List.of("onLivingAttack", "onLivingDamage", "onLivingDeath")) {
                MethodNode event = method(handler, phase);
                assertEquals(0, calls(event, "setHealth"), phase);
                assertEquals(0, calls(event, "setAmount"), phase);
                assertEquals(0, calls(event, "hurt"), phase);
            }
        }
        assertEquals(1, calls(method(own(
                "compat/revelationfix/NetherworldKatanaDamageHandler"),
                "onLivingHurt"), "doubleDamage"));
    }

    @Test
    void exactBossAndRevelationFixMixinsAreConfigured() throws Exception {
        Set<String> names = configuredMixins();
        assertTrue(names.containsAll(Set.of(
                "compat.goety.EnderKeeperTrueChefsKnifeMixin",
                "compat.goety.ApostleTrueChefsKnifeMixin",
                "compat.eeeabsmobs.NamelessGuardianTrueChefsKnifeMixin",
                "compat.revelationfix.ApostleRevelationFixTrueChefsKnifeMixin",
                "compat.revelationfix.SafeClassTrueChefsKnifeMixin",
                "compat.revelationfix.AttackDamageChangeHandlerTrueChefsKnifeMixin")));
        assertTrue(names.stream().noneMatch(name -> name.contains("Wroughtnaut")));
        assertMixinTarget("mixin/compat/goety/EnderKeeperTrueChefsKnifeMixin",
                "com.Polarice3.Goety.common.entities.boss.EnderKeeper");
        assertMixinTarget("mixin/compat/goety/ApostleTrueChefsKnifeMixin",
                "com.Polarice3.Goety.common.entities.boss.Apostle");
        assertMixinTarget("mixin/compat/eeeabsmobs/NamelessGuardianTrueChefsKnifeMixin",
                "com.eeeab.eeeabsmobs.sever.entity.guling.EntityNamelessGuardian");
        AnnotationNode mixin = annotation(own(
                "mixin/compat/revelationfix/ApostleRevelationFixTrueChefsKnifeMixin"));
        assertTrue(mixin.values.toString().contains("-2400"));

        ClassNode rfApostle = own(
                "mixin/compat/revelationfix/ApostleRevelationFixTrueChefsKnifeMixin");
        assertEquals(1, annotationTargets(rfApostle,
                "Apostle;revelaionfix$getHitCooldown()I"));
        assertEquals(1, annotationTargets(rfApostle,
                "ForgeConfigSpec$ConfigValue;get()Ljava/lang/Object;"));
        assertEquals(1, annotationTargets(rfApostle, "Math;min(FF)F"));
    }

    @Test
    void bossMixinsTouchOnlyLockedReadsAndCapCalls() throws Exception {
        ClassNode ender = own("mixin/compat/goety/EnderKeeperTrueChefsKnifeMixin");
        ClassNode apostle = own("mixin/compat/goety/ApostleTrueChefsKnifeMixin");
        ClassNode guardian = own("mixin/compat/eeeabsmobs/NamelessGuardianTrueChefsKnifeMixin");
        assertEquals(2, annotationTargets(ender, "moddedInvul:I"));
        assertEquals(2, annotationTargets(apostle, "moddedInvul:I"));
        assertEquals(1, annotationTargets(apostle, "obsidianInvul:I"));
        assertEquals(1, annotationTargets(guardian, "guardianInvulnerableTime:I"));
        assertEquals(1, annotationTargets(guardian, "Math;min(FF)F"));
        for (ClassNode node : List.of(ender, apostle, guardian)) {
            assertEquals(0, fieldWrites(node, "moddedInvul"));
            assertEquals(0, fieldWrites(node, "guardianInvulnerableTime"));
            for (String forbidden : List.of("hurt", "die", "remove", "discard")) {
                assertEquals(0, allCalls(node, forbidden), forbidden);
            }
        }
    }

    @Test
    void lockedBossJarsStillMatchInjectionContracts() throws Exception {
        Path goety = jarWith("com/Polarice3/Goety/common/entities/boss/EnderKeeper.class");
        Path eeeab = jarWith(
                "com/eeeab/eeeabsmobs/sever/entity/guling/EntityNamelessGuardian.class");
        assertTrue(fieldReads(jarClass(goety,
                "com/Polarice3/Goety/common/entities/boss/EnderKeeper.class"), "moddedInvul") >= 2);
        ClassNode apostle = jarClass(goety,
                "com/Polarice3/Goety/common/entities/boss/Apostle.class");
        assertTrue(fieldReads(apostle, "moddedInvul") >= 2);
        assertTrue(fieldReads(apostle, "obsidianInvul") >= 1);
        ClassNode guardian = jarClass(eeeab,
                "com/eeeab/eeeabsmobs/sever/entity/guling/EntityNamelessGuardian.class");
        assertTrue(fieldReads(guardian, "guardianInvulnerableTime") >= 2);
        MethodNode guardianHurt = guardian.methods.stream()
                .filter(value -> value.desc.equals(
                        "(Lnet/minecraft/world/damagesource/DamageSource;F)Z"))
                .findFirst().orElseThrow();
        assertEquals(1, calls(guardianHurt, "min"));
    }

    @Test
    void lockedRevelationFixHasRequiredOrderTagsAndHooks() throws Exception {
        byte[] nested = revelationFixBytes();
        assertEquals(-2324, nestedJson(nested, "revelationfix.mixins.json")
                .get("priority").getAsInt());
        assertPublicMethod(nestedClass(nested,
                "com/mega/revelationfix/safe/DamageSourceInterface.class"),
                "revelationfix$setBypassAll", "(Z)V");
        ClassNode events = nestedClass(nested,
                "com/mega/revelationfix/safe/entity/LivingEventEC.class");
        assertPublicMethod(events, "revelationfix$hackedUnCancelable", "(Z)V");
        assertPublicMethod(events, "revelationfix$hackedOnlyAmountUp", "(Z)V");

        ClassNode sourceMixin = nestedClass(nested,
                "com/mega/revelationfix/mixin/DamageSourceMixin.class");
        MethodNode tagHook = sourceMixin.methods.stream()
                .filter(value -> value.desc.startsWith("(Lnet/minecraft/tags/TagKey;"))
                .findFirst().orElseThrow();
        Set<String> tags = new HashSet<>();
        StreamSupport.stream(tagHook.instructions.spliterator(), false)
                .filter(value -> value instanceof FieldInsnNode)
                .map(value -> (FieldInsnNode) value)
                .filter(value -> value.owner.equals("net/minecraft/tags/DamageTypeTags"))
                .forEach(value -> tags.add(value.name));
        assertEquals(Set.of("f_268490_", "f_276146_", "f_268738_",
                "f_273918_", "f_268630_", "f_268437_"), tags);

        ClassNode reducer = nestedClass(nested,
                "com/mega/revelationfix/common/apollyon/common/AttackDamageChangeHandler.class");
        assertNotNull(method(reducer, "redirectActuallyHurtAmount"));
        ClassNode apollyon = nestedClass(nested,
                "com/mega/revelationfix/mixin/goety/ApollyonMixin.class");
        assertTrue(allCalls(apollyon, "redirectActuallyHurtAmount") >= 1);
        assertTrue(allCalls(apollyon, "revelaionfix$getHitCooldown") >= 1);
        assertTrue(allCalls(apollyon, "min") >= 2);
        MethodNode setHealth = apollyon.methods.stream()
                .filter(value -> value.name.equals("m_21153_") && value.desc.equals("(F)V"))
                .findFirst().orElseThrow();
        assertEquals(1, calls(setHealth, "revelaionfix$getHitCooldown"));
        assertEquals(2, calls(setHealth,
                "net/minecraftforge/common/ForgeConfigSpec$ConfigValue", "get",
                "()Ljava/lang/Object;"));
        assertEquals(2, calls(setHealth, "java/lang/Math", "min", "(FF)F"));
    }

    private static void verifyContext(ClassNode node, boolean marker) {
        MethodNode capture = method(node, "withAttack");
        assertEquals(1, calls(capture, "getMainHandItem"));
        assertEquals(1, calls(capture, "getEntity"));
        assertEquals(1, calls(capture, "getDirectEntity"));
        assertEquals(1, calls(capture, "withValue"));
        assertEquals(marker ? 1 : 0, allCalls(node, "accept"));
        assertTrue(StreamSupport.stream(capture.instructions.spliterator(), false)
                .anyMatch(value -> value instanceof FieldInsnNode field
                        && field.name.equals("PLAYER_ATTACK")));
    }

    private static Set<String> configuredMixins() throws Exception {
        try (InputStream input = resource("until_eternity.mixins.json")) {
            JsonArray entries = JsonParser.parseReader(new InputStreamReader(
                    input, StandardCharsets.UTF_8)).getAsJsonObject().getAsJsonArray("mixins");
            Set<String> names = new HashSet<>();
            entries.forEach(value -> names.add(value.getAsString()));
            return names;
        }
    }

    private static void assertMixinTarget(String name, String target) throws Exception {
        assertTrue(annotation(own(name)).values.toString().contains(target));
    }

    private static AnnotationNode annotation(ClassNode node) {
        return node.invisibleAnnotations.stream()
                .filter(value -> value.desc.equals("Lorg/spongepowered/asm/mixin/Mixin;"))
                .findFirst().orElseThrow();
    }

    private static Path jarWith(String entry) throws Exception {
        String cp = System.getProperty("untilEternity.productionMixinAuditClasspath", "");
        for (String value : cp.split(java.io.File.pathSeparator)) {
            Path path = Path.of(value);
            if (!Files.isRegularFile(path)) continue;
            try (JarFile jar = new JarFile(path.toFile())) {
                if (jar.getEntry(entry) != null) return path;
            }
        }
        throw new AssertionError("No audit jar contains " + entry);
    }

    private static byte[] revelationFixBytes() throws Exception {
        try (JarFile outer = new JarFile(Path.of(
                "libs/GoetyRevelation-2.3.2.jar").toFile());
             InputStream input = outer.getInputStream(outer.getJarEntry(
                     "META-INF/jarjar/[Forge]RevelationFix-1.20.1-4.2.jar"))) {
            return input.readAllBytes();
        }
    }

    private static JsonObject nestedJson(byte[] bytes, String name) throws Exception {
        try (JarInputStream jar = new JarInputStream(new ByteArrayInputStream(bytes))) {
            for (java.util.jar.JarEntry entry; (entry = jar.getNextJarEntry()) != null;) {
                if (entry.getName().equals(name)) return JsonParser.parseReader(
                        new InputStreamReader(new ByteArrayInputStream(
                                jar.readAllBytes()), StandardCharsets.UTF_8)).getAsJsonObject();
            }
        }
        throw new AssertionError("Missing " + name);
    }

    private static ClassNode own(String name) throws Exception {
        try (InputStream input = resource(ROOT + name + ".class")) {
            assertNotNull(input, name);
            return read(input);
        }
    }

    private static ClassNode jarClass(Path path, String name) throws Exception {
        try (JarFile jar = new JarFile(path.toFile());
             InputStream input = jar.getInputStream(jar.getJarEntry(name))) {
            return read(input);
        }
    }

    private static ClassNode nestedClass(byte[] bytes, String name) throws Exception {
        try (JarInputStream jar = new JarInputStream(new ByteArrayInputStream(bytes))) {
            for (java.util.jar.JarEntry entry; (entry = jar.getNextJarEntry()) != null;) {
                if (entry.getName().equals(name)) return read(
                        new ByteArrayInputStream(jar.readAllBytes()));
            }
        }
        throw new AssertionError("Missing " + name);
    }

    private static ClassNode read(InputStream input) throws Exception {
        ClassNode node = new ClassNode();
        new ClassReader(input).accept(node, 0);
        return node;
    }

    private static InputStream resource(String name) {
        return AbsoluteWeaponCompatibilityTest.class.getClassLoader().getResourceAsStream(name);
    }

    private static MethodNode method(ClassNode node, String name) {
        return node.methods.stream().filter(value -> value.name.equals(name))
                .findFirst().orElseThrow();
    }

    private static void assertPublicMethod(ClassNode node, String name, String desc) {
        MethodNode value = node.methods.stream()
                .filter(candidate -> candidate.name.equals(name) && candidate.desc.equals(desc))
                .findFirst().orElseThrow();
        assertNotEquals(0, value.access & Opcodes.ACC_PUBLIC);
    }

    private static long calls(MethodNode node, String name) {
        return StreamSupport.stream(node.instructions.spliterator(), false)
                .filter(value -> value instanceof MethodInsnNode call
                        && call.name.equals(name)).count();
    }

    private static long calls(MethodNode node, String owner, String name, String desc) {
        return StreamSupport.stream(node.instructions.spliterator(), false)
                .filter(value -> value instanceof MethodInsnNode call
                        && call.owner.equals(owner) && call.name.equals(name)
                        && call.desc.equals(desc)).count();
    }

    private static long allCalls(ClassNode node, String name) {
        return node.methods.stream().mapToLong(value -> calls(value, name)).sum();
    }

    private static int firstCall(MethodNode node, String name) {
        for (int i = 0; i < node.instructions.size(); i++) {
            if (node.instructions.get(i) instanceof MethodInsnNode call
                    && call.name.equals(name)) return i;
        }
        return -1;
    }

    private static long constants(ClassNode node, Object value) {
        return node.methods.stream().flatMap(method ->
                        StreamSupport.stream(method.instructions.spliterator(), false))
                .filter(item -> item instanceof LdcInsnNode constant
                        && value.equals(constant.cst)).count();
    }

    private static long annotationTargets(ClassNode node, String value) {
        return node.methods.stream()
                .flatMap(method -> java.util.stream.Stream.concat(
                        method.visibleAnnotations == null
                                ? java.util.stream.Stream.empty()
                                : method.visibleAnnotations.stream(),
                        method.invisibleAnnotations == null
                                ? java.util.stream.Stream.empty()
                                : method.invisibleAnnotations.stream()))
                .filter(annotation -> annotationContains(annotation, value)).count();
    }

    private static boolean annotationContains(Object object, String value) {
        if (object == null) return false;
        if (object instanceof AnnotationNode annotation) {
            return annotationContains(annotation.values, value);
        }
        if (object instanceof Iterable<?> iterable) {
            for (Object item : iterable) {
                if (annotationContains(item, value)) return true;
            }
            return false;
        }
        return object.toString().contains(value);
    }

    private static long fieldWrites(ClassNode node, String name) {
        return node.methods.stream().flatMap(method ->
                        StreamSupport.stream(method.instructions.spliterator(), false))
                .filter(item -> item instanceof FieldInsnNode field
                        && field.name.equals(name) && (field.getOpcode() == Opcodes.PUTFIELD
                        || field.getOpcode() == Opcodes.PUTSTATIC)).count();
    }

    private static long fieldReads(ClassNode node, String name) {
        return node.methods.stream().flatMap(method ->
                        StreamSupport.stream(method.instructions.spliterator(), false))
                .filter(item -> item instanceof FieldInsnNode field
                        && field.name.equals(name) && field.getOpcode() == Opcodes.GETFIELD).count();
    }
}
