package com.carrot123.until_eternity.compat.curios;

import com.carrot123.until_eternity.until_eternity;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.type.capability.ICuriosItemHandler;
import top.theillusivec4.curios.api.type.inventory.ICurioStacksHandler;
import top.theillusivec4.curios.api.type.inventory.IDynamicStackHandler;

@Mod.EventBusSubscriber(modid = until_eternity.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class CuriosRecoveryStorage {
    private static final String KEY = "UntilEternityCuriosRecoveryV2";
    private static final String STACK = "Stack";
    private static final String SLOT = "Slot";
    private static final String INDEX = "Index";
    private static final String COSMETIC = "Cosmetic";
    private static final ThreadLocal<ArrayDeque<RemovalContext>> REMOVALS = ThreadLocal.withInitial(ArrayDeque::new);

    private CuriosRecoveryStorage() {}

    private static final class RemovalContext {
        private final ServerPlayer player;
        private final String slot;
        private int index;

        private RemovalContext(ServerPlayer player, String slot, int index) {
            this.player = player;
            this.slot = slot;
            this.index = index;
        }
    }

    public static void beginRemoval(ServerPlayer player, String slot, int firstIndex) {
        REMOVALS.get().push(new RemovalContext(player, slot, firstIndex));
    }

    public static void endRemoval() {
        ArrayDeque<RemovalContext> contexts = REMOVALS.get();
        if (!contexts.isEmpty()) contexts.pop();
        if (contexts.isEmpty()) REMOVALS.remove();
    }

    public static boolean interceptInvalidStack(ItemStack stack) {
        ArrayDeque<RemovalContext> contexts = REMOVALS.get();
        if (contexts.isEmpty()) return false;
        RemovalContext context = contexts.peek();
        int index = context.index++;
        if (context.player == null) return false;
        if (!stack.isEmpty()) store(context.player, stack, context.slot, index, false);
        return true;
    }

    private static CompoundTag persisted(Player player) {
        CompoundTag data = player.getPersistentData();
        if (!data.contains(Player.PERSISTED_NBT_TAG, Tag.TAG_COMPOUND)) {
            data.put(Player.PERSISTED_NBT_TAG, new CompoundTag());
        }
        return data.getCompound(Player.PERSISTED_NBT_TAG);
    }

    public static void store(ServerPlayer player, ItemStack stack, String slot, int index, boolean cosmetic) {
        if (stack.isEmpty()) return;
        CompoundTag data = persisted(player);
        ListTag saved = data.getList(KEY, Tag.TAG_COMPOUND);
        CompoundTag entry = new CompoundTag();
        entry.put(STACK, stack.copy().save(new CompoundTag()));
        entry.putString(SLOT, slot == null ? "" : slot);
        entry.putInt(INDEX, index);
        entry.putBoolean(COSMETIC, cosmetic);
        saved.add(entry);
        data.put(KEY, saved);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || !(event.player instanceof ServerPlayer player) || player.tickCount % 10 != 0) return;
        ListTag saved = persisted(player).getList(KEY, Tag.TAG_COMPOUND);
        if (saved.isEmpty()) return;
        CuriosApi.getCuriosInventory(player).resolve().ifPresent(handler -> restore(handler, saved));
    }

    private static void restore(ICuriosItemHandler handler, ListTag saved) {
        List<Integer> order = new ArrayList<>();
        int initialCount = saved.size();
        for (int i = 0; i < initialCount; i++) order.add(i);
        order.sort(Comparator.comparingInt(i -> priority(saved.getCompound(i).getString(SLOT))));
        List<Integer> resolved = new ArrayList<>();
        for (int i : order) {
            CompoundTag entry = saved.getCompound(i);
            ItemStack stack = ItemStack.of(entry.getCompound(STACK));
            if (stack.isEmpty()) {
                resolved.add(i);
                continue;
            }
            String id = entry.getString(SLOT);
            if (id.isEmpty()) continue;
            boolean restored = handler.getStacksHandler(id)
                    .map(slot -> tryRestore(handler, slot, id, entry.getInt(INDEX), entry.getBoolean(COSMETIC), stack))
                    .orElse(false);
            if (restored) resolved.add(i);
        }
        resolved.sort(Comparator.reverseOrder());
        for (int i : resolved) saved.remove(i);
    }

    private static int priority(String slot) {
        return switch (slot) {
            case "ring", "pandora_box" -> 0;
            case "belt", "charm", "hands", "scroll" -> 1;
            case "scar", "curse_spirit" -> 3;
            default -> 2;
        };
    }

    private static boolean tryRestore(ICuriosItemHandler handler, ICurioStacksHandler slot, String id, int preferred,
                                      boolean cosmetic, ItemStack original) {
        IDynamicStackHandler equipment = slot.getStacks();
        IDynamicStackHandler target = cosmetic ? slot.getCosmeticStacks() : equipment;
        int size = target.getSlots();
        if (preferred >= 0 && preferred < size && insert(handler, equipment, target, id, preferred, cosmetic, original)) return true;
        for (int i = 0; i < size; i++) {
            if (i != preferred && insert(handler, equipment, target, id, i, cosmetic, original)) return true;
        }
        return false;
    }

    private static boolean insert(ICuriosItemHandler handler, IDynamicStackHandler equipment,
                                  IDynamicStackHandler target, String id, int index, boolean cosmetic, ItemStack original) {
        if (!target.getStackInSlot(index).isEmpty() || !equipment.isItemValid(index, original)) return false;
        ItemStack restored = original.copy();
        if (cosmetic) target.setStackInSlot(index, restored);
        else handler.setEquippedCurio(id, index, restored);
        ItemStack result = target.getStackInSlot(index);
        return result.getCount() == original.getCount() && ItemStack.isSameItemSameTags(result, original);
    }

    @SubscribeEvent
    public static void onClone(PlayerEvent.Clone event) {
        CompoundTag oldData = event.getOriginal().getPersistentData().getCompound(Player.PERSISTED_NBT_TAG);
        if (!oldData.contains(KEY, Tag.TAG_LIST)) return;
        CompoundTag newData = persisted(event.getEntity());
        if (!newData.contains(KEY, Tag.TAG_LIST)) newData.put(KEY, oldData.getList(KEY, Tag.TAG_COMPOUND).copy());
    }

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("curiosrecover")
                .executes(context -> retrieve(context.getSource())));
    }

    private static int retrieve(CommandSourceStack source) throws CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();
        ListTag saved = persisted(player).getList(KEY, Tag.TAG_COMPOUND);
        int delivered = 0;
        for (int i = saved.size() - 1; i >= 0; i--) {
            CompoundTag entry = saved.getCompound(i);
            ItemStack original = ItemStack.of(entry.getCompound(STACK));
            if (original.isEmpty()) {
                saved.remove(i);
                continue;
            }
            ItemStack remaining = original.copy();
            player.getInventory().add(remaining);
            if (remaining.getCount() < original.getCount()) delivered++;
            if (remaining.isEmpty()) saved.remove(i);
            else entry.put(STACK, remaining.save(new CompoundTag()));
        }
        int count = delivered;
        int pending = saved.size();
        source.sendSuccess(() -> Component.literal("Curios: recovered " + count + " stack(s); " + pending + " stack(s) still safely stored."), false);
        return delivered;
    }
}
