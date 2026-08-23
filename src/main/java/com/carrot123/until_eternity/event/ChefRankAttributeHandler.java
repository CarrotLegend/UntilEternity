package com.carrot123.until_eternity.event;

import com.carrot123.until_eternity.compat.eternalcareer.ChefRank;
import com.carrot123.until_eternity.compat.eternalcareer.ChefRankHelper;
import com.carrot123.until_eternity.until_eternity;

import java.util.ArrayList;
import java.util.Collection;
import java.util.UUID;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;

import net.minecraftforge.event.ItemAttributeModifierEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

@Mod.EventBusSubscriber(
        modid = until_eternity.MODID,
        bus = Mod.EventBusSubscriber.Bus.FORGE
)
public final class ChefRankAttributeHandler {

    private ChefRankAttributeHandler() {
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onItemAttributeModifiers(
            ItemAttributeModifierEvent event
    ) {
        ItemStack stack = event.getItemStack();

        if (!ChefRankHelper.isChefArmor(stack)) {
            return;
        }

        if (!(stack.getItem() instanceof ArmorItem armorItem)) {
            return;
        }

        EquipmentSlot slot = event.getSlotType();

        if (armorItem.getEquipmentSlot() != slot) {
            return;
        }

        ChefRank rank = ChefRankHelper.getRank(stack);

        if (rank == ChefRank.NONE) {
            return;
        }

        RankBonuses bonuses = RankBonuses.forRank(rank);

        replaceVanillaArmorModifier(
                event,
                bonuses.armorBonus()
        );

        replaceVanillaToughnessModifier(
                event,
                bonuses.toughnessBonus()
        );

        replaceKitchenwareDamageModifier(
                event,
                slot,
                bonuses.kitchenwareDamageBonus()
        );

        replaceLuckModifier(
                event,
                slot,
                bonuses.luckBonus()
        );

        replaceMaxHealthModifier(
                event,
                slot,
                bonuses.maxHealthBonus()
        );
    }

    private static void replaceVanillaArmorModifier(
            ItemAttributeModifierEvent event,
            double bonus
    ) {
        if (bonus == 0.0D) {
            return;
        }

        AttributeModifier original =
                findOriginalAdditionModifier(
                        event,
                        Attributes.ARMOR
                );

        if (original == null) {
            return;
        }

        replaceOriginalModifier(
                event,
                Attributes.ARMOR,
                original,
                original.getAmount() + bonus
        );
    }

    private static void replaceVanillaToughnessModifier(
            ItemAttributeModifierEvent event,
            double bonus
    ) {
        if (bonus == 0.0D) {
            return;
        }

        AttributeModifier original =
                findOriginalAdditionModifier(
                        event,
                        Attributes.ARMOR_TOUGHNESS
                );

        if (original == null) {
            return;
        }

        replaceOriginalModifier(
                event,
                Attributes.ARMOR_TOUGHNESS,
                original,
                original.getAmount() + bonus
        );
    }

    private static void replaceKitchenwareDamageModifier(
            ItemAttributeModifierEvent event,
            EquipmentSlot slot,
            double bonus
    ) {
        if (bonus == 0.0D) {
            return;
        }

        Attribute kitchenwareDamage =
                ForgeRegistries.ATTRIBUTES.getValue(
                        ChefRankHelper.KITCHENWARE_DAMAGE_ID
                );

        if (kitchenwareDamage == null) {
            return;
        }

        UUID originalId =
                ChefRankHelper.getOriginalChefModifierId(
                        slot,
                        "kitchenware_damage"
                );

        AttributeModifier original =
                findOriginalModifierByUuid(
                        event,
                        kitchenwareDamage,
                        originalId
                );

        if (original == null) {
            return;
        }

        replaceOriginalModifier(
                event,
                kitchenwareDamage,
                original,
                original.getAmount() + bonus
        );
    }

    private static void replaceLuckModifier(
            ItemAttributeModifierEvent event,
            EquipmentSlot slot,
            double bonus
    ) {
        if (bonus == 0.0D) {
            return;
        }

        UUID originalId =
                ChefRankHelper.getOriginalChefModifierId(
                        slot,
                        "luck"
                );

        AttributeModifier original =
                findOriginalModifierByUuid(
                        event,
                        Attributes.LUCK,
                        originalId
                );

        if (original == null) {
            return;
        }

        replaceOriginalModifier(
                event,
                Attributes.LUCK,
                original,
                original.getAmount() + bonus
        );
    }

    private static void replaceMaxHealthModifier(
            ItemAttributeModifierEvent event,
            EquipmentSlot slot,
            double amount
    ) {
        if (amount <= 0.0D) {
            return;
        }

        UUID id =
                ChefRankHelper.getRankModifierId(
                        slot,
                        "max_health"
                );

        removeCurrentModifierByUuid(
                event,
                Attributes.MAX_HEALTH,
                id
        );

        event.addModifier(
                Attributes.MAX_HEALTH,
                new AttributeModifier(
                        id,
                        "until_eternity.chef_rank.max_health",
                        amount,
                        AttributeModifier.Operation.ADDITION
                )
        );
    }

    private static AttributeModifier findOriginalAdditionModifier(
            ItemAttributeModifierEvent event,
            Attribute attribute
    ) {
        Collection<AttributeModifier> originals =
                event.getOriginalModifiers()
                        .get(attribute);

        for (AttributeModifier modifier : originals) {
            if (modifier.getOperation()
                    == AttributeModifier.Operation.ADDITION) {
                return modifier;
            }
        }

        return null;
    }

    private static AttributeModifier findOriginalModifierByUuid(
            ItemAttributeModifierEvent event,
            Attribute attribute,
            UUID targetId
    ) {
        Collection<AttributeModifier> originals =
                event.getOriginalModifiers()
                        .get(attribute);

        for (AttributeModifier modifier : originals) {
            if (modifier.getId().equals(targetId)) {
                return modifier;
            }
        }

        return null;
    }

    private static void replaceOriginalModifier(
            ItemAttributeModifierEvent event,
            Attribute attribute,
            AttributeModifier original,
            double newAmount
    ) {
        removeCurrentModifierByUuid(
                event,
                attribute,
                original.getId()
        );

        event.addModifier(
                attribute,
                new AttributeModifier(
                        original.getId(),
                        original.getName(),
                        newAmount,
                        original.getOperation()
                )
        );
    }

    private static void removeCurrentModifierByUuid(
            ItemAttributeModifierEvent event,
            Attribute attribute,
            UUID targetId
    ) {
        Collection<AttributeModifier> current =
                new ArrayList<>(
                        event.getModifiers()
                                .get(attribute)
                );

        for (AttributeModifier modifier : current) {
            if (modifier.getId().equals(targetId)) {
                event.removeModifier(
                        attribute,
                        modifier
                );
            }
        }
    }

    private record RankBonuses(
            double armorBonus,
            double toughnessBonus,
            double kitchenwareDamageBonus,
            double luckBonus,
            double maxHealthBonus
    ) {
        private static final RankBonuses NONE = new RankBonuses(0.0D, 0.0D, 0.0D, 0.0D, 0.0D);

        private static RankBonuses forRank(
                ChefRank rank
        ) {
            return switch (rank) {
                case APPRENTICE -> new RankBonuses(1.0D, 0.0D, 0.25D, 0.0D, 20.0D);
                case INTERMEDIATE -> new RankBonuses(2.0D, 0.5D, 0.45D, 10.0D, 40.0D);
                case ADVANCED -> new RankBonuses(2.0D, 1.0D, 0.55D, 10.0D, 65.0D);
                case SENIOR -> new RankBonuses(2.0D, 1.0D, 0.75D, 10.0D, 90.0D);
                case MASTER -> new RankBonuses(3.0D, 2.0D, 0.95D, 20.0D, 90.0D);
                case NONE -> NONE;
            };
        }
    }
}