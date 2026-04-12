package com.ohyeah.ohyeahmod.entity;

import com.ohyeah.ohyeahmod.config.ModSpeciesConfigs;
import com.ohyeah.ohyeahmod.config.SpeciesConfig;
import com.ohyeah.ohyeahmod.sound.definition.SoundCue;
import com.ohyeah.ohyeahmod.sound.bridge.SpeciesSoundFacade;
import com.ohyeah.ohyeahmod.sound.bridge.SoundParticipant;
import com.ohyeah.ohyeahmod.sound.definition.SpeciesSoundCatalog;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ColorParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.animal.WaterAnimal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Set;

/**
 * 酥虾。
 * 一种拥有独特划水物理动画的水生生物。
 */
public class SuxiaEntity extends WaterAnimal implements SoundParticipant {
    private static final int SUXIA_INK_COLOR_RGB = 0xF5D142;
    
    public float tiltAngle;
    public float prevTiltAngle;
    public float rollAngle;
    public float prevRollAngle;
    public float thrustTimer;
    public float prevThrustTimer;
    private float thrustTimerSpeed;
    private float swimVelocityScale;
    private float turningSpeed;
    private float swimX, swimY, swimZ;

    private final Set<String> playedCues = new HashSet<>();

    public SuxiaEntity(EntityType<? extends WaterAnimal> type, Level level) {
        super(type, level);
        this.thrustTimerSpeed = 1.0F / (this.random.nextFloat() + 1.0F) * 0.2F;
    }

