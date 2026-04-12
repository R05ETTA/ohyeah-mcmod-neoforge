package com.ohyeah.ohyeahmod.entity;

import com.ohyeah.ohyeahmod.registry.ModItems;
import com.ohyeah.ohyeahmod.sound.bridge.SoundParticipant;
import com.ohyeah.ohyeahmod.sound.bridge.SpeciesSoundFacade;
import com.ohyeah.ohyeahmod.sound.definition.SoundCue;
import com.ohyeah.ohyeahmod.sound.definition.SpeciesSoundCatalog;
import com.ohyeah.ohyeahmod.config.SpeciesConfig;
import com.ohyeah.ohyeahmod.config.ModSpeciesConfigs;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.animal.WaterAnimal;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

/**
 * 苏霞实体。
 * 特点：水生生物，具有推进逻辑和逃跑行为。
 */
public class SuxiaEntity extends WaterAnimal implements SoundParticipant {
    private static final int INK_COLOR = 0xF5D142;

    public float tiltAngle;
    public float prevTiltAngle;
    public float thrustTimer;
    public float prevThrustTimer;
    private float swimVelocityScale;
    private float thrustTimerSpeed;
    private float swimX;
    private float swimY;
    private float swimZ;

    public SuxiaEntity(EntityType<? extends SuxiaEntity> entityType, Level level) {
        super(entityType, level);
        this.thrustTimerSpeed = 1.0F / (this.random.nextFloat() + 1.0F) * 0.2F;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes().add(Attributes.MAX_HEALTH, 10.0D);
    }

    public static boolean canSpawn(EntityType<? extends WaterAnimal> type, LevelAccessor level, MobSpawnType reason, BlockPos pos, RandomSource random) {
        return WaterAnimal.checkSurfaceWaterAnimalSpawnRules(type, level, reason, pos, random);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new LandHopGoal());
        this.goalSelector.addGoal(1, new EscapeAttackerGoal());
        this.goalSelector.addGoal(3, new SwimGoal(this));
    }

    @Override
    public void aiStep() {
        super.aiStep();
        this.prevTiltAngle = this.tiltAngle;
        this.prevThrustTimer = this.thrustTimer;
        this.thrustTimer += this.thrustTimerSpeed;
        
        if (this.thrustTimer > (float)Math.PI * 2) {
            this.thrustTimer -= (float)Math.PI * 2;
            if (this.random.nextInt(10) == 0) {
                this.thrustTimerSpeed = 1.0F / (this.random.nextFloat() + 1.0F) * 0.2F;
            }
        }

        if (this.isInWaterOrBubble()) {
            if (this.thrustTimer < (float)Math.PI) {
                float f = this.thrustTimer / (float)Math.PI;
                if (f > 0.75F) {
                    this.swimVelocityScale = 1.0F;
                }
            } else {
                this.swimVelocityScale *= 0.9F;
            }

            if (!this.level().isClientSide) {
                this.setDeltaMovement(this.swimX * this.swimVelocityScale, this.swimY * this.swimVelocityScale, this.swimZ * this.swimVelocityScale);
            }

            Vec3 vec3 = this.getDeltaMovement();
            double d0 = vec3.horizontalDistance();
            this.yBodyRot += (-(float)Mth.atan2(vec3.x, vec3.z) * (180F / (float)Math.PI) - this.yBodyRot) * 0.1F;
            this.setYRot(this.yBodyRot);
            this.tiltAngle += (-(float)Mth.atan2(d0, vec3.y) * (180F / (float)Math.PI) - this.tiltAngle) * 0.1F;
        } else {
            this.tiltAngle += (0.0F - this.tiltAngle) * 0.2F;
        }
        
        SpeciesSoundFacade.tick(this);
    }

    @Override
    public void die(DamageSource source) {
        super.die(source);
        if (!this.level().isClientSide) {
            this.spawnAtLocation(ModItems.XIAMI_HUHU.get());
        }
    }

    public void setSwimmingVector(float x, float y, float z) {
        this.swimX = x;
        this.swimY = y;
        this.swimZ = z;
    }

    @Override
    public String soundSpeciesId() {
        return "suxia";
    }

    @Override
    public SpeciesSoundCatalog soundCatalog() {
        return SpeciesSoundCatalog.SUXIA;
    }

    @Override
    public SpeciesConfig.Voice soundVoiceConfig() {
        return ModSpeciesConfigs.SUXIA_VOICE.voice();
    }

    // --- 内部 Goal ---

    class SwimGoal extends Goal {
        private final SuxiaEntity suxia;

        public SwimGoal(SuxiaEntity suxia) {
            this.suxia = suxia;
        }

        @Override
        public boolean canUse() {
            return true;
        }

        @Override
        public void tick() {
            if (this.suxia.getRandom().nextInt(50) == 0 || !this.suxia.isInWater() || this.suxia.swimX == 0) {
                float f = this.suxia.getRandom().nextFloat() * ((float)Math.PI * 2F);
                float f1 = Mth.cos(f) * 0.2F;
                float f2 = -0.1F + this.suxia.getRandom().nextFloat() * 0.2F;
                float f3 = Mth.sin(f) * 0.2F;
                this.suxia.setSwimmingVector(f1, f2, f3);
            }
        }
    }

    class EscapeAttackerGoal extends Goal {
        @Override
        public boolean canUse() {
            LivingEntity livingentity = SuxiaEntity.this.getLastAttacker();
            return SuxiaEntity.this.isInWater() && livingentity != null;
        }

        @Override
        public void tick() {
            LivingEntity livingentity = SuxiaEntity.this.getLastAttacker();
            if (livingentity != null) {
                Vec3 vec3 = new Vec3(SuxiaEntity.this.getX() - livingentity.getX(), SuxiaEntity.this.getY() - livingentity.getY(), SuxiaEntity.this.getZ() - livingentity.getZ());
                SuxiaEntity.this.setSwimmingVector((float)vec3.x / 20.0F, (float)vec3.y / 20.0F, (float)vec3.z / 20.0F);
            }
        }
    }

    class LandHopGoal extends Goal {
        @Override
        public boolean canUse() {
            return !SuxiaEntity.this.isInWater();
        }

        @Override
        public void tick() {
            if (SuxiaEntity.this.onGround() && SuxiaEntity.this.getRandom().nextInt(20) == 0) {
                SuxiaEntity.this.setDeltaMovement(SuxiaEntity.this.getDeltaMovement().add((SuxiaEntity.this.getRandom().nextFloat() * 2.0F - 1.0F) * 0.2F, 0.5D, (SuxiaEntity.this.getRandom().nextFloat() * 2.0F - 1.0F) * 0.2F));
            }
        }
    }
}
