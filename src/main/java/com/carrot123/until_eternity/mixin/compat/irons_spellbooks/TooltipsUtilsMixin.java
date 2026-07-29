package com.carrot123.until_eternity.mixin.compat.irons_spellbooks;

import com.carrot123.until_eternity.compat.ironsspellbooks.PlenitudeManaCost;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.api.spells.CastSource;
import io.redspace.ironsspellbooks.api.spells.SpellData;
import io.redspace.ironsspellbooks.util.TooltipsUtils;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = TooltipsUtils.class, remap = false)
public abstract class TooltipsUtilsMixin {
    @Redirect(
            method = "formatActiveSpellTooltip"
                    + "(Lnet/minecraft/world/item/ItemStack;"
                    + "Lio/redspace/ironsspellbooks/api/spells/SpellData;"
                    + "Lio/redspace/ironsspellbooks/api/spells/CastSource;"
                    + "Lnet/minecraft/client/player/LocalPlayer;)"
                    + "Ljava/util/List;",
            at = @At(
                    value = "INVOKE",
                    target = "Lio/redspace/ironsspellbooks/api/spells/"
                            + "AbstractSpell;getManaCost(I)I"
            ),
            remap = false,
            require = 2
    )
    private static int untilEternity$showPlenitudeInActiveTooltip(
            AbstractSpell spell,
            int spellLevel,
            ItemStack stack,
            SpellData spellData,
            CastSource castSource,
            LocalPlayer player
    ) {
        return PlenitudeManaCost.effectiveCost(
                spell.getManaCost(spellLevel),
                stack,
                castSource);
    }
}
