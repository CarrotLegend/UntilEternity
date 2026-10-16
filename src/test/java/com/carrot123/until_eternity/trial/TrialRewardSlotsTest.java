package com.carrot123.until_eternity.trial;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TrialRewardSlotsTest {
    @Test void findsSeparateMainInventorySlotsForEveryDifficulty() {
        boolean[] empty = new boolean[36];
        empty[2] = empty[8] = empty[13] = empty[35] = true;
        assertEquals(List.of(2), TrialRewardSlots.find(empty.length, slot -> empty[slot], TrialDifficulty.EASY.rewardCount()));
        assertEquals(List.of(2, 8), TrialRewardSlots.find(empty.length, slot -> empty[slot], TrialDifficulty.NORMAL.rewardCount()));
        assertEquals(List.of(2, 8, 13), TrialRewardSlots.find(empty.length, slot -> empty[slot], TrialDifficulty.HARD.rewardCount()));
        assertEquals(List.of(2, 8, 13, 35), TrialRewardSlots.find(empty.length, slot -> empty[slot], TrialDifficulty.MALICE.rewardCount()));
    }

    @Test void insufficientSpaceReturnsNoSlotsInsteadOfPartialReward() {
        boolean[] empty = new boolean[36];
        empty[3] = empty[17] = empty[28] = true;
        assertTrue(TrialRewardSlots.find(empty.length, slot -> empty[slot], TrialDifficulty.MALICE.rewardCount()).isEmpty());
        assertEquals(List.of(3, 17, 28), TrialRewardSlots.find(empty.length, slot -> empty[slot], TrialDifficulty.HARD.rewardCount()));
        assertTrue(TrialRewardSlots.find(empty.length, slot -> false, TrialDifficulty.EASY.rewardCount()).isEmpty());
    }
}
