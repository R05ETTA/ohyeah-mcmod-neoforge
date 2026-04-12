package com.ohyeah.ohyeahmod.entity;

import com.ohyeah.ohyeahmod.config.ModSpeciesConfigs;
import com.ohyeah.ohyeahmod.config.SpeciesConfig;
import com.ohyeah.ohyeahmod.entity.projectile.TiansuluoPinkScarfProjectileEntity;
import com.ohyeah.ohyeahmod.sound.definition.SoundCue;
import com.ohyeah.ohyeahmod.sound.bridge.SpeciesSoundFacade;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

/**
 * 天素罗 - 粉围巾。
 * 擅长远程反击的优雅物种。
 */
public class TiansuluoPinkScarfEntity extends AbstractTiansuluoEntity implements RangedAttackMob {
    public static final float WIDTH = 0.6F;
    public static final float HEIGHT = 1.2F;

    private enum RetaliationState { IDLE, PENDING_RETALIATION_DECLARE, RETALIATING }

    private RetaliationState retaliationState = RetaliationState.IDLE;
    private @Nullable LivingEntity retaliationTarget;
    private int retaliationTicksRemaining;
    private int retaliationBurstShotsFired;
    private int retaliationBurstCooldownTicks;
    private int retaliationDeclareTicksRemaining;

