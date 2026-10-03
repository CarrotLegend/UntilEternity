package com.carrot123.until_eternity.mixin.compat.goetyrevelation;

import com.Polarice3.Goety.common.entities.projectiles.DeathArrow;
import com.Polarice3.Goety.utils.CuriosFinder;
import com.Polarice3.Goety.utils.LichdomHelper;
import com.Polarice3.Goety.utils.MobUtil;
import com.carrot123.until_eternity.compat.goetyrevelation.BowOfRevelationEffectEvents;
import com.carrot123.until_eternity.effect.VoidCorrosionEffectApplier;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.ForgeRegistries;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;

@Mixin(value = DeathArrow.class, priority = 2000)
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
        DeathArrow arrow =
                (DeathArrow) (Object) this;

        if (!arrow.getPersistentData().getBoolean(
                BowOfRevelationEffectEvents.REVELATION_ARROW_TAG
        )) {
            return;
        }

        ci.cancel();

        if (target.level().isClientSide()) {
            return;
        }

        target.invulnerableTime = 0;

        preserveDeathArrowOwnerEffects(
                arrow,
                target
        );

        List<ResourceLocation> availableEffects =
                new ArrayList<>();

        for (ResourceLocation effectId : EFFECT_POOL) {
            if (VOID_CORROSION.equals(effectId)) {
                availableEffects.add(effectId);
                continue;
            }

            if (ForgeRegistries.MOB_EFFECTS
                    .getValue(effectId) != null) {
                availableEffects.add(effectId);
            }
        }

        if (availableEffects.isEmpty()) {
            return;
        }

        ResourceLocation effectId =
                availableEffects.get(
                        arrow.level().random.nextInt(
                                availableEffects.size()
                        )
                );

        Entity owner = arrow.getOwner();

        if (VOID_CORROSION.equals(effectId)) {
            if (owner instanceof Player player) {
                VoidCorrosionEffectApplier.forceApply(
                        target,
                        player
                );
            }
            return;
        }

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

    private static void preserveDeathArrowOwnerEffects(
            DeathArrow arrow,
            LivingEntity target
    ) {
        Entity owner = arrow.getOwner();

        if (!(owner instanceof LivingEntity livingOwner)) {
            return;
        }

        if (!CuriosFinder.hasUnholySet(livingOwner)) {
            return;
        }

        if (livingOwner.level().dimension() == Level.NETHER) {
            float voidDamage =
                    target.getMaxHealth() * 0.05F;

            if (target.getHealth() > voidDamage + 1.0F) {
                target.heal(-voidDamage);
            }
        }

        if (!(livingOwner instanceof Player player)) {
            return;
        }

        if (!LichdomHelper.isLich(player)) {
            return;
        }

        if (LichdomHelper.smited(player) > 0) {
            return;
        }

        if (MobUtil.healthIsHalved(player)) {
            player.heal(4.0F);
        } else {
            player.heal(1.0F);
        }
    }
}