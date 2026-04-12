package com.ohyeah.ohyeahmod.entity;

import com.ohyeah.ohyeahmod.config.ModSpeciesConfigs;
import com.ohyeah.ohyeahmod.config.SpeciesConfig;
import com.ohyeah.ohyeahmod.entity.ai.TiansuluoBattleFacePounceGoal;
import com.ohyeah.ohyeahmod.sound.definition.SoundCue;
import com.ohyeah.ohyeahmod.sound.bridge.SpeciesSoundFacade;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

/**
 * 天素罗 - 战斗脸。
 * 极具攻击性的近战突袭物种。
 */
public final class TiansuluoBattleFaceEntity extends AbstractTiansuluoEntity {
    public static final float TARGET_ADULT_WIDTH = 0.8F;
    public static final float TARGET_ADULT_HEIGHT = 1.2F;
    public static final float BABY_SCALE_FACTOR = 0.5F;
    
    private static final double KNOCKBACK_HORIZONTAL_SPEED = 1.15D;
    private static final double KNOCKBACK_VERTICAL_SPEED = 0.35D;

    public enum RetaliationState {
        IDLE,
        PENDING_DECLARE,
        READY_TO_POUNCE,
        COOLDOWN
    }

    private @Nullable LivingEntity retaliationTarget;
    private RetaliationState retaliationState = RetaliationState.IDLE;
    private int retaliationTicksRemaining;
    private int retaliationDeclareTicksRemaining;
    private int attackCooldownTicksRemaining;

