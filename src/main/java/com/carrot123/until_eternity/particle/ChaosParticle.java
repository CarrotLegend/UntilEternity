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

public class ChaosParticle extends TextureSheetParticle {

    protected ChaosParticle(ClientLevel level, double x, double y, double z,
                            double dx, double dy, double dz) {
        super(level, x, y, z);
        this.xd = dx;
        this.yd = dy;
        this.zd = dz;
        this.x = x;
        this.y = y;
        this.z = z;
        this.lifetime = (int)(Math.random() * 10.0) + 40;
        this.hasPhysics = false;
        // Gray-tinted portal particle
        this.rCol = 0.5F;
        this.gCol = 0.5F;
        this.bCol = 0.5F;
        this.quadSize = 0.2F;
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    @Override
    public void tick() {
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;
        if (this.age++ >= this.lifetime) {
            this.remove();
        } else {
            float progress = (float)this.age / this.lifetime;
            this.x += this.xd * progress;
            this.y += this.yd * progress;
            this.z += this.zd * progress;
        }
    }

    @Override
    public void move(double dx, double dy, double dz) {
        this.setBoundingBox(this.getBoundingBox().move(dx, dy, dz));
        this.setLocationFromBoundingbox();
    }

    @Override
    public float getQuadSize(float partialTick) {
        float progress = (this.age + partialTick) / this.lifetime;
        return this.quadSize * (1.0F - progress * progress * 0.5F);
    }

    @Override
    public int getLightColor(float partialTick) {
        return 15728880;
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
