package com.carrot123.until_eternity.mixin.compat.irons_spellbooks;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class IronsSpellbooksMixinResourceTest {
    private static final Path ROOT = Path.of(".");
    private static final Path MIXIN_PACKAGE = ROOT.resolve(Path.of(
            "src", "main", "java", "com", "carrot123", "until_eternity",
            "mixin", "compat", "irons_spellbooks"));
    private static final Path IRON_COMPAT_PACKAGE = ROOT.resolve(Path.of(
            "src", "main", "java", "com", "carrot123", "until_eternity",
            "compat", "ironsspellbooks"));

    @Test
    void mixinConfigurationPreservesExistingMixinsAndAddsManaCompatMixins()
            throws IOException {
        JsonObject config = JsonParser.parseString(Files.readString(ROOT.resolve(
                Path.of("src", "main", "resources",
                        "until_eternity.mixins.json")))).getAsJsonObject();
        Set<String> mixins = new HashSet<>();
        config.getAsJsonArray("mixins")
                .forEach(entry -> mixins.add(entry.getAsString()));

        assertEquals(Set.of(
                "ItemStackHoverNameMixin",
                "FoodDataMixin",
                "ManaEruptionStackingMixin",
                "SoulGreatSwordItemMixin",
                "TrueChefsKnifePlayerAttackMixin",
                "TrueChefsKnifeLivingEntityDamageMixin",
                "compat.obscure_api.ObscureApiCriticalHitMixin",
                "compat.obscure_api.ObscureApiDodgeMixin",
                "compat.obscure_api.ObscureApiHealingPowerMixin",
                "compat.obscure_api.ObscureApiPenetrationMixin",
                "compat.terra_curio.TerraCurioArrowCriticalMixin",
                "compat.terra_curio.TerraCurioBreakSpeedMixin",
                "compat.terra_curio.TerraCurioDodgeChanceMixin",
                "compat.terra_curio.TerraCurioLivingHurtAttributesMixin",
                "compat.terra_curio.TerraCurioLivingDamageMixin",
                "compat.eeeabsmobs.BlockErosionPortalMixin",
                "compat.goety.DarkWandFocusCastMixin",
                "compat.goety.ServerLevelFocusEntityTickMixin",
                "compat.goetyrevelation.QuietusStarPropertiesMixin",
                "compat.irons_spellbooks.AbstractSpellCastingContextMixin",
                "compat.irons_spellbooks.AbstractSpellMixin",
                "compat.irons_spellbooks.CurioBaseItemMixin",
                "compat.irons_spellbooks.MagicManagerMixin",
                "compat.irons_spellbooks.ServerPlayerEventsMixin",
                "compat.irons_spellbooks.TeleportationAmuletItemMixin",
                "compat.thirst.PlayerThirstMixin"
        ), mixins);
        Set<String> clientMixins = new HashSet<>();
        config.getAsJsonArray("client")
                .forEach(entry -> clientMixins.add(entry.getAsString()));
        assertEquals(Set.of(
                "client.FontRainbowMixin",
                "client.HorrorHuntSelectedItemNameMixin",
                "compat.irons_spellbooks.SpellWheelOverlayMixin",
                "compat.irons_spellbooks.TooltipsUtilsMixin"
        ), clientMixins);
        assertEquals("until_eternity.refmap.json",
                config.get("refmap").getAsString());
    }

    @Test
    void castingContextWrapsTheExact3156EntryPointAndForwardsEveryArgument()
            throws IOException {
        Path wrapper = MIXIN_PACKAGE.resolve(
                "AbstractSpellCastingContextMixin.java");
        String source = Files.readString(wrapper);

        assertTrue(Files.isRegularFile(wrapper));
        assertTrue(source.contains("@WrapMethod("));
        assertTrue(source.contains(
                "attemptInitiateCast\""
                        + "\n                    + "
                        + "\"(Lnet/minecraft/world/item/ItemStack;\""));
        assertTrue(source.contains(
                "Lio/redspace/ironsspellbooks/api/spells/CastSource;\""));
        assertTrue(source.contains("\"ZLjava/lang/String;)Z\""));
        assertTrue(source.contains("remap = false"));
        assertTrue(source.contains("require = 1"));
        assertTrue(source.contains("Operation<Boolean> original"));
        assertTrue(source.contains("IronCastingContext.withCastingContext("));
        assertTrue(source.contains(
                "original.call("
                        + "\n                        castingStack,"
                        + "\n                        spellLevel,"
                        + "\n                        level,"
                        + "\n                        player,"
                        + "\n                        castSource,"
                        + "\n                        triggerCooldown,"
                        + "\n                        castingEquipmentSlot)"));
    }

    @Test
    void oldCastingItemUseMixinIsCompletelyRemoved() throws IOException {
        Path oldMixin = MIXIN_PACKAGE.resolve("CastingItemMixin.java");
        String config = Files.readString(ROOT.resolve(Path.of(
                "src", "main", "resources", "until_eternity.mixins.json")));

        assertFalse(Files.exists(oldMixin));
        assertFalse(config.contains("CastingItemMixin"));
        try (var sources = Files.walk(ROOT.resolve(
                Path.of("src", "main", "java")))) {
            assertTrue(sources.filter(Files::isRegularFile)
                    .filter(path -> path.toString().endsWith(".java"))
                    .noneMatch(path -> {
                        try {
                            String source = Files.readString(path);
                            return source.contains("CastingItem;use")
                                    || source.contains(
                                    "io.redspace.ironsspellbooks.item.CastingItem");
                        } catch (IOException exception) {
                            throw new RuntimeException(exception);
                        }
                    }));
        }
    }

    @Test
    void sharedCastingContextLivesOutsideMixinPackageAndUsesScopedStack()
            throws IOException {
        Path context = IRON_COMPAT_PACKAGE.resolve("IronCastingContext.java");
        String source = Files.readString(context);
        String manaCost = Files.readString(
                IRON_COMPAT_PACKAGE.resolve("PlenitudeManaCost.java"));
        String spellPower = Files.readString(
                IRON_COMPAT_PACKAGE.resolve("IronSpellPowerCompat.java"));
        String spellMixin = Files.readString(
                MIXIN_PACKAGE.resolve("AbstractSpellMixin.java"));

        assertTrue(Files.isRegularFile(context));
        assertTrue(source.contains(
                "package com.carrot123.until_eternity.compat."
                        + "ironsspellbooks;"));
        assertTrue(source.contains(
                "ScopedValueStack<CastingContext> CONTEXTS"));
        assertTrue(source.contains(
                "record CastingContext("
                        + "\n            ItemStack stack,"
                        + "\n            CastSource source,"
                        + "\n            String equipmentSlot"));
        assertTrue(source.contains("magicDataStack != null"));
        assertTrue(source.contains("!magicDataStack.isEmpty()"));
        assertTrue(source.contains("return currentStack();"));
        assertFalse(manaCost.contains("ScopedValueStack"));
        assertFalse(manaCost.contains("withCastingStack"));
        assertTrue(spellPower.contains(
                "IronCastingContext.preferMagicDataStack("));
        assertTrue(spellMixin.contains("IronCastingContext.currentStack()"));
        assertTrue(spellMixin.contains("IronCastingContext.currentSource()"));
        assertTrue(spellMixin.contains(
                "IronCastingContext.preferMagicDataStack("));
        assertFalse(spellMixin.contains(
                "method = \"attemptInitiateCast\""));
    }

    @Test
    void spellPowerMixinTargetsThe3156GenericAttributeRead()
            throws IOException {
        String source = Files.readString(
                MIXIN_PACKAGE.resolve("AbstractSpellMixin.java"));

        assertTrue(source.contains(
                "getSpellPower(ILnet/minecraft/world/entity/Entity;)F"));
        assertTrue(source.contains(
                "LivingEntity;\""
                        + "\n                            + \"getAttributeValue\""));
        assertTrue(source.contains("remap = true"));
        assertTrue(source.contains("require = 1"));
        assertTrue(source.contains(
                "com.carrot123.until_eternity.compat.ironsspellbooks."
                        + "IronSpellPowerCompat"));
    }

    @Test
    void attributeMixinUsesExact3156DescriptorAndDoesNotCompareSlotNames()
            throws IOException {
        String source = Files.readString(
                MIXIN_PACKAGE.resolve("CurioBaseItemMixin.java"));

        assertTrue(source.contains(
                "getAttributeModifiers(Ltop/theillusivec4/curios/api/SlotContext;"
                        + "Ljava/util/UUID;Lnet/minecraft/world/item/ItemStack;)"
                        + "Lcom/google/common/collect/Multimap;"));
        assertTrue(source.contains("slotContext.cosmetic()"));
        assertTrue(source.contains("this.attributes == null"));
        assertTrue(source.contains("this.attributes.apply(slotContext.index())"));
        assertTrue(source.contains("require = 1"));
        assertFalse(source.contains("slotContext.identifier()"));
        assertFalse(source.contains("attributeSlot"));
    }

    @Test
    void runtimeHelperLivesOutsideProtectedMixinPackage() throws IOException {
        Path oldHelper = MIXIN_PACKAGE.resolve("IronCurioAttributeCompat.java");
        Path externalHelper =
                IRON_COMPAT_PACKAGE.resolve("IronCurioAttributeCompat.java");
        String mixinSource = Files.readString(
                MIXIN_PACKAGE.resolve("CurioBaseItemMixin.java"));
        String helperSource = Files.readString(externalHelper);

        assertFalse(Files.exists(oldHelper));
        assertTrue(Files.isRegularFile(externalHelper));
        assertTrue(mixinSource.contains(
                "import com.carrot123.until_eternity.compat.ironsspellbooks."
                        + "IronCurioAttributeCompat;"));
        assertTrue(helperSource.contains(
                "package com.carrot123.until_eternity.compat."
                        + "ironsspellbooks;"));
        assertTrue(helperSource.contains(
                "public final class IronCurioAttributeCompat"));
        assertTrue(helperSource.contains(
                "public static Multimap<Attribute, AttributeModifier> rebuild("));
    }

    @Test
    void teleportationMixinUsesCurrentContextIdentifierOnlyForUnequip()
            throws IOException {
        String source = Files.readString(
                MIXIN_PACKAGE.resolve("TeleportationAmuletItemMixin.java"));

        assertTrue(source.contains(
                "handleCurse(Ltop/theillusivec4/curios/api/SlotContext;"));
        assertTrue(source.contains(
                "handler.getCurios().get(slotContext.identifier())"));
        assertTrue(source.contains(
                "handler.setEquippedCurio("));
        assertTrue(source.contains(
                "slotContext.identifier(),"));
        assertFalse(source.contains("NECKLACE_SLOT"));
    }

    @Test
    void metadataPinsIronSpellbooks3156AsMandatory() throws IOException {
        String modsToml = Files.readString(ROOT.resolve(Path.of(
                "src", "main", "resources", "META-INF", "mods.toml")));

        assertTrue(modsToml.contains("modId=\"irons_spellbooks\""));
        assertTrue(modsToml.contains("versionRange=\"[1.20.1-3.15.6]\""));
        assertTrue(modsToml.contains("mandatory=true"));
    }
}
