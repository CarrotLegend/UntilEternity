package com.carrot123.until_eternity.mixin.compat.irons_spellbooks;

import com.carrot123.until_eternity.compat.ironsspellbooks.PlenitudeCastingStackResolver;
import com.carrot123.until_eternity.compat.ironsspellbooks.PlenitudeManaCost;
import io.redspace.ironsspellbooks.api.magic.SpellSelectionManager;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.gui.overlays.SpellWheelOverlay;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = SpellWheelOverlay.class, remap = false)
public abstract class SpellWheelOverlayMixin {
    @Shadow
    private int wheelSelection;

    @Shadow
    private SpellSelectionManager swsm;

    @Redirect(
            method = "render"
                    + "(Lnet/minecraftforge/client/gui/overlay/ForgeGui;"
                    + "Lnet/minecraft/client/gui/GuiGraphics;FII)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lio/redspace/ironsspellbooks/api/spells/"
                            + "AbstractSpell;getManaCost(I)I"
            ),
            remap = false,
            require = 2
    )
    private int untilEternity$showPlenitudeInSpellWheel(
            AbstractSpell spell,
            int spellLevel,
            ForgeGui gui,
            GuiGraphics graphics,
            float partialTick,
            int screenWidth,
            int screenHeight
    ) {
        SpellSelectionManager.SelectionOption selection =
                swsm == null ? null : swsm.getSpellSlot(wheelSelection);
        ItemStack castingStack = selection == null
                ? ItemStack.EMPTY
                : PlenitudeCastingStackResolver.resolve(
                        Minecraft.getInstance().player,
                        selection.slot);
        return PlenitudeManaCost.effectiveCost(
                spell.getManaCost(spellLevel),
                castingStack,
                selection == null
                        ? null
                        : selection.getCastSource());
    }
}
