package com.carrot123.until_eternity.mixin.compat.aether;

import com.github.L_Ender.cataclysm.entity.projectile.Ignis_Fireball_Entity;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.registries.ForgeRegistries;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.lang.reflect.Field;
import java.util.List;
import java.util.UUID;

@Pseudo
@Mixin(
        targets = "com.aetherteam.aether.entity.monster.dungeon.boss.SunSpirit",
        remap = false
)
public abstract class SunSpiritResetMixin extends PathfinderMob {

    @Unique
    private static final int UNTIL_ETERNITY_SUN_FIREBALL_INTERVAL =
            60;

    @Unique
    private static final int UNTIL_ETERNITY_IGNIS_FIREBALL_INTERVAL =
            200;

    @Unique
    private static final int UNTIL_ETERNITY_MINION_INTERVAL =
            400;

    @Unique
    private static final int UNTIL_ETERNITY_RADIAL_INTERVAL =
            200;

    @Unique
    private static final int UNTIL_ETERNITY_RADIAL_WAVES =
            4;

    @Unique
    private static final int UNTIL_ETERNITY_RADIAL_WAVE_DELAY =
            4;

    @Unique
    private static final double UNTIL_ETERNITY_BASE_MAX_HEALTH =
            400.0D;

    @Unique
    private static final double UNTIL_ETERNITY_BASE_MOVEMENT_SPEED =
            0.23D;

    @Unique
    private static final double UNTIL_ETERNITY_ACTUAL_MOVE_SPEED =
            0.08D;

    @Unique
    private static final double UNTIL_ETERNITY_DESIRED_DISTANCE =
            4.0D;

    @Unique
    private static final double UNTIL_ETERNITY_DISTANCE_MARGIN =
            0.5D;

    @Unique
    private static final double UNTIL_ETERNITY_TARGET_RANGE =
            64.0D;

    @Unique
    private static final double UNTIL_ETERNITY_ARMOR =
            5.0D;

    @Unique
    private static final double UNTIL_ETERNITY_ATTACK_DAMAGE =
            6.0D;

    @Unique
    private static final int UNTIL_ETERNITY_CONTACT_FIRE_SECONDS =
            5;

    @Unique
    private static final double UNTIL_ETERNITY_FIRE_CRYSTAL_SPEED =
            0.5D;
    @Unique
    private static final float UNTIL_ETERNITY_DAMAGE_REDUCTION =
            0.50F;

    @Unique
    private static final float UNTIL_ETERNITY_DAMAGE_IMMUNITY_CHANCE =
            0.13F;

    @Unique
    private static final String UNTIL_ETERNITY_MINION_OWNER =
            "until_eternity:sun_spirit_owner";

    @Unique
    private static final String UNTIL_ETERNITY_RADIAL_FIREBALL =
            "until_eternity:sun_spirit_radial_fireball";

    @Unique
    private static final String UNTIL_ETERNITY_INITIALIZED =
            "until_eternity:sun_spirit_initialized_v2";

    @Unique
    private static final String UNTIL_ETERNITY_SUN_SPIRIT_IGNIS_FIREBALL =
            "until_eternity:sun_spirit_ignis_fireball";

    @Unique
    private static final ResourceLocation UNTIL_ETERNITY_FIRE_CRYSTAL_ID =
            new ResourceLocation(
                    "aether",
                    "fire_crystal"
            );

    @Unique
    private static final ResourceLocation UNTIL_ETERNITY_FIRE_MINION_ID =
            new ResourceLocation(
                    "aether",
                    "fire_minion"
            );

    @Unique
    private static final TagKey<DamageType> UNTIL_ETERNITY_IS_ICE =
            TagKey.create(
                    Registries.DAMAGE_TYPE,
                    new ResourceLocation(
                            "forge",
                            "is_ice"
                    )
            );

    @Unique
    private static final TagKey<DamageType> UNTIL_ETERNITY_COMMON_IS_ICE =
            TagKey.create(
                    Registries.DAMAGE_TYPE,
                    new ResourceLocation(
                            "c",
                            "is_ice"
                    )
            );

    @Unique
    private static final TagKey<DamageType> UNTIL_ETERNITY_AETHER_IS_COLD =
            TagKey.create(
                    Registries.DAMAGE_TYPE,
                    new ResourceLocation(
                            "aether",
                            "is_cold"
                    )
            );

    @Unique
    private int untilEternity$sunFireballCooldown;

