package com.carrot123.until_eternity.event;

import com.carrot123.until_eternity.enchantment.ModEnchantments;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.ItemAttributeModifierEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import top.theillusivec4.curios.api.CuriosApi;

import java.util.UUID;

/**
 * 处理本模组附魔的效果逻辑
 */
public class EnchantmentEventHandler {

    // ========== 力量附魔 UUID ==========
    private static final UUID POWER_ATTACK_SPEED_UUID =
            UUID.fromString("a1b2c3d4-e5f6-7890-abcd-ef1234567890");
    private static final UUID POWER_ATTACK_DAMAGE_UUID =
            UUID.fromString("b2c3d4e5-f6a7-8901-bcde-f12345678901");

    // 短命诅咒耐久消耗计时器（每20 tick = 1秒）
    private int shortLifeTickCounter = 0;

    // ========== 力量附魔：属性修饰 ==========

    @SubscribeEvent
    @SuppressWarnings("null")
    public void onItemAttributeModifier(ItemAttributeModifierEvent event) {
        ItemStack stack = event.getItemStack();
        int level = stack.getEnchantmentLevel(ModEnchantments.POWER.get());
        if (level <= 0) return;

        // 只对主手物品生效
        if (event.getSlotType() != EquipmentSlot.MAINHAND) return;

        // -0.1 攻击速度 每级
        if (event.getOriginalModifiers().containsKey(Attributes.ATTACK_SPEED)) {
            event.addModifier(Attributes.ATTACK_SPEED,
                    new AttributeModifier(POWER_ATTACK_SPEED_UUID,
                            "Power attack speed penalty",
                            -0.1 * level,
                            AttributeModifier.Operation.ADDITION));
        }

        // +15% 攻击伤害 每级
        if (event.getOriginalModifiers().containsKey(Attributes.ATTACK_DAMAGE)) {
            event.addModifier(Attributes.ATTACK_DAMAGE,
                    new AttributeModifier(POWER_ATTACK_DAMAGE_UUID,
                            "Power attack damage bonus",
                            0.15 * level,
                            AttributeModifier.Operation.MULTIPLY_BASE));
        }
    }

    // ========== 玩家 Tick：饥饿诅咒 + 短命诅咒 ==========

    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        Player player = event.player;
        if (player.level().isClientSide) return;

        // ---- 饥饿诅咒：饱食度消耗+5% ----
        if (hasCurseOfHungerEquipped(player)) {
            // 每 tick 增加额外消耗。
            player.causeFoodExhaustion(0.00025F); // 5% of ~0.005/tick
        }

        // ---- 短命诅咒：每秒-1耐久 ----
        shortLifeTickCounter++;
        if (shortLifeTickCounter >= 20) {
            shortLifeTickCounter = 0;
            applyCurseOfShortLife(player);
        }
    }

    // ========== 辅助方法 ==========

    /**
     * 检查玩家是否装备/持有饥饿诅咒附魔的物品
     */
    private boolean hasCurseOfHungerEquipped(Player player) {
        // 检查装备栏
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            if (slot.getType() != EquipmentSlot.Type.ARMOR
                    && slot != EquipmentSlot.MAINHAND
                    && slot != EquipmentSlot.OFFHAND) {
                continue;
            }
            ItemStack stack = player.getItemBySlot(slot);
            if (hasCurseOfHunger(stack)) return true;
        }

        // 检查饰品栏 (Curios)
        return hasCurseOfHungerInCurios(player);
    }

    private boolean hasCurseOfHunger(ItemStack stack) {
        return !stack.isEmpty()
                && stack.getEnchantmentLevel(ModEnchantments.CURSE_OF_HUNGER.get()) > 0;
    }

    private boolean hasCurseOfHungerInCurios(Player player) {
        try {
            return CuriosApi.getCuriosInventory(player).map(handler -> {
                for (var slotEntry : handler.getCurios().entrySet()) {
                    var stacksHandler = slotEntry.getValue();
                    for (int i = 0; i < stacksHandler.getSlots(); i++) {
                        if (hasCurseOfHunger(stacksHandler.getStacks().getStackInSlot(i))) {
                            return true;
                        }
                    }
                }
                return false;
            }).orElse(false);
        } catch (Exception e) {
            return false; // Curios 未安装时静默失败
        }
    }

    /**
     * 每秒减少短命诅咒物品1点耐久
     */
    private void applyCurseOfShortLife(Player player) {
        // 检查所有装备栏
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            ItemStack stack = player.getItemBySlot(slot);
            damageShortLifeStack(stack, player);
        }

        // 检查饰品栏
        try {
            CuriosApi.getCuriosInventory(player).ifPresent(handler -> {
                for (var slotEntry : handler.getCurios().entrySet()) {
                    var stacksHandler = slotEntry.getValue();
                    for (int i = 0; i < stacksHandler.getSlots(); i++) {
                        ItemStack stack = stacksHandler.getStacks().getStackInSlot(i);
                        damageShortLifeStack(stack, player);
                    }
                }
            });
        } catch (Exception ignored) {}
    }

    private void damageShortLifeStack(ItemStack stack, Player player) {
        if (stack.isEmpty()) return;
        if (!stack.isDamageableItem()) return;
        // 无法破坏属性跳过
        if (stack.hasTag() && stack.getTag().getBoolean("Unbreakable")) return;
        // 必须有短命诅咒
        if (stack.getEnchantmentLevel(ModEnchantments.CURSE_OF_SHORT_LIFE.get()) <= 0) return;

        // 造成1点耐久损伤
        stack.hurtAndBreak(1, player, (p) -> {
            p.broadcastBreakEvent(EquipmentSlot.MAINHAND);
        });
    }
}
