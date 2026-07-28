package com.carrot123.until_eternity.compat.goetyrevelation;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import com.mojang.logging.LogUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.registries.ForgeRegistries;
import org.slf4j.Logger;
import top.theillusivec4.curios.api.SlotContext;

import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

public final class DivineSoulLampAttributeCompat {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final String CHARM_SLOT = "charm";

    private static final ResourceLocation SOUL_REFLUX_ID =
            new ResourceLocation("goety_revelation", "soul_decrease_reduction");
    private static final ResourceLocation SOUL_AFFINITY_ID =
            new ResourceLocation("goety_revelation", "soul_increase_efficiency");
    private static final ResourceLocation SPELL_POWER_ID =
            new ResourceLocation("goety_revelation", "spell_power");
    private static final ResourceLocation SPELL_POWER_MULTIPLIER_ID =
            new ResourceLocation("goety_revelation", "spell_power_multiplier");

    static final String SOUL_REFLUX_SALT =
            "until_eternity:divine_soul_lamp/soul_reflux";
    static final String SOUL_AFFINITY_SALT =
            "until_eternity:divine_soul_lamp/soul_affinity";
    static final String SPELL_POWER_FLAT_SALT =
            "until_eternity:divine_soul_lamp/spell_power_flat";
    static final String SPELL_POWER_PERCENT_SALT =
            "until_eternity:divine_soul_lamp/spell_power_percent";
    static final String SPELL_POWER_MULTIPLIER_SALT =
            "until_eternity:divine_soul_lamp/spell_power_multiplier";

    private static final AtomicBoolean MISSING_MODS_WARNED = new AtomicBoolean();
    private static final AtomicBoolean MISSING_ATTRIBUTES_WARNED = new AtomicBoolean();
    private static volatile boolean resolutionAttempted;
    private static volatile ResolvedAttributes cachedAttributes;

    private DivineSoulLampAttributeCompat() {
    }

    public static Multimap<Attribute, AttributeModifier> getAttributeModifiers(
            SlotContext slotContext,
            UUID slotUuid
    ) {
        if (!CHARM_SLOT.equals(slotContext.identifier())) {
            return ImmutableMultimap.of();
        }

        if (!areCompatModsLoaded()) {
            if (MISSING_MODS_WARNED.compareAndSet(false, true)) {
                LOGGER.warn(
                        "Divine Soul Lamp attributes are disabled because Goety, "
                                + "Goety Revelation, or RevelationFix is not loaded"
                );
            }
            return ImmutableMultimap.of();
        }

        ResolvedAttributes attributes = resolveAttributes();
        if (attributes == null) {
            return ImmutableMultimap.of();
        }

        ImmutableMultimap.Builder<Attribute, AttributeModifier> modifiers =
                ImmutableMultimap.builder();
        addModifier(
                modifiers,
                attributes.soulReflux,
                slotUuid,
                SOUL_REFLUX_SALT,
                "until_eternity.divine_soul_lamp.soul_reflux",
                0.33D,
                AttributeModifier.Operation.MULTIPLY_BASE
        );
        addModifier(
                modifiers,
                attributes.soulAffinity,
                slotUuid,
                SOUL_AFFINITY_SALT,
                "until_eternity.divine_soul_lamp.soul_affinity",
                0.33D,
                AttributeModifier.Operation.MULTIPLY_BASE
        );
        addModifier(
                modifiers,
                attributes.spellPower,
                slotUuid,
                SPELL_POWER_FLAT_SALT,
                "until_eternity.divine_soul_lamp.spell_power_flat",
                6.0D,
                AttributeModifier.Operation.ADDITION
        );
        addModifier(
                modifiers,
                attributes.spellPower,
                slotUuid,
                SPELL_POWER_PERCENT_SALT,
                "until_eternity.divine_soul_lamp.spell_power_percent",
                0.25D,
                AttributeModifier.Operation.MULTIPLY_TOTAL
        );
        addModifier(
                modifiers,
                attributes.spellPowerMultiplier,
                slotUuid,
                SPELL_POWER_MULTIPLIER_SALT,
                "until_eternity.divine_soul_lamp.spell_power_multiplier",
                1.2D,
                AttributeModifier.Operation.ADDITION
        );
        return modifiers.build();
    }

    static UUID deriveModifierUuid(UUID slotUuid, String salt) {
        String source = slotUuid + "|" + salt;
        return UUID.nameUUIDFromBytes(source.getBytes(StandardCharsets.UTF_8));
    }

    private static void addModifier(
            ImmutableMultimap.Builder<Attribute, AttributeModifier> modifiers,
            Attribute attribute,
            UUID slotUuid,
            String salt,
            String name,
            double amount,
            AttributeModifier.Operation operation
    ) {
        modifiers.put(
                attribute,
                new AttributeModifier(
                        deriveModifierUuid(slotUuid, salt),
                        name,
                        amount,
                        operation
                )
        );
    }

    private static boolean areCompatModsLoaded() {
        ModList modList = ModList.get();
        return modList.isLoaded("goety")
                && modList.isLoaded("goety_revelation")
                && modList.isLoaded("revelationfix");
    }

    private static ResolvedAttributes resolveAttributes() {
        if (resolutionAttempted) {
            return cachedAttributes;
        }

        synchronized (DivineSoulLampAttributeCompat.class) {
            if (!resolutionAttempted) {
                Attribute soulReflux = ForgeRegistries.ATTRIBUTES.getValue(SOUL_REFLUX_ID);
                Attribute soulAffinity = ForgeRegistries.ATTRIBUTES.getValue(SOUL_AFFINITY_ID);
                Attribute spellPower = ForgeRegistries.ATTRIBUTES.getValue(SPELL_POWER_ID);
                Attribute spellPowerMultiplier =
                        ForgeRegistries.ATTRIBUTES.getValue(SPELL_POWER_MULTIPLIER_ID);

                if (soulReflux != null
                        && soulAffinity != null
                        && spellPower != null
                        && spellPowerMultiplier != null) {
                    cachedAttributes = new ResolvedAttributes(
                            soulReflux,
                            soulAffinity,
                            spellPower,
                            spellPowerMultiplier
                    );
                } else if (MISSING_ATTRIBUTES_WARNED.compareAndSet(false, true)) {
                    LOGGER.warn(
                            "Divine Soul Lamp attributes are disabled because one or more "
                                    + "Goety Revelation attributes are missing: {}, {}, {}, {}",
                            SOUL_REFLUX_ID,
                            SOUL_AFFINITY_ID,
                            SPELL_POWER_ID,
                            SPELL_POWER_MULTIPLIER_ID
                    );
                }
                resolutionAttempted = true;
            }
            return cachedAttributes;
        }
    }

    private static final class ResolvedAttributes {
        private final Attribute soulReflux;
        private final Attribute soulAffinity;
        private final Attribute spellPower;
        private final Attribute spellPowerMultiplier;

        private ResolvedAttributes(
                Attribute soulReflux,
                Attribute soulAffinity,
                Attribute spellPower,
                Attribute spellPowerMultiplier
        ) {
            this.soulReflux = soulReflux;
            this.soulAffinity = soulAffinity;
            this.spellPower = spellPower;
            this.spellPowerMultiplier = spellPowerMultiplier;
        }
    }
}
