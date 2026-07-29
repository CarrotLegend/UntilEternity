package com.carrot123.until_eternity.mixin.compat.irons_spellbooks;

import com.carrot123.until_eternity.compat.ironsspellbooks.PlenitudeCastingStackResolver;
import com.carrot123.until_eternity.compat.ironsspellbooks.PlenitudeManaCost;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.item.CastingItem;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = CastingItem.class, remap = false)
public abstract class CastingItemMixin {
    @Redirect(
            method = "use"
                    + "(Lnet/minecraft/world/level/Level;"
                    + "Lnet/minecraft/world/entity/player/Player;"
                    + "Lnet/minecraft/world/InteractionHand;)"
                    + "Lnet/minecraft/world/InteractionResultHolder;",
            at = @At(
                    value = "INVOKE",
                    target = "Lio/redspace/ironsspellbooks/api/spells/"
                            + "AbstractSpell;getManaCost(I)I"
            ),
            remap = false,
            require = 1
    )
    private int untilEternity$usePlenitudeForClientHandCheck(
            AbstractSpell spell,
            int spellLevel,
            Level level,
            Player player,
            InteractionHand hand
    ) {
        int originalCost = spell.getManaCost(spellLevel);
        if (!level.isClientSide()) {
            return originalCost;
        }
        return PlenitudeManaCost.effectiveCost(
                originalCost,
                player.getItemInHand(hand),
                PlenitudeCastingStackResolver.selectedCastSource(player));
    }
}
