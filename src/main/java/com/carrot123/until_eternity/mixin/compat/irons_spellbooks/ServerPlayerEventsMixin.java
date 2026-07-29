package com.carrot123.until_eternity.mixin.compat.irons_spellbooks;

import com.carrot123.until_eternity.compat.ironsspellbooks.PlenitudeCastingStackResolver;
import com.carrot123.until_eternity.compat.ironsspellbooks.PlenitudeManaCost;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.player.ServerPlayerEvents;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = ServerPlayerEvents.class, remap = false)
public abstract class ServerPlayerEventsMixin {
    @Redirect(
            method = "onUseItem"
                    + "(Lnet/minecraftforge/event/entity/player/"
                    + "PlayerInteractEvent$RightClickItem;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lio/redspace/ironsspellbooks/api/spells/"
                            + "AbstractSpell;getManaCost(I)I"
            ),
            remap = false,
            require = 1
    )
    private static int untilEternity$usePlenitudeForCapabilityItemCheck(
            AbstractSpell spell,
            int spellLevel,
            PlayerInteractEvent.RightClickItem event
    ) {
        int originalCost = spell.getManaCost(spellLevel);
        Player player = event.getEntity();
        if (!player.level().isClientSide()) {
            return originalCost;
        }
        return PlenitudeManaCost.effectiveCost(
                originalCost,
                event.getItemStack(),
                PlenitudeCastingStackResolver.selectedCastSource(player));
    }
}
