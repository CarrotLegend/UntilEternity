package com.carrot123.until_eternity.client.tooltip;

import com.carrot123.until_eternity.registry.ModAttributes;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

class FocusDamageTooltipFormatterTest {
    @ParameterizedTest
    @CsvSource({
            "0.10, 10%",
            "0.13, 13%",
            "0.15, 15%",
            "0.25, 25%",
            "0.66, 66%"
    })
    void equippedFocusBonusesUseExactPercentages(
            String input, String expected) {
        Component line = Component.translatable(
                "attribute.modifier.plus.0",
                input,
                Component.translatable(
                        ModAttributes.FOCUS_DAMAGE_DESCRIPTION_ID));

        TranslatableContents contents = (TranslatableContents)
                FocusDamageTooltipFormatter.format(line).getContents();

        assertEquals(expected, contents.getArgs()[0]);
    }

    @Test
    void additionUsesPercentageAndPreservesStyle() {
        Component line = Component.translatable(
                "attribute.modifier.plus.0",
                "0.2",
                Component.translatable(
                        ModAttributes.FOCUS_DAMAGE_DESCRIPTION_ID))
                .withStyle(ChatFormatting.BLUE);

        Component formatted = FocusDamageTooltipFormatter.format(line);
        TranslatableContents contents =
                (TranslatableContents) formatted.getContents();

        assertEquals("20%", contents.getArgs()[0]);
        assertEquals(line.getStyle(), formatted.getStyle());
        assertEquals("attribute.modifier.plus.0", contents.getKey());
    }

    @Test
    void unrelatedAttributesAndMultiplyOperationsAreUntouched() {
        Component unrelated = Component.translatable(
                "attribute.modifier.plus.0",
                "0.2",
                Component.translatable(
                        "attribute.name.generic.attack_damage"));
        Component multiply = Component.translatable(
                "attribute.modifier.plus.1",
                "20",
                Component.translatable(
                        ModAttributes.FOCUS_DAMAGE_DESCRIPTION_ID));

        assertSame(unrelated,
                FocusDamageTooltipFormatter.format(unrelated));
        assertSame(multiply,
                FocusDamageTooltipFormatter.format(multiply));
    }
}