    public static AttributeSupplier.Builder createAttributes() {
        SpeciesConfig config = ModSpeciesConfigs.suxia();
        return WaterAnimal.createMobAttributes()
                .add(Attributes.MAX_HEALTH, config.attributes().maxHealth())
                .add(Attributes.MOVEMENT_SPEED, config.attributes().movementSpeed());
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new LandHopGoal());
        this.goalSelector.addGoal(1, new SwimGoal(this));
    }

    @Override
    public void aiStep() {
        super.aiStep();
        this.prevTiltAngle = this.tiltAngle;
        this.prevRollAngle = this.rollAngle;
        this.prevThrustTimer = this.thrustTimer;
        this.thrustTimer += this.thrustTimerSpeed;

        if (this.thrustTimer > Mth.TWO_PI) {
            this.thrustTimer -= Mth.TWO_PI;
            if (this.random.nextInt(10) == 0) {
                this.thrustTimerSpeed = 1.0F / (this.random.nextFloat() + 1.0F) * 0.2F;
            }
        }

        if (this.isInWaterOrBubble()) {
            if (this.thrustTimer < Mth.PI) {
                float f = this.thrustTimer / Mth.PI;
                if (f > 0.75F) {
                    this.swimVelocityScale = 1.0F;
                    this.turningSpeed = 1.0F;
                } else {
                    this.turningSpeed *= 0.8F;
                }
            } else {
                this.swimVelocityScale *= 0.9F;
                this.turningSpeed *= 0.99F;
            }

            if (!this.level().isClientSide) {
                this.setDeltaMovement(this.swimX * this.swimVelocityScale, this.swimY * this.swimVelocityScale, this.swimZ * this.swimVelocityScale);
            }

            Vec3 delta = this.getDeltaMovement();
            double horizontalDist = delta.horizontalDistance();
            this.yBodyRot += (-(float) Mth.atan2(delta.x, delta.z) * Mth.RAD_TO_DEG - this.yBodyRot) * 0.1F;
            this.setYRot(this.yBodyRot);
            this.rollAngle += Mth.PI * this.turningSpeed * 1.5F;
            this.tiltAngle += (-(float) Mth.atan2(horizontalDist, delta.y) * Mth.RAD_TO_DEG - this.tiltAngle) * 0.1F;
        } else {
            this.tiltAngle += (0.0F - this.tiltAngle) * 0.2F;
            this.rollAngle += (0.0F - this.rollAngle) * 0.2F;
        }
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (super.hurt(source, amount)) {
            if (!this.level().isClientSide && source.getEntity() instanceof LivingEntity) {
                this.squirt();
            }
            return true;
        }
        return false;
    }

    private void squirt() {
        SpeciesSoundFacade.playCue(this, SoundCue.fromKey("squirt"), 1.0F, 1.0F);
        if (this.level() instanceof ServerLevel serverLevel) {
            for (int i = 0; i < 30; i++) {
                Vec3 velocity = new Vec3(this.random.nextGaussian(), -1.0, this.random.nextGaussian()).scale(0.3 + this.random.nextFloat() * 2.0F);
                serverLevel.sendParticles(ColorParticleOption.create(ParticleTypes.ENTITY_EFFECT, 0.96F, 0.82F, 0.26F), this.getX(), this.getY() + 0.5, this.getZ(), 0, velocity.x, velocity.y, velocity.z, 0.1F);
            }
        }
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        SpeciesConfig config = ModSpeciesConfigs.suxia();
        
        if (config.food().isFavorite(stack)) {
            if (!player.getAbilities().instabuild) stack.shrink(1);
            this.heal(2.0F);
            SpeciesSoundFacade.playCue(this, SoundCue.EAT, 1.0F, 1.0F);
            return InteractionResult.sidedSuccess(this.level().isClientSide);
        }
        return super.mobInteract(player, hand);
    }

    // --- SoundParticipant ---
    @Override public String soundSpeciesId() { return "suxia"; }
    @Override public SpeciesConfig.Voice soundVoiceConfig() { return ModSpeciesConfigs.suxia().voice(); }
    @Override public SpeciesSoundCatalog soundCatalog() { return SpeciesSoundCatalog.suxia(); }
    @Override public Set<String> playedSoundCues() { return this.playedCues; }
    @Override public boolean isSoundSilenced(SoundCue cue) { return false; }

    @Override @Nullable protected SoundEvent getAmbientSound() { return SpeciesSoundFacade.resolveVanillaCue(this, SoundCue.AMBIENT); }
    @Override @Nullable protected SoundEvent getHurtSound(DamageSource source) { return SpeciesSoundFacade.resolveVanillaCue(this, SoundCue.HURT); }
    @Override @Nullable protected SoundEvent getDeathSound() { return SpeciesSoundFacade.resolveVanillaCue(this, SoundCue.DEATH); }

    public void setSwimmingVector(float x, float y, float z) {
        this.swimX = x; this.swimY = y; this.swimZ = z;
    }

    public static boolean canSpawn(EntityType<SuxiaEntity> type, net.minecraft.world.level.LevelAccessor level, MobSpawnType spawnType, BlockPos pos, net.minecraft.util.RandomSource random) {
        return level.getFluidState(pos).is(net.minecraft.tags.FluidTags.WATER) && level.getFluidState(pos.above()).is(net.minecraft.tags.FluidTags.WATER);
    }

    class SwimGoal extends Goal {
        private final SuxiaEntity suxia;
        public SwimGoal(SuxiaEntity suxia) { this.suxia = suxia; }
        @Override public boolean canUse() { return true; }
        @Override public void tick() {
            if (this.suxia.getRandom().nextInt(50) == 0 || !this.suxia.isInWater() || this.suxia.swimX == 0) {
                float angle = this.suxia.getRandom().nextFloat() * Mth.TWO_PI;
                this.suxia.setSwimmingVector(Mth.cos(angle) * 0.2F, -0.1F + this.suxia.getRandom().nextFloat() * 0.2F, Mth.sin(angle) * 0.2F);
            }
        }
    }

    class LandHopGoal extends Goal {
        private int cooldown;
        @Override public boolean canUse() { return !SuxiaEntity.this.isInWater(); }
        @Override public void tick() {
            if (this.cooldown > 0) { this.cooldown--; return; }
            if (!SuxiaEntity.this.onGround()) return;

            float yaw = SuxiaEntity.this.getYRot() + (SuxiaEntity.this.random.nextFloat() - 0.5F) * 70.0F;
            SuxiaEntity.this.setYRot(yaw);
            SuxiaEntity.this.yBodyRot = yaw;

            float rad = yaw * Mth.DEG_TO_RAD;
            SuxiaEntity.this.setDeltaMovement(-Mth.sin(rad) * 0.18F, 0.34F, Mth.cos(rad) * 0.18F);
            this.cooldown = 10 + SuxiaEntity.this.random.nextInt(9);
        }
    }
}
