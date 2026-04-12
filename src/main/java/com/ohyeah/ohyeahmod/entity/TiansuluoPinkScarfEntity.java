package com.ohyeah.ohyeahmod.entity;

import com.ohyeah.ohyeahmod.config.ModSpeciesConfigs;
import com.ohyeah.ohyeahmod.config.SpeciesConfig;
import com.ohyeah.ohyeahmod.entity.common.EggLayingSpecies;
import com.ohyeah.ohyeahmod.entity.tiansuluo.TiansuluoCoreComponent;
import com.ohyeah.ohyeahmod.sound.bridge.SoundParticipant;
import com.ohyeah.ohyeahmod.sound.definition.SoundCue;
import com.ohyeah.ohyeahmod.sound.definition.SpeciesSoundCatalog;
import com.ohyeah.ohyeahmod.registry.ModSoundEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import org.jetbrains.annotations.Nullable;

/**
 * 粉围巾天素罗实体。
 * 特点：远程反击型，支持产卵逻辑，集成组件化核心逻辑。
 */
public class TiansuluoPinkScarfEntity extends Animal implements 
        EggLayingSpecies, 
        SoundParticipant, 
        TiansuluoCoreComponent.TiansuluoEntityInterface,
        RangedAttackMob {

    public static final float WIDTH = 0.7F;
    public static final float HEIGHT = 1.6F;

    private static final EntityDataAccessor<Boolean> HAS_CARRIED_EGG_BLOCK = 
            SynchedEntityData.defineId(TiansuluoPinkScarfEntity.class, EntityDataSerializers.BOOLEAN);

    private final TiansuluoCoreComponent core = new TiansuluoCoreComponent(HAS_CARRIED_EGG_BLOCK);

    public TiansuluoPinkScarfEntity(EntityType<? extends Animal> entityType, Level level) {
        super(entityType, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        SpeciesConfig config = ModSpeciesConfigs.TIANSULUO_PINK_SCARF;
        return Animal.createMobAttributes()
                .add(Attributes.MAX_HEALTH, config.attributes().maxHealth())
                .add(Attributes.MOVEMENT_SPEED, config.attributes().movementSpeed())
                .add(Attributes.FOLLOW_RANGE, config.attributes().followRange());
    }

    public static boolean canSpawn(EntityType<? extends Animal> type, LevelAccessor level, MobSpawnType reason, BlockPos pos, RandomSource random) {
        return Animal.checkAnimalSpawnRules(type, level, reason, pos, random);
    }

    @Override
    protected void registerGoals() {
        SpeciesConfig.Behavior behavior = getSpeciesConfig().behavior();
        this.goalSelector.addGoal(0, new FloatGoal(this));

        this.goalSelector.addGoal(1, new RangedAttackGoal(this, behavior.projectileAttackGoalSpeed(), 
                behavior.retaliationBurstIntervalTicks(), (float) behavior.retaliationRange()));

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
    public void performRangedAttack(LivingEntity target, float velocity) {
        Level level = this.level();
        com.ohyeah.ohyeahmod.entity.projectile.TiansuluoPinkScarfProjectileEntity projectile = 
                new com.ohyeah.ohyeahmod.entity.projectile.TiansuluoPinkScarfProjectileEntity(level, this);

        double d0 = target.getX() - this.getX();
        double d1 = target.getY(0.3333333333333333D) - projectile.getY();
        double d2 = target.getZ() - this.getZ();
        double d3 = Math.sqrt(d0 * d0 + d2 * d2);

        projectile.shoot(d0, d1 + d3 * 0.20000000298023224D, d2, 1.6F, 12.0F);
        this.playSound(SoundEvents.SNOW_GOLEM_SHOOT, 1.0F, 0.4F / (this.getRandom().nextFloat() * 0.4F + 0.8F));
        level.addFreshEntity(projectile);

        core.tryPlayVoice(this, SoundCue.ATTACK_SHOT);
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
        core.tick(this);
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        InteractionResult result = core.handleInteract(this, player, hand, getSpeciesConfig());
        if (result != InteractionResult.PASS) return result;
        return super.mobInteract(player, hand);
    }

    @Override
    public @Nullable AgeableMob getBreedOffspring(ServerLevel level, AgeableMob other) {
        return null;
    }

    @Override
    public SpeciesConfig getSpeciesConfig() {
        return ModSpeciesConfigs.TIANSULUO_PINK_SCARF;
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
        return SpeciesSoundCatalog.PINK_SCARF;
    }

    @Override
    public boolean isSilent() {
        return super.isSilent() || core.isSoundSilenced();
    }
}
