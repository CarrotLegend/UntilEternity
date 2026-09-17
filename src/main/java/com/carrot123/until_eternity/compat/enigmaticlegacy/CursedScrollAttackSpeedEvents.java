package com.carrot123.until_eternity.compat.enigmaticlegacy;

import com.aizistral.enigmaticlegacy.handlers.SuperpositionHandler;
import com.aizistral.enigmaticlegacy.items.CursedScroll;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.registries.ForgeRegistries;
import top.theillusivec4.curios.api.CuriosApi;

import java.util.List;
import java.util.UUID;

public final class CursedScrollAttackSpeedEvents {
    public static final ResourceLocation CURSED_SCROLL =
            new ResourceLocation("enigmaticlegacy", "cursed_scroll");
    public static final UUID ATTACK_SPEED_UUID =
            UUID.fromString("ca0a91d8-bde2-4bbc-bc47-e89ef63bb429");

    private CursedScrollAttackSpeedEvents() {
    }

    public static void register() {
        MinecraftForge.EVENT_BUS.register(CursedScrollAttackSpeedEvents.class);
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.player.level().isClientSide) {
            return;
        }
        synchronizeModifier(event.player);
    }

    @SubscribeEvent
    public static void onTooltip(ItemTooltipEvent event) {
        if (!isCursedScroll(event.getItemStack().getItem())) {
            return;
        }
        replaceTranslation(event.getToolTip(),
                "tooltip.enigmaticlegacy.cursed_scroll2",
                "tooltip.until_eternity.cursed_scroll.attack_speed");
        replaceTranslation(event.getToolTip(),
                "tooltip.enigmaticlegacy.cursed_scroll8",
                "tooltip.until_eternity.cursed_scroll.attack_speed");
    }

    static void synchronizeModifier(Player player) {
        AttributeInstance attackSpeed = player.getAttribute(Attributes.ATTACK_SPEED);
        if (attackSpeed == null) {
            return;
        }

        double desired = desiredAmount(player);
        AttributeModifier current = attackSpeed.getModifier(ATTACK_SPEED_UUID);
        if (current != null && Double.compare(current.getAmount(), desired) == 0) {
            return;
        }
        if (current != null) {
            attackSpeed.removeModifier(ATTACK_SPEED_UUID);
        }
        if (desired > 0.0D) {
            attackSpeed.addTransientModifier(new AttributeModifier(
                    ATTACK_SPEED_UUID,
                    "until_eternity:cursed_scroll_attack_speed",
                    desired,
                    AttributeModifier.Operation.MULTIPLY_TOTAL));
        }
    }

    private static double desiredAmount(Player player) {
        boolean equipped = CuriosApi.getCuriosInventory(player)
                .map(handler -> handler.isEquipped(stack ->
                        isCursedScroll(stack.getItem())))
                .orElse(false);
        if (!equipped || CursedScroll.miningBoost == null) {
            return 0.0D;
        }
        return CursedScrollAttackSpeedMath.calculate(
                CursedScroll.miningBoost.getValue().asModifier(),
                SuperpositionHandler.getCurseAmount(player));
    }

    private static boolean isCursedScroll(net.minecraft.world.item.Item item) {
        return CURSED_SCROLL.equals(ForgeRegistries.ITEMS.getKey(item));
    }

    private static void replaceTranslation(
            List<Component> tooltip,
            String oldKey,
            String newKey
    ) {
        for (int index = 0; index < tooltip.size(); index++) {
            Component original = tooltip.get(index);
            if (!(original.getContents() instanceof TranslatableContents contents)
                    || !oldKey.equals(contents.getKey())) {
                continue;
            }
            MutableComponent replacement = Component.translatable(
                    newKey, contents.getArgs()).setStyle(original.getStyle());
            original.getSiblings().forEach(replacement::append);
            tooltip.set(index, replacement);
        }
    }
}
