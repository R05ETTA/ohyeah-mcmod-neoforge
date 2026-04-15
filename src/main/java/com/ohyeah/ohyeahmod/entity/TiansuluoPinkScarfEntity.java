package com.ohyeah.ohyeahmod.entity;

import com.ohyeah.ohyeahmod.entity.ai.LayEggBlockGoal;
import com.ohyeah.ohyeahmod.entity.ai.MateForEggBlockGoal;
import com.ohyeah.ohyeahmod.entity.common.EggLayingSpecies;
import com.ohyeah.ohyeahmod.entity.projectile.TiansuluoPinkScarfProjectileEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.OwnerHurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.OwnerHurtTargetGoal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * 天素罗粉围巾实体类 (主动注册声音管理版)
 */
public class TiansuluoPinkScarfEntity extends TamableAnimal implements EggLayingSpecies {

    public static final String SPECIES_ID = "tiansuluo_pink_scarf";
    public static final float WIDTH = 0.7F;
    public static final float HEIGHT = 0.7F;
    public static final float BABY_SCALE_FACTOR = 0.5F;

    public static final double BASE_MAX_HEALTH = 20.0D;
    public static final double BASE_MOVEMENT_SPEED = 0.25D;
    public static final double BASE_FOLLOW_RANGE = 16.0D;
    public static final double BASE_ATTACK_DAMAGE = 3.0D;
    public static final double TEMPT_SPEED_MODIFIER = 1.15D;

    public static final int RETALIATION_MEMORY_TICKS = 120;
    public static final float RETALIATION_TURN_SPEED = 20.0F;
    public static final int ATTACK_DECLARE_TICKS = 40;

    public static final int BURST_SHOTS = 3;
    public static final double PROJECTILE_DAMAGE = 5.0D;
    public static final float PROJECTILE_SPEED = 3.0F;
    public static final float PROJECTILE_DIVERGENCE = 1.2F;

    public static final int BABY_GROWTH_AGE = 24000;
    public static final int FOOD_GROWTH_STEP = 6000;
    public static final int HATCH_STAGE_TICKS = 200;
    public static final int HATCH_CHANCE_INV = 500;
    public static final int TAME_CHANCE_DENOMINATOR = 3;
    public static final float FOOD_HEAL_AMOUNT = 4.0F;

    public static final List<String> FOOD_LIKED = List.of("minecraft:wheat", "minecraft:carrot", "minecraft:beetroot", "minecraft:potato");
    public static final List<String> FOOD_FAVORITE = List.of("minecraft:cake", "ohyeah:chips");
    
    public static final int SPAWN_WEIGHT = 10;
    public static final int SPAWN_MIN_GROUP = 1;
    public static final int SPAWN_MAX_GROUP = 3;
    public static final List<String> SPAWN_BIOMES = List.of("minecraft:plains", "minecraft:meadow");

    public static final int AMBIENT_INTERVAL = 6000;

