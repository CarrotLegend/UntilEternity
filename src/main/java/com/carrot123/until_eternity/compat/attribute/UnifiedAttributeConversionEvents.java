package com.carrot123.until_eternity.compat.attribute;

import com.carrot123.until_eternity.until_eternity;
import com.carrot123.until_eternity.compat.GoetyRevelationAttributesCompat;
import com.mojang.logging.LogUtils;
import com.obscuria.obscureapi.registry.ObscureAPIAttributes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraftforge.event.ItemAttributeModifierEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import net.puffish.attributesmod.api.PuffishAttributes;
import org.confluence.terra_curio.misc.ModAttributes;
import org.slf4j.Logger;
import top.theillusivec4.curios.api.event.CurioAttributeModifierEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

@Mod.EventBusSubscriber(
        modid = until_eternity.MODID,
        bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class UnifiedAttributeConversionEvents {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final AtomicBoolean WARNED_TERRA_MIXED_OPERATIONS =
            new AtomicBoolean();

    private UnifiedAttributeConversionEvents() {
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onItemAttributeModifiers(ItemAttributeModifierEvent event) {
        convert(new ModifierAccess() {
            @Override
            public List<AttributeModifier> modifiers(Attribute attribute) {
                return new ArrayList<>(event.getModifiers().get(attribute));
            }

            @Override
            public void remove(Attribute attribute, AttributeModifier modifier) {
                event.removeModifier(attribute, modifier);
            }

            @Override
            public void add(Attribute attribute, AttributeModifier modifier) {
                event.addModifier(attribute, modifier);
            }
        });
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onCurioAttributeModifiers(CurioAttributeModifierEvent event) {
        convert(new ModifierAccess() {
            @Override
            public List<AttributeModifier> modifiers(Attribute attribute) {
                return new ArrayList<>(event.getModifiers().get(attribute));
            }

            @Override
            public void remove(Attribute attribute, AttributeModifier modifier) {
                event.removeModifier(attribute, modifier);
            }

            @Override
            public void add(Attribute attribute, AttributeModifier modifier) {
                event.addModifier(attribute, modifier);
            }
        });
    }

    private static void convert(ModifierAccess access) {
        convertObscureDodge(access);
        convertObscureHealingPower(access);
        convertPercentageShred(
                access,
                ObscureAPIAttributes.PENETRATION.get(),
                new ResourceLocation("obscure_api", "penetration"),
                PuffishAttributes.ARMOR_SHRED,
                PuffishAttributes.ARMOR_SHRED_ID);
        convertOptionalPercentageShred(
                access,
                GoetyRevelationAttributesCompat.ARMOR_PENETRATION,
                PuffishAttributes.ARMOR_SHRED,
                PuffishAttributes.ARMOR_SHRED_ID);
        convertOptionalPercentageShred(
                access,
                GoetyRevelationAttributesCompat.ENCHANTMENT_PIERCING,
                PuffishAttributes.PROTECTION_SHRED,
                PuffishAttributes.PROTECTION_SHRED_ID);
        convertTerraDynamic(access, ModAttributes.RANGED_DAMAGE.get(),
                PuffishAttributes.RANGED_DAMAGE);
        convertTerraDynamic(access, ModAttributes.MAGIC_DAMAGE.get(),
                PuffishAttributes.MAGIC_DAMAGE);
        convertTerraDynamic(access, ModAttributes.MINING_SPEED.get(),
                PuffishAttributes.BREAKING_SPEED);
    }

    private static void convertObscureDodge(ModifierAccess access) {
        Attribute source = ObscureAPIAttributes.DODGE.get();
        Attribute target = ModAttributes.DODGE_CHANCE.get();
        for (AttributeModifier modifier : access.modifiers(source)) {
            access.remove(source, modifier);
            access.add(target,
                    AttributeModifierConversions.obscureDodgeToTerra(modifier));
        }
    }

    private static void convertObscureHealingPower(ModifierAccess access) {
        Attribute source = ObscureAPIAttributes.HEALING_POWER.get();
        Attribute target = PuffishAttributes.HEALING;
        for (AttributeModifier modifier : access.modifiers(source)) {
            access.remove(source, modifier);
            access.add(target,
                    AttributeModifierConversions.healingPowerToDynamic(
                            modifier));
        }
    }

    private static void convertOptionalPercentageShred(
            ModifierAccess access,
            ResourceLocation sourceId,
            Attribute target,
            ResourceLocation targetId) {
        Attribute source = ForgeRegistries.ATTRIBUTES.getValue(sourceId);
        if (source != null) {
            convertPercentageShred(
                    access, source, sourceId, target, targetId);
        }
    }

    private static void convertPercentageShred(
            ModifierAccess access,
            Attribute source,
            ResourceLocation sourceId,
            Attribute target,
            ResourceLocation targetId) {
        for (AttributeModifier modifier : access.modifiers(source)) {
            access.remove(source, modifier);
            access.add(target, AttributeModifierConversions.percentageShred(
                    modifier, sourceId, targetId));
        }
    }

    private static void convertTerraDynamic(
            ModifierAccess access,
            Attribute source,
            Attribute target) {
        List<AttributeModifier> modifiers = access.modifiers(source);
        if (AttributeModifierConversions.hasAdditionAndMultiplyBase(modifiers)
                && WARNED_TERRA_MIXED_OPERATIONS.compareAndSet(false, true)) {
            LOGGER.warn("A Terra Curio attribute source contains both ADDITION "
                    + "and MULTIPLY_BASE modifiers; converting both to Puffish "
                    + "MULTIPLY_BASE cannot preserve their nonlinear interaction "
                    + "exactly");
        }
        for (AttributeModifier modifier : modifiers) {
            access.remove(source, modifier);
            access.add(target,
                    AttributeModifierConversions.terraMultiplierToDynamic(
                            modifier));
        }
    }

    private interface ModifierAccess {
        List<AttributeModifier> modifiers(Attribute attribute);

        void remove(Attribute attribute, AttributeModifier modifier);

        void add(Attribute attribute, AttributeModifier modifier);
    }
}
