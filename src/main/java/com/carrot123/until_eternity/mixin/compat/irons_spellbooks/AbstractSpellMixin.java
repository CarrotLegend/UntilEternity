package com.carrot123.until_eternity.mixin.compat.irons_spellbooks;

import com.carrot123.until_eternity.compat.ironsspellbooks.IronSpellPowerCompat;
import com.carrot123.until_eternity.compat.ironsspellbooks.PlenitudeManaCost;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.api.spells.CastResult;
import io.redspace.ironsspellbooks.api.spells.CastSource;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = AbstractSpell.class, remap = false)
public abstract class AbstractSpellMixin {
    @Redirect(
            method = "attemptInitiateCast"
                    + "(Lnet/minecraft/world/item/ItemStack;"
                    + "ILnet/minecraft/world/level/Level;"
                    + "Lnet/minecraft/world/entity/player/Player;"
                    + "Lio/redspace/ironsspellbooks/api/spells/CastSource;"
                    + "ZLjava/lang/String;)Z",
            at = @At(
                    value = "INVOKE",
                    target = "Lio/redspace/ironsspellbooks/api/spells/"
                            + "AbstractSpell;canBeCastedBy"
                            + "(ILio/redspace/ironsspellbooks/api/spells/"
                            + "CastSource;"
                            + "Lio/redspace/ironsspellbooks/api/magic/"
                            + "MagicData;"
                            + "Lnet/minecraft/world/entity/player/Player;)"
                            + "Lio/redspace/ironsspellbooks/api/spells/"
                            + "CastResult;"
            ),
            remap = false,
            require = 1
    )
    private CastResult untilEternity$bindPlenitudeCastingStack(
            AbstractSpell spell,
            int requestedLevel,
            CastSource requestedSource,
            MagicData magicData,
            Player requestedPlayer,
            ItemStack castingStack,
            int spellLevel,
            Level level,
            Player player,
            CastSource castSource,
            boolean triggerCooldown,
            String castingEquipmentSlot
    ) {
        return PlenitudeManaCost.withCastingStack(
                castingStack,
                castSource,
                () -> spell.canBeCastedBy(
                        requestedLevel,
                        requestedSource,
                        magicData,
                        requestedPlayer));
    }

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
                PlenitudeManaCost.currentCastingStack(),
                PlenitudeManaCost.currentCastSource());
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
                magicData.getPlayerCastingItem(),
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
