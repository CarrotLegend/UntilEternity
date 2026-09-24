package com.carrot123.until_eternity.tarot;

import com.Polarice3.Goety.utils.OwnedDamageSource;
import com.carrot123.until_eternity.compat.GoetyRevelationAttributesCompat;
import com.carrot123.until_eternity.compat.PuffishAttributesCompat;
import com.carrot123.until_eternity.event.PlayerDamageAttackerResolver;
import com.carrot123.until_eternity.registry.ModAttributes;
import com.carrot123.until_eternity.registry.ModMobEffects;
import com.carrot123.until_eternity.until_eternity;
import io.redspace.ironsspellbooks.api.registry.AttributeRegistry;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.function.Supplier;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = until_eternity.MODID,
        bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class TarotSetEffectManager {
    private static final String IRON_WRIST_READY = "until_eternity:tarot_iron_wrist_ready_time";
    private static final String DEATH_REFUSAL_READY = "until_eternity:tarot_death_refusal_ready_time";
    private static final Map<UUID, AsceticState> ASCETIC_STATES = new HashMap<>();
    private static final List<ModifierSpec> MODIFIERS = List.of(
            spec("journeys_midpoint", "movement_speed", () -> Attributes.MOVEMENT_SPEED,
                    0.20D, AttributeModifier.Operation.MULTIPLY_TOTAL, player -> true),
            spec("journeys_midpoint", "sprinting_speed", () -> Attributes.MOVEMENT_SPEED,
                    0.10D, AttributeModifier.Operation.MULTIPLY_TOTAL, ServerPlayer::isSprinting),
            spec("flash_of_inspiration", "mana_regen", AttributeRegistry.MANA_REGEN::get,
                    0.20D, AttributeModifier.Operation.MULTIPLY_BASE, player -> true),
            spec("flash_of_inspiration", "cast_time_reduction",
                    AttributeRegistry.CAST_TIME_REDUCTION::get,
                    0.10D, AttributeModifier.Operation.MULTIPLY_BASE, player -> true),
            spec("flash_of_inspiration", "cooldown_reduction",
                    AttributeRegistry.COOLDOWN_REDUCTION::get,
                    0.10D, AttributeModifier.Operation.MULTIPLY_BASE, player -> true),
            spec("night_walker", "all_damage", ModAttributes.ALL_DAMAGE::get,
                    0.12D, AttributeModifier.Operation.MULTIPLY_BASE,
                    player -> !isDay(player)),
            spec("night_walker", "movement_speed", () -> Attributes.MOVEMENT_SPEED,
                    0.15D, AttributeModifier.Operation.MULTIPLY_TOTAL,
                    player -> !isDay(player)),
            spec("life_death_boundary", "life_steal",
                    () -> PuffishAttributesCompat.resolve(PuffishAttributesCompat.LIFE_STEAL),
                    0.20D, AttributeModifier.Operation.MULTIPLY_TOTAL, player -> true),
            spec("desperado", "all_damage", ModAttributes.ALL_DAMAGE::get,
                    0.25D, AttributeModifier.Operation.MULTIPLY_BASE,
                    player -> belowHealth(player, 0.30D)),
            spec("desperado", "attack_speed", () -> Attributes.ATTACK_SPEED,
                    0.10D, AttributeModifier.Operation.MULTIPLY_TOTAL,
                    player -> belowHealth(player, 0.30D)),
            spec("berserk", "attack_speed", () -> Attributes.ATTACK_SPEED,
                    0.20D, AttributeModifier.Operation.MULTIPLY_TOTAL,
                    player -> belowHealth(player, 0.50D)),
            spec("celestial", "focus_damage", ModAttributes.FOCUS_DAMAGE::get,
                    0.10D, AttributeModifier.Operation.MULTIPLY_TOTAL,
                    TarotSetEffectManager::isDay),
            spec("celestial", "spell_power", AttributeRegistry.SPELL_POWER::get,
                    0.15D, AttributeModifier.Operation.MULTIPLY_BASE,
                    TarotSetEffectManager::isDay),
            spec("celestial", "goety_cast_duration",
                    () -> GoetyRevelationAttributesCompat.resolve(
                            GoetyRevelationAttributesCompat.CAST_DURATION),
                    0.10D, AttributeModifier.Operation.ADDITION,
                    player -> !isDay(player)),
            spec("celestial", "goety_spell_cooldown",
                    () -> GoetyRevelationAttributesCompat.resolve(
                            GoetyRevelationAttributesCompat.SPELL_COOLDOWN),
                    0.10D, AttributeModifier.Operation.ADDITION,
                    player -> !isDay(player)),
            spec("celestial", "iron_cast_time_reduction",
                    AttributeRegistry.CAST_TIME_REDUCTION::get,
                    0.10D, AttributeModifier.Operation.MULTIPLY_BASE,
                    player -> !isDay(player)),
            spec("celestial", "iron_cooldown_reduction",
                    AttributeRegistry.COOLDOWN_REDUCTION::get,
                    0.10D, AttributeModifier.Operation.MULTIPLY_BASE,
                    player -> !isDay(player)),
            spec("empire", "max_health", () -> Attributes.MAX_HEALTH,
                    0.50D, AttributeModifier.Operation.MULTIPLY_TOTAL, player -> true),
            spec("empire", "resistance",
                    () -> PuffishAttributesCompat.resolve(PuffishAttributesCompat.RESISTANCE),
                    0.10D, AttributeModifier.Operation.MULTIPLY_TOTAL, player -> true)
    );

    private TarotSetEffectManager() {
    }

    public static TarotSetEffect effect(ResourceLocation setId) {
        return new TarotSetEffect() {
            @Override
            public void onActivate(ServerPlayer player) {
                activate(player, setId);
            }

            @Override
            public void onDeactivate(ServerPlayer player) {
                deactivate(player, setId);
            }

            @Override
            public void onTick(ServerPlayer player) {
                tick(player, setId);
            }
        };
    }

    public static void syncBeforeAllDamage(ServerPlayer player) {
        for (ResourceLocation setId : TarotSetManager.activeSets(player)) {
            if (setId.equals(TarotSetRegistry.id("night_walker"))
                    || setId.equals(TarotSetRegistry.id("desperado"))) {
                syncModifiers(player, setId);
            }
        }
    }

    private static ModifierSpec spec(String set, String key, Supplier<Attribute> attribute,
            double amount, AttributeModifier.Operation operation,
            Predicate<ServerPlayer> condition) {
        String salt = until_eternity.MODID + ":tarot/" + set + "/" + key;
        return new ModifierSpec(new ResourceLocation(until_eternity.MODID, set),
                salt, UUID.nameUUIDFromBytes(salt.getBytes(StandardCharsets.UTF_8)),
                attribute, amount, operation, condition);
    }

    private static void activate(ServerPlayer player, ResourceLocation setId) {
        syncModifiers(player, setId);
        if (setId.equals(TarotSetRegistry.id("ascetic"))) {
            ASCETIC_STATES.put(player.getUUID(), new AsceticState(gameTime(player) + 100L));
        }
        if (setId.equals(TarotSetRegistry.id("foresight"))) {
            TarotSetManager.startForesight(player);
        }
        if (setId.equals(TarotSetRegistry.id("hero"))) {
            refreshHero(player);
        }
    }

    private static void deactivate(ServerPlayer player, ResourceLocation setId) {
        for (ModifierSpec spec : MODIFIERS) {
            if (spec.setId().equals(setId)) {
                Attribute attribute = spec.attribute().get();
                if (attribute == null) {
                    continue;
                }
                AttributeInstance instance = player.getAttribute(attribute);
                if (instance != null) {
                    instance.removeModifier(spec.uuid());
                }
            }
        }
        if (setId.equals(TarotSetRegistry.id("empire"))) {
            player.setHealth(Math.min(player.getHealth(), player.getMaxHealth()));
        }
        if (setId.equals(TarotSetRegistry.id("ascetic"))) {
            ASCETIC_STATES.remove(player.getUUID());
        }
        if (setId.equals(TarotSetRegistry.id("foresight"))) {
            TarotSetManager.stopForesight(player);
        }
    }

    private static void tick(ServerPlayer player, ResourceLocation setId) {
        syncModifiers(player, setId);
        if (setId.equals(TarotSetRegistry.id("hero")) && player.tickCount % 20 == 0) {
            refreshHero(player);
        }
        if (setId.equals(TarotSetRegistry.id("ascetic"))) {
            AsceticState state = ASCETIC_STATES.computeIfAbsent(player.getUUID(),
                    ignored -> new AsceticState(gameTime(player) + 100L));
            long now = gameTime(player);
            while (state.stacks < 10 && now >= state.nextStackTime) {
                state.stacks++;
                state.nextStackTime += 100L;
            }
        }
        if (setId.equals(TarotSetRegistry.id("foresight"))) {
            TarotSetManager.tickForesight(player);
        }
    }

    private static void syncModifiers(ServerPlayer player, ResourceLocation setId) {
        for (ModifierSpec spec : MODIFIERS) {
            if (!spec.setId().equals(setId)) {
                continue;
            }
            Attribute attribute = spec.attribute().get();
            if (attribute == null) {
                continue;
            }
            AttributeInstance instance = player.getAttribute(attribute);
            if (instance == null) {
                continue;
            }
            AttributeModifier current = instance.getModifier(spec.uuid());
            if (!spec.condition().test(player)) {
                if (current != null) {
                    instance.removeModifier(spec.uuid());
                }
                continue;
            }
            if (current != null && Double.compare(current.getAmount(), spec.amount()) == 0
                    && current.getOperation() == spec.operation()) {
                continue;
            }
            if (current != null) {
                instance.removeModifier(spec.uuid());
            }
            instance.addTransientModifier(new AttributeModifier(spec.uuid(), spec.name(),
                    spec.amount(), spec.operation()));
        }
    }

    private static void refreshHero(ServerPlayer player) {
        MobEffectInstance current = player.getEffect(MobEffects.DAMAGE_BOOST);
        if (current == null || current.getAmplifier() < 3
                || current.getAmplifier() == 3 && current.getDuration() <= 20) {
            player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 40, 3,
                    false, false, true));
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onLivingDamage(LivingDamageEvent event) {
        float amount = event.getAmount();
        if (event.getEntity().level().isClientSide || !(amount > 0.0F)
                || !Float.isFinite(amount)) {
            return;
        }
        ServerPlayer attacker = PlayerDamageAttackerResolver.resolve(event.getSource());
        if (attacker != null && attacker != event.getEntity()) {
            if (hasSet(attacker, "apocalypse")) {
                event.getEntity().addEffect(new MobEffectInstance(
                        ModMobEffects.CALAMITY.get(), 100, 0,
                        false, true, false), attacker);
            }
            if (hasSet(attacker, "burning_desire")
                    && (event.getEntity() instanceof Enemy
                    || event.getEntity() instanceof Mob mob && mob.getTarget() == attacker)) {
                amount = safeMultiply(amount, 1.20D);
            }
            if (hasSet(attacker, "ascetic")) {
                int stacks = consumeAscetic(attacker);
                amount = safeMultiply(amount, 1.0D + stacks * 0.10D);
            }
        }
        if (event.getEntity().hasEffect(ModMobEffects.CALAMITY.get())) {
            amount = safeMultiply(amount, 1.40D);
        }
        event.setAmount(amount);
        if (event.getEntity() instanceof ServerPlayer victim) {
            if (hasSet(victim, "iron_wrist") && hasExternalAttacker(event.getSource(), victim)) {
                triggerIronWrist(victim);
            }
            if (hasSet(victim, "misfortune")) {
                triggerMisfortune(victim);
            }
            float incoming = event.getAmount();
            if (!event.isCanceled() && incoming > 0.0F && Float.isFinite(incoming)
                    && TarotSetManager.isForesightReady(victim)) {
                event.setAmount(incoming * 0.5F);
                if (event.getAmount() < incoming) {
                    TarotSetManager.consumeForesight(victim);
                }
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onLivingDeath(LivingDeathEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)
                || !hasSet(player, "death_refusal")) {
            return;
        }
        CompoundTag data = player.getPersistentData();
        long now = gameTime(player);
        if (now < data.getLong(DEATH_REFUSAL_READY)) {
            return;
        }
        data.putLong(DEATH_REFUSAL_READY, now + 3600L);
        player.setHealth(1.0F);
        event.setCanceled(true);
    }

    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        CompoundTag oldData = event.getOriginal().getPersistentData();
        CompoundTag newData = player.getPersistentData();
        for (String key : List.of(IRON_WRIST_READY, DEATH_REFUSAL_READY)) {
            if (oldData.contains(key, Tag.TAG_LONG)) {
                newData.putLong(key, oldData.getLong(key));
            }
        }
    }

    private static boolean hasSet(ServerPlayer player, String path) {
        return TarotSetManager.activeSets(player).contains(TarotSetRegistry.id(path));
    }

    private static int consumeAscetic(ServerPlayer player) {
        AsceticState state = ASCETIC_STATES.computeIfAbsent(player.getUUID(),
                ignored -> new AsceticState(gameTime(player) + 100L));
        int stacks = state.stacks;
        state.stacks = 0;
        state.lastAttackTime = gameTime(player);
        state.nextStackTime = state.lastAttackTime + 100L;
        return stacks;
    }

    private static void triggerIronWrist(ServerPlayer player) {
        CompoundTag data = player.getPersistentData();
        long now = gameTime(player);
        if (now < data.getLong(IRON_WRIST_READY)) {
            return;
        }
        data.putLong(IRON_WRIST_READY, now + 200L);
        player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE,
                100, 3, false, true, true));
    }

    private static void triggerMisfortune(ServerPlayer player) {
        MobEffect effect;
        int amplifier;
        switch (player.getRandom().nextInt(4)) {
            case 0 -> {
                effect = MobEffects.REGENERATION;
                amplifier = 3;
            }
            case 1 -> {
                effect = MobEffects.MOVEMENT_SPEED;
                amplifier = 1;
            }
            case 2 -> {
                effect = MobEffects.DAMAGE_BOOST;
                amplifier = 4;
            }
            default -> {
                effect = MobEffects.WEAKNESS;
                amplifier = 1;
            }
        }
        player.addEffect(new MobEffectInstance(effect, 100, amplifier));
    }

    private static boolean hasExternalAttacker(DamageSource source, ServerPlayer victim) {
        var visited = Collections.newSetFromMap(new IdentityHashMap<Entity, Boolean>());
        if (source instanceof OwnedDamageSource owned) {
            Entity owner = resolveAttacker(owned.getOwner(), visited);
            if (owner != null) {
                return owner != victim;
            }
        }
        Entity attacker = resolveAttacker(source.getEntity(), visited);
        if (attacker == null) {
            attacker = resolveAttacker(source.getDirectEntity(), visited);
        }
        return attacker != null && attacker != victim;
    }

    private static Entity resolveAttacker(Entity entity, Set<Entity> visited) {
        if (entity == null || !visited.add(entity)) {
            return null;
        }
        if (entity instanceof Projectile projectile) {
            return resolveAttacker(projectile.getOwner(), visited);
        }
        if (entity instanceof OwnableEntity ownable) {
            return resolveAttacker(ownable.getOwner(), visited);
        }
        return entity;
    }

    private static boolean belowHealth(ServerPlayer player, double fraction) {
        return player.getMaxHealth() > 0.0F
                && player.getHealth() < player.getMaxHealth() * fraction;
    }

    private static boolean isDay(ServerPlayer player) {
        return Math.floorMod(player.level().getDayTime(), 24000L) < 12000L;
    }

    private static long gameTime(ServerPlayer player) {
        return player.serverLevel().getServer().overworld().getGameTime();
    }

    private static float safeMultiply(float amount, double multiplier) {
        double result = amount * multiplier;
        return Double.isFinite(result) ? (float) Math.min(result, Float.MAX_VALUE) : amount;
    }

    private record ModifierSpec(ResourceLocation setId, String name, UUID uuid,
            Supplier<Attribute> attribute, double amount, AttributeModifier.Operation operation,
            Predicate<ServerPlayer> condition) {
    }

    private static final class AsceticState {
        private int stacks;
        private long nextStackTime;
        private long lastAttackTime;

        private AsceticState(long nextStackTime) {
            this.nextStackTime = nextStackTime;
        }
    }
}
