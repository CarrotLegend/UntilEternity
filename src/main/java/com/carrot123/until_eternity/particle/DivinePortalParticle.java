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

public final class DivinePortalParticle extends TextureSheetParticle {
    private final double startX;
    private final double startY;
    private final double startZ;

    private DivinePortalParticle(ClientLevel level, double x, double y, double z,
                                 double vx, double vy, double vz) {
        super(level, x, y, z);
        this.startX = x;
        this.startY = y;
        this.startZ = z;
        this.xd = vx;
        this.yd = vy;
        this.zd = vz;
        this.quadSize = 0.07F + random.nextFloat() * 0.055F;
        this.rCol = 1.0F;
        this.gCol = 0.72F + random.nextFloat() * 0.22F;
        this.bCol = 0.16F + random.nextFloat() * 0.12F;
        this.lifetime = 28 + random.nextInt(20);
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    @Override
    public void move(double x, double y, double z) {
        setBoundingBox(getBoundingBox().move(x, y, z));
        setLocationFromBoundingbox();
    }

    @Override
    public void tick() {
        xo = x;
        yo = y;
        zo = z;
        if (age++ >= lifetime) {
            remove();
            return;
        }
        float progress = (float) age / lifetime;
        float curve = 1.0F + progress - 2.0F * progress * progress;
        x = startX + xd * curve;
        y = startY + yd * curve + 0.3D * progress;
        z = startZ + zd * curve;
        alpha = 1.0F - progress * progress;
    }

    @Override
    public int getLightColor(float partialTick) {
        return 0xF000F0;
    }

    @Mod.EventBusSubscriber(modid = until_eternity.MODID,
            bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static final class ProviderRegistration {
        @SubscribeEvent
        public static void register(RegisterParticleProvidersEvent event) {
            event.registerSpriteSet(ModParticles.DIVINE_PORTAL_PARTICLE.get(), Provider::new);
        }
    }

    public static final class Provider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprites;
        public Provider(SpriteSet sprites) { this.sprites = sprites; }

        @Override
        public Particle createParticle(SimpleParticleType type, ClientLevel level,
                                       double x, double y, double z,
                                       double vx, double vy, double vz) {
            DivinePortalParticle particle = new DivinePortalParticle(level, x, y, z, vx, vy, vz);
            particle.pickSprite(sprites);
            return particle;
        }
    }
}
