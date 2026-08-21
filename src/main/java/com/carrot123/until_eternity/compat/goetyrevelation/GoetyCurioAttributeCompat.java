package com.carrot123.until_eternity.compat.goetyrevelation;

import com.carrot123.until_eternity.until_eternity;
import com.carrot123.until_eternity.item.curio.CurioModifierId;
import com.carrot123.until_eternity.registry.ModAttributes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import net.puffish.attributesmod.api.PuffishAttributes;
import top.theillusivec4.curios.api.event.CurioAttributeModifierEvent;

import java.util.UUID;

@Mod.EventBusSubscriber(modid = until_eternity.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class GoetyCurioAttributeCompat {
    private static final String BELT_SLOT = "belt";
    private static final String BODY_SLOT = "body";
    private static final String CHARM_SLOT = "charm";
    private static final String BRACELET_SLOT = "bracelet";

    private static final ResourceLocation FOCUS_BAG_ID =
            new ResourceLocation("goety", "focus_bag");
    private static final ResourceLocation FOCUS_PACK_ID =
            new ResourceLocation("goety", "focus_pack");
    private static final ResourceLocation DARK_ROBE_ID =
            new ResourceLocation("goety", "dark_robe");
    private static final ResourceLocation GRAND_ROBE_ID =
            new ResourceLocation("goety", "grand_robe");
    private static final ResourceLocation GOLD_FEATHER_ID =
            new ResourceLocation("goety_revelation", "gold_feather");
    private static final ResourceLocation QUIETUS_STAR_ID =
            new ResourceLocation("goety_revelation", "quietus_star");
    private static final ResourceLocation SPELL_POWER_ID =
            new ResourceLocation("goety_revelation", "spell_power");

    private static final double FOCUS_BAG_BONUS = 1.0D;
    private static final double FOCUS_PACK_BONUS = 1.5D;
    static final double DARK_ROBE_FOCUS_DAMAGE = 0.15D;
    static final double GRAND_ROBE_FOCUS_DAMAGE = 0.25D;
    static final double GOLD_FEATHER_FOCUS_DAMAGE = 0.10D;
    static final double GOLD_FEATHER_RANGED_DAMAGE = 0.10D;
    static final double QUIETUS_STAR_FOCUS_DAMAGE = 0.30D;

    /*
     * The previous implementation wrote these modifiers permanently to player data.
     * Keep their UUIDs only so existing worlds can remove that stale data on login.
     */
    private static final UUID LEGACY_FOCUS_BAG_UUID =
            UUID.fromString("c3d4e5f6-a7b8-9012-cdef-123456789003");
    private static final UUID LEGACY_FOCUS_PACK_UUID =
            UUID.fromString("d4e5f6a7-b8c9-0123-defa-234567890014");

    private static volatile Attribute cachedSpellPower;

    private GoetyCurioAttributeCompat() {
    }

    @SubscribeEvent
    public static void onCurioAttributeModifiers(CurioAttributeModifierEvent event) {
        if (event.getSlotContext() == null
                || event.getSlotContext().cosmetic()
                || !areCompatModsLoaded()) {
            return;
        }

        ResourceLocation itemId = ForgeRegistries.ITEMS.getKey(event.getItemStack().getItem());
        String slot = event.getSlotContext().identifier();
        if (BRACELET_SLOT.equals(slot)
                && QUIETUS_STAR_ID.equals(itemId)
                && event.getItemStack().getCount() == 1) {
            addModifier(event, ModAttributes.FOCUS_DAMAGE.get(),
                    "quietus_star_focus_damage",
                    QUIETUS_STAR_FOCUS_DAMAGE,
                    AttributeModifier.Operation.MULTIPLY_BASE);
            return;
        }
        if (BODY_SLOT.equals(slot) && DARK_ROBE_ID.equals(itemId)) {
            addModifier(event, ModAttributes.FOCUS_DAMAGE.get(),
                    "dark_robe_focus_damage", DARK_ROBE_FOCUS_DAMAGE,
                    AttributeModifier.Operation.ADDITION);
            return;
        }
        if (BODY_SLOT.equals(slot) && GRAND_ROBE_ID.equals(itemId)) {
            addModifier(event, ModAttributes.FOCUS_DAMAGE.get(),
                    "grand_robe_focus_damage", GRAND_ROBE_FOCUS_DAMAGE,
                    AttributeModifier.Operation.ADDITION);
            return;
        }
        if (CHARM_SLOT.equals(slot) && GOLD_FEATHER_ID.equals(itemId)) {
            addModifier(event, ModAttributes.FOCUS_DAMAGE.get(),
                    "gold_feather_focus_damage", GOLD_FEATHER_FOCUS_DAMAGE,
                    AttributeModifier.Operation.MULTIPLY_BASE);
            addModifier(event, PuffishAttributes.RANGED_DAMAGE,
                    "gold_feather_ranged_damage", GOLD_FEATHER_RANGED_DAMAGE,
                    AttributeModifier.Operation.MULTIPLY_BASE);
            return;
        }
        if (!BELT_SLOT.equals(slot)) {
            return;
        }

        double bonus;
        String modifierName;
        if (FOCUS_BAG_ID.equals(itemId)) {
            bonus = FOCUS_BAG_BONUS;
            modifierName = "Until Eternity focus bag spell power";
        } else if (FOCUS_PACK_ID.equals(itemId)) {
            bonus = FOCUS_PACK_BONUS;
            modifierName = "Until Eternity focus pack spell power";
        } else {
            return;
        }

        Attribute spellPower = resolveSpellPower();
        if (spellPower == null) {
            return;
        }

        event.addModifier(spellPower, new AttributeModifier(
                event.getUuid(),
                modifierName,
                bonus,
                AttributeModifier.Operation.ADDITION
        ));
    }

    private static void addModifier(
            CurioAttributeModifierEvent event,
            Attribute attribute,
            String modifierKey,
            double amount,
            AttributeModifier.Operation operation
    ) {
        event.addModifier(attribute, new AttributeModifier(
                CurioModifierId.create(event.getUuid(), modifierKey),
                until_eternity.MODID + ":external_curio/" + modifierKey,
                amount,
                operation
        ));
    }

    @SubscribeEvent
    public static void removeLegacyPermanentModifiers(PlayerEvent.PlayerLoggedInEvent event) {
        Player player = event.getEntity();
        if (player.level().isClientSide || !areCompatModsLoaded()) {
            return;
        }

        Attribute spellPower = resolveSpellPower();
        if (spellPower == null) {
            return;
        }

        AttributeInstance instance = player.getAttribute(spellPower);
        if (instance != null) {
            instance.removeModifier(LEGACY_FOCUS_BAG_UUID);
            instance.removeModifier(LEGACY_FOCUS_PACK_UUID);
        }
    }

    private static boolean areCompatModsLoaded() {
        ModList modList = ModList.get();
        return modList.isLoaded("goety")
                && modList.isLoaded("goety_revelation")
                && modList.isLoaded("revelationfix");
    }

    private static Attribute resolveSpellPower() {
        Attribute spellPower = cachedSpellPower;
        if (spellPower == null) {
            spellPower = ForgeRegistries.ATTRIBUTES.getValue(SPELL_POWER_ID);
            if (spellPower != null) {
                cachedSpellPower = spellPower;
            }
        }
        return spellPower;
    }
}
