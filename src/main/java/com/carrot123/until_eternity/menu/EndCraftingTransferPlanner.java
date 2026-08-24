package com.carrot123.until_eternity.menu;

import com.carrot123.until_eternity.recipe.EndCraftingIngredient;
import com.carrot123.until_eternity.recipe.EndCraftingRecipe;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * Pure, side-neutral transfer planning. Nothing is mutated unless the caller applies a successful plan.
 */
public final class EndCraftingTransferPlanner {
    public static final int INPUT_COUNT = EndCraftingRecipe.GRID_SIZE * EndCraftingRecipe.GRID_SIZE;
    public static final int PLAYER_SLOT_COUNT = 36;
    private static final int PLAYER_SLOT_LIMIT = 64;

    private EndCraftingTransferPlanner() {
    }

    public static Plan plan(EndCraftingRecipe recipe, List<ItemStack> input,
                            List<ItemStack> playerInventory, boolean maxTransfer) {
        if (input.size() != INPUT_COUNT || playerInventory.size() != PLAYER_SLOT_COUNT) {
            throw new IllegalArgumentException("Unexpected end-crafting slot count");
        }

        List<Group> groups = collectGroups(input, playerInventory);
        if (maxTransfer) {
            for (int sets = PLAYER_SLOT_LIMIT; sets >= 1; sets--) {
                Attempt attempt = attempt(recipe, input, groups, sets);
                if (attempt.error == Error.NONE) {
                    return attempt.toPlan();
                }
            }
        }
        return attempt(recipe, input, groups, 1).toPlan();
    }

    private static Attempt attempt(EndCraftingRecipe recipe, List<ItemStack> input,
                                   List<Group> groups, int sets) {
        List<Cell> cells = new ArrayList<>();
        for (int gridIndex = 0; gridIndex < INPUT_COUNT; gridIndex++) {
            EndCraftingIngredient ingredient = recipe.displayIngredientAt(gridIndex);
            if (!ingredient.isEmpty()) {
                cells.add(new Cell(gridIndex, ingredient, findGroup(groups, input.get(gridIndex))));
            }
        }

        int[] groupCounts = new int[groups.size()];
        int[] groupLimits = new int[groups.size()];
        for (int groupIndex = 0; groupIndex < groups.size(); groupIndex++) {
            Group group = groups.get(groupIndex);
            groupCounts[groupIndex] = group.count;
            groupLimits[groupIndex] = Math.min(PLAYER_SLOT_LIMIT, group.prototype.getMaxStackSize());
        }
        int[] currentGroups = cells.stream().mapToInt(Cell::currentGroup).toArray();
        boolean[][] acceptedGroups = new boolean[cells.size()][groups.size()];
        for (int cell = 0; cell < cells.size(); cell++) {
            for (int group = 0; group < groups.size(); group++) {
                acceptedGroups[cell][group] = cells.get(cell).ingredient.test(groups.get(group).prototype);
            }
        }
        EndCraftingTransferMatcher.Match match = EndCraftingTransferMatcher.match(
                groupCounts, groupLimits, currentGroups, acceptedGroups, sets);
        if (!match.success()) {
            List<Integer> missing = match.missingCells().stream()
                    .map(cell -> cells.get(cell).gridIndex).toList();
            return Attempt.failed(Error.MISSING_ITEMS, missing);
        }
        int[] cellToGroup = match.assignments();

        int[] remaining = groups.stream().mapToInt(group -> group.count).toArray();
        List<ItemStack> finalInput = emptyStacks(INPUT_COUNT);
        for (int cellIndex = 0; cellIndex < cells.size(); cellIndex++) {
            int groupIndex = cellToGroup[cellIndex];
            ItemStack target = groups.get(groupIndex).prototype.copy();
            target.setCount(sets);
            finalInput.set(cells.get(cellIndex).gridIndex, target);
            remaining[groupIndex] -= sets;
        }

        List<ItemStack> finalInventory = new ArrayList<>(PLAYER_SLOT_COUNT);
        for (int groupIndex = 0; groupIndex < groups.size(); groupIndex++) {
            Group group = groups.get(groupIndex);
            int left = remaining[groupIndex];
            int stackLimit = Math.min(PLAYER_SLOT_LIMIT, group.prototype.getMaxStackSize());
            while (left > 0) {
                if (finalInventory.size() >= PLAYER_SLOT_COUNT || stackLimit <= 0) {
                    return Attempt.failed(Error.INVENTORY_FULL, List.of());
                }
                ItemStack stack = group.prototype.copy();
                int count = Math.min(stackLimit, left);
                stack.setCount(count);
                finalInventory.add(stack);
                left -= count;
            }
        }
        while (finalInventory.size() < PLAYER_SLOT_COUNT) finalInventory.add(ItemStack.EMPTY);
        return Attempt.success(finalInput, finalInventory, sets);
    }

    private static List<Group> collectGroups(List<ItemStack> input, List<ItemStack> inventory) {
        List<Group> groups = new ArrayList<>();
        input.forEach(stack -> add(groups, stack));
        inventory.forEach(stack -> add(groups, stack));
        return groups;
    }

    private static void add(List<Group> groups, ItemStack stack) {
        if (stack.isEmpty()) return;
        int group = findGroup(groups, stack);
        if (group >= 0) groups.get(group).count += stack.getCount();
        else {
            ItemStack prototype = stack.copy();
            prototype.setCount(1);
            groups.add(new Group(prototype, stack.getCount()));
        }
    }

    private static int findGroup(List<Group> groups, ItemStack stack) {
        if (stack.isEmpty()) return -1;
        for (int i = 0; i < groups.size(); i++) {
            if (ItemStack.isSameItemSameTags(groups.get(i).prototype, stack)) return i;
        }
        return -1;
    }

    private static List<ItemStack> emptyStacks(int size) {
        List<ItemStack> result = new ArrayList<>(size);
        for (int i = 0; i < size; i++) result.add(ItemStack.EMPTY);
        return result;
    }

    public enum Error { NONE, MISSING_ITEMS, INVENTORY_FULL }

    public record Plan(Error error, List<ItemStack> input, List<ItemStack> playerInventory,
                       List<Integer> missingGridSlots, int sets) {
        public Plan {
            input = copy(input);
            playerInventory = copy(playerInventory);
            missingGridSlots = List.copyOf(missingGridSlots);
        }

        public boolean success() { return error == Error.NONE; }

        private static List<ItemStack> copy(List<ItemStack> stacks) {
            return stacks.stream().map(ItemStack::copy).toList();
        }
    }

    private record Cell(int gridIndex, EndCraftingIngredient ingredient, int currentGroup) { }
    private static final class Group {
        private final ItemStack prototype;
        private int count;

        private Group(ItemStack prototype, int count) {
            this.prototype = prototype;
            this.count = count;
        }
    }

    private record Attempt(Error error, List<ItemStack> input, List<ItemStack> inventory,
                           List<Integer> missing, int sets) {
        private static Attempt success(List<ItemStack> input, List<ItemStack> inventory, int sets) {
            return new Attempt(Error.NONE, input, inventory, List.of(), sets);
        }

        private static Attempt failed(Error error, List<Integer> missing) {
            return new Attempt(error, List.of(), List.of(), missing, 0);
        }

        private Plan toPlan() { return new Plan(error, input, inventory, missing, sets); }
    }
}
