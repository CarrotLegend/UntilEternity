package com.carrot123.until_eternity.event;

import com.carrot123.until_eternity.compat.PuffishAttributesCompat;
import com.carrot123.until_eternity.item.curio.HellPendantItem;
import com.carrot123.until_eternity.registry.ModAttributes;
import com.carrot123.until_eternity.until_eternity;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;

import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(
        modid = until_eternity.MODID,
        bus = Mod.EventBusSubscriber.Bus.FORGE
)
public final class HellPendantEvents {

    private static final UUID ALL_DAMAGE_UUID =
            UUID.nameUUIDFromBytes(
                    "until_eternity:hell_pendant/all_damage"
                            .getBytes(StandardCharsets.UTF_8)
            );

    private static final UUID RESISTANCE_UUID =
            UUID.nameUUIDFromBytes(
                    "until_eternity:hell_pendant/resistance"
                            .getBytes(StandardCharsets.UTF_8)
            );

    private static final Map<UUID, Integer>
            START_FIRE_TICKS =
            new ConcurrentHashMap<>();

    private HellPendantEvents() {
    }

    @SubscribeEvent(
            priority = EventPriority.LOWEST
    )
    public static void onLivingHurt(
            LivingHurtEvent event
    ) {
        if (!(event.getEntity()
                instanceof ServerPlayer player)
                || !HellPendantItem.isEquipped(
                player
        )) {
            return;
        }

        float amount =
                event.getAmount();

        if (amount <= 0.0F
                || !Float.isFinite(
                amount
        )) {
            return;
        }

        double multiplier =
                1.20D;

        if (event.getSource().is(
                DamageTypeTags.IS_FIRE
        )) {
            multiplier *= 2.0D;
        }

        double modified =
                amount * multiplier;

        if (!Double.isFinite(
                modified
        )) {
            return;
        }

        event.setAmount(
                modified >= Float.MAX_VALUE
                        ? Float.MAX_VALUE
                        : (float) modified
        );
    }

    @SubscribeEvent
    public static void onPlayerTick(
            TickEvent.PlayerTickEvent event
    ) {
        if (!(event.player
                instanceof ServerPlayer player)) {
            return;
        }

        if (event.phase == TickEvent.Phase.START) {
            captureFire(
                    player
            );

            return;
        }

        restoreFire(
                player
        );

        syncBlessingModifiers(
                player
        );

        if (HellPendantItem.isEquipped(
                player
        )
                && player.getHealth()
                > player.getMaxHealth()) {

            player.setHealth(
                    player.getMaxHealth()
            );
        }
    }

    private static void captureFire(
            ServerPlayer player
    ) {
        if (!HellPendantItem.isEquipped(
                player
        )) {
            START_FIRE_TICKS.remove(
                    player.getUUID()
            );

            return;
        }

        int fireTicks =
                player.getRemainingFireTicks();

        if (fireTicks > 0) {
            START_FIRE_TICKS.put(
                    player.getUUID(),
                    fireTicks
            );
        } else {
            START_FIRE_TICKS.remove(
                    player.getUUID()
            );
        }
    }

    private static void restoreFire(
            ServerPlayer player
    ) {
        Integer previous =
                START_FIRE_TICKS.remove(
                        player.getUUID()
                );

        if (previous == null
                || previous <= 1
                || !HellPendantItem.isEquipped(
                player
        )
                || !player.isInWaterRainOrBubble()) {
            return;
        }

        int expected =
                previous - 1;

        if (player.getRemainingFireTicks()
                < expected) {

            player.setRemainingFireTicks(
                    expected
            );
        }
    }

    private static void syncBlessingModifiers(
            ServerPlayer player
    ) {
        boolean blessed =
                HellPendantItem
                        .isBlessedAndEquipped(
                                player
                        );

        syncModifier(
                player.getAttribute(
                        ModAttributes.ALL_DAMAGE.get()
                ),
                ALL_DAMAGE_UUID,
                "until_eternity:hell_pendant/all_damage",
                blessed ? 4.0D : 0.0D,
                AttributeModifier.Operation.MULTIPLY_BASE
        );

        Attribute resistance =
                PuffishAttributesCompat.resolve(
                        PuffishAttributesCompat.RESISTANCE
                );

        syncModifier(
                resistance == null
                        ? null
                        : player.getAttribute(
                                resistance
                        ),
                RESISTANCE_UUID,
                "until_eternity:hell_pendant/resistance",
                blessed ? 0.25D : 0.0D,
                AttributeModifier.Operation.MULTIPLY_TOTAL
        );
    }

    private static void syncModifier(
            AttributeInstance instance,
            UUID uuid,
            String name,
            double amount,
            AttributeModifier.Operation operation
    ) {
        if (instance == null) {
            return;
        }

        AttributeModifier current =
                instance.getModifier(
                        uuid
                );

        if (amount == 0.0D) {
            if (current != null) {
                instance.removeModifier(
                        uuid
                );
            }

            return;
        }

        if (current != null
                && current.getOperation()
                == operation
                && Double.compare(
                current.getAmount(),
                amount
        ) == 0) {
            return;
        }

        if (current != null) {
            instance.removeModifier(
                    uuid
            );
        }

        instance.addTransientModifier(
                new AttributeModifier(
                        uuid,
                        name,
                        amount,
                        operation
                )
        );
    }

    @SubscribeEvent
    public static void onLogout(
            PlayerEvent.PlayerLoggedOutEvent event
    ) {
        START_FIRE_TICKS.remove(
                event.getEntity()
                        .getUUID()
        );
    }
}