    @Unique
    private int untilEternity$ignisFireballCooldown;

    @Unique
    private int untilEternity$minionCooldown;

    @Unique
    private int untilEternity$radialCooldown;

    @Unique
    private int untilEternity$radialWavesRemaining;

    @Unique
    private int untilEternity$radialWaveDelay;

    @Unique
    private boolean untilEternity$wasFighting;

    @Unique
    private static Field untilEternity$fireCrystalXPower;

    @Unique
    private static Field untilEternity$fireCrystalYPower;

    @Unique
    private static Field untilEternity$fireCrystalZPower;

    @Unique
    private static boolean untilEternity$fireCrystalFieldsResolved;

    protected SunSpiritResetMixin(
            EntityType<? extends PathfinderMob> type,
            Level level
    ) {
        super(
                type,
                level
        );
    }

    @Shadow(remap = false)
    public abstract boolean isBossFight();

    @Inject(
            method = {
                    "registerGoals()V",
                    "m_8099_()V"
            },
            at = @At("HEAD"),
            cancellable = true,
            remap = false,
            require = 1
    )
    private void untilEternity$removeAllOriginalGoals(
            CallbackInfo ci
    ) {
        ci.cancel();
    }

    @Inject(
        method = {
                "hurt(Lnet/minecraft/world/damagesource/DamageSource;F)Z",
                "m_6469_(Lnet/minecraft/world/damagesource/DamageSource;F)Z"
        },
        at = @At("HEAD"),
        cancellable = true,
        remap = false,
        require = 1
)
private void untilEternity$replaceDamageLogic(
        DamageSource source,
        float amount,
        CallbackInfoReturnable<Boolean> cir
) {
    if (!this.isBossFight()) {
        cir.setReturnValue(
                false
        );

        return;
    }

    if (untilEternity$hasLivingMinion()) {
        cir.setReturnValue(
                false
        );

        return;
    }

    if (this.getRandom().nextFloat()
            < UNTIL_ETERNITY_DAMAGE_IMMUNITY_CHANCE) {
        cir.setReturnValue(
                false
        );

        return;
    }

    amount *=
            UNTIL_ETERNITY_DAMAGE_REDUCTION;

    if (untilEternity$isIceDamage(
            source
    )) {
        amount *=
                1.20F;
    }

    cir.setReturnValue(
            super.hurt(
                    source,
                    amount
            )
    );
}
    @Inject(
            method = {
                    "isInvulnerableTo(Lnet/minecraft/world/damagesource/DamageSource;)Z",
                    "m_6673_(Lnet/minecraft/world/damagesource/DamageSource;)Z"
            },
            at = @At("HEAD"),
            cancellable = true,
            remap = false,
            require = 1
    )
    private void untilEternity$replaceInvulnerability(
            DamageSource source,
            CallbackInfoReturnable<Boolean> cir
    ) {
        if (this.isRemoved()) {
            cir.setReturnValue(
                    true
            );

            return;
        }

        cir.setReturnValue(
                untilEternity$hasLivingMinion()
        );
    }

    @Inject(
            method = "evaporate",
            at = @At("HEAD"),
            cancellable = true,
            remap = false,
            require = 1
    )
    private void untilEternity$disableEvaporation(
            CallbackInfo ci
    ) {
        ci.cancel();
    }

    @Inject(
            method = "burnEntities",
            at = @At("HEAD"),
            cancellable = true,
            remap = false,
            require = 1
    )
    private void untilEternity$disableOriginalContactDamage(
            CallbackInfo ci
    ) {
        ci.cancel();
    }

    @Inject(
            method = "checkIceCrystals",
            at = @At("HEAD"),
            cancellable = true,
            remap = false,
            require = 1
    )
    private void untilEternity$disableOriginalIceCrystalLogic(
            CallbackInfo ci
    ) {
        ci.cancel();
    }

