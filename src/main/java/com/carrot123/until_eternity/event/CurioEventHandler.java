package com.carrot123.until_eternity.event;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import com.carrot123.until_eternity.item.curio.ImmuneCurioItem;
import com.carrot123.until_eternity.item.curio.LifeCapItem;
import com.carrot123.until_eternity.until_eternity;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingHealEvent;
import net.minecraftforge.event.entity.living.MobEffectEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.registries.ForgeRegistries;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.event.CurioChangeEvent;


public class CurioEventHandler {

    private static final Set<MobEffect> LIMITED_IMMUNE_EFFECTS = Set.of(
            MobEffects.WEAKNESS, MobEffects.POISON, MobEffects.MOVEMENT_SLOWDOWN,
            MobEffects.HUNGER, MobEffects.CONFUSION, MobEffects.WITHER,
            MobEffects.LEVITATION, MobEffects.DIG_SLOWDOWN, MobEffects.BLINDNESS,
            MobEffects.DARKNESS
    );
    // ==================== 事件监听 ====================

    /**
 * 当玩家佩戴 proof_of_spurner 时，任何致命伤害都会触发不死图腾效果（无消耗）。
 */
@SubscribeEvent
public void onLivingDeath(LivingDeathEvent event) {
    if (!(event.getEntity() instanceof Player player)) return;
    if (!hasProofOfSpurner(player)) return;

    // 取消死亡事件 → 玩家不会真正死亡
    event.setCanceled(true);

    // 恢复生命值（图腾默认恢复 1 点生命）
    player.setHealth(1.0F);

    // 施加增益效果（与原版不死图腾一致）
    player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 900, 1));   // 生命恢复 II，45秒
    player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 100, 1));     // 伤害吸收 II，5秒
    player.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 800, 0)); // 防火 I，40秒

    // 播放图腾动画和音效
    player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
            SoundEvents.TOTEM_USE, SoundSource.PLAYERS, 1.0F, 1.0F);
}

    @SubscribeEvent
    public void onEffectApplicable(MobEffectEvent.Applicable event) {
        if (event.getEntity() instanceof Player player) {
            MobEffect effect = event.getEffectInstance().getEffect();
            if (shouldBlockEffect(player, effect)) {
                event.setResult(Event.Result.DENY);
            }
        }
    }

    @SubscribeEvent
    public void onLivingAttack(LivingAttackEvent event) {
        if (event.getEntity() instanceof Player player) {
            if (isFireDamage(event.getSource()) && hasAnyImmuneCurio(player)) {
                event.setCanceled(true);
            }
        }
    }

    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        Player player = event.player;

        if (player.isOnFire() && hasAnyImmuneCurio(player)) {
            player.clearFire();
        }

        findLifeCapCurio(player).ifPresent(curio -> {
            float max = getMaxHealthForCurio(player, curio);
            if (player.getHealth() > max) {
                player.setHealth(max);
            }
        });
    }

     @SubscribeEvent
    public void onLivingHeal(LivingHealEvent event) {
        if (event.getEntity() instanceof Player player) {
            findLifeCapCurio(player).ifPresent(curio -> {
                float allowedMax = getMaxHealthForCurio(player, curio);
                float newHealth = player.getHealth() + event.getAmount();
                if (newHealth > allowedMax) {
                    event.setAmount(Math.max(0, allowedMax - player.getHealth()));
                }
            });
        }
    }

    @SubscribeEvent
    public void onCurioChange(CurioChangeEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        if (player.level().isClientSide) return;

        ItemStack toStack = event.getTo();
        ResourceLocation id = ForgeRegistries.ITEMS.getKey(toStack.getItem());
        // 只处理本模组的物品
        if (id != null && id.getNamespace().equals(until_eternity.MODID)) {
            MinecraftServer server = player.getServer();      // 提取服务器对象，避免空指针警告
            if (server != null) {
                server.execute(() -> enforceNoDuplicate(player, id));
            }
        }

        // 清除负面效果（免疫饰品）
        if (hasAnyImmuneCurio(player)) {
            clearExistingImmuneEffects(player);
        }
    }

    /**
    * 移除多余的相同注册名饰品，仅保留一件。
    */
    private void enforceNoDuplicate(Player player, ResourceLocation itemId) {
        CuriosApi.getCuriosInventory(player).ifPresent(handler -> {
            List<SlotPos> matchingSlots = new ArrayList<>();

            // 收集所有含有该注册名物品的槽位
            for (var entry : handler.getCurios().entrySet()) {
                var stacksHandler = entry.getValue();
                for (int i = 0; i < stacksHandler.getSlots(); i++) {
                    ItemStack stack = stacksHandler.getStacks().getStackInSlot(i);
                    if (!stack.isEmpty() && itemId.equals(ForgeRegistries.ITEMS.getKey(stack.getItem()))) {
                        matchingSlots.add(new SlotPos(entry.getKey(), i));
                    }
                }
            }

            // 如果不超过 1 个，无需处理
            if (matchingSlots.size() <= 1) return;

            // 保留第一个，移除其余
            for (int idx = 1; idx < matchingSlots.size(); idx++) {
                SlotPos pos = matchingSlots.get(idx);
                var stacksHandler = handler.getCurios().get(pos.slotId);
                if (stacksHandler == null) continue;

                ItemStack stackToRemove = stacksHandler.getStacks().getStackInSlot(pos.index);
                if (stackToRemove.isEmpty()) continue;

                // 将多余的饰品放回背包或掉落
                ItemStack copy = stackToRemove.copy();
                stacksHandler.getStacks().setStackInSlot(pos.index, ItemStack.EMPTY);
                if (!player.getInventory().add(copy)) {
                    player.drop(copy, false);
                }
            }
        });
    }

