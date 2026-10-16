package com.carrot123.until_eternity.client.trial;

import com.carrot123.until_eternity.block.ModBlocks;
import com.carrot123.until_eternity.trial.TrialPortalBlock;
import com.carrot123.until_eternity.trial.TrialPortalBlockEntity;
import com.carrot123.until_eternity.trial.TrialPortalGeometry;
import com.carrot123.until_eternity.until_eternity;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexSorting;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.RegisterShadersEvent;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL14;
import org.lwjgl.opengl.GL30;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/** World-only portal refraction. No full-screen overlay is ever drawn over the HUD. */
@Mod.EventBusSubscriber(modid = until_eternity.MODID, value = Dist.CLIENT)
public final class TrialPortalVisuals {
    private static final int SEGMENTS = 96;
    private static final double MASK_RADIUS = 2.45;
    private static ShaderInstance shader;
    private static TextureTarget scene;
    private static TextureTarget mask;
    private static float strength;
    private static ClientLevel lastLevel;
    private static int lastScanTick = Integer.MIN_VALUE;
    private static final List<Plane> PLANES = new ArrayList<>();

    private TrialPortalVisuals() {}

    @Mod.EventBusSubscriber(modid = until_eternity.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static final class Registration {
        @SubscribeEvent public static void registerShaders(RegisterShadersEvent event) {
            shader = null;
            try {
                event.registerShader(new ShaderInstance(event.getResourceProvider(),
                        new ResourceLocation(until_eternity.MODID, "trial_refraction"), DefaultVertexFormat.POSITION_TEX),
                        loaded -> shader = loaded);
            } catch (IOException | RuntimeException ex) {
                until_eternity.LOGGER.warn("Trial refraction shader failed to load; using dynamic geometry", ex);
            }
            releaseTargets();
        }
    }

    @SubscribeEvent public static void logout(ClientPlayerNetworkEvent.LoggingOut event) { releaseTargets(); PLANES.clear(); lastLevel = null; strength = 0; }

    @SubscribeEvent public static void render(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_LEVEL) return;
        Minecraft minecraft = Minecraft.getInstance();
        ClientLevel level = minecraft.level;
        if (level == null || minecraft.player == null) return;
        if (lastLevel != level) { releaseTargets(); PLANES.clear(); lastLevel = level; lastScanTick = Integer.MIN_VALUE; strength = 0; }
        Vec3 camera = event.getCamera().getPosition();
        if (lastScanTick != minecraft.player.tickCount / 5) { scan(level, camera); lastScanTick = minecraft.player.tickCount / 5; }
        PLANES.removeIf(plane -> !(level.getBlockEntity(plane.anchor) instanceof TrialPortalBlockEntity portal)
                || portal.shapeVersion() != 2);
        float target = 0;
        for (Plane plane : PLANES) {
            double distance = camera.distanceTo(plane.center());
            float proximity = distance >= 16 ? 0 : distance >= 10 ? .15f * smooth((float)((16 - distance) / 6))
                    : distance >= 6 ? .15f + .25f * smooth((float)((10 - distance) / 4))
                    : distance >= 3 ? .40f + .60f * smooth((float)((6 - distance) / 3)) : 1f;
            target = Math.max(target, proximity);
        }
        strength += (target - strength) * .18f;
        if (PLANES.isEmpty() || strength < .005f) return;
        boolean wasDepth = GL11.glIsEnabled(GL11.GL_DEPTH_TEST);
        boolean wasBlend = GL11.glIsEnabled(GL11.GL_BLEND);
        boolean wasCull = GL11.glIsEnabled(GL11.GL_CULL_FACE);
        boolean wasDepthWrite = GL11.glGetBoolean(GL11.GL_DEPTH_WRITEMASK);
        int srcRgb = GL11.glGetInteger(GL14.GL_BLEND_SRC_RGB);
        int dstRgb = GL11.glGetInteger(GL14.GL_BLEND_DST_RGB);
        int srcAlpha = GL11.glGetInteger(GL14.GL_BLEND_SRC_ALPHA);
        int dstAlpha = GL11.glGetInteger(GL14.GL_BLEND_DST_ALPHA);
        ShaderInstance previousShader = RenderSystem.getShader();
        int oldFbo = GL11.glGetInteger(GL30.GL_DRAW_FRAMEBUFFER_BINDING);
        int oldReadFbo = GL11.glGetInteger(GL30.GL_READ_FRAMEBUFFER_BINDING);
        int[] viewport = new int[4];
        GL11.glGetIntegerv(GL11.GL_VIEWPORT, viewport);
        Matrix4f oldProjection = new Matrix4f(RenderSystem.getProjectionMatrix());
        VertexSorting oldSorting = RenderSystem.getVertexSorting();
        boolean pushedModelView = false;
        try {
            RenderSystem.getModelViewStack().pushPose();
            pushedModelView = true;
            RenderSystem.getModelViewStack().last().pose().identity();
            RenderSystem.applyModelViewMatrix();
            if (shader != null && ensureTargets(minecraft)) {
                RenderTarget main = minecraft.getMainRenderTarget();
                GL30.glBindFramebuffer(GL30.GL_READ_FRAMEBUFFER, main.frameBufferId);
                GL30.glBindFramebuffer(GL30.GL_DRAW_FRAMEBUFFER, scene.frameBufferId);
                GL30.glBlitFramebuffer(0, 0, main.width, main.height, 0, 0, scene.width, scene.height,
                        GL11.GL_COLOR_BUFFER_BIT, GL11.GL_NEAREST);
                mask.clear(Minecraft.ON_OSX);
                mask.copyDepthFrom(main);
                mask.bindWrite(false);
                RenderSystem.enableDepthTest();
                RenderSystem.depthMask(false);
                RenderSystem.disableCull();
                RenderSystem.disableBlend();
                drawPlanes(event, false);
                main.bindWrite(false);
                RenderSystem.disableDepthTest();
                shader.setSampler("SceneSampler", scene);
                shader.setSampler("MaskSampler", mask);
                if (shader.getUniform("Time") != null) shader.getUniform("Time").set((level.getGameTime() + event.getPartialTick()) / 20f);
                if (shader.getUniform("Strength") != null) shader.getUniform("Strength").set(strength);
                if (shader.getUniform("ScreenSize") != null) shader.getUniform("ScreenSize").set((float)main.width, (float)main.height);
                RenderSystem.setProjectionMatrix(new Matrix4f(), VertexSorting.DISTANCE_TO_ORIGIN);
                RenderSystem.setShader(() -> shader);
                BufferBuilder buffer = Tesselator.getInstance().getBuilder();
                buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
                Matrix4f identity = new Matrix4f();
                buffer.vertex(identity, -1, -1, 0).uv(0, 0).endVertex();
                buffer.vertex(identity, 1, -1, 0).uv(1, 0).endVertex();
                buffer.vertex(identity, 1, 1, 0).uv(1, 1).endVertex();
                buffer.vertex(identity, -1, 1, 0).uv(0, 1).endVertex();
                BufferUploader.drawWithShader(buffer.end());
            } else {
                minecraft.getMainRenderTarget().bindWrite(false);
                RenderSystem.enableDepthTest();
                RenderSystem.depthMask(false);
                RenderSystem.disableCull();
                RenderSystem.enableBlend();
                RenderSystem.defaultBlendFunc();
                drawPlanes(event, true);
            }
        } catch (RuntimeException ex) {
            until_eternity.LOGGER.warn("Trial portal shader unavailable; using dynamic geometry fallback", ex);
            shader = null;
        } finally {
            if (pushedModelView) {
                RenderSystem.getModelViewStack().popPose();
                RenderSystem.applyModelViewMatrix();
            }
            GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, oldFbo);
            GL30.glBindFramebuffer(GL30.GL_READ_FRAMEBUFFER, oldReadFbo);
            GL11.glViewport(viewport[0], viewport[1], viewport[2], viewport[3]);
            RenderSystem.setProjectionMatrix(oldProjection, oldSorting);
            RenderSystem.depthMask(wasDepthWrite);
            if (wasDepth) RenderSystem.enableDepthTest(); else RenderSystem.disableDepthTest();
            if (wasBlend) RenderSystem.enableBlend(); else RenderSystem.disableBlend();
            if (wasCull) RenderSystem.enableCull(); else RenderSystem.disableCull();
            RenderSystem.blendFuncSeparate(srcRgb, dstRgb, srcAlpha, dstAlpha);
            RenderSystem.setShader(() -> previousShader);
        }
    }

    private static float smooth(float value) {
        float t = Math.max(0, Math.min(1, value));
        return t * t * (3 - 2 * t);
    }

    private static boolean ensureTargets(Minecraft minecraft) {
        RenderTarget main = minecraft.getMainRenderTarget();
        if (main.width <= 0 || main.height <= 0) return false;
        if (scene != null && scene.width == main.width && scene.height == main.height) return true;
        releaseTargets();
        scene = new TextureTarget(main.width, main.height, true, Minecraft.ON_OSX);
        mask = new TextureTarget(main.width, main.height, true, Minecraft.ON_OSX);
        mask.setClearColor(0, 0, 0, 0);
        return true;
    }

    private static void releaseTargets() {
        if (scene != null) { scene.destroyBuffers(); scene = null; }
        if (mask != null) { mask.destroyBuffers(); mask = null; }
    }

    private static void scan(ClientLevel level, Vec3 camera) {
        PLANES.clear();
        Set<UUID> seen = new HashSet<>();
        BlockPos center = BlockPos.containing(camera);
        for (int x = -18; x <= 18; x++) for (int y = -8; y <= 8; y++) for (int z = -18; z <= 18; z++) {
            BlockPos pos = center.offset(x, y, z);
            if (level.getBlockState(pos).getBlock() != ModBlocks.TRIAL_PORTAL.get()
                    || !(level.getBlockEntity(pos) instanceof TrialPortalBlockEntity portal)
                    || portal.shapeVersion() != 2 || portal.anchor() == null || portal.portalId() == null
                    || !seen.add(portal.portalId())) continue;
            BlockPos anchor = portal.anchor();
            Direction.Axis axis = portal.getBlockState().getValue(TrialPortalBlock.AXIS);
            boolean complete = true;
            for (int dx = 0; dx < TrialPortalGeometry.WIDTH && complete; dx++)
                for (int dy = 0; dy < TrialPortalGeometry.HEIGHT; dy++) {
                BlockPos cell = TrialPortalGeometry.cell(anchor, axis, dx, dy);
                if (!(level.getBlockEntity(cell) instanceof TrialPortalBlockEntity part)
                        || !portal.portalId().equals(part.portalId())) { complete = false; break; }
            }
            if (complete) PLANES.add(new Plane(anchor, axis));
        }
    }

    private static void drawPlanes(RenderLevelStageEvent event, boolean fallback) {
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        BufferBuilder buffer = Tesselator.getInstance().getBuilder();
        buffer.begin(VertexFormat.Mode.TRIANGLES, DefaultVertexFormat.POSITION_COLOR);
        PoseStack pose = new PoseStack();
        pose.mulPose(Axis.XP.rotationDegrees(event.getCamera().getXRot()));
        pose.mulPose(Axis.YP.rotationDegrees(event.getCamera().getYRot() + 180f));
        Vec3 camera = event.getCamera().getPosition();
        pose.translate(-camera.x, -camera.y, -camera.z);
        Matrix4f transform = pose.last().pose();
        double time = (event.getRenderTick() + event.getPartialTick()) * .035;
        for (Plane plane : PLANES) {
            Vec3 center = plane.center();
            for (int i = 0; i < SEGMENTS; i++) {
                double a = Math.PI * 2 * i / SEGMENTS;
                double b = Math.PI * 2 * (i + 1) / SEGMENTS;
                if (!fallback) {
                    vertex(buffer, transform, center, plane.axis, 0, 0, 127, 127, 255, 255);
                    maskVertex(buffer, transform, center, plane.axis, a);
                    maskVertex(buffer, transform, center, plane.axis, b);
                } else {
                    int alpha = Math.max(24, (int)(100 * strength));
                    vertex(buffer, transform, center, plane.axis, 0, 0, 72, 30, 125, alpha);
                    fallbackVertex(buffer, transform, center, plane.axis, a, 1.86, 92, 40, 160, alpha);
                    fallbackVertex(buffer, transform, center, plane.axis, b, 1.86, 92, 40, 160, alpha);
                    double wobbleA = 2.0 + .07 * Math.sin(a * 7 - time * 2);
                    double wobbleB = 2.0 + .07 * Math.sin(b * 7 - time * 2);
                    int pulse = (int)(140 + 65 * Math.sin(a * 5 - time * 3));
                    fallbackVertex(buffer, transform, center, plane.axis, a, 1.86, 73, 22, 125, 180);
                    fallbackVertex(buffer, transform, center, plane.axis, a, wobbleA, 125, 84, pulse, 210);
                    fallbackVertex(buffer, transform, center, plane.axis, b, wobbleB, 125, 84, pulse, 210);
                    fallbackVertex(buffer, transform, center, plane.axis, a, 1.86, 73, 22, 125, 180);
                    fallbackVertex(buffer, transform, center, plane.axis, b, wobbleB, 125, 84, pulse, 210);
                    fallbackVertex(buffer, transform, center, plane.axis, b, 1.86, 73, 22, 125, 180);
                }
            }
        }
        BufferUploader.drawWithShader(buffer.end());
    }

    private static void maskVertex(BufferBuilder buffer, Matrix4f matrix, Vec3 center, Direction.Axis axis, double angle) {
        vertex(buffer, matrix, center, axis, Math.cos(angle) * MASK_RADIUS, Math.sin(angle) * MASK_RADIUS,
                (int)(127 + 127 * Math.cos(angle)), (int)(127 + 127 * Math.sin(angle)), 255, 255);
    }

    private static void fallbackVertex(BufferBuilder buffer, Matrix4f matrix, Vec3 center, Direction.Axis axis,
                                       double angle, double radius, int r, int g, int b, int alpha) {
        vertex(buffer, matrix, center, axis, Math.cos(angle) * radius, Math.sin(angle) * radius, r, g, b, alpha);
    }

    private static void vertex(BufferBuilder buffer, Matrix4f matrix, Vec3 center, Direction.Axis axis,
                               double horizontal, double vertical, int r, int g, int b, int alpha) {
        double x = center.x + (axis == Direction.Axis.X ? horizontal : 0);
        double z = center.z + (axis == Direction.Axis.Z ? horizontal : 0);
        buffer.vertex(matrix, (float)x, (float)(center.y + vertical), (float)z)
                .color(r, g, b, alpha).endVertex();
    }

    private record Plane(BlockPos anchor, Direction.Axis axis) {
        Vec3 center() { return TrialPortalGeometry.center(anchor, axis); }
    }
}
