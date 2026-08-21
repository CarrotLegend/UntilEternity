package com.carrot123.until_eternity.compat.goety;

import com.carrot123.until_eternity.registry.ModAttributes;
import com.carrot123.until_eternity.until_eternity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(
        modid = until_eternity.MODID,
        bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class FocusDamageAttributeMigration {
    static final String MIGRATION_KEY =
            "until_eternity:focus_damage_multiplier_v1";

    private FocusDamageAttributeMigration() {
    }

    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        Player player = event.getEntity();
        if (player.level().isClientSide) {
            return;
        }

        CompoundTag root = player.getPersistentData();
        CompoundTag persisted = root.contains(
                Player.PERSISTED_NBT_TAG, Tag.TAG_COMPOUND)
                ? root.getCompound(Player.PERSISTED_NBT_TAG)
                : new CompoundTag();
        if (persisted.getBoolean(MIGRATION_KEY)) {
            return;
        }

        AttributeInstance focusDamage = player.getAttribute(
                ModAttributes.FOCUS_DAMAGE.get());
        if (focusDamage == null) {
            return;
        }
        if (shouldMigrate(focusDamage.getBaseValue())) {
            focusDamage.setBaseValue(1.0D);
        }
        persisted.putBoolean(MIGRATION_KEY, true);
        root.put(Player.PERSISTED_NBT_TAG, persisted);
    }

    static boolean shouldMigrate(double baseValue) {
        return baseValue == 0.0D;
    }
}
