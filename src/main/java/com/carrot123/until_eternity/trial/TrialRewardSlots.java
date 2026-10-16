package com.carrot123.until_eternity.trial;

import java.util.ArrayList;
import java.util.List;
import java.util.function.IntPredicate;

final class TrialRewardSlots {
    private TrialRewardSlots() {}

    static List<Integer> find(int slotCount, IntPredicate isEmpty, int rewardCount) {
        List<Integer> freeSlots = new ArrayList<>(rewardCount);
        for (int slot = 0; slot < slotCount && freeSlots.size() < rewardCount; slot++) {
            if (isEmpty.test(slot)) freeSlots.add(slot);
        }
        return freeSlots.size() == rewardCount ? List.copyOf(freeSlots) : List.of();
    }
}