    @Inject(
            method = {
                    "tick()V",
                    "m_8119_()V"
            },
            at = @At("TAIL"),
            remap = false,
            require = 1
    )
    private void untilEternity$customSunSpiritAi(
            CallbackInfo ci
    ) {
        if (this.level().isClientSide) {
            return;
        }

        untilEternity$initializeAttributes();

        if (!this.isBossFight()) {
            untilEternity$wasFighting =
                    false;

            untilEternity$radialWavesRemaining =
                    0;

            this.setTarget(
                    null
            );

            this.setDeltaMovement(
                    Vec3.ZERO
            );

            return;
        }

        if (!untilEternity$wasFighting) {
            untilEternity$wasFighting =
                    true;

            untilEternity$sunFireballCooldown =
                    UNTIL_ETERNITY_SUN_FIREBALL_INTERVAL;

            untilEternity$ignisFireballCooldown =
                    UNTIL_ETERNITY_IGNIS_FIREBALL_INTERVAL;

            untilEternity$minionCooldown =
                    UNTIL_ETERNITY_MINION_INTERVAL;

            untilEternity$radialCooldown =
                    UNTIL_ETERNITY_RADIAL_INTERVAL;

            untilEternity$radialWavesRemaining =
                    0;

            untilEternity$radialWaveDelay =
                    0;
        }

        Player target =
                untilEternity$findTarget();

        if (target == null) {
            this.setTarget(
                    null
            );

            this.setDeltaMovement(
                    Vec3.ZERO
            );

            return;
        }

        this.setTarget(
                target
        );

        untilEternity$faceTarget(
                target
        );

        untilEternity$moveAroundTarget(
                target
        );

        untilEternity$doContactDamage(
                target
        );

        untilEternity$tickSkills(
                target
        );
    }

    @Unique
    private void untilEternity$initializeAttributes() {
        if (this.getPersistentData()
                .getBoolean(
                        UNTIL_ETERNITY_INITIALIZED
                )) {
            return;
        }

        AttributeInstance maxHealth =
                this.getAttribute(
                        Attributes.MAX_HEALTH
                );

        if (maxHealth != null) {
            double base =
                    maxHealth.getBaseValue();

            if (Math.abs(
                    base - 500.0D
            ) < 0.0001D) {
                maxHealth.setBaseValue(
                        UNTIL_ETERNITY_BASE_MAX_HEALTH
                );
            }
        }

        AttributeInstance movementSpeed =
                this.getAttribute(
                        Attributes.MOVEMENT_SPEED
                );

        if (movementSpeed != null) {
            movementSpeed.setBaseValue(
                    UNTIL_ETERNITY_BASE_MOVEMENT_SPEED
            );
        }

        AttributeInstance armor =
                this.getAttribute(
                        Attributes.ARMOR
                );

        if (armor != null) {
            armor.setBaseValue(
                    UNTIL_ETERNITY_ARMOR
            );
        }

        AttributeInstance attackDamage =
                this.getAttribute(
                        Attributes.ATTACK_DAMAGE
                );

        if (attackDamage != null) {
            attackDamage.setBaseValue(
                    UNTIL_ETERNITY_ATTACK_DAMAGE
            );
        }

        this.setHealth(
                this.getMaxHealth()
        );

        this.getPersistentData()
                .putBoolean(
                        UNTIL_ETERNITY_INITIALIZED,
                        true
                );
    }

    @Unique
    private void untilEternity$tickSkills(
            Player target
    ) {
        if (--untilEternity$sunFireballCooldown
                <= 0) {
            untilEternity$shootSunFireball(
                    target
            );

            untilEternity$sunFireballCooldown =
                    UNTIL_ETERNITY_SUN_FIREBALL_INTERVAL;
        }

        if (--untilEternity$ignisFireballCooldown
                <= 0) {
            untilEternity$shootIgnisFireball(
                    target
            );

            untilEternity$ignisFireballCooldown =
                    UNTIL_ETERNITY_IGNIS_FIREBALL_INTERVAL;
        }

        if (--untilEternity$minionCooldown
                <= 0) {
            untilEternity$summonFireMinion(
                    target
            );

            untilEternity$minionCooldown =
                    UNTIL_ETERNITY_MINION_INTERVAL;
        }

        if (--untilEternity$radialCooldown
                <= 0) {
            untilEternity$radialCooldown =
                    UNTIL_ETERNITY_RADIAL_INTERVAL;

            untilEternity$radialWavesRemaining =
                    UNTIL_ETERNITY_RADIAL_WAVES;

            untilEternity$radialWaveDelay =
                    0;
        }

        if (untilEternity$radialWavesRemaining
                <= 0) {
            return;
        }

        if (untilEternity$radialWaveDelay
                > 0) {
            untilEternity$radialWaveDelay--;

            return;
        }

        untilEternity$shootRadialWave();

        untilEternity$radialWavesRemaining--;

        untilEternity$radialWaveDelay =
                UNTIL_ETERNITY_RADIAL_WAVE_DELAY;
    }

