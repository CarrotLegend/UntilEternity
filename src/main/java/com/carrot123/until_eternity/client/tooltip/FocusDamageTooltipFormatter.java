package com.carrot123.until_eternity.client.tooltip;

import com.carrot123.until_eternity.registry.ModAttributes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.contents.TranslatableContents;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;

public final class FocusDamageTooltipFormatter {
    private static final DecimalFormat PERCENT_FORMAT =
            new DecimalFormat("#.##");

    private FocusDamageTooltipFormatter() {
    }

    public static Component format(Component line) {
        if (!(line.getContents() instanceof TranslatableContents contents)
                || !isAdditionKey(contents.getKey())) {
            return line;
        }
        Object[] arguments = contents.getArgs();
        if (arguments.length < 2 || !isFocusDamageName(arguments[1])) {
            return line;
        }

        Double amount = readAmount(arguments[0]);
        if (amount == null || !Double.isFinite(amount)) {
            return line;
        }
        Object[] converted = arguments.clone();
        converted[0] = PERCENT_FORMAT.format(
                amount * 100.0D) + "%";

        MutableComponent result = MutableComponent.create(
                new TranslatableContents(
                        contents.getKey(),
                        contents.getFallback(),
                        converted)).setStyle(line.getStyle());
        line.getSiblings().forEach(sibling -> result.append(sibling.copy()));
        return result;
    }

    private static boolean isAdditionKey(String key) {
        return "attribute.modifier.plus.0".equals(key)
                || "attribute.modifier.take.0".equals(key)
                || "attribute.modifier.equals.0".equals(key);
    }

    private static boolean isFocusDamageName(Object argument) {
        return argument instanceof Component component
                && component.getContents() instanceof TranslatableContents name
                && ModAttributes.FOCUS_DAMAGE_DESCRIPTION_ID.equals(
                        name.getKey());
    }

    private static Double readAmount(Object argument) {
        if (argument instanceof Number number) {
            return number.doubleValue();
        }
        if (!(argument instanceof String text)) {
            return null;
        }
        try {
            NumberFormat format = NumberFormat.getNumberInstance();
            return format.parse(text).doubleValue();
        } catch (ParseException ignored) {
            return null;
        }
    }
}
