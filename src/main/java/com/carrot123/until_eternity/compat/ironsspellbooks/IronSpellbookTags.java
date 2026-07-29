package com.carrot123.until_eternity.compat.ironsspellbooks;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

public final class IronSpellbookTags {
    public static final TagKey<Item> STAFFS = ItemTags.create(
            new ResourceLocation("irons_spellbooks", "staff"));

    private IronSpellbookTags() {
    }
}
