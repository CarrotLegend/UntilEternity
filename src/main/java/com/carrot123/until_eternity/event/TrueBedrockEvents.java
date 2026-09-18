package com.carrot123.until_eternity.event;

import com.carrot123.until_eternity.item.ModItems;
import com.carrot123.until_eternity.network.ModNetworking;
import com.carrot123.until_eternity.network.TrueBedrockActivationS2CPacket;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;

import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.MobEffectEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;

import top.theillusivec4.curios.api.CuriosApi;

@Mod.EventBusSubscriber(
        modid = "until_eternity",
        bus = Mod.EventBusSubscriber.Bus.FORGE
)
public final class TrueBedrockEvents {

    private static final int REVIVE_COOLDOWN_TICKS = 40;

    private static final TagKey<DamageType> FORGE_MAGIC_DAMAGE =
            TagKey.create(
                    Registries.DAMAGE_TYPE,
                    new ResourceLocation(
                            "forge",
                            "is_magic"
                    )
            );

    private TrueBedrockEvents() {
    }

    @SubscribeEvent(
            priority = EventPriority.HIGHEST
    )
    public static void onLivingAttack(
            LivingAttackEvent event
    ) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }

        if (!hasTrueBedrock(player)) {
            return;
        }

        DamageSource source =
                event.getSource();

        if (isImmuneDamage(source)) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent(
            priority = EventPriority.HIGHEST
    )
    public static void onLivingDamage(
            LivingDamageEvent event
    ) {

        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }

        if (!hasTrueBedrock(player)) {
            return;
        }

        if (player.getCooldowns().isOnCooldown(
                ModItems.TRUE_BEDROCK.get()
        )) {
            return;
        }

        float damage =
                event.getAmount();

        float currentHealth =
                player.getHealth();

        if (damage < currentHealth) {
            return;
        }

        event.setCanceled(true);

        player.setHealth(
                player.getMaxHealth()
        );

        player.getCooldowns().addCooldown(
                ModItems.TRUE_BEDROCK.get(),
                REVIVE_COOLDOWN_TICKS
        );


        ServerLevel level =
                player.serverLevel();

        level.sendParticles(
                ParticleTypes.TOTEM_OF_UNDYING,
                player.getX(),
                player.getY()
                        + player.getBbHeight() * 0.5D,
                player.getZ(),
                30,
                player.getBbWidth() * 0.5D,
                player.getBbHeight() * 0.5D,
                player.getBbWidth() * 0.5D,
                0.2D
        );

        level.playSound(
                null,
                player.blockPosition(),
                SoundEvents.TOTEM_USE,
                player.getSoundSource(),
                1.0F,
                1.0F
        );

        ModNetworking.CHANNEL.send(
                PacketDistributor.PLAYER.with(
                        () -> player
                ),
                new TrueBedrockActivationS2CPacket()
        );
    }

    @SubscribeEvent(
            priority = EventPriority.HIGHEST
    )
    public static void onEffectApplicable(
            MobEffectEvent.Applicable event
    ) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }

        if (!hasTrueBedrock(player)) {
            return;
        }

        if (event.getEffectInstance() == null) {
            return;
        }

        MobEffect effect =
                event.getEffectInstance()
                        .getEffect();

        if (effect == MobEffects.SLOW_FALLING) {
            event.setResult(
                    Event.Result.DENY
            );
            return;
        }

        if (effect == MobEffects.LEVITATION) {
            event.setResult(
                    Event.Result.DENY
            );
        }
    }

    private static boolean hasTrueBedrock(
            Player player
    ) {
        return CuriosApi
                .getCuriosInventory(player)
                .resolve()
                .flatMap(
                        curios ->
                                curios.findFirstCurio(
                                        ModItems.TRUE_BEDROCK.get()
                                )
                )
                .isPresent();
    }
    private static boolean isImmuneDamage(
            DamageSource source
    ) {
        if (source.is(
                DamageTypeTags.IS_FIRE
        )) {
            return true;
        }
        if (source.is(
                DamageTypeTags.IS_EXPLOSION
        )) {
            return true;
        }
        if (source.is(
                DamageTypeTags.IS_FALL
        )) {
            return true;
        }
        if (source.is(
                DamageTypes.IN_WALL
        )) {
            return true;
        }
        if (source.is(
                DamageTypes.DROWN
        )) {
            return true;
        }
        if (source.is(
                DamageTypes.MAGIC
        )) {
            return true;
        }
        if (source.is(
                DamageTypes.INDIRECT_MAGIC
        )) {
            return true;
        }
        if (source.is(
                DamageTypes.DRAGON_BREATH
        )) {
            return true;
        }
        return source.is(
                FORGE_MAGIC_DAMAGE
        );
    }
}