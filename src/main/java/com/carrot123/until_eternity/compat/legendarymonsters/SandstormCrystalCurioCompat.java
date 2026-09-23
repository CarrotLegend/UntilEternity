package com.carrot123.until_eternity.compat.legendarymonsters;

import com.carrot123.until_eternity.item.curio.CurioModifierId;
import com.carrot123.until_eternity.registry.ModAttributes;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurioItem;

import java.util.UUID;

public final class SandstormCrystalCurioCompat {
    private static final ResourceLocation ITEM_ID =
            new ResourceLocation(
                    "legendary_monsters",
                    "crystal_of_sandstorm"
            );

    private SandstormCrystalCurioCompat() {
    }

    public static void register() {
        Item item = ForgeRegistries.ITEMS.getValue(ITEM_ID);

        if (item == null) {
            return;
        }

        CuriosApi.registerCurio(
                item,
                new SandstormCrystalCurio()
        );
    }

    private static final class SandstormCrystalCurio
            implements ICurioItem {

        @Override
        public boolean canEquip(
                SlotContext slotContext,
                ItemStack stack
        ) {
            return !slotContext.cosmetic()
                    && "necklace".equals(
                            slotContext.identifier()
                    );
        }

        @Override
        public Multimap<Attribute, AttributeModifier>
        getAttributeModifiers(
                SlotContext slotContext,
                UUID slotUuid,
                ItemStack stack
        ) {
            if (slotContext.cosmetic()
                    || !"necklace".equals(
                            slotContext.identifier()
                    )) {
                return ImmutableMultimap.of();
            }

            ImmutableMultimap.Builder<
                    Attribute,
                    AttributeModifier
                    > builder =
                    ImmutableMultimap.builder();

            builder.put(
                    ModAttributes.ALL_DAMAGE.get(),
                    new AttributeModifier(
                            CurioModifierId.create(
                                    slotUuid,
                                    "sandstorm_crystal/all_damage"
                            ),
                            "until_eternity.sandstorm_crystal.all_damage",
                            0.02D,
                            AttributeModifier.Operation.MULTIPLY_BASE
                    )
            );

            builder.put(
                    Attributes.MOVEMENT_SPEED,
                    new AttributeModifier(
                            CurioModifierId.create(
                                    slotUuid,
                                    "sandstorm_crystal/movement_speed"
                            ),
                            "until_eternity.sandstorm_crystal.movement_speed",
                            0.05D,
                            AttributeModifier.Operation.MULTIPLY_TOTAL
                    )
            );

            builder.put(
                    org.confluence.terra_curio.misc.ModAttributes
                            .DODGE_CHANCE
                            .get(),
                    new AttributeModifier(
                            CurioModifierId.create(
                                    slotUuid,
                                    "sandstorm_crystal/dodge_chance"
                            ),
                            "until_eternity.sandstorm_crystal.dodge_chance",
                            0.05D,
                            AttributeModifier.Operation.ADDITION
                    )
            );

            return builder.build();
        }
    }
}