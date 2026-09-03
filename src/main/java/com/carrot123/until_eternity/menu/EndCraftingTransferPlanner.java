package com.carrot123.until_eternity.menu;

import com.carrot123.until_eternity.recipe.EndCraftingIngredient;
import com.carrot123.until_eternity.recipe.EndCraftingRecipe;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class EndCraftingTransferPlanner {
    public static final int INPUT_COUNT =
            EndCraftingRecipe.GRID_SIZE * EndCraftingRecipe.GRID_SIZE;

    public static final int PLAYER_SLOT_COUNT = 36;

    private static final int PLAYER_SLOT_LIMIT = 64;

    private EndCraftingTransferPlanner() {
    }

    public static Plan plan(
            EndCraftingRecipe recipe,
            List<ItemStack> input,
            List<ItemStack> playerInventory,
            boolean maxTransfer
    ) {
        if (input.size() != INPUT_COUNT
                || playerInventory.size() != PLAYER_SLOT_COUNT) {

            throw new IllegalArgumentException(
                    "Unexpected end-crafting slot count"
            );
        }

        List<Group> groups = collectGroups(input, playerInventory);

        if (maxTransfer) {
            for (int sets = PLAYER_SLOT_LIMIT; sets >= 1; sets--) {
                Attempt attempt = attempt(
                        recipe,
                        input,
                        playerInventory,
                        groups,
                        sets
                );

                if (attempt.error() == Error.NONE) {
                    return attempt.toPlan();
                }
            }
        }

        return attempt(
                recipe,
                input,
                playerInventory,
                groups,
                1
        ).toPlan();
    }

    private static Attempt attempt(
            EndCraftingRecipe recipe,
            List<ItemStack> input,
            List<ItemStack> playerInventory,
            List<Group> groups,
            int sets
    ) {
        List<Cell> cells = new ArrayList<>();

        for (int gridIndex = 0; gridIndex < INPUT_COUNT; gridIndex++) {
            EndCraftingIngredient ingredient =
                    recipe.displayIngredientAt(gridIndex);

            if (!ingredient.isEmpty()) {
                cells.add(
                        new Cell(
                                gridIndex,
                                ingredient,
                                findGroup(
                                        groups,
                                        input.get(gridIndex)
                                )
                        )
                );
            }
        }
        int[] groupCounts = new int[groups.size()];
        int[] groupLimits = new int[groups.size()];

        for (int groupIndex = 0;
             groupIndex < groups.size();
             groupIndex++) {

            Group group = groups.get(groupIndex);

            groupCounts[groupIndex] = group.count;

            groupLimits[groupIndex] = Math.min(
                    PLAYER_SLOT_LIMIT,
                    group.prototype.getMaxStackSize()
            );
        }

        int[] currentGroups = cells.stream()
                .mapToInt(Cell::currentGroup)
                .toArray();
        boolean[][] acceptedGroups =
                new boolean[cells.size()][groups.size()];

        for (int cell = 0; cell < cells.size(); cell++) {
            for (int group = 0;
                 group < groups.size();
                 group++) {

                acceptedGroups[cell][group] =
                        cells.get(cell)
                                .ingredient()
                                .test(groups.get(group).prototype);
            }
        }

        EndCraftingTransferMatcher.Match match =
                EndCraftingTransferMatcher.match(
                        groupCounts,
                        groupLimits,
                        currentGroups,
                        acceptedGroups,
                        sets
                );

        if (!match.success()) {
            List<Integer> missing =
                    match.missingCells()
                            .stream()
                            .map(cell ->
                                    cells.get(cell).gridIndex()
                            )
                            .toList();

            return Attempt.failed(
                    Error.MISSING_ITEMS,
                    missing
            );
        }

        int[] cellToGroup = match.assignments();
        List<ItemStack> workingInventory =
                mutableCopy(playerInventory);

        int[] remainingInput = new int[INPUT_COUNT];

        for (int i = 0; i < INPUT_COUNT; i++) {
            remainingInput[i] = input.get(i).getCount();
        }

        List<ItemStack> finalInput =
                emptyStacks(INPUT_COUNT);
        int[] assignedGroupByGrid =
                new int[INPUT_COUNT];

        Arrays.fill(assignedGroupByGrid, -1);

        int[] neededByCell =
                new int[cells.size()];
        for (int cellIndex = 0;
             cellIndex < cells.size();
             cellIndex++) {

            Cell cell = cells.get(cellIndex);
            int gridIndex = cell.gridIndex();
            int assignedGroup = cellToGroup[cellIndex];

            assignedGroupByGrid[gridIndex] =
                    assignedGroup;

            ItemStack current =
                    input.get(gridIndex);

            int currentGroup =
                    findGroup(groups, current);

            int kept = 0;

            if (currentGroup == assignedGroup) {
                kept = Math.min(
                        sets,
                        current.getCount()
                );

                remainingInput[gridIndex] -= kept;
            }

            ItemStack target =
                    groups.get(assignedGroup)
                            .prototype
                            .copy();

            target.setCount(sets);

            finalInput.set(
                    gridIndex,
                    target
            );

            neededByCell[cellIndex] =
                    sets - kept;
        }

        for (int cellIndex = 0;
             cellIndex < cells.size();
             cellIndex++) {

            int needed =
                    neededByCell[cellIndex];

            if (needed <= 0) {
                continue;
            }

            int assignedGroup =
                    cellToGroup[cellIndex];

            needed = takeFromInput(
                    input,
                    remainingInput,
                    groups,
                    assignedGroup,
                    needed
            );

            if (needed > 0) {
                needed = takeFromInventory(
                        workingInventory,
                        groups,
                        assignedGroup,
                        needed
                );
            }
            if (needed > 0) {
                return Attempt.failed(
                        Error.MISSING_ITEMS,
                        List.of(
                                cells.get(cellIndex)
                                        .gridIndex()
                        )
                );
            }
        }
        for (int gridIndex = 0;
             gridIndex < INPUT_COUNT;
             gridIndex++) {

            if (remainingInput[gridIndex] <= 0) {
                continue;
            }

            int assignedGroup =
                    assignedGroupByGrid[gridIndex];

            if (assignedGroup < 0) {
                continue;
            }

            ItemStack original =
                    input.get(gridIndex);

            if (findGroup(groups, original)
                    != assignedGroup) {
                continue;
            }

            ItemStack target =
                    finalInput.get(gridIndex);

            if (target.isEmpty()
                    || !ItemStack.isSameItemSameTags(
                            target,
                            original
                    )) {
                continue;
            }

            int limit = Math.min(
                    PLAYER_SLOT_LIMIT,
                    target.getMaxStackSize()
            );

            int space =
                    limit - target.getCount();

            if (space <= 0) {
                continue;
            }

            int moved = Math.min(
                    space,
                    remainingInput[gridIndex]
            );

            target.grow(moved);

            remainingInput[gridIndex] -= moved;
        }
        for (int gridIndex = 0;
             gridIndex < INPUT_COUNT;
             gridIndex++) {

            int count =
                    remainingInput[gridIndex];

            if (count <= 0) {
                continue;
            }

            ItemStack leftover =
                    input.get(gridIndex).copy();

            leftover.setCount(count);

            if (!addToInventoryPreservingSlots(
                    workingInventory,
                    leftover
            )) {
                return Attempt.failed(
                        Error.INVENTORY_FULL,
                        List.of()
                );
            }
        }

        return Attempt.success(
                finalInput,
                workingInventory,
                sets
        );
    }
    private static int takeFromInput(
            List<ItemStack> input,
            int[] remainingInput,
            List<Group> groups,
            int wantedGroup,
            int needed
    ) {
        for (int slot = 0;
             slot < INPUT_COUNT && needed > 0;
             slot++) {

            int available =
                    remainingInput[slot];

            if (available <= 0) {
                continue;
            }

            ItemStack stack =
                    input.get(slot);

            if (findGroup(groups, stack)
                    != wantedGroup) {
                continue;
            }

            int taken =
                    Math.min(
                            available,
                            needed
                    );

            remainingInput[slot] -= taken;
            needed -= taken;
        }

        return needed;
    }
    private static int takeFromInventory(
            List<ItemStack> inventory,
            List<Group> groups,
            int wantedGroup,
            int needed
    ) {
        for (int slot = 0;
             slot < inventory.size() && needed > 0;
             slot++) {

            ItemStack stack =
                    inventory.get(slot);

            if (stack.isEmpty()) {
                continue;
            }

            if (findGroup(groups, stack)
                    != wantedGroup) {
                continue;
            }

            int taken =
                    Math.min(
                            stack.getCount(),
                            needed
                    );

            stack.shrink(taken);

            needed -= taken;

            if (stack.isEmpty()) {
                inventory.set(
                        slot,
                        ItemStack.EMPTY
                );
            }
        }

        return needed;
    }

    private static boolean addToInventoryPreservingSlots(
            List<ItemStack> inventory,
            ItemStack stack
    ) {
        if (stack.isEmpty()) {
            return true;
        }

        for (int slot = 0;
             slot < inventory.size()
                     && !stack.isEmpty();
             slot++) {

            ItemStack current =
                    inventory.get(slot);

            if (current.isEmpty()) {
                continue;
            }

            if (!ItemStack.isSameItemSameTags(
                    current,
                    stack
            )) {
                continue;
            }

            int limit = Math.min(
                    PLAYER_SLOT_LIMIT,
                    current.getMaxStackSize()
            );

            int space =
                    limit - current.getCount();

            if (space <= 0) {
                continue;
            }

            int moved =
                    Math.min(
                            space,
                            stack.getCount()
                    );

            current.grow(moved);
            stack.shrink(moved);
        }
        for (int slot = 0;
             slot < inventory.size()
                     && !stack.isEmpty();
             slot++) {

            ItemStack current =
                    inventory.get(slot);

            if (!current.isEmpty()) {
                continue;
            }

            int limit = Math.min(
                    PLAYER_SLOT_LIMIT,
                    stack.getMaxStackSize()
            );

            if (limit <= 0) {
                return false;
            }

            int moved =
                    Math.min(
                            limit,
                            stack.getCount()
                    );

            ItemStack placed =
                    stack.copy();

            placed.setCount(moved);

            inventory.set(
                    slot,
                    placed
            );

            stack.shrink(moved);
        }

        return stack.isEmpty();
    }

    private static List<Group> collectGroups(
            List<ItemStack> input,
            List<ItemStack> inventory
    ) {
        List<Group> groups =
                new ArrayList<>();

        input.forEach(
                stack -> add(groups, stack)
        );

        inventory.forEach(
                stack -> add(groups, stack)
        );

        return groups;
    }

    private static void add(
            List<Group> groups,
            ItemStack stack
    ) {
        if (stack.isEmpty()) {
            return;
        }

        int group =
                findGroup(groups, stack);

        if (group >= 0) {
            groups.get(group).count +=
                    stack.getCount();
            return;
        }

        ItemStack prototype =
                stack.copy();

        prototype.setCount(1);

        groups.add(
                new Group(
                        prototype,
                        stack.getCount()
                )
        );
    }

    private static int findGroup(
            List<Group> groups,
            ItemStack stack
    ) {
        if (stack.isEmpty()) {
            return -1;
        }

        for (int i = 0;
             i < groups.size();
             i++) {

            if (ItemStack.isSameItemSameTags(
                    groups.get(i).prototype,
                    stack
            )) {
                return i;
            }
        }

        return -1;
    }

    private static List<ItemStack> emptyStacks(
            int size
    ) {
        List<ItemStack> result =
                new ArrayList<>(size);

        for (int i = 0; i < size; i++) {
            result.add(ItemStack.EMPTY);
        }

        return result;
    }

    private static List<ItemStack> mutableCopy(
            List<ItemStack> stacks
    ) {
        List<ItemStack> result =
                new ArrayList<>(stacks.size());

        for (ItemStack stack : stacks) {
            result.add(stack.copy());
        }

        return result;
    }

    public enum Error {
        NONE,
        MISSING_ITEMS,
        INVENTORY_FULL
    }

    public record Plan(
            Error error,
            List<ItemStack> input,
            List<ItemStack> playerInventory,
            List<Integer> missingGridSlots,
            int sets
    ) {
        public Plan {
            input = copy(input);
            playerInventory =
                    copy(playerInventory);

            missingGridSlots =
                    List.copyOf(
                            missingGridSlots
                    );
        }

        public boolean success() {
            return error == Error.NONE;
        }

        private static List<ItemStack> copy(
                List<ItemStack> stacks
        ) {
            return stacks.stream()
                    .map(ItemStack::copy)
                    .toList();
        }
    }

    private record Cell(
            int gridIndex,
            EndCraftingIngredient ingredient,
            int currentGroup
    ) {
    }

    private static final class Group {
        private final ItemStack prototype;
        private int count;

        private Group(
                ItemStack prototype,
                int count
        ) {
            this.prototype = prototype;
            this.count = count;
        }
    }

    private record Attempt(
            Error error,
            List<ItemStack> input,
            List<ItemStack> inventory,
            List<Integer> missing,
            int sets
    ) {
        private static Attempt success(
                List<ItemStack> input,
                List<ItemStack> inventory,
                int sets
        ) {
            return new Attempt(
                    Error.NONE,
                    input,
                    inventory,
                    List.of(),
                    sets
            );
        }

        private static Attempt failed(
                Error error,
                List<Integer> missing
        ) {
            return new Attempt(
                    error,
                    List.of(),
                    List.of(),
                    missing,
                    0
            );
        }

        private Plan toPlan() {
            return new Plan(
                    error,
                    input,
                    inventory,
                    missing,
                    sets
            );
        }
    }
}