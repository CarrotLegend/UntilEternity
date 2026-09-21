package com.carrot123.until_eternity.item.curio;

import com.carrot123.until_eternity.compat.eternalcareer.ChefRankHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.registries.ForgeRegistries;
import top.theillusivec4.curios.api.SlotContext;

import javax.annotation.Nullable;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

public final class HeadChefGlovesItem extends BaseModCurioItem {

    private static final ResourceLocation ITEM_ID =
            new ResourceLocation(
                    "until_eternity",
                    "head_chef_gloves"
            );

    private static final TagKey<Item> KNIVES =
            TagKey.create(
                    Registries.ITEM,
                    new ResourceLocation(
                            "farmersdelight",
                            "tools/knives"
                    )
            );

    private static final UUID KNIFE_REACH_MODIFIER_ID =
            UUID.nameUUIDFromBytes(
                    "until_eternity:head_chef_gloves/knife_reach"
                            .getBytes(StandardCharsets.UTF_8)
            );

    private static final AttributeModifier KNIFE_REACH_MODIFIER =
            new AttributeModifier(
                    KNIFE_REACH_MODIFIER_ID,
                    "until_eternity:head_chef_gloves/knife_reach",
                    1.0D,
                    AttributeModifier.Operation.ADDITION
            );

    public HeadChefGlovesItem() {
        super(
                new Properties(),
                ITEM_ID,
                List.of(
                        CurioAttributeSpec.of(
                                () -> ForgeRegistries.ATTRIBUTES.getValue(
                                        ChefRankHelper.KITCHENWARE_DAMAGE_ID
                                ),
                                "kitchenware_damage",
                                0.15D,
                                AttributeModifier.Operation.MULTIPLY_BASE
                        )
                )
        );
    }

    @Override
    public boolean canEquip(
            SlotContext slotContext,
            ItemStack stack
    ) {
        return slotContext != null
                && "hands".equals(slotContext.identifier())
                && !slotContext.cosmetic()
                && CurioMutualExclusionHandler.canEquip(
                        slotContext,
                        stack
                );
    }

    @Override
    public void curioTick(
            SlotContext slotContext,
            ItemStack stack
    ) {
        if (!(slotContext.entity() instanceof Player player)
                || player.level().isClientSide) {
            return;
        }

        AttributeInstance reach =
                player.getAttribute(
                        ForgeMod.ENTITY_REACH.get()
                );

        if (reach == null) {
            return;
        }

        if (player.getMainHandItem().is(KNIVES)) {
            if (reach.getModifier(
                    KNIFE_REACH_MODIFIER_ID
            ) == null) {
                reach.addTransientModifier(
                        KNIFE_REACH_MODIFIER
                );
            }
        } else {
            reach.removeModifier(
                    KNIFE_REACH_MODIFIER_ID
            );
        }
    }

    @Override
    public void onUnequip(
            SlotContext slotContext,
            ItemStack newStack,
            ItemStack stack
    ) {
        if (!(slotContext.entity() instanceof Player player)
                || player.level().isClientSide) {
            return;
        }

        AttributeInstance reach =
                player.getAttribute(
                        ForgeMod.ENTITY_REACH.get()
                );

        if (reach != null) {
            reach.removeModifier(
                    KNIFE_REACH_MODIFIER_ID
            );
        }
    }

    @Override
    public void appendHoverText(
            ItemStack stack,
            @Nullable Level level,
            List<Component> tooltip,
            TooltipFlag flag
    ) {
        tooltip.add(
                Component.translatable(
                        "tooltip.until_eternity.head_chef_gloves.knife_reach"
                ).withStyle(ChatFormatting.DARK_PURPLE)
        );
    }
}