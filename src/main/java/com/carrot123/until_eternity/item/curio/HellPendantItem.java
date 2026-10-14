package com.carrot123.until_eternity.item.curio;

import com.aizistral.enigmaticlegacy.handlers.SuperpositionHandler;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

import javax.annotation.Nullable;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurio.DropRule;
import top.theillusivec4.curios.api.type.capability.ICurioItem;

public final class HellPendantItem extends Item implements ICurioItem {

    public static final String SLOT = "charm";
    public static final String BLESSED_TAG =
            "UntilEternityHellPendantBlessed";

    private static final UUID MAX_HEALTH_UUID =
            UUID.nameUUIDFromBytes(
                    "until_eternity:hell_pendant/max_health"
                            .getBytes(StandardCharsets.UTF_8)
            );

    public HellPendantItem(Properties properties) {
        super(properties.stacksTo(1));
    }

    public static boolean isBlessed(ItemStack stack) {
        return stack.hasTag()
                && stack.getTag().getBoolean(BLESSED_TAG);
    }

    public static ItemStack findEquipped(LivingEntity entity) {
        if (entity == null) {
            return ItemStack.EMPTY;
        }

        return CuriosApi.getCuriosInventory(entity)
                .resolve()
                .flatMap(handler ->
                        handler.findCurios(
                                        stack ->
                                                stack.getItem()
                                                        instanceof HellPendantItem
                                )
                                .stream()
                                .filter(result ->
                                        SLOT.equals(
                                                result.slotContext()
                                                        .identifier()
                                        )
                                                && !result.slotContext()
                                                .cosmetic()
                                )
                                .findFirst()
                )
                .map(result -> result.stack())
                .orElse(ItemStack.EMPTY);
    }

    public static boolean isEquipped(LivingEntity entity) {
        return !findEquipped(entity).isEmpty();
    }

    public static boolean isBlessedAndEquipped(
            LivingEntity entity
    ) {
        ItemStack stack = findEquipped(entity);

        return !stack.isEmpty()
                && isBlessed(stack);
    }

    @Override
    public boolean canEquip(
            SlotContext context,
            ItemStack stack
    ) {
        return context != null
                && SLOT.equals(context.identifier())
                && !context.cosmetic()
                && CurioEquipmentHelper
                .countEquippedExceptSlot(
                        context.entity(),
                        this,
                        context.identifier(),
                        context.index()
                ) == 0;
    }

    @Override
    public boolean canEquipFromUse(
            SlotContext context,
            ItemStack stack
    ) {
        return canEquip(context, stack);
    }

    @Override
    public boolean canUnequip(
            SlotContext context,
            ItemStack stack
    ) {
        if (context.entity() instanceof Player player
                && SuperpositionHandler
                .canUnequipBoundRelics(player)) {
            return ICurioItem.super.canUnequip(
                    context,
                    stack
            );
        }

        return false;
    }

    @Override
    public DropRule getDropRule(
            SlotContext context,
            DamageSource source,
            int lootingLevel,
            boolean recentlyHit,
            ItemStack stack
    ) {
        return DropRule.ALWAYS_KEEP;
    }

    @Override
    public Multimap<Attribute, AttributeModifier>
    getAttributeModifiers(
            SlotContext context,
            UUID slotUuid,
            ItemStack stack
    ) {
        if (context == null
                || context.cosmetic()
                || !SLOT.equals(
                context.identifier()
        )) {
            return ImmutableMultimap.of();
        }

        return ImmutableMultimap.of(
                Attributes.MAX_HEALTH,
                new AttributeModifier(
                        MAX_HEALTH_UUID,
                        "until_eternity:hell_pendant/max_health",
                        -0.50D,
                        AttributeModifier.Operation.MULTIPLY_TOTAL
                )
        );
    }

    @Override
    public boolean overrideOtherStackedOnMe(
            ItemStack pendantStack,
            ItemStack cursorStack,
            Slot slot,
            ClickAction action,
            Player player,
            SlotAccess carriedAccess
    ) {
        if (action != ClickAction.SECONDARY
                || !cursorStack.is(Items.NETHER_STAR)
                || isBlessed(pendantStack)
                || !isExactEquippedStack(
                player,
                pendantStack
        )) {
            return false;
        }

        if (player.level().isClientSide) {
            return true;
        }

        ItemStack blessed =
                pendantStack.copy();

        blessed.getOrCreateTag()
                .putBoolean(
                        BLESSED_TAG,
                        true
                );

        slot.set(blessed);
        slot.setChanged();

        if (!player.getAbilities().instabuild) {
            ItemStack remaining =
                    cursorStack.copy();

            remaining.shrink(1);

            carriedAccess.set(
                    remaining
            );
        }

        player.level().playSound(
                null,
                player.getX(),
                player.getY(),
                player.getZ(),
                SoundEvents.BEACON_ACTIVATE,
                SoundSource.PLAYERS,
                1.0F,
                1.0F
        );

        return true;
    }

    private boolean isExactEquippedStack(
            Player player,
            ItemStack stack
    ) {
        return CuriosApi.getCuriosInventory(player)
                .resolve()
                .map(handler ->
                        handler.findCurios(this)
                                .stream()
                                .anyMatch(result ->
                                        result.stack() == stack
                                                && SLOT.equals(
                                                result.slotContext()
                                                        .identifier()
                                        )
                                                && !result.slotContext()
                                                .cosmetic()
                                )
                )
                .orElse(false);
    }

    @Override
    public void appendHoverText(
            ItemStack stack,
            @Nullable Level level,
            List<Component> tooltip,
            TooltipFlag flag
    ) {
        tooltip.add(
                Component.translatable(
                                "tooltip.until_eternity.hell_pendant.health"
                        )
                        .withStyle(
                                ChatFormatting.RED
                        )
        );

        tooltip.add(
                Component.translatable(
                                "tooltip.until_eternity.hell_pendant.fire_damage"
                        )
                        .withStyle(
                                ChatFormatting.RED
                        )
        );

        tooltip.add(
                Component.translatable(
                                "tooltip.until_eternity.hell_pendant.eternal_fire"
                        )
                        .withStyle(
                                ChatFormatting.RED
                        )
        );

        tooltip.add(
                Component.translatable(
                                "tooltip.until_eternity.hell_pendant.damage_taken"
                        )
                        .withStyle(
                                ChatFormatting.RED
                        )
        );

        tooltip.add(
                Component.translatable(
                                isBlessed(stack)
                                        ? "tooltip.until_eternity.hell_pendant.blessed"
                                        : "tooltip.until_eternity.hell_pendant.blessing"
                        )
                        .withStyle(
                                ChatFormatting.GOLD
                        )
        );
    }

    @Override
    public List<Component> getAttributesTooltip(
            List<Component> tooltips,
            ItemStack stack
    ) {
        tooltips.clear();

        return tooltips;
    }
}