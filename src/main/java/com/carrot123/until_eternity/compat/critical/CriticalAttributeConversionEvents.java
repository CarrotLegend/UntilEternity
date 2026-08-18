package com.carrot123.until_eternity.compat.critical;

import com.carrot123.until_eternity.until_eternity;
import com.github.L_Ender.cataclysm.init.ModAttribute;
import com.github.L_Ender.cataclysm.init.ModItems;
import com.mojang.logging.LogUtils;
import com.obscuria.obscureapi.registry.ObscureAPIAttributes;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraftforge.event.ItemAttributeModifierEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.confluence.terra_curio.misc.ModAttributes;
import org.slf4j.Logger;
import top.theillusivec4.curios.api.event.CurioAttributeModifierEvent;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

@Mod.EventBusSubscriber(
        modid = until_eternity.MODID,
        bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class CriticalAttributeConversionEvents {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final AtomicBoolean WARNED_UNSUPPORTED_OPERATION =
            new AtomicBoolean();

    private CriticalAttributeConversionEvents() {
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onItemAttributeModifiers(ItemAttributeModifierEvent event) {
        convertCriticalHitModifiers(new ModifierAccess() {
            @Override
            public Iterable<AttributeModifier> modifiers(Attribute attribute) {
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
        convertCriticalHitModifiers(new ModifierAccess() {
            @Override
            public Iterable<AttributeModifier> modifiers(Attribute attribute) {
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

        if (!event.getItemStack().is(ModItems.RING_OF_GRUDGED.get())) {
            return;
        }
        Attribute cataclysmCriticalDamage =
                ModAttribute.ADDITIONAL_CRITICAL_DAMAGE.get();
        Attribute obscureCriticalDamage = ObscureAPIAttributes.CRITICAL_DAMAGE.get();
        for (AttributeModifier modifier : new ArrayList<>(
                event.getModifiers().get(cataclysmCriticalDamage))) {
            event.removeModifier(cataclysmCriticalDamage, modifier);
            event.addModifier(obscureCriticalDamage, modifier);
        }
    }

    private static void convertCriticalHitModifiers(ModifierAccess access) {
        Attribute obscureCriticalHit = ObscureAPIAttributes.CRITICAL_HIT.get();
        Attribute terraCriticalChance = ModAttributes.getCriticalChance();
        for (AttributeModifier modifier : access.modifiers(obscureCriticalHit)) {
            AttributeModifier converted =
                    CriticalChanceConversion.toTerraAddition(modifier);
            if (converted == null) {
                warnUnsupportedOperation(modifier);
                continue;
            }
            access.remove(obscureCriticalHit, modifier);
            access.add(terraCriticalChance, converted);
        }
    }

    private static void warnUnsupportedOperation(AttributeModifier modifier) {
        if (WARNED_UNSUPPORTED_OPERATION.compareAndSet(false, true)) {
            LOGGER.warn(
                    "Cannot safely convert obscure_api:critical_hit modifier '{}' "
                            + "with operation {}; leaving it unchanged",
                    modifier.getName(), modifier.getOperation());
        }
    }

    private interface ModifierAccess {
        Iterable<AttributeModifier> modifiers(Attribute attribute);

        void remove(Attribute attribute, AttributeModifier modifier);

        void add(Attribute attribute, AttributeModifier modifier);
    }
}
