package com.carrot123.until_eternity.mixin.compat.goetyrevelation;

import com.carrot123.until_eternity.compat.goetyrevelation.BowOfRevelationEffectEvents;
import com.carrot123.until_eternity.effect.VoidCorrosionEffectApplier;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraftforge.registries.ForgeRegistries;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(Arrow.class)
public abstract class BowOfRevelationArrowEffectMixin {

    private static final int DURATION = 200;
    private static final int AMPLIFIER = 0;

    private static final ResourceLocation VOID_CORROSION =
            new ResourceLocation(
                    "until_eternity",
                    "void_corrosion"
            );

    private static final List<ResourceLocation> EFFECT_POOL =
            List.of(
                    new ResourceLocation(
                            "goety",
                            "sapped"
                    ),
                    new ResourceLocation(
                            "goety",
                            "ender_ground"
                    ),
                    new ResourceLocation(
                            "goety",
                            "wane"
                    ),
                    new ResourceLocation(
                            "goety",
                            "busted"
                    ),
                    VOID_CORROSION,
                    new ResourceLocation(
                            "minecraft",
                            "weakness"
                    ),
                    new ResourceLocation(
                            "minecraft",
                            "blindness"
                    ),
                    new ResourceLocation(
                            "minecraft",
                            "poison"
                    ),
                    new ResourceLocation(
                            "minecraft",
                            "wither"
                    ),
                    new ResourceLocation(
                            "cataclysm",
                            "abyssal_burn"
                    ),
                    new ResourceLocation(
                            "cataclysm",
                            "blazing_brand"
                    ),
                    new ResourceLocation(
                            "irons_spellbooks",
                            "rend"
                    )
            );

    @Inject(
            method = {
                    "doPostHurtEffects",
                    "m_7761_"
            },
            at = @At("HEAD"),
            cancellable = true,
            remap = false
    )
    private void untilEternity$replaceRevelationBowEffects(
            LivingEntity target,
            CallbackInfo ci
    ) {
        Arrow arrow =
                (Arrow) (Object) this;

        if (!arrow.getPersistentData().getBoolean(
                BowOfRevelationEffectEvents.REVELATION_ARROW_TAG
        )) {
            return;
        }

        ci.cancel();

        if (target.level().isClientSide()) {
            return;
        }

        ResourceLocation effectId =
                EFFECT_POOL.get(
                        arrow.level().random.nextInt(
                                EFFECT_POOL.size()
                        )
                );

        Entity owner =
                arrow.getOwner();

        if (VOID_CORROSION.equals(effectId)) {
            if (owner instanceof Player player) {
                VoidCorrosionEffectApplier.forceApply(
                        target,
                        player
                );
            } else {
                applyNormalEffect(
                        target,
                        owner,
                        effectId
                );
            }

            return;
        }

        applyNormalEffect(
                target,
                owner,
                effectId
        );
    }

    private static void applyNormalEffect(
            LivingEntity target,
            Entity owner,
            ResourceLocation effectId
    ) {
        MobEffect effect =
                ForgeRegistries.MOB_EFFECTS
                        .getValue(effectId);

        if (effect == null) {
            return;
        }

        target.addEffect(
                new MobEffectInstance(
                        effect,
                        DURATION,
                        AMPLIFIER
                ),
                owner
        );
    }
}