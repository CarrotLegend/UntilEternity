package com.carrot123.until_eternity.util;

import java.util.List;
import javax.annotation.Nullable;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

public final class CurioTooltipHelper {

    private CurioTooltipHelper() {
    }

    public static void addBlank(List<Component> tooltip) {
        tooltip.add(Component.empty());
    }

    public static void addLocalizedString(
            List<Component> tooltip,
            String translationKey
    ) {
        tooltip.add(Component.translatable(translationKey));
    }

    public static void addLocalizedString(
            List<Component> tooltip,
            String translationKey,
            @Nullable ChatFormatting valueFormatting,
            Object... values
    ) {
        Component[] components = new Component[values.length];

        for (int i = 0; i < values.length; i++) {
            Object value = values[i];

            MutableComponent component;

            if (value instanceof MutableComponent mutable) {
                component = mutable.copy();
            } else if (value instanceof Component componentValue) {
                component = componentValue.copy();
            } else {
                component = Component.literal(String.valueOf(value));
            }

            if (valueFormatting != null) {
                component.withStyle(valueFormatting);
            }

            components[i] = component;
        }

        tooltip.add(
                Component.translatable(
                        translationKey,
                        (Object[]) components
                )
        );
    }

    public static void addLocalizedString(
            List<Component> tooltip,
            String translationKey,
            Object... values
    ) {
        addLocalizedString(
                tooltip,
                translationKey,
                ChatFormatting.GOLD,
                values
        );
    }

    public static List<Component> clearCuriosAttributes(
            List<Component> tooltips
    ) {
        tooltips.clear();
        return tooltips;
    }
}