package com.carrot123.until_eternity.menu;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

/** Package-independent capacity matching used by the ItemStack transfer planner. */
public final class EndCraftingTransferMatcher {
    private EndCraftingTransferMatcher() {
    }

    public static Match match(int[] groupCounts, int[] groupStackLimits, int[] currentGroups,
                              boolean[][] acceptedGroups, int sets) {
        int cellCount = acceptedGroups.length;
        List<Integer> unitGroups = new ArrayList<>();
        for (int group = 0; group < groupCounts.length; group++) {
            if (sets > groupStackLimits[group]) continue;
            int capacity = Math.min(cellCount, groupCounts[group] / sets);
            for (int i = 0; i < capacity; i++) unitGroups.add(group);
        }

        List<List<Integer>> candidates = new ArrayList<>(cellCount);
        for (int cell = 0; cell < cellCount; cell++) {
            List<Integer> cellCandidates = new ArrayList<>();
            for (int unit = 0; unit < unitGroups.size(); unit++) {
                if (acceptedGroups[cell][unitGroups.get(unit)]) cellCandidates.add(unit);
            }
            int currentGroup = currentGroups[cell];
            cellCandidates.sort(Comparator
                    .comparingInt((Integer unit) -> unitGroups.get(unit) == currentGroup ? 0 : 1)
                    .thenComparingInt(Integer::intValue));
            candidates.add(cellCandidates);
        }

        Integer[] order = new Integer[cellCount];
        for (int i = 0; i < order.length; i++) order[i] = i;
        Arrays.sort(order, Comparator
                .comparingInt((Integer cell) -> candidates.get(cell).size())
                .thenComparingInt(Integer::intValue));

        int[] unitToCell = new int[unitGroups.size()];
        Arrays.fill(unitToCell, -1);
        int[] assignments = new int[cellCount];
        Arrays.fill(assignments, -1);
        for (int cell : order) {
            if (!assign(cell, candidates, unitGroups, unitToCell, assignments,
                    new boolean[unitGroups.size()])) {
                List<Integer> missing = new ArrayList<>();
                for (int i = 0; i < assignments.length; i++) {
                    if (assignments[i] < 0) missing.add(i);
                }
                return new Match(assignments, missing);
            }
        }
        return new Match(assignments, List.of());
    }

    private static boolean assign(int cell, List<List<Integer>> candidates, List<Integer> unitGroups,
                                  int[] unitToCell, int[] assignments, boolean[] seenUnits) {
        for (int unit : candidates.get(cell)) {
            if (seenUnits[unit]) continue;
            seenUnits[unit] = true;
            int previous = unitToCell[unit];
            if (previous < 0 || assign(previous, candidates, unitGroups,
                    unitToCell, assignments, seenUnits)) {
                unitToCell[unit] = cell;
                assignments[cell] = unitGroups.get(unit);
                return true;
            }
        }
        return false;
    }

    public record Match(int[] assignments, List<Integer> missingCells) {
        public Match {
            assignments = assignments.clone();
            missingCells = List.copyOf(missingCells);
        }

        public boolean success() { return missingCells.isEmpty(); }
    }
}