    @Unique
    private Player untilEternity$findTarget() {
        AABB area =
                this.getBoundingBox()
                        .inflate(
                                UNTIL_ETERNITY_TARGET_RANGE
                        );

        List<Player> players =
                this.level()
                        .getEntitiesOfClass(
                                Player.class,
                                area,
                                player ->
                                        player.isAlive()
                                                && !player.isSpectator()
                                                && !player.isCreative()
                        );

        Player closest =
                null;

        double closestDistance =
                Double.MAX_VALUE;

        for (Player player : players) {
            double distance =
                    this.distanceToSqr(
                            player
                    );

            if (distance
                    < closestDistance) {
                closestDistance =
                        distance;

                closest =
                        player;
            }
        }

        return closest;
    }

    @Unique
    private void untilEternity$faceTarget(
            LivingEntity target
    ) {
        double dx =
                target.getX()
                        - this.getX();

        double dz =
                target.getZ()
                        - this.getZ();

        double dy =
                target.getEyeY()
                        - this.getEyeY();

        double horizontal =
                Math.sqrt(
                        dx * dx
                                + dz * dz
                );

        float yaw =
                (float) (
                        Math.atan2(
                                dz,
                                dx
                        )
                                * 180.0D
                                / Math.PI
                ) - 90.0F;

        float pitch =
                (float) (
                        -Math.atan2(
                                dy,
                                horizontal
                        )
                                * 180.0D
                                / Math.PI
                );

        this.setYRot(
                yaw
        );

        this.setYHeadRot(
                yaw
        );

        this.yBodyRot =
                yaw;

        this.setXRot(
                pitch
        );
    }

    @Unique
    private void untilEternity$moveAroundTarget(
            LivingEntity target
    ) {
        double dx =
                target.getX()
                        - this.getX();

        double dz =
                target.getZ()
                        - this.getZ();

        double horizontalDistance =
                Math.sqrt(
                        dx * dx
                                + dz * dz
                );

        if (horizontalDistance
                <= 1.0E-6D) {
            this.setDeltaMovement(
                    0.0D,
                    0.0D,
                    0.0D
            );

            return;
        }

        double normalX =
                dx / horizontalDistance;

        double normalZ =
                dz / horizontalDistance;

        double moveX =
                0.0D;

        double moveZ =
                0.0D;

        if (horizontalDistance
                > UNTIL_ETERNITY_DESIRED_DISTANCE
                + UNTIL_ETERNITY_DISTANCE_MARGIN) {
            moveX =
                    normalX
                            * UNTIL_ETERNITY_ACTUAL_MOVE_SPEED;

            moveZ =
                    normalZ
                            * UNTIL_ETERNITY_ACTUAL_MOVE_SPEED;
        } else if (horizontalDistance
                < UNTIL_ETERNITY_DESIRED_DISTANCE
                - UNTIL_ETERNITY_DISTANCE_MARGIN) {
            moveX =
                    -normalX
                            * UNTIL_ETERNITY_ACTUAL_MOVE_SPEED;

            moveZ =
                    -normalZ
                            * UNTIL_ETERNITY_ACTUAL_MOVE_SPEED;
        }

        this.setDeltaMovement(
                moveX,
                0.0D,
                moveZ
        );

        this.hasImpulse =
                true;
    }

    @Unique
    private void untilEternity$doContactDamage(
            Player player
    ) {
        if (!this.getBoundingBox()
                .inflate(
                        0.15D
                )
                .intersects(
                        player.getBoundingBox()
                )) {
            return;
        }

        float damage =
                (float) this.getAttributeValue(
                        Attributes.ATTACK_DAMAGE
                );

        if (player.hurt(
                this.damageSources()
                        .mobAttack(
                                this
                        ),
                damage
        )) {
            player.setSecondsOnFire(
                    UNTIL_ETERNITY_CONTACT_FIRE_SECONDS
            );
        }
    }

