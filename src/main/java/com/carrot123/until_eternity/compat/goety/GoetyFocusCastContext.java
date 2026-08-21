package com.carrot123.until_eternity.compat.goety;

import com.Polarice3.Goety.api.magic.ISpell;
import com.carrot123.until_eternity.compat.ScopedValueStack;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;

import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;

public final class GoetyFocusCastContext {
    private static final CastContext EMPTY = new CastContext(null, "");
    private static final ScopedValueStack<CastContext> CONTEXTS =
            new ScopedValueStack<>();

    private GoetyFocusCastContext() {
    }

    public static <T> T withPlayerCast(
            LivingEntity caster,
            ISpell spell,
            Supplier<T> action
    ) {
        if (!(caster instanceof ServerPlayer player) || spell == null) {
            return action.get();
        }
        return CONTEXTS.withValue(new CastContext(
                player.getUUID(), spell.getClass().getName()), action);
    }

    public static <T> T withTrackedCaster(
            UUID casterUuid,
            Supplier<T> action
    ) {
        if (casterUuid == null) {
            return action.get();
        }
        return CONTEXTS.withValue(
                new CastContext(casterUuid, "tracked_entity"), action);
    }

    public static Optional<UUID> currentCasterUuid() {
        return Optional.ofNullable(CONTEXTS.current(EMPTY).casterUuid());
    }

    public static String currentSpellClass() {
        return CONTEXTS.current(EMPTY).spellClass();
    }

    private record CastContext(UUID casterUuid, String spellClass) {
    }
}
