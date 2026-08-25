package com.carrot123.until_eternity.event;

import com.carrot123.until_eternity.until_eternity;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

@SuppressWarnings("null")
@Mod.EventBusSubscriber(modid = until_eternity.MODID)
public final class GravititeSwordEvents {

    private static final ResourceLocation GRAVITITE_SWORD_ID =
            new ResourceLocation(
                    "aether",
                    "gravitite_sword"
            );

    private static final ResourceLocation ICE_CRYSTAL_ID =
            new ResourceLocation(
                    "aether",
                    "ice_crystal"
            );

    private static final int ICE_CRYSTAL_COOLDOWN = 20;
    private static final double SPAWN_OFFSET = 1.2D;
    private static final double PROJECTILE_VELOCITY = 2.5D;

    private GravititeSwordEvents() {
    }

    @SubscribeEvent
    public static void onRightClickItem(
            PlayerInteractEvent.RightClickItem event
    ) {

        if (event.getHand() != InteractionHand.MAIN_HAND) {
            return;
        }

        Player player = event.getEntity();
        ItemStack stack = event.getItemStack();

        ResourceLocation itemId =
                ForgeRegistries.ITEMS.getKey(
                        stack.getItem()
                );

        if (!GRAVITITE_SWORD_ID.equals(itemId)) {
            return;
        }

        if (player.getCooldowns().isOnCooldown(
                stack.getItem()
        )) {
            return;
        }

        if (!(event.getLevel() instanceof ServerLevel level)) {
            return;
        }

        EntityType<?> iceCrystalType =
                ForgeRegistries.ENTITY_TYPES.getValue(
                        ICE_CRYSTAL_ID
                );

        if (iceCrystalType == null) {
            return;
        }

        Entity entity = iceCrystalType.create(level);

        if (!(entity instanceof Projectile projectile)) {
            return;
        }

        Vec3 viewVector =
                player.getViewVector(1.0F)
                        .normalize();

        CompoundTag entityNbt = new CompoundTag();

        projectile.saveWithoutId(entityNbt);

        entityNbt.putBoolean(
                "Attacked",
                true
        );

        projectile.load(entityNbt);
        double spawnX =
                player.getX()
                        + viewVector.x * SPAWN_OFFSET;

        double spawnY =
                player.getY()
                        + 1.2D
                        + viewVector.y * SPAWN_OFFSET;

        double spawnZ =
                player.getZ()
                        + viewVector.z * SPAWN_OFFSET;

        projectile.setPos(
                spawnX,
                spawnY,
                spawnZ
        );
        projectile.setOwner(player);
        projectile.setDeltaMovement(
                viewVector.x * PROJECTILE_VELOCITY,
                viewVector.y * PROJECTILE_VELOCITY,
                viewVector.z * PROJECTILE_VELOCITY
        );

        level.addFreshEntity(projectile);

        player.getCooldowns().addCooldown(
                stack.getItem(),
                ICE_CRYSTAL_COOLDOWN
        );

        player.swing(
                InteractionHand.MAIN_HAND,
                true
        );

        level.playSound(
                null,
                player.getX(),
                player.getY(),
                player.getZ(),
                SoundEvents.SNOWBALL_THROW,
                SoundSource.PLAYERS,
                1.0F,
                1.0F
        );
    }
}