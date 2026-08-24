package com.carrot123.until_eternity.effect;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CookingFrenzyResourceTest {
    private static final Path ROOT = Path.of(".");
    private static final Path MAIN_JAVA = ROOT.resolve(Path.of(
            "src", "main", "java", "com", "carrot123", "until_eternity"));
    private static final Path RESOURCES = ROOT.resolve(Path.of("src", "main", "resources"));

    @Test
    void effectUsesTheExactLinearAttackSpeedModifier() throws IOException {
        String effect = source("effect/CookingFrenzyEffect.java");
        String registry = source("registry/ModMobEffects.java");

        assertTrue(effect.contains("MobEffectCategory.BENEFICIAL"));
        assertTrue(effect.contains("COLOR = 0xE65A24"));
        assertTrue(effect.contains("Attributes.ATTACK_SPEED"));
        assertTrue(effect.contains("ATTACK_SPEED_AMOUNT_PER_LEVEL = 0.02D"));
        assertTrue(effect.contains("AttributeModifier.Operation.MULTIPLY_BASE"));
        assertEquals(UUID.fromString("26c9568e-4df4-3fa9-bb50-63d92d8e9029"),
                UUID.fromString(CookingFrenzyEffect.ATTACK_SPEED_UUID));
        assertEquals(0.02D, CookingFrenzyEffect.ATTACK_SPEED_AMOUNT_PER_LEVEL * 1, 0.0D);
        assertEquals(0.20D, CookingFrenzyEffect.ATTACK_SPEED_AMOUNT_PER_LEVEL * 10, 0.0D);
        assertEquals(0.08D, 4.0D * CookingFrenzyEffect.ATTACK_SPEED_AMOUNT_PER_LEVEL, 0.0D);
        assertEquals(0.8D, 4.0D * CookingFrenzyEffect.ATTACK_SPEED_AMOUNT_PER_LEVEL * 10, 0.0D);
        assertTrue(registry.contains("MOB_EFFECTS.register(\"cooking_frenzy\""));
    }

    @Test
    void knifePreservesItsTierAndAddsOneServerSideEffectPerSuccessfulHit() throws IOException {
        String item = source("item/TrueChefsKnifeItem.java");
        String items = source("item/ModItems.java");

        assertTrue(item.contains("extends SwordItem"));
        assertTrue(item.contains("super(ModTiers.TRUE_CHEFS_KNIFE, 0, -1.8F, properties)"));
        assertEquals(1, occurrences(item, "super.hurtEnemy("));
        assertTrue(item.contains("!attacker.level().isClientSide"));
        assertTrue(item.contains("player.getMainHandItem() == stack"));
        assertTrue(item.contains("CookingFrenzyProgression.DURATION_TICKS"));
        String progression = source("combat/CookingFrenzyProgression.java");
        assertTrue(progression.contains("DURATION_TICKS = 200"));
        assertTrue(progression.contains("MAX_AMPLIFIER = 9"));
        assertTrue(items.contains("new TrueChefsKnifeItem("));
        assertTrue(items.contains("rarity(Rarity.EPIC).fireResistant()"));
    }

    @Test
    void mixinsWrapOnlyTheVanillaPrimaryHitAndPreservePositiveHooks() throws IOException {
        String playerMixin = source("mixin/TrueChefsKnifePlayerAttackMixin.java");
        String livingMixin = source("mixin/TrueChefsKnifeLivingEntityDamageMixin.java");
        String context = source("combat/TrueChefsKnifeAttackContext.java");
        JsonObject mixins = JsonParser.parseString(Files.readString(
                RESOURCES.resolve("until_eternity.mixins.json"))).getAsJsonObject();

        assertTrue(playerMixin.contains("ForgeHooks;onPlayerAttackTarget"));
        assertTrue(playerMixin.contains("Entity;isAttackable()Z"));
        assertTrue(playerMixin.contains("Entity;skipAttackInteraction"));
        assertTrue(playerMixin.contains("Entity;hurt(Lnet/minecraft/world/damagesource/DamageSource;F)Z"));
        assertFalse(playerMixin.contains("LivingEntity;hurt(Lnet/minecraft/world/damagesource/DamageSource;F)Z"));
        assertEquals(4, occurrences(playerMixin, "require = 1"));
        assertEquals(1, occurrences(playerMixin, "withAttack("));
        assertTrue(playerMixin.contains("invulnerableTime = 0"));
        assertTrue(playerMixin.contains("if (!completed || !hurt)"));

        assertTrue(livingMixin.contains("ForgeHooks;onLivingAttack"));
        assertTrue(livingMixin.contains("isDamageSourceBlocked"));
        assertEquals(2, occurrences(livingMixin, "isInvulnerableTo"));
        assertTrue(livingMixin.contains("ForgeHooks;onLivingHurt"));
        assertTrue(livingMixin.contains("ForgeHooks;onLivingDamage"));
        assertEquals(6, occurrences(livingMixin, "require = 1"));
        assertTrue(context.contains("ScopedValueStack<Attack>"));
        assertTrue(context.contains("source.getEntity() == attack.player()"));
        String damageMath = source("combat/ForcedHitDamageMath.java");
        assertTrue(damageMath.contains("Float.isFinite(hookResult) && hookResult > 0.0F"));

        assertTrue(mixins.getAsJsonArray("mixins").asList().stream()
                .anyMatch(value -> value.getAsString().equals("TrueChefsKnifePlayerAttackMixin")));
        assertTrue(mixins.getAsJsonArray("mixins").asList().stream()
                .anyMatch(value -> value.getAsString().equals("TrueChefsKnifeLivingEntityDamageMixin")));
    }

    @Test
    void translationsAndTransparentEighteenPixelIconExist() throws IOException {
        for (String language : new String[]{"en_us.json", "zh_cn.json"}) {
            JsonObject translations = JsonParser.parseString(Files.readString(
                    RESOURCES.resolve(Path.of("assets", "until_eternity", "lang", language))))
                    .getAsJsonObject();
            assertTrue(translations.has("effect.until_eternity.cooking_frenzy"));
        }

        Path icon = RESOURCES.resolve(Path.of(
                "assets", "until_eternity", "textures", "mob_effect", "cooking_frenzy.png"));
        BufferedImage image = ImageIO.read(icon.toFile());
        assertNotNull(image);
        assertEquals(18, image.getWidth());
        assertEquals(18, image.getHeight());
        assertTrue(image.getColorModel().hasAlpha());
        boolean hasTransparentPixel = false;
        boolean hasVisiblePixel = false;
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                int alpha = image.getRGB(x, y) >>> 24;
                hasTransparentPixel |= alpha == 0;
                hasVisiblePixel |= alpha != 0;
            }
        }
        assertTrue(hasTransparentPixel);
        assertTrue(hasVisiblePixel);
    }

    private static String source(String relative) throws IOException {
        return Files.readString(MAIN_JAVA.resolve(relative));
    }

    private static int occurrences(String source, String target) {
        return (source.length() - source.replace(target, "").length()) / target.length();
    }
}
