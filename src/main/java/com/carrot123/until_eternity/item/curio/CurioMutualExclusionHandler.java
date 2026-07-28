package com.carrot123.until_eternity.item.curio;

import com.carrot123.until_eternity.compat.TerraCurioCompat;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.event.CurioEquipEvent;

import java.util.List;
import java.util.Set;

@Mod.EventBusSubscriber(modid = "until_eternity", bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class CurioMutualExclusionHandler {
    private static final Set<ResourceLocation> SHIELD_GROUP = Set.of(
            id("empowered_shield"),
            id("cosmic_aegis"),
            TerraCurioCompat.ANKH_SHIELD
    );
    private static final Set<ResourceLocation> SHARK_TOOTH_GROUP = Set.of(
            id("reaper_tooth_necklace"),
            id("sand_shark_tooth_necklace"),
            TerraCurioCompat.SHARK_TOOTH_NECKLACE
    );
    private static final Set<ResourceLocation> LIFE_CHARM_GROUP = Set.of(
            id("regenerator"),
            id("guttering_candle")
    );
    private static final List<Set<ResourceLocation>> GROUPS = List.of(
            SHIELD_GROUP,
            SHARK_TOOTH_GROUP,
            LIFE_CHARM_GROUP
    );

    private CurioMutualExclusionHandler() {
    }

    @SubscribeEvent
    public static void onCurioEquip(CurioEquipEvent event) {
        if (!canEquip(event.getSlotContext(), event.getStack())) {
            event.setResult(Event.Result.DENY);
        }
    }

    public static boolean canEquip(SlotContext targetContext, ItemStack candidate) {
        if (candidate.isEmpty() || targetContext.entity() == null) {
            return true;
        }

        ResourceLocation candidateId = ForgeRegistries.ITEMS.getKey(candidate.getItem());
        Set<ResourceLocation> candidateGroup = findGroup(candidateId);
        if (candidateGroup == null) {
            return true;
        }

        return CuriosApi.getCuriosInventory(targetContext.entity()).map(handler -> {
            for (var entry : handler.getCurios().entrySet()) {
                var stacksHandler = entry.getValue();
                for (int index = 0; index < stacksHandler.getSlots(); index++) {
                    if (entry.getKey().equals(targetContext.identifier())
                            && index == targetContext.index()) {
                        continue;
                    }

                    ItemStack equipped = stacksHandler.getStacks().getStackInSlot(index);
                    if (!equipped.isEmpty()) {
                        ResourceLocation equippedId =
                                ForgeRegistries.ITEMS.getKey(equipped.getItem());
                        if (candidateGroup.contains(equippedId)) {
                            return false;
                        }
                    }
                }
            }
            return true;
        }).orElse(true);
    }

    static Set<ResourceLocation> findGroup(ResourceLocation itemId) {
        if (itemId == null) {
            return null;
        }
        for (Set<ResourceLocation> group : GROUPS) {
            if (group.contains(itemId)) {
                return group;
            }
        }
        return null;
    }

    private static ResourceLocation id(String path) {
        return new ResourceLocation("until_eternity", path);
    }
}
