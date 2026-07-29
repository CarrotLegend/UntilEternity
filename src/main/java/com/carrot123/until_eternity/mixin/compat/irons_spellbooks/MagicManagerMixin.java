package com.carrot123.until_eternity.mixin.compat.irons_spellbooks;

import com.carrot123.until_eternity.compat.ironsspellbooks.PlenitudeManaCost;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.capabilities.magic.MagicManager;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Dynamic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = MagicManager.class, remap = false)
public abstract class MagicManagerMixin {
    @Dynamic("Synthetic lambda in Iron's Spells 1.20.1-3.15.6")
    @Redirect(
            method = "lambda$tick$0"
                    + "(ZLnet/minecraft/world/entity/player/Player;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lio/redspace/ironsspellbooks/api/spells/"
                            + "AbstractSpell;getManaCost(I)I"
            ),
            remap = false,
            require = 1
    )
    private int untilEternity$usePlenitudeForContinuousCast(
            AbstractSpell spell,
            int spellLevel,
            boolean doManaRegen,
            Player player
    ) {
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return spell.getManaCost(spellLevel);
        }
        MagicData magicData = MagicData.getPlayerMagicData(serverPlayer);
        return PlenitudeManaCost.effectiveCost(
                spell.getManaCost(spellLevel),
                magicData.getPlayerCastingItem(),
                magicData.getCastSource());
    }
}