    public TiansuluoBattleFaceEntity(EntityType<? extends Animal> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        SpeciesConfig config = ModSpeciesConfigs.battleFace();
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, config.attributes().maxHealth())
                .add(Attributes.MOVEMENT_SPEED, config.attributes().movementSpeed())
                .add(Attributes.FOLLOW_RANGE, config.attributes().followRange())
                .add(Attributes.ATTACK_DAMAGE, config.attributes().attackDamage());
    }

    @Override
    protected void registerGoals() {
        SpeciesConfig config = this.getSpeciesConfig();
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new TiansuluoBattleFacePounceGoal(this));

        // 仅当配置启用了卵块逻辑时，才添加相关繁殖与产卵 Goal
        if (config.breeding().usesEggBlock()) {
            this.goalSelector.addGoal(2, new MateForEggBlockGoal(this, config.behavior().mateGoalSpeed()));
            this.goalSelector.addGoal(3, new LayEggBlockGoal());
        }

        this.goalSelector.addGoal(5, new FollowParentGoal(this, config.behavior().followParentSpeed()));        this.goalSelector.addGoal(6, new WaterAvoidingRandomStrollGoal(this, config.behavior().wanderSpeed()));
        this.goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (!this.level().isClientSide) {
            if (this.attackCooldownTicksRemaining > 0) this.attackCooldownTicksRemaining--;
            this.updateRetaliationTarget();
        }
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        boolean damaged = super.hurt(source, amount);
        if (damaged && !this.level().isClientSide && source.getEntity() instanceof LivingEntity attacker && attacker != this) {
            this.rememberRetaliationTarget(attacker);
        }
        return damaged;
    }

    @Override
    public boolean doHurtTarget(Entity target) {
        if (!super.doHurtTarget(target)) return false;

        if (target instanceof LivingEntity livingTarget) {
            Vec3 direction = this.getHorizontalDirectionTo(livingTarget);
            livingTarget.push(direction.x * KNOCKBACK_HORIZONTAL_SPEED, KNOCKBACK_VERTICAL_SPEED, direction.z * KNOCKBACK_HORIZONTAL_SPEED);
            this.push(-direction.x * KNOCKBACK_HORIZONTAL_SPEED, KNOCKBACK_VERTICAL_SPEED, -direction.z * KNOCKBACK_HORIZONTAL_SPEED);

            if (livingTarget instanceof Player player) {
                this.applyHungerPenalty(player);
            }
        }
        return true;
    }

    private Vec3 getHorizontalDirectionTo(LivingEntity target) {
        Vec3 direction = new Vec3(target.getX() - this.getX(), 0.0D, target.getZ() - this.getZ());
        if (direction.lengthSqr() > 1.0E-7D) return direction.normalize();
        float yaw = this.getYRot() * Mth.DEG_TO_RAD;
        return new Vec3(-Mth.sin(yaw), 0.0D, Mth.cos(yaw));
    }

    private void applyHungerPenalty(Player player) {
        SpeciesConfig config = this.getSpeciesConfig();
        int hungerLoss = switch (this.level().getDifficulty()) {
            case PEACEFUL -> 0;
            case EASY -> config.behavior().hungerDamageEasy();
            case NORMAL -> config.behavior().hungerDamageNormal();
            case HARD -> config.behavior().hungerDamageHard();
        };
        if (hungerLoss > 0) {
            player.getFoodData().setFoodLevel(Math.max(0, player.getFoodData().getFoodLevel() - hungerLoss));
        }
    }

    private void rememberRetaliationTarget(LivingEntity attacker) {
        this.retaliationTarget = attacker;
        this.retaliationTicksRemaining = this.getSpeciesConfig().behavior().retaliationMemoryTicks();
        this.retaliationState = RetaliationState.PENDING_DECLARE;
        this.setTarget(attacker);
    }

    private void updateRetaliationTarget() {
        if (this.retaliationTicksRemaining > 0) this.retaliationTicksRemaining--;
        
        LivingEntity currentTarget = this.retaliationTarget;
        if (this.isValidRetaliationTarget(currentTarget)) {
            this.setTarget(currentTarget);
            this.faceRetaliationTarget(currentTarget);
            this.updateRetaliationState(currentTarget);
            return;
        }
        this.clearRetaliationState(true);
    }

    private void updateRetaliationState(LivingEntity target) {
        if (this.retaliationState == RetaliationState.COOLDOWN) {
            if (this.attackCooldownTicksRemaining <= 0) this.retaliationState = RetaliationState.READY_TO_POUNCE;
            return;
        }

        if (this.retaliationState == RetaliationState.PENDING_DECLARE) {
            if (this.retaliationDeclareTicksRemaining > 0) {
                this.retaliationDeclareTicksRemaining--;
                if (this.retaliationDeclareTicksRemaining <= 0) this.retaliationState = RetaliationState.READY_TO_POUNCE;
                return;
            }
            if (SpeciesSoundFacade.playCue(this, SoundCue.ATTACK_DECLARE, 1.0F, 1.0F)) {
                this.retaliationDeclareTicksRemaining = this.getSpeciesConfig().behavior().attackDeclareDurationTicks();
            } else {
                this.retaliationState = RetaliationState.READY_TO_POUNCE;
            }
        }
    }

    public void faceRetaliationTarget(LivingEntity target) {
        float turnSpeed = (float) this.getSpeciesConfig().behavior().retaliationFaceTargetTurnSpeed();
        this.getLookControl().setLookAt(target, turnSpeed, turnSpeed);
    }

    private void clearRetaliationState(boolean playEndVoice) {
        boolean shouldPlayEndVoice = playEndVoice && this.retaliationState != RetaliationState.IDLE;
        this.retaliationTarget = null;
        this.retaliationState = RetaliationState.IDLE;
        if (this.getTarget() != null) this.setTarget(null);
        if (shouldPlayEndVoice) SpeciesSoundFacade.playCue(this, SoundCue.ATTACK_END, 1.0F, 1.0F);
    }

    private boolean isValidRetaliationTarget(@Nullable LivingEntity candidate) {
        if (candidate == null || this.retaliationTicksRemaining <= 0) return false;
        if (!candidate.isAlive()) return false;
        double maxRange = this.getSpeciesConfig().behavior().retaliationRange();
        return this.distanceToSqr(candidate) <= Mth.square(maxRange) && this.getSensing().hasLineOfSight(candidate);
    }

    public boolean isReadyToPounce() { return this.retaliationState == RetaliationState.READY_TO_POUNCE; }
    public boolean isAttackCooldownReady() { return this.attackCooldownTicksRemaining <= 0; }
    public boolean isWithinPounceWindow(LivingEntity target) {
        double dist = this.distanceTo(target);
        return dist >= 2.5D && dist <= 3.5D; 
    }
    public Vec3 getPounceAimPoint(LivingEntity target) { return target.getBoundingBox().getCenter(); }
    public void beginCharge() { this.getNavigation().stop(); }
    public void startCooldown() {
        this.attackCooldownTicksRemaining = 100;
        this.retaliationState = RetaliationState.COOLDOWN;
    }
    public void finishSuccessfulRetaliation() { this.clearRetaliationState(true); }

    @Override
    public AgeableMob getBreedOffspring(ServerLevel level, AgeableMob other) { return null; }
    
    @Override
    public SpeciesConfig getSpeciesConfig() { return ModSpeciesConfigs.battleFace(); }

    public static boolean canSpawn(EntityType<TiansuluoBattleFaceEntity> type, net.minecraft.world.level.LevelAccessor level, MobSpawnType spawnType, net.minecraft.core.BlockPos pos, net.minecraft.util.RandomSource random) {
        return level.getBlockState(pos.below()).isSolidRender(level, pos.below());
    }
}
