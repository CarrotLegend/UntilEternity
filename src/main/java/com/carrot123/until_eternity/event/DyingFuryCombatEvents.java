package com.carrot123.until_eternity.event;

import com.carrot123.until_eternity.item.ModItems;
import com.carrot123.until_eternity.registry.ModAttributes;
import com.carrot123.until_eternity.until_eternity;
import java.util.UUID;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import top.theillusivec4.curios.api.CuriosApi;

@Mod.EventBusSubscriber(
        modid = until_eternity.MODID,
        bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class DyingFuryCombatEvents {
    private static final UUID ALL_DAMAGE_UUID = UUID.fromString(
            "ed69f8e5-2fa8-4e73-a420-9490f3429c51");

    private DyingFuryCombatEvents() {
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase == TickEvent.Phase.END
                && event.player instanceof ServerPlayer player) {
            syncModifier(player);
        }
    }

    public static void syncModifier(ServerPlayer player) {
        AttributeInstance attribute = player.getAttribute(
                ModAttributes.ALL_DAMAGE.get());
        if (attribute == null) {
            return;
        }

        boolean equipped = CuriosApi.getCuriosInventory(player)
                .map(handler -> handler.isEquipped(ModItems.DYING_FURY.get()))
                .orElse(false);
        double amount = equipped && player.isAlive()
                ? DyingFuryDamageLogic.modifierAmount(
                        player.getMaxHealth(), player.getHealth())
                : 0.0D;
        AttributeModifier current = attribute.getModifier(ALL_DAMAGE_UUID);
        if (amount <= 0.0D || !Double.isFinite(amount)) {
            if (current != null) {
                attribute.removeModifier(ALL_DAMAGE_UUID);
            }
            return;
        }
        if (current != null
                && Double.compare(current.getAmount(), amount) == 0
                && current.getOperation()
                == AttributeModifier.Operation.MULTIPLY_BASE) {
            return;
        }
        if (current != null) {
            attribute.removeModifier(ALL_DAMAGE_UUID);
        }
        attribute.addTransientModifier(new AttributeModifier(
                ALL_DAMAGE_UUID,
                "until_eternity:dying_fury/all_damage",
                amount,
                AttributeModifier.Operation.MULTIPLY_BASE));
    }
}
