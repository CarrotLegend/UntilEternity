package com.carrot123.until_eternity.particle;

import com.carrot123.until_eternity.until_eternity;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterParticleProvidersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Direct port of vanilla PortalParticle with gray tint.
 * Uses the same quadratic-drift-from-start-position formula.
 * xd/yd/zd are TOTAL displacement, not per-tick velocity.
 */
public class ChaosParticle extends TextureSheetParticle {
    private final double xStart;
    private final double yStart;
    private final double zStart;

    protected ChaosParticle(ClientLevel level, double x, double y, double z,
                             double dx, double dy, double dz) {
        super(level, x, y, z);
        this.xd = dx;
        this.yd = dy;
        this.zd = dz;
        this.x = x;
        this.y = y;
        this.z = z;
        this.xStart = x;
        this.yStart = y;
        this.zStart = z;
        this.quadSize = 0.1F * (this.random.nextFloat() * 0.2F + 0.5F);
        // Gray tint instead of purple
        float gray = this.random.nextFloat() * 0.4F + 0.4F;
        this.rCol = gray;
        this.gCol = gray;
        this.bCol = gray;
        this.lifetime = (int)(Math.random() * 10.0) + 40;
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_OPAQUE;
    }

    @Override
    public void move(double dx, double dy, double dz) {
        this.setBoundingBox(this.getBoundingBox().move(dx, dy, dz));
        this.setLocationFromBoundingbox();
    }

    @Override
    public float getQuadSize(float partialTick) {
        float f = ((float)this.age + partialTick) / (float)this.lifetime;
        f = 1.0F - f;
        f *= f;
        f = 1.0F - f;
        return this.quadSize * f;
    }

    @Override
    public int getLightColor(float partialTick) {
        int i = super.getLightColor(partialTick);
        float f = (float)this.age / (float)this.lifetime;
        f *= f;
        f *= f;
        int j = i & 0xFF;
        int k = i >> 16 & 0xFF;
        k += (int)(f * 15.0F * 16.0F);
        if (k > 240) {
            k = 240;
        }
        return j | k << 16;
    }

    @Override
    public void tick() {
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;
        if (this.age++ >= this.lifetime) {
            this.remove();
        } else {
            float f = (float)this.age / (float)this.lifetime;
            float f1 = -f + f * f * 2.0F;
            float f2 = 1.0F - f1;
            // Quadratic drift: particles drift out then sink back toward origin
            this.x = this.xStart + this.xd * f2;
            this.y = this.yStart + this.yd * f2 + (double)(1.0F - f);
            this.z = this.zStart + this.zd * f2;
        }
    }

    @Mod.EventBusSubscriber(modid = until_eternity.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ChaosParticleProvider {
        @SubscribeEvent
        public static void registerParticleProviders(RegisterParticleProvidersEvent event) {
            event.registerSpriteSet(ModParticles.CHAOS_PARTICLE.get(), SpriteProvider::new);
        }
    }

    public static class SpriteProvider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprite;

        public SpriteProvider(SpriteSet sprite) {
            this.sprite = sprite;
        }

        @Override
        public Particle createParticle(SimpleParticleType type, ClientLevel level,
                                        double x, double y, double z,
                                        double dx, double dy, double dz) {
            ChaosParticle particle = new ChaosParticle(level, x, y, z, dx, dy, dz);
            particle.pickSprite(this.sprite);
            return particle;
        }
    }
}
