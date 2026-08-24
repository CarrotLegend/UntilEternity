package com.carrot123.until_eternity.item;

import net.minecraft.nbt.CompoundTag;

final class UnbreakableStackData {
    private UnbreakableStackData() { }

    static void apply(CompoundTag tag) {
        tag.putBoolean("Unbreakable", true);
    }
}
