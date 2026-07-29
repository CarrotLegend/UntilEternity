package com.carrot123.until_eternity.loot;

import com.carrot123.until_eternity.Config;
import com.carrot123.until_eternity.compat.ironsspellbooks.IronSpellbookTags;
import com.carrot123.until_eternity.enchantment.ModEnchantments;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.EnchantedBookItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraftforge.common.loot.IGlobalLootModifier;
import net.minecraftforge.common.loot.LootModifier;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

public final class PlenitudeLootModifier extends LootModifier {
    public static final Codec<PlenitudeLootModifier> CODEC =
            RecordCodecBuilder.create(instance ->
                    codecStart(instance).apply(
                            instance,
                            PlenitudeLootModifier::new));

    private static final Logger LOGGER = LogUtils.getLogger();
    private static boolean warnedInvalidOutput;

    public PlenitudeLootModifier(LootItemCondition[] conditions) {
        super(conditions);
    }

    @Override
    protected @NotNull ObjectArrayList<ItemStack> doApply(
            ObjectArrayList<ItemStack> generatedLoot,
            LootContext context
    ) {
        stripPlenitude(generatedLoot);

        ResourceLocation tableId = context.getQueriedLootTableId();
        if (Config.plenitudeLootTables == null
                || !tableId.getPath().startsWith("chests/")
                || !Config.plenitudeLootTables.contains(tableId)
                || context.getRandom().nextDouble()
                        >= Config.plenitudeLootChance) {
            return generatedLoot;
        }

        Item outputItem = ForgeRegistries.ITEMS.getValue(
                Config.plenitudeLootOutputItem);
        if (outputItem == null) {
            warnInvalidOutput(Config.plenitudeLootOutputItem);
            return generatedLoot;
        }

        int minimum = Math.max(1, Math.min(3, Config.plenitudeLootMinLevel));
        int maximum = Math.max(
                minimum,
                Math.min(3, Config.plenitudeLootMaxLevel));
        int level = minimum + context.getRandom().nextInt(
                maximum - minimum + 1);
        ItemStack generated = new ItemStack(outputItem);

        if (generated.is(Items.ENCHANTED_BOOK)) {
            EnchantedBookItem.addEnchantment(
                    generated,
                    new EnchantmentInstance(
                            ModEnchantments.PLENITUDE.get(),
                            level));
        } else if (generated.is(IronSpellbookTags.STAFFS)) {
            generated.enchant(ModEnchantments.PLENITUDE.get(), level);
        } else {
            warnInvalidOutput(Config.plenitudeLootOutputItem);
            return generatedLoot;
        }

        generatedLoot.add(generated);
        return generatedLoot;
    }

    @Override
    public Codec<? extends IGlobalLootModifier> codec() {
        return ModLootModifiers.PLENITUDE_LOOT.get();
    }

    static void stripPlenitude(ObjectArrayList<ItemStack> generatedLoot) {
        ResourceLocation enchantmentId = ForgeRegistries.ENCHANTMENTS.getKey(
                ModEnchantments.PLENITUDE.get());
        if (enchantmentId == null) {
            return;
        }
        String targetId = enchantmentId.toString();
        for (ItemStack stack : generatedLoot) {
            CompoundTag root = stack.getTag();
            if (root == null) {
                continue;
            }
            removeFromList(root, "Enchantments", targetId);
            removeFromList(root, "StoredEnchantments", targetId);
        }
    }

    private static void removeFromList(
            CompoundTag root,
            String key,
            String targetId
    ) {
        if (!root.contains(key, Tag.TAG_LIST)) {
            return;
        }
        ListTag enchantments = root.getList(key, Tag.TAG_COMPOUND);
        enchantments.removeIf(tag ->
                tag instanceof CompoundTag entry
                        && targetId.equals(entry.getString("id")));
        if (enchantments.isEmpty()) {
            root.remove(key);
        } else {
            root.put(key, enchantments);
        }
    }

    private static void warnInvalidOutput(ResourceLocation outputId) {
        if (!warnedInvalidOutput) {
            warnedInvalidOutput = true;
            LOGGER.warn(
                    "Plenitude loot output {} is neither an enchanted book "
                            + "nor an irons_spellbooks:staff item; generation "
                            + "will be skipped",
                    outputId);
        }
    }
}
