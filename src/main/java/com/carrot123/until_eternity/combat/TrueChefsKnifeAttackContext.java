package com.carrot123.until_eternity.combat;

import com.carrot123.until_eternity.item.ModItems;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.entity.PartEntity;

public final class TrueChefsKnifeAttackContext {
    private TrueChefsKnifeAttackContext() {
    }

    public static boolean isEligible(Player player, Entity target) {
        LivingEntity victim = resolveVictim(target);
        return !player.level().isClientSide
                && player.getMainHandItem().is(ModItems.TRUE_CHEFS_KNIFE.get())
                && victim != null
                && victim.isAlive()
                && !victim.isRemoved()
                && !victim.isSpectator();
    }

    public static LivingEntity resolveVictim(Entity target) {
        if (target instanceof LivingEntity livingEntity) {
            return livingEntity;
        }
        if (target instanceof PartEntity<?> part
                && part.getParent() instanceof LivingEntity livingParent) {
            return livingParent;
        }
        return null;
    }
}