    private static final EntityDataAccessor<Boolean> IS_ATTACKING = SynchedEntityData.defineId(TiansuluoPinkScarfEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> HAS_CARRIED_EGG_BLOCK = SynchedEntityData.defineId(TiansuluoPinkScarfEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> IS_SILENCED = SynchedEntityData.defineId(TiansuluoPinkScarfEntity.class, EntityDataSerializers.BOOLEAN);

    private static final String TAG_SILENCED = "SilencedByShears";
    private static final String TAG_HAS_CARRIED_EGG_BLOCK = "HasLuanluanBlock";
    private static final String TAG_EGG_BLOCK_TARGET_X = "LuanluanBlockTargetX";
    private static final String TAG_EGG_BLOCK_TARGET_Y = "LuanluanBlockTargetY";
    private static final String TAG_EGG_BLOCK_TARGET_Z = "LuanluanBlockTargetZ";
    private static final String TAG_EGG_BLOCK_PLACING_COUNTER = "LuanluanBlockPlacingCounter";
    private static final String TAG_EGG_BLOCK_PLAYER_UUID = "LuanluanBlockPlayerUuid";

    private final Set<String> playedCues = new HashSet<>();
    private @Nullable BlockPos eggBlockTargetPos;
    private int eggBlockPlacingCounter;
    private @Nullable UUID eggBlockPlayerUuid;
    private boolean wasBabyLastTick;

    private int retaliationTicksRemaining;
    private int retaliationDeclareTicksRemaining;

    public TiansuluoPinkScarfEntity(EntityType<? extends TiansuluoPinkScarfEntity> entityType, Level level) {
        super(entityType, level);
    }

    public static boolean canSpawn(EntityType<? extends Animal> type, LevelAccessor level, MobSpawnType spawnType, BlockPos pos, RandomSource random) {
        return Animal.checkAnimalSpawnRules(type, level, spawnType, pos, random);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, BASE_MAX_HEALTH)
                .add(Attributes.MOVEMENT_SPEED, BASE_MOVEMENT_SPEED)
                .add(Attributes.FOLLOW_RANGE, BASE_FOLLOW_RANGE)
                .add(Attributes.ATTACK_DAMAGE, BASE_ATTACK_DAMAGE);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new SitWhenOrderedToGoal(this));
        this.goalSelector.addGoal(2, new MateForEggBlockGoal<>(this, 1.1D, "message.ohyeah.tiansuluo_pink_scarf.luanluan_block_carried", com.ohyeah.ohyeahmod.registry.ModSoundEvents.TIANSULUO_PS_BREED_SUCCESS.get()));
        this.goalSelector.addGoal(3, new TemptGoal(this, TEMPT_SPEED_MODIFIER, stack -> this.isLikedFood(stack) || this.isFavoriteFood(stack), false));
        this.goalSelector.addGoal(4, new FollowOwnerGoal(this, 1.1D, 10.0F, 2.0F));
        this.goalSelector.addGoal(5, new FollowParentGoal(this, 1.1D));
        this.goalSelector.addGoal(6, new LayEggBlockGoal<>(this, HATCH_STAGE_TICKS));
        this.goalSelector.addGoal(7, new WaterAvoidingRandomStrollGoal(this, 1.0D));
        this.goalSelector.addGoal(8, new LookAtPlayerGoal(this, Player.class, 6.0F));
        this.goalSelector.addGoal(9, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(1, new OwnerHurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new OwnerHurtTargetGoal(this));
        this.targetSelector.addGoal(3, new HurtByTargetGoal(this));
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(IS_ATTACKING, false);
        builder.define(HAS_CARRIED_EGG_BLOCK, false);
        builder.define(IS_SILENCED, false);
    }

    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType spawnType, @Nullable SpawnGroupData spawnGroupData) {
        SpawnGroupData data = super.finalizeSpawn(level, difficulty, spawnType, spawnGroupData);
        if (spawnType == MobSpawnType.SPAWN_EGG || spawnType == MobSpawnType.BREEDING) {
            this.setBaby(true);
            this.setAge(BABY_GROWTH_AGE);
        }
        return data;
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (this.level().isClientSide) {
            this.handleClientParticlesTick();
            
            // --- 主动式声音管理：背景音轮流发言申请 ---
            if (this.isAlive() && !this.isSilenced()) {
                com.ohyeah.ohyeahmod.client.sound.ClientSoundManager.tick(
                    this, com.ohyeah.ohyeahmod.registry.ModSoundEvents.TIANSULUO_PS_AMBIENT.get());
            }
            return;
        }

        this.handleVoiceSystemTick();
        this.handleEggBlockLogicTick();
        this.handleRetaliationTick();
    }

