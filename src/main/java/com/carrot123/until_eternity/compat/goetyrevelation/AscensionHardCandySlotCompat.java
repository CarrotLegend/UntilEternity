package com.carrot123.until_eternity.compat.goetyrevelation;

import com.carrot123.until_eternity.until_eternity;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.type.capability.ICuriosItemHandler;
import top.theillusivec4.curios.api.type.inventory.ICurioStacksHandler;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Mod.EventBusSubscriber(
        modid = until_eternity.MODID,
        bus = Mod.EventBusSubscriber.Bus.FORGE
)
public final class AscensionHardCandySlotCompat {

    private static final ResourceLocation ASCENSION_HARD_CANDY =
            new ResourceLocation(
                    "goety_revelation",
                    "ascension_hard_candy"
            );

    private static final String CHARM_SLOT = "charm";

    private static final String[] PREFERRED_GENERIC_SLOTS = {
            "curio",
            "accessory"
    };

    private static final Map<UUID, Map<String, Integer>> SLOT_SNAPSHOTS =
            new HashMap<>();

    private AscensionHardCandySlotCompat() {
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onUseStart(
            LivingEntityUseItemEvent.Start event
    ) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }

        if (player.level().isClientSide) {
            return;
        }

        if (!isAscensionHardCandy(event.getItem())) {
            return;
        }

        CuriosApi.getCuriosInventory(player)
                .ifPresent(handler ->
                        SLOT_SNAPSHOTS.put(
                                player.getUUID(),
                                snapshotSlots(handler)
                        )
                );
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onUseFinish(
            LivingEntityUseItemEvent.Finish event
    ) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }

        if (player.level().isClientSide) {
            return;
        }

        Map<String, Integer> before =
                SLOT_SNAPSHOTS.remove(player.getUUID());

        if (before == null) {
            return;
        }

        CuriosApi.getCuriosInventory(player)
                .ifPresent(handler ->
                        transferAddedSlotToCharm(
                                handler,
                                before
                        )
                );
    }

    @SubscribeEvent
    public static void onUseStop(
            LivingEntityUseItemEvent.Stop event
    ) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }

        if (player.level().isClientSide) {
            return;
        }

        SLOT_SNAPSHOTS.remove(player.getUUID());
    }

    @SubscribeEvent
    public static void onLogout(
            PlayerEvent.PlayerLoggedOutEvent event
    ) {
        SLOT_SNAPSHOTS.remove(
                event.getEntity().getUUID()
        );
    }

    private static void transferAddedSlotToCharm(
            ICuriosItemHandler handler,
            Map<String, Integer> before
    ) {
        if (handler.getStacksHandler(CHARM_SLOT).isEmpty()) {
            return;
        }

        String sourceSlot =
                findIncreasedPreferredSlot(
                        handler,
                        before
                );

        if (sourceSlot == null) {
            sourceSlot =
                    findAnyIncreasedSlot(
                            handler,
                            before
                    );
        }

        if (sourceSlot == null) {
            return;
        }

        handler.shrinkSlotType(
                sourceSlot,
                1
        );

        handler.growSlotType(
                CHARM_SLOT,
                1
        );
    }

    private static String findIncreasedPreferredSlot(
            ICuriosItemHandler handler,
            Map<String, Integer> before
    ) {
        for (String slot : PREFERRED_GENERIC_SLOTS) {
            ICurioStacksHandler stacksHandler =
                    handler.getCurios().get(slot);

            if (stacksHandler == null) {
                continue;
            }

            int oldSize = before.getOrDefault(
                    slot,
                    stacksHandler.getSlots()
            );

            if (stacksHandler.getSlots() > oldSize) {
                return slot;
            }
        }

        return null;
    }

    private static String findAnyIncreasedSlot(
            ICuriosItemHandler handler,
            Map<String, Integer> before
    ) {
        for (Map.Entry<String, ICurioStacksHandler> entry :
                handler.getCurios().entrySet()) {

            String slot = entry.getKey();

            if (CHARM_SLOT.equals(slot)) {
                continue;
            }

            int currentSize =
                    entry.getValue().getSlots();

            int oldSize =
                    before.getOrDefault(
                            slot,
                            currentSize
                    );

            if (currentSize > oldSize) {
                return slot;
            }
        }

        return null;
    }

    private static Map<String, Integer> snapshotSlots(
            ICuriosItemHandler handler
    ) {
        Map<String, Integer> result =
                new HashMap<>();

        for (Map.Entry<String, ICurioStacksHandler> entry :
                handler.getCurios().entrySet()) {
            result.put(
                    entry.getKey(),
                    entry.getValue().getSlots()
            );
        }

        return result;
    }

    private static boolean isAscensionHardCandy(
            ItemStack stack
    ) {
        if (stack.isEmpty()) {
            return false;
        }

        ResourceLocation id =
                ForgeRegistries.ITEMS.getKey(
                        stack.getItem()
                );

        return ASCENSION_HARD_CANDY.equals(id);
    }
}