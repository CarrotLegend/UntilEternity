package com.carrot123.until_eternity.compat.eeeabsmobs;

import com.eeeab.eeeabsmobs.sever.block.BlockErosionPortal;
import com.eeeab.eeeabsmobs.sever.entity.immortal.EntityImmortalExecutioner;
import com.eeeab.eeeabsmobs.sever.entity.immortal.EntityImmortalKnight;
import com.eeeab.eeeabsmobs.sever.entity.immortal.EntityImmortalSkeleton;
import com.eeeab.eeeabsmobs.sever.init.BlockInit;
import com.eeeab.eeeabsmobs.sever.init.EntityInit;
import com.eeeab.eeeabsmobs.sever.item.ItemGuardianCore;
import com.eeeab.eeeabsmobs.sever.world.portal.CuboidPortalShape;
import com.eeeab.eeeabsmobs.sever.world.portal.VoidCrackTeleporter;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.NetherPortalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.border.WorldBorder;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EeeabImmortalPortalContractTest {
    @Test
    void exactPortalAndEntityClassesRemainPresent() throws Exception {
        assertTrue(NetherPortalBlock.class.isAssignableFrom(
                BlockErosionPortal.class));
        assertNotNull(BlockErosionPortal.class.getDeclaredMethod(
                "entityInside",
                BlockState.class,
                Level.class,
                BlockPos.class,
                Entity.class));
        assertNotNull(BlockErosionPortal.class.getDeclaredMethod(
                "portalSpawn", Level.class, BlockPos.class));
        assertNotNull(ItemGuardianCore.class.getDeclaredMethod(
                "useOn", UseOnContext.class));

        assertNotNull(EntityInit.class.getDeclaredField(
                "IMMORTAL_SKELETON"));
        assertNotNull(EntityInit.class.getDeclaredField(
                "IMMORTAL_KNIGHT"));
        assertNotNull(EntityInit.class.getDeclaredField(
                "IMMORTAL_EXECUTIONER"));
        assertTrue(EntityInit.class.getDeclaredField("IMMORTAL_SKELETON")
                .getGenericType().getTypeName().contains(
                        EntityImmortalSkeleton.class.getName()));
        assertTrue(EntityInit.class.getDeclaredField("IMMORTAL_KNIGHT")
                .getGenericType().getTypeName().contains(
                        EntityImmortalKnight.class.getName()));
        assertTrue(EntityInit.class.getDeclaredField("IMMORTAL_EXECUTIONER")
                .getGenericType().getTypeName().contains(
                        EntityImmortalExecutioner.class.getName()));
    }

    @Test
    void eeeabPortalForcerOffersPoiSearchAndCreation()
            throws Exception {
        assertNotNull(VoidCrackTeleporter.class.getDeclaredMethod(
                "findPortalAround",
                BlockPos.class,
                boolean.class,
                WorldBorder.class));
        assertNotNull(VoidCrackTeleporter.class.getDeclaredMethod(
                "createPortal",
                BlockPos.class,
                net.minecraft.core.Direction.Axis.class));
        assertNotNull(VoidCrackTeleporter.class.getDeclaredField("poi"));
        assertNotNull(BlockInit.class.getDeclaredField("EROSION_PORTAL"));
        assertNotNull(BlockInit.class.getDeclaredField(
                "EROSION_DEEPSLATE_BRICKS"));
        assertNotNull(CuboidPortalShape.class.getDeclaredMethod(
                "getRelativePosition",
                net.minecraft.BlockUtil.FoundRectangle.class,
                net.minecraft.core.Direction.Axis.class,
                net.minecraft.world.phys.Vec3.class,
                net.minecraft.world.entity.EntityDimensions.class));
    }
}
