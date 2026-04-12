package com.ohyeah.ohyeahmod.entity;

import com.ohyeah.ohyeahmod.config.ModSpeciesConfigs;
import com.ohyeah.ohyeahmod.config.SpeciesConfig;
import com.ohyeah.ohyeahmod.entity.common.EggLayingSpecies;
import com.ohyeah.ohyeahmod.entity.tiansuluo.TiansuluoCoreComponent;
import com.ohyeah.ohyeahmod.sound.bridge.SoundParticipant;
import com.ohyeah.ohyeahmod.sound.definition.SoundCue;
import com.ohyeah.ohyeahmod.sound.definition.SpeciesSoundCatalog;
import com.ohyeah.ohyeahmod.entity.ai.TiansuluoBattleFacePounceGoal;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

/**
 * 战斗脸天素罗实体。
 * 特点：近战扑击型，支持产卵逻辑，集成组件化核心逻辑。
 */
public class TiansuluoBattleFaceEntity extends Animal implements 
        EggLayingSpecies, 
        SoundParticipant, 
        TiansuluoCoreComponent.TiansuluoEntityInterface {

    public static final float TARGET_ADULT_HEIGHT = 1.2F;
    public static final float TARGET_ADULT_WIDTH = 0.8F;
    public static final float BABY_SCALE_FACTOR = 0.5F;

    private static final EntityDataAccessor<Boolean> HAS_CARRIED_EGG_BLOCK = 
            SynchedEntityData.defineId(TiansuluoBattleFaceEntity.class, EntityDataSerializers.BOOLEAN);

    private final TiansuluoCoreComponent core = new TiansuluoCoreComponent(HAS_CARRIED_EGG_BLOCK);

    private int attackCooldownTicksRemaining;

    public TiansuluoBattleFaceEntity(EntityType<? extends Animal> entityType, Level level) {
        super(entityType, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        SpeciesConfig config = ModSpeciesConfigs.TIANSULUO_BATTLE_FACE;
        return Animal.createMobAttributes()
                .add(Attributes.MAX_HEALTH, config.attributes().maxHealth())
                .add(Attributes.MOVEMENT_SPEED, config.attributes().movementSpeed())
                .add(Attributes.FOLLOW_RANGE, config.attributes().followRange())
                .add(Attributes.ATTACK_DAMAGE, config.attributes().attackDamage());
    }

    public static boolean canSpawn(EntityType<? extends Animal> type, LevelAccessor level, MobSpawnType reason, BlockPos pos, RandomSource random) {
        return Animal.checkAnimalSpawnRules(type, level, reason, pos, random);
    }

    @Override
    protected void registerGoals() {
        SpeciesConfig.Behavior behavior = getSpeciesConfig().behavior();
        this.goalSelector.addGoal(0, new FloatGoal(this));
        
        // 扑击 Goal
        this.goalSelector.addGoal(1, new TiansuluoBattleFacePounceGoal(this));
        
        this.goalSelector.addGoal(2, new TiansuluoCoreComponent.MateForEggBlockGoal(this, behavior.mateGoalSpeed()));
        this.goalSelector.addGoal(3, new TiansuluoCoreComponent.LayEggBlockGoal(this));
        
        this.goalSelector.addGoal(4, new TemptGoal(this, behavior.temptSpeed(), this::isFood, false));
        this.goalSelector.addGoal(5, new FollowParentGoal(this, behavior.followParentSpeed()));
        this.goalSelector.addGoal(6, new WaterAvoidingRandomStrollGoal(this, behavior.wanderSpeed()));
        this.goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 6.0F));
        this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));
    }

    @Override
    public boolean isFood(ItemStack stack) {
        return core.getInteractionSupport().isBreedingItem(this, stack);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        core.defineSynchedData(builder);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        core.addAdditionalSaveData(tag, this);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        core.readAdditionalSaveData(tag, this);
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (this.attackCooldownTicksRemaining > 0) this.attackCooldownTicksRemaining--;
        core.tick(this);
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        InteractionResult result = core.handleInteract(this, player, hand, getSpeciesConfig());
        if (result != InteractionResult.PASS) return result;
        return super.mobInteract(player, hand);
    }

    @Override
    public boolean doHurtTarget(Entity target) {
        boolean flag = super.doHurtTarget(target);
        if (flag && target instanceof LivingEntity living) {
            double d0 = living.getX() - this.getX();
            double d1 = living.getZ() - this.getZ();
            Vec3 vec3 = (new Vec3(d0, 0.0D, d1)).normalize().scale(1.15D);
            living.push(vec3.x, 0.35D, vec3.z);
            living.hasImpulse = true;
        }
        return flag;
    }

    @Override
    public @Nullable AgeableMob getBreedOffspring(ServerLevel level, AgeableMob other) {
        return null;
    }

    @Override
    public SpeciesConfig getSpeciesConfig() {
        return ModSpeciesConfigs.TIANSULUO_BATTLE_FACE;
    }

    @Override
    public TiansuluoCoreComponent getCore() {
        return core;
    }

    @Override
    public TiansuluoCoreComponent.EggLayingSupport getEggLayingSupport() {
        return core.getEggLayingSupport();
    }

    @Override
    public String soundSpeciesId() {
        return getSpeciesConfig().speciesId();
    }

    @Override
    public SpeciesSoundCatalog soundCatalog() {
        return SpeciesSoundCatalog.TIANSULUO;
    }

    @Override
    public boolean isSilent() {
        return super.isSilent() || core.isSoundSilenced();
    }

    public void setAttackCooldown(int ticks) {
        this.attackCooldownTicksRemaining = ticks;
    }

    public boolean isAttackCooldownReady() {
        return this.attackCooldownTicksRemaining <= 0;
    }
}