    @Unique
private void untilEternity$shootIgnisFireball(
        LivingEntity target
) {
    Vec3 center =
            this.position()
                    .add(
                            0.0D,
                            this.getBbHeight()
                                    * 0.55D,
                            0.0D
                    );

    Vec3 targetPos =
            target.position()
                    .add(
                            0.0D,
                            target.getBbHeight()
                                    * 0.5D,
                            0.0D
                    );

    Vec3 initialDirection =
            targetPos.subtract(
                    center
            );

    if (initialDirection.lengthSqr()
            <= 1.0E-8D) {
        return;
    }

    Vec3 horizontalDirection =
            new Vec3(
                    initialDirection.x,
                    0.0D,
                    initialDirection.z
            );

    if (horizontalDirection.lengthSqr()
            <= 1.0E-8D) {
        Vec3 look =
                this.getLookAngle();

        horizontalDirection =
                new Vec3(
                        look.x,
                        0.0D,
                        look.z
                );
    }

    if (horizontalDirection.lengthSqr()
            <= 1.0E-8D) {
        horizontalDirection =
                new Vec3(
                        0.0D,
                        0.0D,
                        1.0D
                );
    }

    horizontalDirection =
            horizontalDirection.normalize();

    double spawnDistance =
            this.getBbWidth()
                    * 0.5D
                    + 1.0D;

    Vec3 spawn =
            center.add(
                    horizontalDirection.scale(
                            spawnDistance
                    )
            );

    Vec3 direction =
            targetPos.subtract(
                    spawn
            );

    if (direction.lengthSqr()
            <= 1.0E-8D) {
        return;
    }

    Ignis_Fireball_Entity fireball =
            new Ignis_Fireball_Entity(
                    this.level(),
                    this
            );

    fireball.setPos(
            spawn.x,
            spawn.y,
            spawn.z
    );

    fireball.getPersistentData()
            .putBoolean(
                    UNTIL_ETERNITY_SUN_SPIRIT_IGNIS_FIREBALL,
                    true
            );

    fireball.shoot(
            direction.x,
            direction.y,
            direction.z,
            0.25F,
            0.0F
    );

    fireball.setUp(
            4
    );

    this.level().addFreshEntity(
            fireball
    );
}

    @Unique
    private void untilEternity$shootSunFireball(
            LivingEntity target
    ) {
        Vec3 spawn =
                this.position()
                        .add(
                                0.0D,
                                this.getBbHeight()
                                        * 0.55D,
                                0.0D
                        );

        Vec3 targetPos =
                target.position()
                        .add(
                                0.0D,
                                target.getBbHeight()
                                        * 0.5D,
                                0.0D
                        );

        Vec3 difference =
                targetPos.subtract(
                        spawn
                );

        if (difference.lengthSqr()
                <= 1.0E-8D) {
            return;
        }

        untilEternity$spawnFireCrystal(
                spawn,
                difference.normalize()
                        .scale(
                                UNTIL_ETERNITY_FIRE_CRYSTAL_SPEED
                        ),
                false
        );
    }

    @Unique
    private void untilEternity$shootRadialWave() {
        Vec3 spawn =
                this.position()
                        .add(
                                0.0D,
                                this.getBbHeight()
                                        * 0.5D,
                                0.0D
                        );

        for (int i = 0; i < 8; i++) {
            double angle =
                    Math.PI
                            * 2.0D
                            * i
                            / 8.0D;

            Vec3 velocity =
                    new Vec3(
                            Math.cos(
                                    angle
                            ) * UNTIL_ETERNITY_FIRE_CRYSTAL_SPEED,
                            0.0D,
                            Math.sin(
                                    angle
                            ) * UNTIL_ETERNITY_FIRE_CRYSTAL_SPEED
                    );

            untilEternity$spawnFireCrystal(
                    spawn,
                    velocity,
                    true
            );
        }
    }

    @Unique
    private void untilEternity$spawnFireCrystal(
            Vec3 spawn,
            Vec3 velocity,
            boolean radial
    ) {
        EntityType<?> type =
                ForgeRegistries.ENTITY_TYPES
                        .getValue(
                                UNTIL_ETERNITY_FIRE_CRYSTAL_ID
                        );

        if (type == null) {
            return;
        }

        Entity crystal =
                type.create(
                        this.level()
                );

        if (crystal == null) {
            return;
        }

        crystal.setPos(
                spawn.x,
                spawn.y,
                spawn.z
        );

        if (crystal
                instanceof Projectile projectile) {
            projectile.setOwner(
                    this
            );
        }

        crystal.setDeltaMovement(
                velocity
        );

        untilEternity$setFireCrystalPower(
                crystal,
                velocity
        );

        if (radial) {
            crystal.getPersistentData()
                    .putBoolean(
                            UNTIL_ETERNITY_RADIAL_FIREBALL,
                            true
                    );
        }

        this.level().addFreshEntity(
                crystal
        );
    }

