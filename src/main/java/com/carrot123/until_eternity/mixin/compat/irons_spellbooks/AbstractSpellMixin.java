package com.carrot123.until_eternity.mixin.compat.irons_spellbooks;

import com.carrot123.until_eternity.compat.ironsspellbooks.IronCastingContext;
import com.carrot123.until_eternity.compat.ironsspellbooks.IronSpellPowerCompat;
import com.carrot123.until_eternity.compat.ironsspellbooks.PlenitudeManaCost;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.api.spells.CastSource;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = AbstractSpell.class, remap = false)
public abstract class AbstractSpellMixin {
    @Redirect(
            method = "canBeCastedBy"
                    + "(ILio/redspace/ironsspellbooks/api/spells/"
                    + "CastSource;"
                    + "Lio/redspace/ironsspellbooks/api/magic/MagicData;"
                    + "Lnet/minecraft/world/entity/player/Player;)"
                    + "Lio/redspace/ironsspellbooks/api/spells/CastResult;",
            at = @At(
                    value = "INVOKE",
                    target = "Lio/redspace/ironsspellbooks/api/spells/"
                            + "AbstractSpell;getManaCost(I)I"
            ),
            remap = false,
            require = 1
    )
    private int untilEternity$usePlenitudeForEligibility(
            AbstractSpell spell,
            int spellLevel
    ) {
        return PlenitudeManaCost.effectiveCost(
                spell.getManaCost(spellLevel),
                IronCastingContext.currentStack(),
                IronCastingContext.currentSource());
    }

    @Redirect(
            method = "castSpell"
                    + "(Lnet/minecraft/world/level/Level;"
                    + "ILnet/minecraft/server/level/ServerPlayer;"
                    + "Lio/redspace/ironsspellbooks/api/spells/CastSource;"
                    + "Z)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lio/redspace/ironsspellbooks/api/spells/"
                            + "AbstractSpell;getManaCost(I)I"
            ),
            remap = false,
            require = 1
    )
    private int untilEternity$usePlenitudeForCastEvent(
            AbstractSpell spell,
            int requestedLevel,
            Level world,
            int spellLevel,
            ServerPlayer serverPlayer,
            CastSource castSource,
            boolean triggerCooldown
    ) {
        MagicData magicData = MagicData.getPlayerMagicData(serverPlayer);
        return PlenitudeManaCost.effectiveCost(
                spell.getManaCost(requestedLevel),
                IronCastingContext.preferMagicDataStack(
                        magicData.getPlayerCastingItem()),
                castSource);
    }

    @Redirect(
            method = "getSpellPower(ILnet/minecraft/world/entity/Entity;)F",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/LivingEntity;"
                            + "getAttributeValue"
                            + "(Lnet/minecraft/world/entity/ai/attributes/"
                            + "Attribute;)D",
                    remap = true
            ),
            remap = false,
            require = 1
    )
    private double untilEternity$applyCastingItemSpellPower(
            LivingEntity caster,
            Attribute attribute
    ) {
        return IronSpellPowerCompat.adjustSpellPowerAttribute(
                caster,
                attribute,
                caster.getAttributeValue(attribute));
    }
}
