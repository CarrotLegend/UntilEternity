package com.carrot123.until_eternity.tarot;

import com.carrot123.until_eternity.until_eternity;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.type.inventory.IDynamicStackHandler;

@Mod.EventBusSubscriber(modid = until_eternity.MODID,
        bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class TarotSetManager {
    private static final Map<UUID, PlayerState> STATES = new HashMap<>();

    private TarotSetManager() {
    }

    public static void markDirty(ServerPlayer player) {
        STATES.computeIfAbsent(player.getUUID(), ignored -> new PlayerState()).dirty = true;
        refresh(player);
    }

    public static Set<ResourceLocation> activeSets(ServerPlayer player) {
        return STATES.containsKey(player.getUUID())
                ? STATES.get(player.getUUID()).activeSets : Set.of();
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || !(event.player instanceof ServerPlayer player)) {
            return;
        }
        PlayerState state = STATES.computeIfAbsent(player.getUUID(), ignored -> new PlayerState());
        if (state.dirty || player.tickCount % 10 == 0) {
            refresh(player);
        }
        for (ResourceLocation id : state.activeSets) {
            TarotSetEffect effect = TarotSetRegistry.effects().get(id);
            if (effect != null) {
                effect.onTick(player);
            }
        }
    }

    @SubscribeEvent
    public static void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            clearState(player);
            markDirty(player);
        }
    }

    @SubscribeEvent
    public static void onRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            clearState(player);
            markDirty(player);
        }
    }

    @SubscribeEvent
    public static void onDimensionChange(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            clearState(player);
            markDirty(player);
        }
    }

    @SubscribeEvent
    public static void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            clearState(player);
        }
    }

    private static void clearState(ServerPlayer player) {
        PlayerState state = STATES.remove(player.getUUID());
        if (state == null) {
            return;
        }
        for (ResourceLocation id : state.activeSets) {
            TarotSetEffect effect = TarotSetRegistry.effects().get(id);
            if (effect != null) {
                effect.onDeactivate(player);
            }
        }
    }

    private static void refresh(ServerPlayer player) {
        PlayerState state = STATES.computeIfAbsent(player.getUUID(), ignored -> new PlayerState());
        Map<String, TarotDeckSnapshot> snapshots = scanEquippedDecks(player);
        state.dirty = false;
        if (snapshots.equals(state.snapshots)) {
            return;
        }
        Set<ResourceLocation> next = new HashSet<>();
        for (TarotDeckSnapshot snapshot : snapshots.values()) {
            next.addAll(TarotSetMatcher.match(snapshot, TarotSetRegistry.definitions()));
        }
        Set<ResourceLocation> old = state.activeSets;
        state.snapshots = Map.copyOf(snapshots);
        state.activeSets = Set.copyOf(next);
        for (ResourceLocation id : old) {
            if (!next.contains(id)) {
                TarotSetEffect effect = TarotSetRegistry.effects().get(id);
                if (effect != null) {
                    effect.onDeactivate(player);
                }
            }
        }
        for (ResourceLocation id : next) {
            if (!old.contains(id)) {
                TarotSetEffect effect = TarotSetRegistry.effects().get(id);
                if (effect != null) {
                    effect.onActivate(player);
                }
            }
        }
    }

    private static Map<String, TarotDeckSnapshot> scanEquippedDecks(ServerPlayer player) {
        Map<String, TarotDeckSnapshot> snapshots = new HashMap<>();
        CuriosApi.getCuriosInventory(player).ifPresent(inventory -> {
            for (var entry : inventory.getCurios().entrySet()) {
                IDynamicStackHandler stacks = entry.getValue().getStacks();
                for (int slot = 0; slot < stacks.getSlots(); slot++) {
                    ItemStack stack = stacks.getStackInSlot(slot);
                    if (TarotDeckScanner.isDeck(stack)) {
                        snapshots.put(entry.getKey() + ":" + slot,
                                TarotDeckScanner.scan(stack));
                    }
                }
            }
        });
        return snapshots;
    }

    private static final class PlayerState {
        private boolean dirty = true;
        private Map<String, TarotDeckSnapshot> snapshots = Map.of();
        private Set<ResourceLocation> activeSets = Set.of();
    }
}