    @Unique
    private static void untilEternity$setFireCrystalPower(
            Entity crystal,
            Vec3 velocity
    ) {
        try {
            untilEternity$resolveFireCrystalFields(
                    crystal.getClass()
            );

            if (untilEternity$fireCrystalXPower
                    != null) {
                untilEternity$fireCrystalXPower
                        .setDouble(
                                crystal,
                                velocity.x
                        );
            }

            if (untilEternity$fireCrystalYPower
                    != null) {
                untilEternity$fireCrystalYPower
                        .setDouble(
                                crystal,
                                velocity.y
                        );
            }

            if (untilEternity$fireCrystalZPower
                    != null) {
                untilEternity$fireCrystalZPower
                        .setDouble(
                                crystal,
                                velocity.z
                        );
            }
        } catch (
                IllegalAccessException ignored
        ) {
        }
    }

    @Unique
    private static void untilEternity$resolveFireCrystalFields(
            Class<?> fireCrystalClass
    ) {
        if (untilEternity$fireCrystalFieldsResolved) {
            return;
        }

        untilEternity$fireCrystalFieldsResolved =
                true;

        try {
            untilEternity$fireCrystalXPower =
                    fireCrystalClass.getDeclaredField(
                            "xPower"
                    );

            untilEternity$fireCrystalXPower
                    .setAccessible(
                            true
                    );
        } catch (
                ReflectiveOperationException ignored
        ) {
        }

        try {
            untilEternity$fireCrystalYPower =
                    fireCrystalClass.getDeclaredField(
                            "yPower"
                    );

            untilEternity$fireCrystalYPower
                    .setAccessible(
                            true
                    );
        } catch (
                ReflectiveOperationException ignored
        ) {
        }

        try {
            untilEternity$fireCrystalZPower =
                    fireCrystalClass.getDeclaredField(
                            "zPower"
                    );

            untilEternity$fireCrystalZPower
                    .setAccessible(
                            true
                    );
        } catch (
                ReflectiveOperationException ignored
        ) {
        }
    }

    @Unique
    private void untilEternity$summonFireMinion(
            LivingEntity target
    ) {
        EntityType<?> type =
                ForgeRegistries.ENTITY_TYPES
                        .getValue(
                                UNTIL_ETERNITY_FIRE_MINION_ID
                        );

        if (type == null) {
            return;
        }

        Entity entity =
                type.create(
                        this.level()
                );

        if (!(entity
                instanceof PathfinderMob minion)) {
            return;
        }

        double angle =
                this.getRandom()
                        .nextDouble()
                        * Math.PI
                        * 2.0D;

        double offsetX =
                Math.cos(
                        angle
                ) * 2.0D;

        double offsetZ =
                Math.sin(
                        angle
                ) * 2.0D;

        minion.setPos(
                this.getX()
                        + offsetX,
                this.getY(),
                this.getZ()
                        + offsetZ
        );

        minion.getPersistentData()
                .putUUID(
                        UNTIL_ETERNITY_MINION_OWNER,
                        this.getUUID()
                );

        minion.setTarget(
                target
        );

        this.level().addFreshEntity(
                minion
        );
    }

    @Unique
    private boolean untilEternity$hasLivingMinion() {
        UUID ownerId =
                this.getUUID();

        return !this.level()
                .getEntities(
                        this,
                        this.getBoundingBox()
                                .inflate(
                                        256.0D
                                ),
                        entity -> {
                            if (!entity.isAlive()) {
                                return false;
                            }

                            ResourceLocation id =
                                    ForgeRegistries.ENTITY_TYPES
                                            .getKey(
                                                    entity.getType()
                                            );

                            if (!UNTIL_ETERNITY_FIRE_MINION_ID
                                    .equals(
                                            id
                                    )) {
                                return false;
                            }

                            if (!entity.getPersistentData()
                                    .hasUUID(
                                            UNTIL_ETERNITY_MINION_OWNER
                                    )) {
                                return false;
                            }

                            return ownerId.equals(
                                    entity.getPersistentData()
                                            .getUUID(
                                                    UNTIL_ETERNITY_MINION_OWNER
                                            )
                            );
                        }
                )
                .isEmpty();
    }

    @Unique
    private static boolean untilEternity$isIceDamage(
            DamageSource source
    ) {
        return source.is(
                UNTIL_ETERNITY_IS_ICE
        )
                || source.is(
                UNTIL_ETERNITY_COMMON_IS_ICE
        )
                || source.is(
                UNTIL_ETERNITY_AETHER_IS_COLD
        );
    }
}