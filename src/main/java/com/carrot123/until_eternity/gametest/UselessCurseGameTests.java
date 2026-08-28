package com.carrot123.until_eternity.gametest;

import com.carrot123.until_eternity.until_eternity;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.EnchantedBookItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.minecraftforge.gametest.GameTestHolder;

import java.util.List;

@GameTestHolder(until_eternity.MODID)
public final class UselessCurseGameTests {
    private static final ResourceLocation USELESS_CURSE_ID =
            new ResourceLocation(until_eternity.MODID, "useless_curse");

    private UselessCurseGameTests() {
    }

    @GameTest(template = "statue", timeoutTicks = 100)
    public static void uselessCurseUsesVanillaCandidateAndStoragePaths(
            GameTestHelper helper) {
        Enchantment curse = BuiltInRegistries.ENCHANTMENT.get(
                USELESS_CURSE_ID);
        helper.assertTrue(curse != null,
                "Useless Curse must exist in the enchantment registry");
        helper.assertTrue(curse.isCurse(),
                "Useless Curse must be recognized as a real curse");
        helper.assertTrue(curse.getMinLevel() == 1
                        && curse.getMaxLevel() == 1,
                "Useless Curse must only have level I");
        helper.assertTrue(!curse.isTreasureOnly()
                        && curse.isDiscoverable(),
                "Useless Curse must be available to normal random enchanting");

        Component fullName = curse.getFullname(1);
        helper.assertTrue(fullName.getStyle().getColor() != null
                        && fullName.getStyle().getColor().getValue()
                        == ChatFormatting.RED.getColor(),
                "Useless Curse must use the vanilla curse color");
        helper.assertTrue(fullName.getSiblings().stream()
                        .map(Component::getContents)
                        .filter(TranslatableContents.class::isInstance)
                        .map(TranslatableContents.class::cast)
                        .anyMatch(contents -> "enchantment.level.1"
                                .equals(contents.getKey())),
                "The displayed name must include the vanilla level I suffix");

        for (Item item : List.of(
                Items.NETHERITE_SWORD,
                Items.NETHERITE_AXE,
                Items.NETHERITE_PICKAXE,
                Items.NETHERITE_SHOVEL,
                Items.NETHERITE_HOE,
                Items.BOW,
                Items.CROSSBOW,
                Items.TRIDENT,
                Items.NETHERITE_HELMET,
                Items.NETHERITE_CHESTPLATE,
                Items.NETHERITE_LEGGINGS,
                Items.NETHERITE_BOOTS,
                Items.SHIELD)) {
            ItemStack stack = new ItemStack(item);
            helper.assertTrue(curse.canEnchant(stack),
                    "Expected a breakable item to accept Useless Curse: "
                            + BuiltInRegistries.ITEM.getKey(item));
        }
        for (Item item : List.of(Items.DIAMOND, Items.IRON_INGOT,
                Items.STICK)) {
            helper.assertTrue(!curse.canEnchant(new ItemStack(item)),
                    "Ordinary materials must reject Useless Curse: "
                            + BuiltInRegistries.ITEM.getKey(item));
        }

        assertCandidate(helper, curse, new ItemStack(Items.BOOK));
        assertCandidate(helper, curse,
                new ItemStack(Items.NETHERITE_SWORD));

        ItemStack sword = new ItemStack(Items.NETHERITE_SWORD);
        sword.enchant(curse, 1);
        ItemStack loadedSword = ItemStack.of(
                sword.save(new CompoundTag()));
        helper.assertTrue(loadedSword.getEnchantmentLevel(curse) == 1,
                "The curse must survive normal item NBT save and load");

        ItemStack book = new ItemStack(Items.ENCHANTED_BOOK);
        EnchantedBookItem.addEnchantment(book,
                new EnchantmentInstance(curse, 1));
        ItemStack loadedBook = ItemStack.of(book.save(new CompoundTag()));
        helper.assertTrue(EnchantmentHelper.getEnchantments(loadedBook)
                        .getOrDefault(curse, 0) == 1,
                "The curse must survive enchanted-book NBT save and load");
        helper.succeed();
    }

    private static void assertCandidate(GameTestHelper helper,
            Enchantment curse, ItemStack stack) {
        boolean present = EnchantmentHelper
                .getAvailableEnchantmentResults(30, stack, false)
                .stream()
                .anyMatch(instance -> instance.enchantment == curse
                        && instance.level == 1);
        helper.assertTrue(present,
                "Useless Curse must be present in the level-30 candidate pool for "
                        + BuiltInRegistries.ITEM.getKey(stack.getItem()));
    }
}
