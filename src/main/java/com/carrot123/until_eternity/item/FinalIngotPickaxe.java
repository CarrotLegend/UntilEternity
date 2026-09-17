package com.carrot123.until_eternity.item;

import com.carrot123.until_eternity.compat.SummoningRitualsCompat;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.PickaxeItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.registries.ForgeRegistries;

@SuppressWarnings("null")
public class FinalIngotPickaxe extends PickaxeItem {
    private static final int AREA_BREAK_COOLDOWN = 10;
    private static final float FINAL_PICKAXE_SPEED_MULTIPLIER = 12.0F;

    private static final ResourceLocation EROSION_DEEPSLATE_BRICKS_ID =
            new ResourceLocation(
                    "eeeabsmobs",
                    "erosion_deepslate_bricks"
            );

    public FinalIngotPickaxe(
            Tier tier,
            int attackDamageModifier,
            float attackSpeedModifier,
            Properties properties
    ) {
        super(
                tier,
                attackDamageModifier,
                attackSpeedModifier,
                properties
        );
    }

    @Override
    public InteractionResult useOn(@Nonnull UseOnContext context) {
        Player player = context.getPlayer();

        if (player == null) {
            return super.useOn(context);
        }

        if (!player.isShiftKeyDown()) {
            return super.useOn(context);
        }

        if (player.getCooldowns().isOnCooldown(this)) {
            return InteractionResult.FAIL;
        }

        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();

        BlockState state = level.getBlockState(pos);

        if (state.isAir()) {
            return InteractionResult.PASS;
        }

        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }

        boolean destroyed = tryBreakArea(
                level,
                pos,
                context.getClickedFace(),
                player
        );

        return destroyed
                ? InteractionResult.SUCCESS
                : InteractionResult.FAIL;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(
            @Nonnull Level level,
            @Nonnull Player player,
            @Nonnull InteractionHand hand
    ) {
        ItemStack stack = player.getItemInHand(hand);

        if (!player.isShiftKeyDown()) {
            return InteractionResultHolder.pass(stack);
        }

        if (player.getCooldowns().isOnCooldown(this)) {
            return InteractionResultHolder.fail(stack);
        }

        HitResult hit = player.pick(
                5.0D,
                0.0F,
                false
        );

        if (hit.getType() != HitResult.Type.BLOCK) {
            return InteractionResultHolder.pass(stack);
        }

        BlockHitResult blockHit = (BlockHitResult) hit;

        BlockPos pos = blockHit.getBlockPos();

        BlockState state = level.getBlockState(pos);

        if (state.isAir()) {
            return InteractionResultHolder.pass(stack);
        }

        if (level.isClientSide) {
            return InteractionResultHolder.success(stack);
        }

        boolean destroyed = tryBreakArea(
                level,
                pos,
                blockHit.getDirection(),
                player
        );

        return destroyed
                ? InteractionResultHolder.success(stack)
                : InteractionResultHolder.fail(stack);
    }

