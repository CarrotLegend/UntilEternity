package com.carrot123.until_eternity.mixin.compat;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TrueChefsKnifeProductionMixinAuditTest {
    private static final Path ROOT = Path.of(".");
    private static final Path MIXIN_ROOT = ROOT.resolve(Path.of(
            "src", "main", "java", "com", "carrot123", "until_eternity", "mixin"));
    private static final String HURT =
            "hurt(Lnet/minecraft/world/damagesource/DamageSource;F)Z";
    private static final String PRODUCTION_HURT =
            "m_6469_(Lnet/minecraft/world/damagesource/DamageSource;F)Z";

    private static final Set<String> THIRD_PARTY_KNIFE_MIXINS = Set.of(
            "compat.mowziesmobs.EntityWroughtnautTrueChefsKnifeMixin",
            "compat.eeeabsmobs.EeeabTrueChefsKnifeDamageGateMixin",
            "compat.eeeabsmobs.NamelessGuardianTrueChefsKnifeMixin",
            "compat.cataclysm.CataclysmTrueChefsKnifeDamageGateMixin",
            "compat.cataclysm.ClawdianTrueChefsKnifeBlockMixin",
            "compat.cataclysm.IgnisTrueChefsKnifeBlockMixin",
            "compat.cataclysm.IgnitedRevenantTrueChefsKnifeBlockMixin",
            "compat.cataclysm.KobolediatorTrueChefsKnifeBlockMixin",
            "compat.cataclysm.RoyalDraugrTrueChefsKnifeBlockMixin",
            "compat.cataclysm.WadjetTrueChefsKnifeBlockMixin",
            "compat.goety.ApostleTrueChefsKnifeMixin",
            "compat.goety.EnderKeeperTrueChefsKnifeMixin",
            "compat.goety.VizierTrueChefsKnifeMixin",
            "compat.legendary_monsters.LegendaryMonstersTrueChefsKnifeMixin");

    @Test
    void lockedRawProductionJarsExposeEveryAuditedOverrideAndGate() throws IOException {
        List<Path> jars = productionJars();

        assertClassesContain(jars, List.of(
                "com.bobmowzie.mowziesmobs.server.entity.wroughtnaut.EntityWroughtnaut"),
                "m_6469_", "m_7639_", "m_269533_");

        assertClassesContain(jars, List.of(
                "com.github.L_Ender.cataclysm.entity.AnimationMonster.BossMonsters.Amethyst_Crab_Entity",
                "com.github.L_Ender.cataclysm.entity.AnimationMonster.BossMonsters.Ender_Golem_Entity",
                "com.github.L_Ender.cataclysm.entity.AnimationMonster.BossMonsters.Ender_Guardian_Entity",
                "com.github.L_Ender.cataclysm.entity.AnimationMonster.BossMonsters.Ignis_Entity",
                "com.github.L_Ender.cataclysm.entity.AnimationMonster.BossMonsters.LLibrary_Boss_Monster",
                "com.github.L_Ender.cataclysm.entity.AnimationMonster.BossMonsters.The_Harbinger_Entity",
                "com.github.L_Ender.cataclysm.entity.AnimationMonster.BossMonsters.The_Leviathan.The_Leviathan_Entity",
                "com.github.L_Ender.cataclysm.entity.InternalAnimationMonster.Draugar.Aptrgangr_Entity",
                "com.github.L_Ender.cataclysm.entity.InternalAnimationMonster.IABossMonsters.Ancient_Remnant.Ancient_Remnant_Entity",
                "com.github.L_Ender.cataclysm.entity.InternalAnimationMonster.IABossMonsters.IABoss_monster",
                "com.github.L_Ender.cataclysm.entity.InternalAnimationMonster.IABossMonsters.Maledictus.Maledictus_Entity",
                "com.github.L_Ender.cataclysm.entity.InternalAnimationMonster.IABossMonsters.NewNetherite_Monstrosity.Netherite_Monstrosity_Entity",
                "com.github.L_Ender.cataclysm.entity.InternalAnimationMonster.IABossMonsters.Scylla.Scylla_Entity",
                "com.github.L_Ender.cataclysm.entity.InternalAnimationMonster.Kobolediator_Entity",
                "com.github.L_Ender.cataclysm.entity.InternalAnimationMonster.Wadjet_Entity"),
                "m_6469_", "m_269533_", "f_268738_");

        assertClassesContain(jars, List.of(
                "com.github.L_Ender.cataclysm.entity.InternalAnimationMonster.AcropolisMonsters.Clawdian_Entity",
                "com.github.L_Ender.cataclysm.entity.AnimationMonster.BossMonsters.Ignis_Entity",
                "com.github.L_Ender.cataclysm.entity.AnimationMonster.BossMonsters.Ignited_Revenant_Entity",
                "com.github.L_Ender.cataclysm.entity.InternalAnimationMonster.Kobolediator_Entity",
                "com.github.L_Ender.cataclysm.entity.InternalAnimationMonster.Draugar.Royal_Draugr_Entity",
                "com.github.L_Ender.cataclysm.entity.InternalAnimationMonster.Wadjet_Entity"),
                "m_6469_", "canBlockDamageSource");

        assertClassesContain(jars, List.of(
                "com.eeeab.eeeabsmobs.sever.entity.corpse.EntityCorpseWarlock",
                "com.eeeab.eeeabsmobs.sever.entity.guling.EntityGulingSentinel",
                "com.eeeab.eeeabsmobs.sever.entity.guling.EntityNamelessGuardian",
                "com.eeeab.eeeabsmobs.sever.entity.immortal.EntityImmortal",
                "com.eeeab.eeeabsmobs.sever.entity.immortal.EntityImmortalExecutioner"),
                "m_6469_", "m_269533_");

        assertClassesContain(jars, List.of(
                "net.miauczel.legendary_monsters.entity.AnimatedMonster.Chorusling.TheWarpedOne.TheWarpedOneOld",
                "net.miauczel.legendary_monsters.entity.AnimatedMonster.IAnimatedBoss.CloudGolem.Cloud_GolemEntity",
                "net.miauczel.legendary_monsters.entity.AnimatedMonster.IAnimatedBoss.PossessedPaladin.NewPossessedPaladin",
                "net.miauczel.legendary_monsters.entity.AnimatedMonster.IAnimatedBoss.PossessedPaladin.PossessedPaladinEntity",
                "net.miauczel.legendary_monsters.entity.AnimatedMonster.IAnimatedBoss.TheObliterator.TheObliteratorEntity",
                "net.miauczel.legendary_monsters.entity.AnimatedMonster.Mobs.AncientStronghold.Ancient_GuardianEntity",
                "net.miauczel.legendary_monsters.entity.AnimatedMonster.Mobs.Chorusling.EndersentEntity",
                "net.miauczel.legendary_monsters.entity.AnimatedMonster.Mobs.CollapsedKingdom.OldKnights.HauntedKnightEntityOld",
                "net.miauczel.legendary_monsters.entity.AnimatedMonster.Mobs.CollapsedKingdom.PosessedPaladinEntity",
                "net.miauczel.legendary_monsters.entity.AnimatedMonster.Mobs.Frostbitten_GolemEntity",
                "net.miauczel.legendary_monsters.entity.AnimatedMonster.Mobs.Lava_eaterEntity",
                "net.miauczel.legendary_monsters.entity.AnimatedMonster.Mobs.Overgrown_colossusEntity",
                "net.miauczel.legendary_monsters.entity.AnimatedMonster.Mobs.SkeletosaurusEntity",
                "net.miauczel.legendary_monsters.entity.AnimatedMonster.Mobs.SpaceStation.Flameborn.AnnihilationPursuer.AnnihilationPursuerEntity",
                "net.miauczel.legendary_monsters.entity.AnimatedMonster.Mobs.Withered_AbominationEntity",
                "net.miauczel.legendary_monsters.entity.AnimatedMonster.OriginClasses.IAnimatedBoss",
                "net.miauczel.legendary_monsters.entity.AnimatedMonster.OriginClasses.IAnimatedMiniBoss"),
                "m_6469_", "m_269533_", "f_268738_");

        assertClassContains(jars, "com.Polarice3.Goety.common.entities.boss.Apostle",
                "m_6469_", "moddedInvul", "obsidianInvul");
        assertClassContains(jars, "com.Polarice3.Goety.common.entities.boss.EnderKeeper",
                "m_6469_", "moddedInvul");
        assertClassContains(jars, "com.Polarice3.Goety.common.entities.boss.Vizier",
                "m_6469_", "moddedInvul");
    }

    @Test
    void everyRegisteredThirdPartyKnifeMixinUsesExplicitProductionSelectors()
            throws IOException {
        JsonObject config = JsonParser.parseString(Files.readString(ROOT.resolve(Path.of(
                "src", "main", "resources", "until_eternity.mixins.json"))))
                .getAsJsonObject();
        Set<String> registered = config.getAsJsonArray("mixins").asList().stream()
                .map(value -> value.getAsString())
                .filter(value -> value.startsWith("compat."))
                .filter(value -> value.contains("TrueChefsKnife"))
                .collect(java.util.stream.Collectors.toSet());
        assertEquals(THIRD_PARTY_KNIFE_MIXINS, registered);

        for (String mixin : THIRD_PARTY_KNIFE_MIXINS) {
            Path sourcePath = MIXIN_ROOT.resolve(mixin.replace('.', File.separatorChar) + ".java");
            String source = Files.readString(sourcePath);
            assertTrue(source.contains('"' + HURT + '"'), mixin);
            assertTrue(source.contains('"' + PRODUCTION_HURT + '"'), mixin);
            assertTrue(source.contains("remap = false"), mixin);
            assertTrue(source.contains("require = 1"), mixin);
            assertFalse(source.contains("require = 0"), mixin);
            assertTrue(source.contains("TrueChefsKnifeAttackContext.matches"), mixin);
            assertFalse(source.contains(".hurt("), "must not replay damage: " + mixin);
        }
    }

    @Test
    void targetSpecificMixinsDoNotReintroduceRemovedBroadBypasses() throws IOException {
        String eeeab = source("compat.eeeabsmobs.EeeabTrueChefsKnifeDamageGateMixin");
        assertFalse(eeeab.contains("EntityImmortalShaman"));
        String cataclysm = source("compat.cataclysm.CataclysmTrueChefsKnifeDamageGateMixin");
        assertFalse(cataclysm.contains("isInvulnerableTo"));

        for (String mixin : List.of(
                "compat.goety.ApostleTrueChefsKnifeMixin",
                "compat.goety.EnderKeeperTrueChefsKnifeMixin",
                "compat.goety.VizierTrueChefsKnifeMixin")) {
            String source = source(mixin);
            assertFalse(source.contains("isSettingUpSecond"), mixin);
            assertFalse(source.contains("isIntro"), mixin);
            assertFalse(source.contains("getInvulnerableTicks"), mixin);
        }

        String config = Files.readString(ROOT.resolve(Path.of(
                "src", "main", "resources", "until_eternity.mixins.json")));
        assertFalse(config.contains("CataclysmTrueChefsKnifeBlockMixin"));
        assertFalse(config.contains("GoetyBossTrueChefsKnifeMixin"));
        assertTrue(config.contains("IceBouquetTrapBlockEntityMixin"));
        assertTrue(config.contains("IceBouquetDamageSourceMixin"));
    }

    @Test
    void auditCoordinatesAndOptionalMowzieLoadingRemainLocked() throws IOException {
        String build = Files.readString(ROOT.resolve("build.gradle"));
        for (String coordinate : List.of(
                "curse.maven:mowzies-mobs-250498:7815705",
                "curse.maven:lendercataclysm-551586:7908487",
                "curse.maven:eeeabs-mobs-921600:6095880",
                "curse.maven:legendary-monsters-944035:8444425",
                "curse.maven:goety-586095:7966952")) {
            assertTrue(build.contains("productionMixinAudit \"" + coordinate + "\""), coordinate);
        }

        String plugin = Files.readString(ROOT.resolve(Path.of(
                "src", "main", "java", "com", "carrot123", "until_eternity",
                "compat", "mixin", "UntilEternityMixinPlugin.java")));
        assertTrue(plugin.contains("mixin.compat.mowziesmobs."));
        assertTrue(plugin.contains("getModFileById(\"mowziesmobs\") != null"));

        String modsToml = Files.readString(ROOT.resolve(Path.of(
                "src", "main", "resources", "META-INF", "mods.toml")))
                .replace("\r", "");
        assertTrue(modsToml.contains("modId=\"mowziesmobs\"\n    mandatory=false"));
        for (String mandatory : List.of("eeeabsmobs", "cataclysm", "legendary_monsters", "goety")) {
            assertTrue(modsToml.contains("modId=\"" + mandatory + "\"\n    mandatory=true"), mandatory);
        }
    }

    private static String source(String mixin) throws IOException {
        return Files.readString(MIXIN_ROOT.resolve(
                mixin.replace('.', File.separatorChar) + ".java"));
    }

    private static List<Path> productionJars() {
        String classpath = System.getProperty("untilEternity.productionMixinAuditClasspath");
        assertNotNull(classpath, "Gradle must provide the raw production audit classpath");
        List<Path> jars = Arrays.stream(classpath.split(java.util.regex.Pattern.quote(
                        File.pathSeparator)))
                .map(Path::of)
                .toList();
        assertEquals(5, jars.size(), "locked production audit dependency count changed");
        jars.forEach(jar -> assertTrue(Files.isRegularFile(jar), jar.toString()));
        return jars;
    }

    private static void assertClassesContain(
            List<Path> jars, List<String> classes, String... symbols) throws IOException {
        for (String className : classes) {
            assertClassContains(jars, className, symbols);
        }
    }

    private static void assertClassContains(
            List<Path> jars, String className, String... symbols) throws IOException {
        String entryName = className.replace('.', '/') + ".class";
        byte[] classBytes = null;
        Path owner = null;
        for (Path jar : jars) {
            try (ZipFile zip = new ZipFile(jar.toFile())) {
                ZipEntry entry = zip.getEntry(entryName);
                if (entry != null) {
                    classBytes = zip.getInputStream(entry).readAllBytes();
                    owner = jar;
                    break;
                }
            }
        }
        assertNotNull(classBytes, "production class not found: " + className);
        String constants = new String(classBytes, StandardCharsets.ISO_8859_1);
        for (String symbol : symbols) {
            assertTrue(constants.contains(symbol),
                    owner.getFileName() + " / " + className + " lacks " + symbol);
        }
    }
}
