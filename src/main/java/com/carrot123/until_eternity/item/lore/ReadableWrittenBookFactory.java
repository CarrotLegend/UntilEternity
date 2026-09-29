package com.carrot123.until_eternity.item.lore;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public final class ReadableWrittenBookFactory {
    private ReadableWrittenBookFactory() {
    }

    public static ItemStack create(String title, String author, ListTag pages) {
        ItemStack book = new ItemStack(Items.WRITTEN_BOOK);
        CompoundTag tag = new CompoundTag();
        tag.putString("title", title);
        tag.putString("author", author);
        tag.putInt("generation", 0);
        tag.putBoolean("resolved", true);
        tag.put("pages", pages);
        book.setTag(tag);
        return book;
    }
}
