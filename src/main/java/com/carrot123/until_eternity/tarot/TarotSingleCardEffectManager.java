package com.carrot123.until_eternity.tarot;

import com.carrot123.until_eternity.until_eternity;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import shiroroku.tarotcards.Registry.ItemRegistry;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

@Mod.EventBusSubscriber(
        modid = until_eternity.MODID,
        bus = Mod.EventBusSubscriber.Bus.FORGE
)
public final class TarotSingleCardEffectManager {

    private static final ResourceKey<DamageType> JUSTICE_DAMAGE =
            ResourceKey.create(
                    Registries.DAMAGE_TYPE,
                    new ResourceLocation(
                            "tarotcards",
                            "justice"
                    )
            );

    private static final UUID STRENGTH_UUID =
            UUID.nameUUIDFromBytes(
                    "until_eternity:tarot_strength"
                            .getBytes(StandardCharsets.UTF_8)
            );

    private static final UUID STAR_REACH_UUID =
            UUID.nameUUIDFromBytes(
                    "until_eternity:tarot_star_reach"
                            .getBytes(StandardCharsets.UTF_8)
            );

    private static final AttributeModifier STRENGTH_MODIFIER =
            new AttributeModifier(
                    STRENGTH_UUID,
                    "until_eternity:tarot_strength",
                    2.0D,
                    AttributeModifier.Operation.ADDITION
            );

    private static final AttributeModifier STAR_REACH_MODIFIER =
            new AttributeModifier(
                    STAR_REACH_UUID,
                    "until_eternity:tarot_star_reach",
                    0.50D,
                    AttributeModifier.Operation.MULTIPLY_BASE
            );

    private TarotSingleCardEffectManager() {
    }

    @SubscribeEvent
    public static void onPlayerTick(
            TickEvent.PlayerTickEvent event
    ) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }

        Player player = event.player;

        if (player.level().isClientSide) {
            return;
        }

        updateModifier(
                player,
                Attributes.ATTACK_DAMAGE,
                STRENGTH_MODIFIER,
                ItemRegistry.strength.get()
        );

        updateModifier(
                player,
                ForgeMod.ENTITY_REACH.get(),
                STAR_REACH_MODIFIER,
                ItemRegistry.the_star.get()
        );
    }

    @SubscribeEvent
    public static void onJusticeHurt(
            LivingHurtEvent event
    ) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }

        if (player.level().isClientSide) {
            return;
        }

        if (event.getSource().is(JUSTICE_DAMAGE)) {
            return;
        }

        if (!TarotSingleCardPresence.has(
                player,
                ItemRegistry.justice.get()
        )) {
            return;
        }

        if (!(event.getSource().getEntity()
                instanceof LivingEntity attacker)) {
            return;
        }

        if (attacker == player || !attacker.isAlive()) {
            return;
        }

        var holder =
                player.level()
                    .registryAccess()
                    .registryOrThrow(
                            Registries.DAMAGE_TYPE
                    )
                    .getHolderOrThrow(
                            JUSTICE_DAMAGE
                    );

        DamageSource source =
            new DamageSource(
                    holder,
                    player
            );

       attacker.hurt(
            source,
            20.0F
        );
    }

    private static void updateModifier(
            Player player,
            Attribute attribute,
            AttributeModifier modifier,
            Item tarot
    ) {
        AttributeInstance instance =
                player.getAttribute(attribute);

        if (instance == null) {
            return;
        }

        boolean active =
                TarotSingleCardPresence.has(
                        player,
                        tarot
                );

        AttributeModifier existing =
                instance.getModifier(
                        modifier.getId()
                );

        if (active) {
            if (existing == null) {
                instance.addTransientModifier(
                        modifier
                );
            }
        } else if (existing != null) {
            instance.removeModifier(
                    modifier.getId()
            );
        }
    }
}