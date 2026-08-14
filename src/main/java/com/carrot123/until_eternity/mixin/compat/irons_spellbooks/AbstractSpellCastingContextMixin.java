package com.carrot123.until_eternity.mixin.compat.irons_spellbooks;

import com.carrot123.until_eternity.compat.ironsspellbooks.IronCastingContext;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.api.spells.CastSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(value = AbstractSpell.class, remap = false)
public abstract class AbstractSpellCastingContextMixin {
    @WrapMethod(
            method = "attemptInitiateCast"
                    + "(Lnet/minecraft/world/item/ItemStack;"
                    + "ILnet/minecraft/world/level/Level;"
                    + "Lnet/minecraft/world/entity/player/Player;"
                    + "Lio/redspace/ironsspellbooks/api/spells/CastSource;"
                    + "ZLjava/lang/String;)Z",
            remap = false,
            require = 1
    )
    private boolean untilEternity$bindCastingContext(
            ItemStack castingStack,
            int spellLevel,
            Level level,
            Player player,
            CastSource castSource,
            boolean triggerCooldown,
            String castingEquipmentSlot,
            Operation<Boolean> original
    ) {
        return IronCastingContext.withCastingContext(
                castingStack,
                castSource,
                castingEquipmentSlot,
                () -> original.call(
                        castingStack,
                        spellLevel,
                        level,
                        player,
                        castSource,
                        triggerCooldown,
                        castingEquipmentSlot));
    }
}