    public TiansuluoPinkScarfEntity(EntityType<? extends Animal> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        // 使用动态配置工厂获取初始属性
        SpeciesConfig config = ModSpeciesConfigs.pinkScarf();
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, config.attributes().maxHealth())
                .add(Attributes.MOVEMENT_SPEED, config.attributes().movementSpeed())
                .add(Attributes.FOLLOW_RANGE, config.attributes().followRange());
    }

    @Override
    protected void registerGoals() {
        SpeciesConfig config = this.getSpeciesConfig();
        this.goalSelector.addGoal(0, new FloatGoal(this));

        // 远程攻击 AI：使用配置定义的参数
        this.goalSelector.addGoal(1, new RangedAttackGoal(this, config.behavior().projectileAttackGoalSpeed(), config.behavior().retaliationBurstIntervalTicks(), (float) config.behavior().retaliationRange()));

        // 仅当配置启用了卵块逻辑时，才添加相关繁殖与产卵 Goal
        if (config.breeding().usesEggBlock()) {
            this.goalSelector.addGoal(2, new MateForEggBlockGoal(this, config.behavior().mateGoalSpeed()));
            this.goalSelector.addGoal(3, new LayEggBlockGoal());
        }

        this.goalSelector.addGoal(5, new FollowParentGoal(this, config.behavior().followParentSpeed()));        this.goalSelector.addGoal(6, new WaterAvoidingRandomStrollGoal(this, config.behavior().wanderSpeed()));
        this.goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 6.0F));
        this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (!this.level().isClientSide) {
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
    public void performRangedAttack(LivingEntity target, float velocity) {
        if (this.retaliationState != RetaliationState.RETALIATING || this.retaliationBurstCooldownTicks > 0) return;
        
        SpeciesConfig config = this.getSpeciesConfig();
        Vec3 muzzlePos = this.getProjectileMuzzlePos();
        
        TiansuluoPinkScarfProjectileEntity projectile = new TiansuluoPinkScarfProjectileEntity(this.level(), this);
        projectile.setPos(muzzlePos.x, muzzlePos.y, muzzlePos.z);
        projectile.setDamage((float) config.behavior().retaliationProjectileDamage());
        
        double targetY = target.getEyeY() - config.behavior().retaliationTargetEyeOffset();
        double d0 = target.getX() - muzzlePos.x;
        double d1 = targetY - projectile.getY();
        double d2 = target.getZ() - muzzlePos.z;
        double d3 = Math.sqrt(d0 * d0 + d2 * d2) * 0.2F;
        
        projectile.shoot(d0, d1 + d3, d2, config.behavior().retaliationProjectileSpeed(), config.behavior().retaliationProjectileDivergence());
        
        SpeciesSoundFacade.playCue(this, SoundCue.ATTACK_SHOT, 1.0F, 1.0F);
        this.level().playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.SNOW_GOLEM_SHOOT, this.getSoundSource(), 1.0F, 0.4F / (this.getRandom().nextFloat() * 0.4F + 0.8F));
        this.level().addFreshEntity(projectile);
        
        this.retaliationBurstShotsFired++;
        if (this.retaliationBurstShotsFired >= config.behavior().retaliationBurstShots()) {
            this.retaliationBurstShotsFired = 0;
            this.retaliationBurstCooldownTicks = config.behavior().retaliationBurstCooldownTicks();
        }
    }

    private Vec3 getProjectileMuzzlePos() {
        Vec3 forward = Vec3.directionFromRotation(0.0F, this.getYHeadRot()).normalize();
        double horizontalOffset = this.getBbWidth() * 0.5D + this.getSpeciesConfig().behavior().projectileFrontOffset();
        return new Vec3(this.getX() + forward.x * horizontalOffset, this.getY() + this.getBbHeight() * this.getSpeciesConfig().behavior().projectileMuzzleHeightRatio(), this.getZ() + forward.z * horizontalOffset);
    }

    private void rememberRetaliationTarget(LivingEntity attacker) {
        SpeciesConfig config = this.getSpeciesConfig();
        int memoryTicks = config.behavior().retaliationMemoryTicks();
        
        // 只有配置了反击记忆时长（非被动生物）才启动状态机
        if (memoryTicks <= 0) return;

        this.retaliationTarget = attacker;
        this.retaliationTicksRemaining = memoryTicks;
        this.setTarget(attacker);
        
        if (this.retaliationState == RetaliationState.IDLE) {
            this.retaliationState = RetaliationState.PENDING_RETALIATION_DECLARE;
            this.retaliationDeclareTicksRemaining = 0;
        }
    }

    private void updateRetaliationTarget() {
        SpeciesConfig config = this.getSpeciesConfig();
        if (this.retaliationBurstCooldownTicks > 0) this.retaliationBurstCooldownTicks--;
        if (this.retaliationTicksRemaining > 0) this.retaliationTicksRemaining--;
        
        LivingEntity currentTarget = this.retaliationTarget;
        if (this.isValidRetaliationTarget(currentTarget)) {
            this.setTarget(currentTarget);
            this.updateRetaliationState(currentTarget);
            return;
        }
        this.clearRetaliationState(true);
    }

    private void updateRetaliationState(LivingEntity target) {
        if (this.retaliationState == RetaliationState.PENDING_RETALIATION_DECLARE) {
            if (this.retaliationDeclareTicksRemaining > 0) {
                this.retaliationDeclareTicksRemaining--;
                if (this.retaliationDeclareTicksRemaining <= 0) this.retaliationState = RetaliationState.RETALIATING;
                return;
            }
            if (SpeciesSoundFacade.playCue(this, SoundCue.ATTACK_DECLARE, 1.0F, 1.0F)) {
                this.retaliationDeclareTicksRemaining = this.getSpeciesConfig().behavior().attackDeclareDurationTicks();
            } else {
                this.retaliationState = RetaliationState.RETALIATING;
            }
        }
    }

    private void clearRetaliationState(boolean playEndVoice) {
        boolean shouldPlayEndVoice = playEndVoice && this.retaliationState == RetaliationState.RETALIATING;
        this.retaliationTarget = null;
        this.retaliationState = RetaliationState.IDLE;
        this.retaliationBurstShotsFired = 0;
        this.retaliationBurstCooldownTicks = 0;
        if (this.getTarget() != null) this.setTarget(null);
        if (shouldPlayEndVoice) SpeciesSoundFacade.playCue(this, SoundCue.ATTACK_END, 1.0F, 1.0F);
    }

    private boolean isValidRetaliationTarget(@Nullable LivingEntity candidate) {
        if (candidate == null || this.retaliationTicksRemaining <= 0) return false;
        if (!candidate.isAlive()) return false;
        if (this.distanceToSqr(candidate) > Mth.square(this.getSpeciesConfig().behavior().retaliationRange())) return false;
        return this.getSensing().hasLineOfSight(candidate);
    }

    @Override
    public AgeableMob getBreedOffspring(ServerLevel level, AgeableMob other) {
        return null;
    }

    @Override
    public SpeciesConfig getSpeciesConfig() {
        return ModSpeciesConfigs.pinkScarf();
    }

    public static boolean canSpawn(EntityType<TiansuluoPinkScarfEntity> type, net.minecraft.world.level.LevelAccessor level, MobSpawnType spawnType, net.minecraft.core.BlockPos pos, net.minecraft.util.RandomSource random) {
        return level.getBlockState(pos.below()).isSolidRender(level, pos.below());
    }
}
