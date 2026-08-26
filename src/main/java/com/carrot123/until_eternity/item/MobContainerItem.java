package com.carrot123.until_eternity.item;

import com.carrot123.until_eternity.registry.ModTags;
import com.mojang.logging.LogUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.registries.ForgeRegistries;
import org.slf4j.Logger;

import javax.annotation.Nullable;
import java.util.List;

public final class MobContainerItem extends Item {
    public static final String TAG_STORED_ENTITY = "StoredEntity";

    private static final Logger LOGGER = LogUtils.getLogger();
    private static final String TAG_ENTITY_ID = "id";
    private static final String TAG_CUSTOM_NAME = "CustomName";

    public MobContainerItem() {
        super(new Item.Properties()
                .stacksTo(1)
                .rarity(Rarity.UNCOMMON));
    }

    public static boolean canCapture(Entity entity) {
        if (!(entity instanceof LivingEntity) || entity instanceof Player) {
            return false;
        }
        if (entity.getType() == EntityType.WARDEN) {
            return false;
        }
        if (entity.getType().is(ModTags.EntityTypes.BOSS)) {
            return false;
        }
        if (!entity.getType().is(
                ModTags.EntityTypes.MOB_CONTAINER_WHITELIST)) {
            return false;
        }
        return entity.isAlive()
                && !entity.isRemoved()
                && entity.getType().canSerialize()
                && !entity.isPassenger()
                && !entity.isVehicle();
    }

    public static boolean hasStoredEntity(ItemStack stack) {
        return stack.getTagElement(TAG_STORED_ENTITY) != null;
    }

    @Override
    public InteractionResult interactLivingEntity(
            ItemStack stack,
            Player player,
            LivingEntity target,
            InteractionHand hand) {
        if (!player.isShiftKeyDown()) {
            return InteractionResult.PASS;
        }
        if (hasStoredEntity(stack)) {
            return InteractionResult.FAIL;
        }
        if (!canCapture(target)) {
            return InteractionResult.PASS;
        }
        if (player.level().isClientSide) {
            return InteractionResult.SUCCESS;
        }

        CompoundTag storedEntity = new CompoundTag();
        try {
            if (!target.save(storedEntity)
                    || !storedEntity.contains(TAG_ENTITY_ID, Tag.TAG_STRING)
                    || storedEntity.getString(TAG_ENTITY_ID).isBlank()) {
                LOGGER.warn(
                        "Could not serialize entity {} into a mob container",
                        target.getType());
                return InteractionResult.FAIL;
            }

            stack.getOrCreateTag().put(TAG_STORED_ENTITY, storedEntity);
            target.discard();
            return InteractionResult.CONSUME;
        } catch (RuntimeException exception) {
            LOGGER.warn(
                    "Failed to serialize entity {} into a mob container",
                    target.getType(),
                    exception);
            return InteractionResult.FAIL;
        }
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        ItemStack stack = context.getItemInHand();
        CompoundTag storedEntity = stack.getTagElement(TAG_STORED_ENTITY);
        if (storedEntity == null) {
            return InteractionResult.PASS;
        }

        Level level = context.getLevel();
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }
        if (!(level instanceof ServerLevel serverLevel)) {
            return InteractionResult.FAIL;
        }

        BlockPos spawnPos = context.getClickedPos()
                .relative(context.getClickedFace());
        if (!Level.isInSpawnableBounds(spawnPos)
                || !serverLevel.hasChunkAt(spawnPos)) {
            return InteractionResult.FAIL;
        }

        double x = spawnPos.getX() + 0.5D;
        double y = spawnPos.getY();
        double z = spawnPos.getZ() + 0.5D;
        float yaw = context.getRotation();

        Entity restored;
        try {
            restored = EntityType.loadEntityRecursive(
                    storedEntity.copy(),
                    serverLevel,
                    entity -> positionForRelease(entity, x, y, z, yaw));
        } catch (RuntimeException exception) {
            LOGGER.warn(
                    "Failed to restore entity from a mob container",
                    exception);
            return InteractionResult.FAIL;
        }

