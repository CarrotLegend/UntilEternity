package com.carrot123.until_eternity.compat.ironsspellbooks;

import com.carrot123.until_eternity.compat.ScopedValueStack;
import io.redspace.ironsspellbooks.api.spells.CastSource;
import net.minecraft.world.item.ItemStack;

import java.util.function.Supplier;

public final class IronCastingContext {
    private static final CastingContext EMPTY = new CastingContext(
            ItemStack.EMPTY,
            CastSource.NONE,
            "");
    private static final ScopedValueStack<CastingContext> CONTEXTS =
            new ScopedValueStack<>();

    private IronCastingContext() {
    }

    public static <T> T withCastingContext(
            ItemStack castingStack,
            CastSource castSource,
            String equipmentSlot,
            Supplier<T> action
    ) {
        return CONTEXTS.withValue(
                new CastingContext(
                        castingStack == null ? ItemStack.EMPTY : castingStack,
                        castSource == null ? CastSource.NONE : castSource,
                        equipmentSlot == null ? "" : equipmentSlot),
                action);
    }

    public static ItemStack currentStack() {
        return CONTEXTS.current(EMPTY).stack();
    }

    public static CastSource currentSource() {
        return CONTEXTS.current(EMPTY).source();
    }

    public static String currentEquipmentSlot() {
        return CONTEXTS.current(EMPTY).equipmentSlot();
    }

    public static ItemStack preferMagicDataStack(ItemStack magicDataStack) {
        if (magicDataStack != null && !magicDataStack.isEmpty()) {
            return magicDataStack;
        }
        return currentStack();
    }

    private record CastingContext(
            ItemStack stack,
            CastSource source,
            String equipmentSlot
    ) {
    }
}
