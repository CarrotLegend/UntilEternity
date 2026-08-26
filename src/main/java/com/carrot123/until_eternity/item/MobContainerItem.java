package com.carrot123.until_eternity.item;

import com.carrot123.until_eternity.registry.ModTags;
import com.mojang.logging.LogUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
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
    private static final double RELEASE_EPSILON = 0.01D;
    private static final double[] RELEASE_SEARCH_OFFSETS =
            new double[]{0.0D, 0.5D, 1.0D, 1.5D, 2.0D};

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

    public boolean tryCapture(
            ServerPlayer player,
            InteractionHand hand,
            LivingEntity target) {
        ItemStack stack = player.getItemInHand(hand);
        if (stack.getItem() != this
                || hasStoredEntity(stack)
                || !canCapture(target)) {
            return false;
        }

        CompoundTag storedEntity = new CompoundTag();
        try {
            if (!target.save(storedEntity)
                    || !storedEntity.contains(TAG_ENTITY_ID, Tag.TAG_STRING)
                    || storedEntity.getString(TAG_ENTITY_ID).isBlank()) {
                LOGGER.warn(
                        "Could not serialize entity {} into a mob container",
                        target.getType());
                return false;
            }

            stack.getOrCreateTag().put(
                    TAG_STORED_ENTITY,
                    storedEntity.copy());
            CompoundTag written = stack.getTagElement(TAG_STORED_ENTITY);
            if (written == null
                    || !written.contains(TAG_ENTITY_ID, Tag.TAG_STRING)
                    || written.getString(TAG_ENTITY_ID).isBlank()) {
                rollbackCapture(player, hand, stack);
                LOGGER.warn(
                        "Stored entity data did not persist in the player's held mob container");
                return false;
            }
            syncHeldStack(player, hand, stack);
        } catch (RuntimeException exception) {
            rollbackCapture(player, hand, stack);
            LOGGER.warn(
                    "Failed to serialize entity {} into a mob container",
                    target.getType(),
                    exception);
            return false;
        }

        try {
            target.discard();
        } catch (RuntimeException exception) {
            if (!target.isRemoved()) {
                rollbackCapture(player, hand, stack);
                LOGGER.warn(
                        "Failed to remove entity {} after storing it",
                        target.getType(),
                        exception);
                return false;
            }
            LOGGER.warn(
                    "Entity {} was removed but discard reported an error",
                    target.getType(),
                    exception);
        }
        if (!target.isRemoved()) {
            rollbackCapture(player, hand, stack);
            LOGGER.warn(
                    "Entity {} remained in the world after storing it",
                    target.getType());
            return false;
        }
        return true;
    }

    public boolean tryRelease(
            ServerPlayer player,
            InteractionHand hand,
            ServerLevel serverLevel,
            BlockPos clickedPos,
            Direction face) {
        ItemStack stack = player.getItemInHand(hand);
        if (stack.getItem() != this) {
            return false;
        }
        CompoundTag storedEntity = stack.getTagElement(TAG_STORED_ENTITY);
        if (storedEntity == null) {
            return false;
        }

        Entity restored;
        try {
            restored = EntityType.loadEntityRecursive(
                    storedEntity.copy(),
                    serverLevel,
                    entity -> entity);
        } catch (RuntimeException exception) {
            LOGGER.warn(
                    "Failed to restore entity from a mob container",
                    exception);
            return false;
        }

        if (restored == null) {
            LOGGER.warn("Could not load the entity stored in a mob container");
            return false;
        }
        if (!canRelease(restored)) {
            LOGGER.warn(
                    "Refused to release invalid or forbidden entity {} from a mob container",
                    restored.getType());
            discardTemporaryEntity(restored);
            return false;
        }
        if (!findSafeReleasePosition(
                restored,
                serverLevel,
                clickedPos,
                face,
                player.getYRot())) {
            LOGGER.warn(
                    "Could not find a loaded, unobstructed release position for stored entity {}",
                    restored.getType());
            discardTemporaryEntity(restored);
            return false;
        }
        if (!serverLevel.tryAddFreshEntityWithPassengers(restored)) {
            LOGGER.warn(
                    "Could not release stored entity {} with UUID {}; a matching UUID may already be loaded",
                    restored.getType(),
                    restored.getUUID());
            discardTemporaryEntity(restored);
            return false;
        }

        stack.removeTagKey(TAG_STORED_ENTITY);
        try {
            syncHeldStack(player, hand, stack);
        } catch (RuntimeException exception) {
            LOGGER.warn(
                    "Released entity {}, but immediate held-item synchronization failed",
                    restored.getType(),
                    exception);
        }
        return true;
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

    private static boolean findSafeReleasePosition(
            Entity entity,
            ServerLevel level,
            BlockPos clickedPos,
            Direction face,
            float yaw) {
        Vec3 base = baseReleasePosition(entity, clickedPos, face);
        Vec3 normal = Vec3.atLowerCornerOf(face.getNormal());
        for (double offset : RELEASE_SEARCH_OFFSETS) {
            Vec3 candidate = base.add(normal.scale(offset));
            positionForRelease(
                    entity,
                    candidate.x,
                    candidate.y,
                    candidate.z,
                    yaw);
            AABB bounds = entity.getBoundingBox();
            BlockPos min = BlockPos.containing(
                    bounds.minX,
                    bounds.minY,
                    bounds.minZ);
            BlockPos max = BlockPos.containing(
                    bounds.maxX,
                    bounds.maxY,
                    bounds.maxZ);
            if (Level.isInSpawnableBounds(min)
                    && Level.isInSpawnableBounds(max)
                    && level.hasChunksAt(min, max)
                    && level.getWorldBorder().isWithinBounds(bounds)
                    && level.noCollision(entity, bounds)) {
                return true;
            }
        }
        return false;
    }

    private static Vec3 baseReleasePosition(
            Entity entity,
            BlockPos clickedPos,
            Direction face) {
        double x = clickedPos.getX() + 0.5D;
        double y = clickedPos.getY() + RELEASE_EPSILON;
        double z = clickedPos.getZ() + 0.5D;
        double halfWidth = entity.getBbWidth() / 2.0D;
        return switch (face) {
            case UP -> new Vec3(
                    x,
                    clickedPos.getY() + 1.0D + RELEASE_EPSILON,
                    z);
            case DOWN -> new Vec3(
                    x,
                    clickedPos.getY() - entity.getBbHeight()
                            - RELEASE_EPSILON,
                    z);
            case EAST -> new Vec3(
                    clickedPos.getX() + 1.0D + halfWidth
                            + RELEASE_EPSILON,
                    y,
                    z);
            case WEST -> new Vec3(
                    clickedPos.getX() - halfWidth - RELEASE_EPSILON,
                    y,
                    z);
            case SOUTH -> new Vec3(
                    x,
                    y,
                    clickedPos.getZ() + 1.0D + halfWidth
                            + RELEASE_EPSILON);
            case NORTH -> new Vec3(
                    x,
                    y,
                    clickedPos.getZ() - halfWidth - RELEASE_EPSILON);
        };
    }

    private static void positionForRelease(
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

    private static void rollbackCapture(
            ServerPlayer player,
            InteractionHand hand,
            ItemStack stack) {
        stack.removeTagKey(TAG_STORED_ENTITY);
        try {
            syncHeldStack(player, hand, stack);
        } catch (RuntimeException exception) {
            LOGGER.warn(
                    "Failed to synchronize a rolled-back mob container",
                    exception);
        }
    }

    private static void syncHeldStack(
            ServerPlayer player,
            InteractionHand hand,
            ItemStack stack) {
        player.setItemInHand(hand, stack);
        player.getInventory().setChanged();
        player.inventoryMenu.broadcastChanges();
        if (player.containerMenu != player.inventoryMenu) {
            player.containerMenu.broadcastChanges();
        }
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
