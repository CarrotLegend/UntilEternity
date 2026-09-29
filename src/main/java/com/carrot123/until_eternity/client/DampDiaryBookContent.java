package com.carrot123.until_eternity.client;

import com.carrot123.until_eternity.item.lore.ReadableWrittenBookFactory;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public final class DampDiaryBookContent {
    private static final String ROOT = "book.until_eternity.damp_diary.";
    private static final int TEXT_WIDTH = 114;
    private static final int MAX_LINES = 14;

    private DampDiaryBookContent() {
    }

    public static ItemStack createReadableBook() {
        Font font = Minecraft.getInstance().font;
        ListTag pages = new ListTag();
        addEntry(pages, font, 1, 6);
        addEntry(pages, font, 2, 15);
        return ReadableWrittenBookFactory.create(
                Component.translatable("item.until_eternity.damp_diary").getString(),
                Component.translatable(ROOT + "author").getString(),
                pages
        );
    }

    private static void addEntry(ListTag pages, Font font, int entry, int paragraphCount) {
        MutableComponent current = Component.translatable(ROOT + "date." + entry)
                .withStyle(ChatFormatting.BOLD);
        for (int paragraphIndex = 1; paragraphIndex <= paragraphCount; paragraphIndex++) {
            MutableComponent paragraph = Component.translatable(
                    ROOT + "entry." + entry + "." + paragraphIndex
            );
            if (entry == 2 && paragraphIndex >= 14) {
                paragraph.withStyle(ChatFormatting.BOLD);
            }
            MutableComponent candidate = current.copy()
                    .append(Component.literal("\n\n"))
                    .append(paragraph);
            if (font.split(candidate, TEXT_WIDTH).size() > MAX_LINES) {
                pages.add(StringTag.valueOf(Component.Serializer.toJson(current)));
                current = paragraph;
            } else {
                current = candidate;
            }
        }
        pages.add(StringTag.valueOf(Component.Serializer.toJson(current)));
    }
}
