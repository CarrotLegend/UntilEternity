package com.carrot123.until_eternity.mixin.compat.goety;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import com.Polarice3.Goety.common.blocks.entities.PithosBlockEntity;
import com.Polarice3.Goety.common.entities.hostile.SkullLord;
import com.Polarice3.Goety.common.events.TimedEvents;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

@Mixin(value = SkullLord.class, remap = false)
public abstract class SkullLordPithosUnlockMixin {

    @Redirect(
            method = "onRemovedFromWorld",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/Polarice3/Goety/common/blocks/entities/PithosBlockEntity;setSkullLordName(Lnet/minecraft/network/chat/Component;)V"
            ),
            require = 0,
            remap = false
    )
    private void untilEternity$deferSkullLordNameClear(PithosBlockEntity pithos, Component name) {
    }

    @Redirect(
            method = "onRemovedFromWorld",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/Polarice3/Goety/common/blocks/entities/PithosBlockEntity;unlock()V"
            ),
            require = 0,
            remap = false
    )
    private void untilEternity$deferPithosUnlock(PithosBlockEntity pithosTile) {
        BlockPos pithosPos = pithosTile.getBlockPos().immutable();
        Level pithosLevel = pithosTile.getLevel();

        TimedEvents.submitTask(
                "goety:pithos_unlock_" + pithosPos.toShortString(),
                () -> {
                    if (pithosLevel != null) {
                        BlockEntity blockEntity = pithosLevel.getBlockEntity(pithosPos);

                        if (blockEntity instanceof PithosBlockEntity pithos) {
                            pithos.setSkullLordName(null);
                            pithos.unlock();
                        }
                    }

                    return true;
                }
        );
    }
}