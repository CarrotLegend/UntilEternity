package com.carrot123.until_eternity.item;

import com.carrot123.until_eternity.until_eternity;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.loot.LootDataId;
import net.minecraft.world.level.storage.loot.LootDataType;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public final class RandomLootGeneratorItem extends Item {
    public RandomLootGeneratorItem() {
        super(new Item.Properties().stacksTo(1));
    }

    @Override
    public void inventoryTick(
            @NotNull ItemStack stack,
            @NotNull Level level,
            @NotNull Entity entity,
            int slot,
            boolean selected
    ) {
        super.inventoryTick(stack, level, entity, slot, selected);
        if (!level.isClientSide && entity instanceof Player player
                && ensureLootTableAssigned(stack, player.getRandom())) {
            player.getInventory().setChanged();
        }
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(
            @NotNull Level level,
            @NotNull Player player,
            @NotNull InteractionHand hand
    ) {
        ItemStack generator = player.getItemInHand(hand);
        if (level.isClientSide) {
            return InteractionResultHolder.success(generator);
        }
        if (!(level instanceof ServerLevel serverLevel)) {
            return InteractionResultHolder.fail(generator);
        }

        if (ensureLootTableAssigned(generator, player.getRandom())) {
            player.getInventory().setChanged();
        }

        String storedId = generator.getOrCreateTag().getString(RandomLootGeneratorData.TAG_LOOT_TABLE);
        ResourceLocation lootTableId = RandomLootGeneratorData.parseLootTableId(storedId);
        if (lootTableId == null) {
            return fail(player, generator, storedId, null);
        }

        LootTable lootTable = serverLevel.getServer().getLootData().getElement(
                new LootDataId<>(LootDataType.TABLE, lootTableId)
        );
        if (lootTable == null) {
            return fail(player, generator, storedId, null);
        }

        final List<ItemStack> rewards;
        try {
            LootParams params = new LootParams.Builder(serverLevel)
                    .withParameter(LootContextParams.ORIGIN, player.position())
                    .withOptionalParameter(LootContextParams.THIS_ENTITY, player)
                    .withLuck(player.getLuck())
                    .create(LootContextParamSets.CHEST);
            rewards = lootTable.getRandomItems(params);
        } catch (RuntimeException exception) {
            return fail(player, generator, storedId, exception);
        }

        if (!player.isCreative()) {
            generator.shrink(1);
        }

        for (ItemStack reward : rewards) {
            if (!reward.isEmpty()) {
                player.getInventory().add(reward);
                if (!reward.isEmpty()) {
                    player.drop(reward, false);
                }
            }
        }

        player.awardStat(Stats.ITEM_USED.get(this));
        BlockPos position = player.blockPosition();
        serverLevel.playSound(
                null,
                position,
                SoundEvents.CHEST_OPEN,
                SoundSource.PLAYERS,
                1.0F,
                1.0F
        );
        return InteractionResultHolder.consume(generator);
    }

    @Override
    public void appendHoverText(
            @NotNull ItemStack stack,
            @Nullable Level level,
            @NotNull List<Component> tooltip,
            @NotNull TooltipFlag flag
    ) {
        tooltip.add(Component.translatable("tooltip.until_eternity.random_loot_generator")
                .withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
        super.appendHoverText(stack, level, tooltip, flag);
    }

    private static boolean ensureLootTableAssigned(ItemStack stack, RandomSource random) {
        return RandomLootGeneratorData.assignLootTableIfMissing(stack.getOrCreateTag(), random);
    }

    private InteractionResultHolder<ItemStack> fail(
            Player player,
            ItemStack generator,
            String storedId,
            @Nullable RuntimeException exception
    ) {
        if (exception == null) {
            until_eternity.LOGGER.warn(
                    "Player {} ({}) tried to open a random loot generator with invalid loot table '{}'",
                    player.getGameProfile().getName(),
                    player.getUUID(),
                    storedId
            );
        } else {
            until_eternity.LOGGER.warn(
                    "Failed to generate loot table '{}' for player {} ({})",
                    storedId,
                    player.getGameProfile().getName(),
                    player.getUUID(),
                    exception
            );
        }
        player.displayClientMessage(
                Component.translatable("message.until_eternity.random_loot_generator.failed"),
                true
        );
        return InteractionResultHolder.fail(generator);
    }
}