    @Override
    public void handleEntityEvent(byte status) {
        if (this.level().isClientSide) {
            // 根据状态码主动申请动作音（会打断背景音）
            switch (status) {
                case 2 -> com.ohyeah.ohyeahmod.client.sound.ClientSoundManager.playAction(
                        this, com.ohyeah.ohyeahmod.registry.ModSoundEvents.TIANSULUO_PS_HURT.get(), SoundSource.NEUTRAL);
                case 3 -> com.ohyeah.ohyeahmod.client.sound.ClientSoundManager.playAction(
                        this, com.ohyeah.ohyeahmod.registry.ModSoundEvents.TIANSULUO_PS_DEATH.get(), SoundSource.NEUTRAL);
                case 60 -> com.ohyeah.ohyeahmod.client.sound.ClientSoundManager.playAction(
                        this, com.ohyeah.ohyeahmod.registry.ModSoundEvents.TIANSULUO_PS_ATTACK_DECLARE.get(), SoundSource.NEUTRAL);
                case 61 -> com.ohyeah.ohyeahmod.client.sound.ClientSoundManager.playAction(
                        this, com.ohyeah.ohyeahmod.registry.ModSoundEvents.TIANSULUO_PS_GROW_UP.get(), SoundSource.NEUTRAL);
                case 62 -> com.ohyeah.ohyeahmod.client.sound.ClientSoundManager.playAction(
                        this, com.ohyeah.ohyeahmod.registry.ModSoundEvents.TIANSULUO_PS_SHEAR_REACT.get(), SoundSource.NEUTRAL);
                case 63 -> com.ohyeah.ohyeahmod.client.sound.ClientSoundManager.playAction(
                        this, com.ohyeah.ohyeahmod.registry.ModSoundEvents.TIANSULUO_PS_EAT.get(), SoundSource.NEUTRAL);
                case 64 -> com.ohyeah.ohyeahmod.client.sound.ClientSoundManager.playAction(
                        this, com.ohyeah.ohyeahmod.registry.ModSoundEvents.TIANSULUO_PS_EAT_FAVORITE.get(), SoundSource.NEUTRAL);
                case 65 -> com.ohyeah.ohyeahmod.client.sound.ClientSoundManager.playAction(
                        this, com.ohyeah.ohyeahmod.registry.ModSoundEvents.TIANSULUO_PS_ATTACK_SHOT.get(), SoundSource.NEUTRAL);
            }
        }
        super.handleEntityEvent(status);
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        InteractionResult toolResult = this.handleShearInteraction(player, hand, stack);
        if (toolResult.consumesAction()) return toolResult;
        InteractionResult foodResult = this.handleTamingAndFeeding(player, hand, stack);
        if (foodResult.consumesAction()) return foodResult;
        return super.mobInteract(player, hand);
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        boolean actuallyHurt = super.hurt(source, amount);
        if (actuallyHurt && !this.level().isClientSide) {
            this.handleHurtRetaliationTrigger(source); 
        }
        return actuallyHurt;
    }

    private void handleClientParticlesTick() {
        if (this.hasCarriedEggBlock() && !this.isBaby() && this.tickCount % 10 == 0) {
            this.level().addParticle(ParticleTypes.HEART, this.getRandomX(0.6D), this.getRandomY() + 0.5D, this.getRandomZ(0.6D), 0, 0.02D, 0);
        }
    }

    private void handleVoiceSystemTick() {
        boolean babyNow = this.isBaby();
        if (this.wasBabyLastTick && !babyNow) {
            this.level().broadcastEntityEvent(this, (byte) 61);
        }
        this.wasBabyLastTick = babyNow;
    }

    private void handleEggBlockLogicTick() {
        if (this.hasCarriedEggBlock() && this.eggBlockPlayerUuid != null) {
            Player player = this.level().getPlayerByUUID(this.eggBlockPlayerUuid);
            if (player == null || !player.isAlive() || player.isSpectator()) {
                this.eggBlockPlayerUuid = null;
            }
        }
    }

    private void handleRetaliationTick() {
        if (this.retaliationTicksRemaining > 0) {
            this.retaliationTicksRemaining--;
            LivingEntity target = this.getTarget();
            if (target != null && target.isAlive()) {
                this.getLookControl().setLookAt(target, RETALIATION_TURN_SPEED, RETALIATION_TURN_SPEED);
                if (this.retaliationDeclareTicksRemaining > 0) {
                    this.retaliationDeclareTicksRemaining--;
                    this.setAttacking(true);
                    if (this.retaliationDeclareTicksRemaining == 0) {
                        this.performRetaliationAttack(target);
                    }
                }
            } else {
                this.retaliationTicksRemaining = 0;
                this.setAttacking(false);
            }
        } else {
            this.setAttacking(false);
        }
    }

    private InteractionResult handleShearInteraction(Player player, InteractionHand hand, ItemStack stack) {
        if (stack.is(Items.SHEARS)) {
            if (this.level().isClientSide) return !this.isSilenced() ? InteractionResult.CONSUME : InteractionResult.PASS;
            if (!this.isSilenced()) {
                this.spawnAtLocation(Items.WHITE_WOOL);
                this.level().playSound(null, this, SoundEvents.SHEEP_SHEAR, SoundSource.PLAYERS, 1.0F, 1.0F);
                this.level().broadcastEntityEvent(this, (byte) 62);
                this.setSilenced(true);
                stack.hurtAndBreak(1, player, hand == InteractionHand.MAIN_HAND ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND);
                return InteractionResult.SUCCESS;
            }
            return InteractionResult.PASS;
        }
        return InteractionResult.PASS;
    }