        if (restored == null) {
            LOGGER.warn("Could not load the entity stored in a mob container");
            return InteractionResult.FAIL;
        }
        if (!canRelease(restored)) {
            LOGGER.warn(
                    "Refused to release invalid or forbidden entity {} from a mob container",
                    restored.getType());
            discardTemporaryEntity(restored);
            return InteractionResult.FAIL;
        }
        if (!serverLevel.getWorldBorder()
                .isWithinBounds(restored.getBoundingBox())) {
            LOGGER.warn(
                    "Could not release stored entity {} outside the world border",
                    restored.getType());
            discardTemporaryEntity(restored);
            return InteractionResult.FAIL;
        }
        if (!serverLevel.noCollision(
                restored,
                restored.getBoundingBox())) {
            LOGGER.warn(
                    "Could not release stored entity {} because its destination is obstructed",
                    restored.getType());
            discardTemporaryEntity(restored);
            return InteractionResult.FAIL;
        }
        if (!serverLevel.tryAddFreshEntityWithPassengers(restored)) {
            LOGGER.warn(
                    "Could not release stored entity {} with UUID {}; a matching UUID may already be loaded",
                    restored.getType(),
                    restored.getUUID());
            discardTemporaryEntity(restored);
            return InteractionResult.FAIL;
        }

        stack.removeTagKey(TAG_STORED_ENTITY);
        return InteractionResult.CONSUME;
    }

    @Override
    public void appendHoverText(
            ItemStack stack,
            @Nullable Level level,
            List<Component> tooltip,
            TooltipFlag flag) {
        tooltip.add(Component.translatable(
                        "tooltip.until_eternity.mob_container.usage")
                .withStyle(ChatFormatting.GOLD));
        tooltip.add(Component.translatable(
                        "tooltip.until_eternity.mob_container.current",
                        storedEntityName(stack))
                .withStyle(ChatFormatting.GRAY));
    }

    private static Entity positionForRelease(
            Entity entity,
            double x,
            double y,
            double z,
            float yaw) {
        entity.moveTo(x, y, z, yaw, 0.0F);
        entity.setDeltaMovement(Vec3.ZERO);
        entity.fallDistance = 0.0F;
        if (entity instanceof LivingEntity living) {
            living.setYHeadRot(yaw);
            living.setYBodyRot(yaw);
        }
        return entity;
    }

    private static boolean canRelease(Entity entity) {
        return entity instanceof LivingEntity
                && !(entity instanceof Player)
                && entity.getType() != EntityType.WARDEN
                && !entity.getType().is(ModTags.EntityTypes.BOSS)
                && !entity.isPassenger()
                && !entity.isVehicle();
    }

    private static void discardTemporaryEntity(Entity entity) {
        entity.getSelfAndPassengers().toList().forEach(Entity::discard);
    }

    private static Component storedEntityName(ItemStack stack) {
        CompoundTag storedEntity = stack.getTagElement(TAG_STORED_ENTITY);
        if (storedEntity == null) {
            return Component.translatable(
                    "tooltip.until_eternity.mob_container.empty");
        }

        if (storedEntity.contains(TAG_CUSTOM_NAME, Tag.TAG_STRING)) {
            try {
                Component customName = Component.Serializer.fromJson(
                        storedEntity.getString(TAG_CUSTOM_NAME));
                if (customName != null) {
                    return customName;
                }
            } catch (RuntimeException ignored) {
                // A malformed display name falls back to the stored entity id.
            }
        }

        if (storedEntity.contains(TAG_ENTITY_ID, Tag.TAG_STRING)) {
            try {
                ResourceLocation entityId = new ResourceLocation(
                        storedEntity.getString(TAG_ENTITY_ID));
                EntityType<?> entityType =
                        ForgeRegistries.ENTITY_TYPES.getValue(entityId);
                if (entityType != null) {
                    return entityType.getDescription();
                }
            } catch (RuntimeException ignored) {
                // Invalid or missing registry ids use the localized fallback.
            }
        }

        return Component.translatable(
                "tooltip.until_eternity.mob_container.unknown");
    }
}
