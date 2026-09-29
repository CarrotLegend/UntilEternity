package com.carrot123.until_eternity.loot;

import com.carrot123.until_eternity.item.ModItems;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraftforge.common.loot.IGlobalLootModifier;
import net.minecraftforge.common.loot.LootModifier;
import org.jetbrains.annotations.NotNull;

public final class MinerLogLootModifier extends LootModifier {
    public static final Codec<MinerLogLootModifier> CODEC =
            RecordCodecBuilder.create(instance ->
                    codecStart(instance).apply(instance, MinerLogLootModifier::new));

    public MinerLogLootModifier(LootItemCondition[] conditions) {
        super(conditions);
    }

    @Override
    protected @NotNull ObjectArrayList<ItemStack> doApply(
            ObjectArrayList<ItemStack> generatedLoot,
            LootContext context
    ) {
        generatedLoot.add(new ItemStack(ModItems.MINER_LOG.get()));
        return generatedLoot;
    }

    @Override
    public Codec<? extends IGlobalLootModifier> codec() {
        return ModLootModifiers.MINER_LOG_LOOT.get();
    }
}