    private InteractionResult handleTamingAndFeeding(Player player, InteractionHand hand, ItemStack stack) {
        boolean isLiked = this.isLikedFood(stack);
        boolean isFavorite = this.isFavoriteFood(stack);
        if (!isLiked && !isFavorite) return InteractionResult.PASS;
        if (this.level().isClientSide) return InteractionResult.CONSUME;
        if (isFavorite && !this.isTame()) {
            this.usePlayerItem(player, hand, stack);
            if (this.random.nextInt(TAME_CHANCE_DENOMINATOR) == 0 && !net.neoforged.neoforge.event.EventHooks.onAnimalTame(this, player)) {
                this.tame(player);
                this.navigation.stop();
                this.setTarget(null);
                this.level().broadcastEntityEvent(this, (byte) 7);
            } else {
                this.level().broadcastEntityEvent(this, (byte) 6);
            }
            return InteractionResult.SUCCESS;
        }
        if (this.isTame() && this.isOwnedBy(player) && isFavorite && this.getHealth() < this.getMaxHealth()) {
            this.usePlayerItem(player, hand, stack);
            this.heal(FOOD_HEAL_AMOUNT);
            this.level().broadcastEntityEvent(this, (byte) 64);
            return InteractionResult.SUCCESS;
        }
        boolean didSomething = false;
        if (this.isBaby()) {
            int growth = isFavorite ? -this.getAge() : FOOD_GROWTH_STEP; 
            this.ageUp(growth);
            didSomething = true;
        }
        if (this.isSilenced()) {
            this.setSilenced(false);
            didSomething = true;
        }
        if (didSomething || (isLiked || isFavorite)) { 
            this.usePlayerItem(player, hand, stack);
            this.level().broadcastEntityEvent(this, isFavorite ? (byte) 64 : (byte) 63);
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }

    private void handleHurtRetaliationTrigger(DamageSource source) {
        Entity attackerEntity = source.getEntity();
        if (!(attackerEntity instanceof LivingEntity attackerLiving)) return;
        if (this.isTame() && attackerLiving == this.getOwner()) return;
        this.retaliationTicksRemaining = RETALIATION_MEMORY_TICKS;
        this.retaliationDeclareTicksRemaining = ATTACK_DECLARE_TICKS;
        this.level().broadcastEntityEvent(this, (byte) 60);
    }

    private void performRetaliationAttack(LivingEntity target) {
        if (this.level().isClientSide) return;
        for (int i = 0; i < BURST_SHOTS; i++) {
            TiansuluoPinkScarfProjectileEntity projectile = new TiansuluoPinkScarfProjectileEntity(this.level(), this);
            projectile.setDamage((float) PROJECTILE_DAMAGE);
            Vec3 muzzlePos = this.getRetaliationMuzzlePosition();
            projectile.setPos(muzzlePos.x, muzzlePos.y, muzzlePos.z);
            double d0 = target.getX() - muzzlePos.x;
            double d1 = target.getY(0.33D) - muzzlePos.y;
            double d2 = target.getZ() - muzzlePos.z;
            double d3 = Math.sqrt(d0 * d0 + d2 * d2);
            projectile.shoot(d0, d1 + d3 * 0.2D, d2, PROJECTILE_SPEED, PROJECTILE_DIVERGENCE);
            this.level().addFreshEntity(projectile);
        }
        this.level().broadcastEntityEvent(this, (byte) 65);
    }

    private Vec3 getRetaliationMuzzlePosition() {
        Vec3 forward = this.getViewVector(1.0F);
        double horizontalOffset = this.getBbWidth() * 0.5D + 0.65D; 
        return new Vec3(this.getX() + forward.x * horizontalOffset, this.getY() + this.getBbHeight() * 0.15D, this.getZ() + forward.z * horizontalOffset);
    }

    private boolean isLikedFood(ItemStack stack) { return FOOD_LIKED.stream().anyMatch(id -> BuiltInRegistries.ITEM.get(ResourceLocation.parse(id)) == stack.getItem()); }
    private boolean isFavoriteFood(ItemStack stack) { return FOOD_FAVORITE.stream().anyMatch(id -> BuiltInRegistries.ITEM.get(ResourceLocation.parse(id)) == stack.getItem()); }
    @Override public boolean isFood(ItemStack stack) { return this.isLikedFood(stack) || this.isFavoriteFood(stack); }
    public void setAttacking(boolean attacking) { this.entityData.set(IS_ATTACKING, attacking); }
    public boolean isAttacking() { return this.entityData.get(IS_ATTACKING); }
    public void setSilenced(boolean silenced) { this.entityData.set(IS_SILENCED, silenced); }
    public boolean isSilenced() { return this.entityData.get(IS_SILENCED); }
    @Override public boolean hasCarriedEggBlock() { return this.entityData.get(HAS_CARRIED_EGG_BLOCK); }
    @Override public void setHasCarriedEggBlock(boolean has) { 
        this.entityData.set(HAS_CARRIED_EGG_BLOCK, has);
        if (!has) { this.eggBlockTargetPos = null; this.eggBlockPlacingCounter = 0; this.eggBlockPlayerUuid = null; }
    }
    @Override public @Nullable BlockPos getCarriedEggBlockTargetPos() { return this.eggBlockTargetPos; }
    @Override public void setCarriedEggBlockTargetPos(@Nullable BlockPos pos) { this.eggBlockTargetPos = pos; }
    @Override public int getEggBlockPlacingCounter() { return this.eggBlockPlacingCounter; }
    @Override public void setEggBlockPlacingCounter(int counter) { this.eggBlockPlacingCounter = counter; }
    @Override public @Nullable UUID getEggBlockAttractedPlayerUuid() { return this.eggBlockPlayerUuid; }
    @Override public void setEggBlockAttractedPlayerUuid(@Nullable UUID uuid) { this.eggBlockPlayerUuid = uuid; }

    @Override @Nullable protected SoundEvent getAmbientSound() { return null; }
    @Override @Nullable protected SoundEvent getHurtSound(DamageSource source) { return null; }
    @Override @Nullable protected SoundEvent getDeathSound() { return null; }
    @Override public int getAmbientSoundInterval() { return AMBIENT_INTERVAL; }

    @Override
    public void addAdditionalSaveData(CompoundTag nbt) {
        super.addAdditionalSaveData(nbt);
        nbt.putBoolean(TAG_SILENCED, this.isSilenced());
        nbt.putBoolean(TAG_HAS_CARRIED_EGG_BLOCK, this.hasCarriedEggBlock());
        if (this.eggBlockTargetPos != null) {
            nbt.putInt(TAG_EGG_BLOCK_TARGET_X, this.eggBlockTargetPos.getX());
            nbt.putInt(TAG_EGG_BLOCK_TARGET_Y, this.eggBlockTargetPos.getY());
            nbt.putInt(TAG_EGG_BLOCK_TARGET_Z, this.eggBlockTargetPos.getZ());
        }
        nbt.putInt(TAG_EGG_BLOCK_PLACING_COUNTER, this.eggBlockPlacingCounter);
        if (this.eggBlockPlayerUuid != null) nbt.putUUID(TAG_EGG_BLOCK_PLAYER_UUID, this.eggBlockPlayerUuid);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag nbt) {
        super.readAdditionalSaveData(nbt);
        this.setSilenced(nbt.getBoolean(TAG_SILENCED));
        this.setHasCarriedEggBlock(nbt.getBoolean(TAG_HAS_CARRIED_EGG_BLOCK));
        if (nbt.contains(TAG_EGG_BLOCK_TARGET_X)) {
            this.eggBlockTargetPos = new BlockPos(nbt.getInt(TAG_EGG_BLOCK_TARGET_X), nbt.getInt(TAG_EGG_BLOCK_TARGET_Y), nbt.getInt(TAG_EGG_BLOCK_TARGET_Z));
        }
        this.eggBlockPlacingCounter = nbt.getInt(TAG_EGG_BLOCK_PLACING_COUNTER);
        if (nbt.hasUUID(TAG_EGG_BLOCK_PLAYER_UUID)) this.eggBlockPlayerUuid = nbt.getUUID(TAG_EGG_BLOCK_PLAYER_UUID);
    }

    @Override public @Nullable AgeableMob getBreedOffspring(ServerLevel level, AgeableMob other) { return null; }
}