    private boolean tryBreakArea(
            Level level,
            BlockPos center,
            Direction clickedFace,
            Player player
    ) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return false;
        }

        if (!(player instanceof ServerPlayer serverPlayer)) {
            return false;
        }

        if (player.getCooldowns().isOnCooldown(this)) {
            return false;
        }

        BlockState centerState =
                serverLevel.getBlockState(center);

        if (centerState.isAir()) {
            return false;
        }

        if (SummoningRitualsCompat.isIndestructibleAltar(
                centerState
        )) {
            boolean destroyed =
                    breakSingleBlock(
                            serverLevel,
                            center,
                            serverPlayer
                    );

            if (destroyed) {
                player.getCooldowns().addCooldown(
                        this,
                        AREA_BREAK_COOLDOWN
                );
            }

            return destroyed;
        }

        boolean destroyedAny = false;

        if (breakSingleBlock(
                serverLevel,
                center,
                serverPlayer
        )) {
            destroyedAny = true;
        }

        Direction.Axis axis =
                clickedFace.getAxis();

        for (int firstOffset = -1;
             firstOffset <= 1;
             firstOffset++) {

            for (int secondOffset = -1;
                 secondOffset <= 1;
                 secondOffset++) {

                if (firstOffset == 0
                        && secondOffset == 0) {
                    continue;
                }

                BlockPos targetPos =
                        getAreaPosition(
                                center,
                                axis,
                                firstOffset,
                                secondOffset
                        );

                if (breakSingleBlock(
                        serverLevel,
                        targetPos,
                        serverPlayer
                )) {
                    destroyedAny = true;
                }
            }
        }

        if (destroyedAny) {
            player.getCooldowns().addCooldown(
                    this,
                    AREA_BREAK_COOLDOWN
            );
        }

        return destroyedAny;
    }

    public boolean tryBreakUnbreakableBlock(
            Level level,
            BlockPos pos,
            Player player
    ) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return false;
        }

        if (!(player instanceof ServerPlayer serverPlayer)) {
            return false;
        }

        if (player.getCooldowns().isOnCooldown(this)) {
            return false;
        }

        BlockState state = serverLevel.getBlockState(pos);
        if (state.isAir()
                || state.getDestroySpeed(serverLevel, pos) >= 0.0F) {
            return false;
        }

        boolean destroyed = breakSingleBlock(
                serverLevel,
                pos,
                serverPlayer
        );

        if (destroyed) {
            player.getCooldowns().addCooldown(
                    this,
                    AREA_BREAK_COOLDOWN
            );
        }

        return destroyed;
    }

    private static BlockPos getAreaPosition(
            BlockPos center,
            Direction.Axis axis,
            int firstOffset,
            int secondOffset
    ) {
        return switch (axis) {
            case X -> center.offset(
                    0,
                    firstOffset,
                    secondOffset
            );

            case Y -> center.offset(
                    firstOffset,
                    0,
                    secondOffset
            );

            case Z -> center.offset(
                    firstOffset,
                    secondOffset,
                    0
            );
        };
    }

    private boolean breakSingleBlock(
            ServerLevel serverLevel,
            BlockPos pos,
            ServerPlayer serverPlayer
    ) {
        BlockState originalState =
                serverLevel.getBlockState(pos);

        if (originalState.isAir()) {
            return false;
        }

        boolean isIndestructibleAltar =
                SummoningRitualsCompat.isIndestructibleAltar(
                        originalState
                );

        Item guaranteedSelfDrop =
                getGuaranteedSelfDrop(originalState);

        Set<UUID> previousItemEntities;

        if (isIndestructibleAltar) {
            previousItemEntities =
                    SummoningRitualsCompat
                            .snapshotNearbyItemEntities(
                                    serverLevel,
                                    pos
                            );
        } else {
            previousItemEntities = Set.of();
        }

        boolean destroyed =
                serverPlayer.gameMode.destroyBlock(pos);

        if (!destroyed) {
            return false;
        }

        serverLevel.levelEvent(
                2001,
                pos,
                Block.getId(originalState)
        );

        if (isIndestructibleAltar) {
            SummoningRitualsCompat
                    .replaceNormalAltarDropWithIndestructibleAltar(
                            serverLevel,
                            pos,
                            previousItemEntities
                    );
        } else if (guaranteedSelfDrop != null) {
            /*
             * Bedrock and EEEAB's Erosion Deepslate Bricks normally do not
             * produce a usable block drop when destroyed. The Finalite
             * Pickaxe explicitly guarantees one corresponding block item.
             */
            Block.popResource(
                    serverLevel,
                    pos,
                    new ItemStack(guaranteedSelfDrop)
            );
        }

        return true;
    }

    @Nullable
    private static Item getGuaranteedSelfDrop(
            BlockState state
    ) {
        if (state.is(Blocks.BEDROCK)) {
            return Items.BEDROCK;
        }

        ResourceLocation blockId =
                ForgeRegistries.BLOCKS.getKey(
                        state.getBlock()
                );

        if (!EROSION_DEEPSLATE_BRICKS_ID.equals(blockId)) {
            return null;
        }

        if (!ForgeRegistries.ITEMS.containsKey(
                EROSION_DEEPSLATE_BRICKS_ID
        )) {
            return null;
        }

        Item item =
                ForgeRegistries.ITEMS.getValue(
                        EROSION_DEEPSLATE_BRICKS_ID
                );

        return item == null || item == Items.AIR
                ? null
                : item;
    }

    @Override
    public float getDestroySpeed(
            @Nonnull ItemStack stack,
            @Nonnull BlockState state
    ) {
        float vanillaSpeed =
                super.getDestroySpeed(
                        stack,
                        state
                );

        float hardness =
                state.getBlock()
                        .defaultDestroyTime();

        if (hardness < 0.0F) {
            return vanillaSpeed;
        }

        if (hardness >= 1.0F) {
            return Math.max(
                    vanillaSpeed,
                    FINAL_PICKAXE_SPEED_MULTIPLIER
                            * hardness
            );
        }

        return Math.max(
                vanillaSpeed,
                FINAL_PICKAXE_SPEED_MULTIPLIER
        );
    }

    @Override
    public boolean canBeDepleted() {
        return false;
    }

    @Override
    public boolean isBarVisible(
            @Nonnull ItemStack stack
    ) {
        return false;
    }

    @Override
    public void appendHoverText(
            @Nonnull ItemStack stack,
            @Nullable Level level,
            @Nonnull List<Component> tooltip,
            @Nonnull TooltipFlag flag
    ) {
        tooltip.add(
                Component.translatable(
                                "item.unbreakable"
                        )
                        .withStyle(
                                ChatFormatting.BLUE
                        )
        );

        tooltip.add(
                Component.translatable(
                                "item.until_eternity.final_ingot_pickaxe.desc1"
                        )
                        .withStyle(
                                ChatFormatting.GOLD
                        )
        );

        tooltip.add(
                Component.translatable(
                                "item.until_eternity.final_ingot_pickaxe.desc2"
                        )
                        .withStyle(
                                ChatFormatting.GOLD
                        )
        );

        tooltip.add(
                Component.translatable(
                                "item.until_eternity.final_ingot_pickaxe.desc3"
                        )
                        .withStyle(
                                ChatFormatting.GOLD
                        )
        );

        super.appendHoverText(
                stack,
                level,
                tooltip,
                flag
        );
    }

    @Override
    public boolean isEnchantable(
            @Nonnull ItemStack stack
    ) {
        return true;
    }
}
