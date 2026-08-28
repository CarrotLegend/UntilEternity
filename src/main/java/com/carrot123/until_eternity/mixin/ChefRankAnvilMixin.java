package com.carrot123.until_eternity.mixin;

import com.carrot123.until_eternity.compat.eternalcareer.ChefRank;
import com.carrot123.until_eternity.compat.eternalcareer.ChefRankHelper;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AnvilMenu.class)
public abstract class ChefRankAnvilMixin {
    @Shadow
    public int repairItemCountCost;

    @Unique
    private boolean until_eternity$chefRankUpgradeResult;

    @Inject(method = "createResult", at = @At("RETURN"))
    private void until_eternity$cacheChefRankUpgradeResult(CallbackInfo callbackInfo) {
        AnvilMenu menu = (AnvilMenu) (Object) this;
        ItemStack armor = menu.getSlot(AnvilMenu.INPUT_SLOT).getItem();
        ItemStack badge = menu.getSlot(AnvilMenu.ADDITIONAL_SLOT).getItem();
        ItemStack output = menu.getSlot(AnvilMenu.RESULT_SLOT).getItem();

        this.until_eternity$chefRankUpgradeResult =
                until_eternity$isChefRankUpgradeResult(armor, badge, output);
    }

    @Redirect(
            method = "onTake",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/Container;setItem(ILnet/minecraft/world/item/ItemStack;)V",
                    ordinal = 0
            )
    )
    private void until_eternity$preserveSingleBadgeCost(
            Container inputSlots,
            int slot,
            ItemStack stack
    ) {
        boolean forceSingleBadge = this.until_eternity$chefRankUpgradeResult;

        inputSlots.setItem(slot, stack);

        if (forceSingleBadge) {
            this.repairItemCountCost = 1;
        }
    }

    @Unique
    private static boolean until_eternity$isChefRankUpgradeResult(
            ItemStack armor,
            ItemStack badge,
            ItemStack output
    ) {
        if (!ChefRankHelper.isChefArmor(armor)
                || output.isEmpty()
                || output.getCount() != 1
                || !ChefRankHelper.isChefArmor(output)
                || !ItemStack.isSameItem(armor, output)) {
            return false;
        }

        ChefRank targetRank = ChefRankHelper.getRankForBadge(badge);
        if (targetRank == ChefRank.NONE) {
            return false;
        }

        ChefRank currentRank = ChefRankHelper.getRank(armor);
        return targetRank.id() == currentRank.id() + 1
                && ChefRankHelper.getRank(output) == targetRank;
    }
}
