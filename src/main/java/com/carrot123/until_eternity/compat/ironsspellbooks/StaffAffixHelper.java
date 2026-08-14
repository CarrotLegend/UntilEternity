package com.carrot123.until_eternity.compat.ironsspellbooks;

import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;

import java.util.Optional;

public final class StaffAffixHelper {
    public static final String AFFIX_TAG = "until_eternity:staff_affix";

    private StaffAffixHelper() {
    }

    public static Optional<StaffAffix> getAffix(ItemStack stack) {
        if (!StaffUpgradeHelper.isUpgradeableStaff(stack)
                || stack.getTag() == null
                || !stack.getTag().contains(AFFIX_TAG, Tag.TAG_STRING)) {
            return Optional.empty();
        }
        return StaffAffix.byId(stack.getTag().getString(AFFIX_TAG));
    }

    public static StaffAffix roll(ItemStack stack, RandomSource random) {
        StaffAffix[] values = StaffAffix.values();
        StaffAffix affix = values[random.nextInt(values.length)];
        stack.getOrCreateTag().putString(AFFIX_TAG, affix.id());
        return affix;
    }

    public static Optional<Component> getUpgradePrefix(ItemStack stack) {
        return getUpgradePrefix(StaffUpgradeHelper.getValidLevel(stack));
    }

    public static Optional<Component> getUpgradePrefix(int level) {
        if (level < StaffUpgradeHelper.MIN_LEVEL
                || level > StaffUpgradeHelper.MAX_LEVEL) {
            return Optional.empty();
        }
        return Optional.of(Component.translatable(
                "staff_upgrade.until_eternity." + switch (level) {
                    case 1 -> "common";
                    case 2 -> "excellent";
                    case 3 -> "rare";
                    case 4 -> "epic";
                    case 5 -> "legendary";
                    default -> throw new IllegalStateException();
                }));
    }

    public static Component composeHoverName(
            ItemStack stack,
            Component originalName
    ) {
        Optional<StaffAffix> affix = getAffix(stack);
        Optional<Component> upgrade = getUpgradePrefix(stack);
        if (affix.isEmpty() && upgrade.isEmpty()) {
            return originalName;
        }

        Component result = Component.empty();
        if (affix.isPresent()) {
            result = result.copy()
                    .append(Component.translatable(
                            affix.get().translationKey()))
                    .append(" ");
        }
        if (upgrade.isPresent()) {
            result = result.copy().append(upgrade.get()).append(" ");
        }
        return result.copy().append(originalName.copy());
    }
}
