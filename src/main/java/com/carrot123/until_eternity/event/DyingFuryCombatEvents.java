package com.carrot123.until_eternity.event;

import com.carrot123.until_eternity.item.ModItems;
import com.carrot123.until_eternity.until_eternity;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import top.theillusivec4.curios.api.CuriosApi;

/**
 * 垂死之怒（Dying Fury）：每损失 1 点生命值，增加 0.2% 全伤害，无上限。
 * <p>
 * 使用 {@link LivingHurtEvent}（与 Enigmatic Legacy 千咒卷轴相同的模式），
 * 通过 {@code source.getEntity() instanceof Player} 捕获所有以玩家为来源的伤害类型
 * （近战、弓箭、三叉戟、魔法等），统一进行百分比增伤。
 */
@Mod.EventBusSubscriber(modid = until_eternity.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class DyingFuryCombatEvents {
    private DyingFuryCombatEvents() {
    }

    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        float amount = event.getAmount();
        if (amount <= 0.0F || !Float.isFinite(amount)) {
            return;
        }

        // 千咒卷轴模式：source.getEntity() 是伤害的根源实体。
        // 对弓箭/三叉戟等弹射物，getEntity() 返回投掷者（玩家），getDirectEntity() 返回弹射物本体。
        // 这能覆盖所有玩家造成的伤害类型。
        if (!(event.getSource().getEntity() instanceof ServerPlayer player)) {
            return;
        }
        // 不自伤
        if (player == event.getEntity()) {
            return;
        }
        if (!isDyingFuryEquipped(player)) {
            return;
        }

        double lostHealth = DyingFuryDamageLogic.calculateLostHealth(
                player.getMaxHealth(),
                player.getHealth());
        if (lostHealth <= 0.0D) {
            return;
        }

        event.setAmount(DyingFuryDamageLogic.enhanceDamage(
                amount,
                player.getMaxHealth(),
                player.getHealth()));
    }

    private static boolean isDyingFuryEquipped(Player player) {
        return CuriosApi.getCuriosInventory(player)
                .map(handler -> handler.isEquipped(ModItems.DYING_FURY.get()))
                .orElse(false);
    }
}
