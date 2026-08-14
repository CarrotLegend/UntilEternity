package com.carrot123.until_eternity.mixin.compat.irons_spellbooks;

import com.google.common.collect.Multimap;
import io.redspace.ironsspellbooks.item.curios.CurioBaseItem;
import io.redspace.ironsspellbooks.item.curios.TeleportationAmuletItem;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.capabilities.magic.MagicManager;
import io.redspace.ironsspellbooks.player.ServerPlayerEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import org.junit.jupiter.api.Test;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICuriosItemHandler;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.UUID;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class IronsSpellbooks3156ContractTest {
    @Test
    void curioBaseItemHasThe3156AttributeFactoryContract() throws Exception {
        Field attributeSlot = CurioBaseItem.class.getDeclaredField("attributeSlot");
        Field attributes = CurioBaseItem.class.getDeclaredField("attributes");
        Method method = CurioBaseItem.class.getDeclaredMethod(
                "getAttributeModifiers",
                SlotContext.class,
                UUID.class,
                ItemStack.class
        );

        assertEquals(String.class, attributeSlot.getType());
        assertEquals(Function.class, attributes.getType());
        assertFalse(Modifier.isPrivate(attributes.getModifiers()));
        assertEquals(Multimap.class, method.getReturnType());
        assertNotNull(attributes.getGenericType());
    }

    @Test
    void teleportationAmuletHasThe3156CurseLambdaContract() throws Exception {
        Method lambda = TeleportationAmuletItem.class.getDeclaredMethod(
                "lambda$handleCurse$0",
                SlotContext.class,
                ItemStack.class,
                ICuriosItemHandler.class
        );

        assertEquals(void.class, lambda.getReturnType());
        assertTruePrivate(lambda);
    }

    @Test
    void abstractSpellHasThe3156SpellPowerContract() throws Exception {
        Method method = AbstractSpell.class.getDeclaredMethod(
                "getSpellPower",
                int.class,
                Entity.class);

        assertEquals(float.class, method.getReturnType());
    }

    @Test
    void manaFlowHasThe3156MixinContracts() throws Exception {
        assertEquals(boolean.class, AbstractSpell.class.getDeclaredMethod(
                "attemptInitiateCast",
                ItemStack.class,
                int.class,
                Level.class,
                Player.class,
                io.redspace.ironsspellbooks.api.spells.CastSource.class,
                boolean.class,
                String.class).getReturnType());
        assertEquals(void.class, AbstractSpell.class.getDeclaredMethod(
                "castSpell",
                Level.class,
                int.class,
                net.minecraft.server.level.ServerPlayer.class,
                io.redspace.ironsspellbooks.api.spells.CastSource.class,
                boolean.class).getReturnType());
        assertEquals(void.class, MagicManager.class.getDeclaredMethod(
                "lambda$tick$0",
                boolean.class,
                Player.class).getReturnType());
        assertEquals(void.class, ServerPlayerEvents.class.getDeclaredMethod(
                "onUseItem",
                PlayerInteractEvent.RightClickItem.class).getReturnType());
    }

    private static void assertTruePrivate(Method method) {
        org.junit.jupiter.api.Assertions.assertTrue(
                Modifier.isPrivate(method.getModifiers()));
    }
}
