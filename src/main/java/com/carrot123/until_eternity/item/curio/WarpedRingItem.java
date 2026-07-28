package com.carrot123.until_eternity.item.curio;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.ForgeRegistries;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurioItem;

import javax.annotation.Nullable;
import java.util.List;
import java.util.UUID;

public class WarpedRingItem extends Item implements ICurioItem {

    private static final String SLOT = "warped_ring";

    private final String attrNamespace;
    private final String attrPath;
    private final double amount;
    private final AttributeModifier.Operation operation;
    private final int maxCount;
    private final UUID attrUuid;

    public WarpedRingItem(String attrNamespace, String attrPath,
                          double amount, AttributeModifier.Operation operation,
                          int maxCount, UUID attrUuid) {
        super(new Properties().stacksTo(1).rarity(Rarity.RARE).fireResistant());
        this.attrNamespace = attrNamespace;
        this.attrPath = attrPath;
        this.amount = amount;
        this.operation = operation;
        this.maxCount = maxCount;
        this.attrUuid = attrUuid;
    }

    @Override
    public Multimap<Attribute, AttributeModifier> getAttributeModifiers(
            SlotContext slotContext, UUID uuid, ItemStack stack) {
        return buildModifiers();
    }

    @Override
    public Multimap<Attribute, AttributeModifier> getAttributeModifiers(
            String identifier, ItemStack stack) {
        return buildModifiers();
    }

    private Multimap<Attribute, AttributeModifier> buildModifiers() {
        Multimap<Attribute, AttributeModifier> modifiers = ArrayListMultimap.create();
        Attribute attr = ForgeRegistries.ATTRIBUTES.getValue(
                new ResourceLocation(attrNamespace, attrPath));
        if (attr != null) {
            modifiers.put(attr, new AttributeModifier(
                    attrUuid, "Warped ring modifier",
                    amount, operation));
        }
        return modifiers;
    }

    @Override
    public boolean canEquip(SlotContext slotContext, ItemStack stack) {
        return CuriosApi.getCuriosInventory(slotContext.entity()).map(handler -> {
            int count = 0;
            var stacksHandler = handler.getCurios().get(SLOT);
            if (stacksHandler != null) {
                for (int i = 0; i < stacksHandler.getSlots(); i++) {
                    ItemStack s = stacksHandler.getStacks().getStackInSlot(i);
                    if (s.getItem() == this) count++;
                }
            }
            return count < maxCount;
        }).orElse(false);
    }
}