// 内部辅助类
private static class SlotPos {
    final String slotId;
    final int index;

    SlotPos(String slotId, int index) {
        this.slotId = slotId;
        this.index = index;
    }
}

    // ==================== 辅助方法 ====================

    private boolean hasProofOfSpurner(Player player) {
    return CuriosApi.getCuriosInventory(player).map(handler -> {
        for (var stacksHandler : handler.getCurios().values()) {
            for (int i = 0; i < stacksHandler.getSlots(); i++) {
                ItemStack stack = stacksHandler.getStacks().getStackInSlot(i);
                // 通过注册名精确识别 proof_of_spurner，避免误判其他 ImmuneCurioItem
                ResourceLocation id = ForgeRegistries.ITEMS.getKey(stack.getItem());
                if (id != null
                    && id.getNamespace().equals(until_eternity.MODID)
                    && id.getPath().equals("proof_of_spurner")) {
                    return true;
                }
            }
        }
        return false;
    }).orElse(false);
}

    private boolean isFireDamage(DamageSource source) {
        if (source.is(DamageTypeTags.IS_FIRE)) return true;
        String msgId = source.type().msgId();
        return switch (msgId) {
            case "inFire", "onFire", "lava", "hotFloor", "campfire",
                 "soul_campfire", "fireball", "fireworks", "flame" -> true;
            default -> false;
        };
    }

    private boolean hasAnyImmuneCurio(Player player) {
        return CuriosApi.getCuriosInventory(player).map(handler -> {
            for (var slotEntry : handler.getCurios().entrySet()) {
                var stacksHandler = slotEntry.getValue();
                for (int i = 0; i < stacksHandler.getSlots(); i++) {
                    if (stacksHandler.getStacks().getStackInSlot(i).getItem() instanceof ImmuneCurioItem)
                        return true;
                }
            }
            return false;
        }).orElse(false);
    }

    private boolean shouldBlockEffect(Player player, MobEffect effect) {
        return CuriosApi.getCuriosInventory(player).map(handler -> {
            for (var slotEntry : handler.getCurios().entrySet()) {
                var stacksHandler = slotEntry.getValue();
                for (int i = 0; i < stacksHandler.getSlots(); i++) {
                    ItemStack stack = stacksHandler.getStacks().getStackInSlot(i);
                    if (stack.getItem() instanceof ImmuneCurioItem curio) {
                        switch (curio.getCurioType()) {
                            case LIMITED:
                                if (LIMITED_IMMUNE_EFFECTS.contains(effect)) return true;
                                break;
                            case ALL:
                                if (effect.getCategory() == MobEffectCategory.HARMFUL) return true;
                                break;
                        }
                    }
                }
            }
            return false;
        }).orElse(false);
    }

    private void clearExistingImmuneEffects(Player player) {
        List<MobEffect> toRemove = new ArrayList<>();
        for (MobEffectInstance instance : player.getActiveEffects()) {
            if (shouldBlockEffect(player, instance.getEffect())) {
                toRemove.add(instance.getEffect());
            }
        }
        for (MobEffect effect : toRemove) {
            player.removeEffect(effect);
        }
    }

    private Optional<LifeCapItem> findLifeCapCurio(Player player) {
    var handler = CuriosApi.getCuriosInventory(player).resolve().orElse(null);
    if (handler == null) return Optional.empty();
    for (var slotEntry : handler.getCurios().entrySet()) {
        var stacksHandler = slotEntry.getValue();
        for (int i = 0; i < stacksHandler.getSlots(); i++) {
            ItemStack stack = stacksHandler.getStacks().getStackInSlot(i);
            if (stack.getItem() instanceof LifeCapItem lc) return Optional.of(lc);
        }
    }
    return Optional.empty();
}

    private float getMaxHealthForCurio(Player player, LifeCapItem curio) {
        if (curio.isAbsolute()) {
            return curio.getMaxHealthFraction();
        }
        return player.getMaxHealth() * curio.getMaxHealthFraction();
    }
}