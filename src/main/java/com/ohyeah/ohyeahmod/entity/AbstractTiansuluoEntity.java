package com.ohyeah.ohyeahmod.entity;

import com.ohyeah.ohyeahmod.config.SpeciesConfig;
import com.ohyeah.ohyeahmod.entity.common.EggLayingSpecies;
import com.ohyeah.ohyeahmod.sound.bridge.SoundParticipant;
import com.ohyeah.ohyeahmod.sound.bridge.SpeciesSoundFacade;
import com.ohyeah.ohyeahmod.sound.definition.SoundCue;
import com.ohyeah.ohyeahmod.sound.definition.SpeciesSoundCatalog;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.goal.BreedGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import net.minecraft.world.DifficultyInstance;

/**
 * 田螺物种基类。
 * 统筹处理音效交互、产卵块逻辑及剪刀禁言等通用玩法。
 */
public abstract class AbstractTiansuluoEntity extends Animal implements EggLayingSpecies, SoundParticipant {
    private static final EntityDataAccessor<Boolean> HAS_CARRIED_EGG_BLOCK = SynchedEntityData.defineId(AbstractTiansuluoEntity.class, EntityDataSerializers.BOOLEAN);
    
    private static final String TAG_SILENCED_BY_SHEARS = "SilencedByShears";
    private static final String TAG_HAS_CARRIED_EGG_BLOCK = "HasLuanluanBlock";
    private static final String TAG_EGG_BLOCK_TARGET_X = "LuanluanBlockTargetX";
    private static final String TAG_EGG_BLOCK_TARGET_Y = "LuanluanBlockTargetY";
    private static final String TAG_EGG_BLOCK_TARGET_Z = "LuanluanBlockTargetZ";
    private static final String TAG_EGG_BLOCK_PLACING_COUNTER = "LuanluanBlockPlacingCounter";
    private static final String TAG_EGG_BLOCK_PLAYER_UUID = "LuanluanBlockPlayerUuid";

    private final Set<String> playedCues = new HashSet<>();
    private boolean silencedByShears;
    private @Nullable BlockPos eggBlockTargetPos;
    private int eggBlockPlacingCounter;
    private @Nullable UUID eggBlockPlayerUuid;
    private boolean wasBabyLastTick;

    protected AbstractTiansuluoEntity(EntityType<? extends Animal> entityType, Level level) {
        super(entityType, level);
    }

    /**
     * 获取物种特定的配置 Record。
     * 由子类实现，内部应指向 ModSpeciesConfigs 的动态工厂。
     */
    public abstract SpeciesConfig getSpeciesConfig();

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(HAS_CARRIED_EGG_BLOCK, false);
    }

    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType spawnType, @Nullable SpawnGroupData spawnGroupData) {
        SpawnGroupData data = super.finalizeSpawn(level, difficulty, spawnType, spawnGroupData);
        if (spawnType == MobSpawnType.SPAWN_EGG || spawnType == MobSpawnType.BREEDING) {
            this.setBaby(true);
            this.setAge(this.getSpeciesConfig().breeding().babyGrowthAgeTicks());
        }
        return data;
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (this.level().isClientSide) {
            if (this.shouldDisplayCarriedEggBlockParticles()) this.spawnCarriedEggBlockParticles();
            return;
        }
        
        // 只有在语音系统启用时才更新音效流水线
        if (this.getSpeciesConfig().voice().enabled()) {
            SpeciesSoundFacade.tick(this);
        }
        
        // 只有在使用卵块逻辑时才更新吸引玩家的状态
        if (this.getSpeciesConfig().breeding().usesEggBlock()) {
            this.updateEggBlockAttractedPlayer();
        }
        
        this.updateGrowthVoice();
    }

    @Override
    public boolean isFood(ItemStack stack) {
        // 利用配置 Record 内置的智能判定
        return this.getSpeciesConfig().food().isAnyFood(stack);
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        SpeciesConfig config = this.getSpeciesConfig();
        
        // 剪刀禁言交互逻辑
        if (config.voice().silenceRules().contains("shears") && stack.is(Items.SHEARS)) {
            if (this.level().isClientSide) return !this.silencedByShears ? InteractionResult.CONSUME : InteractionResult.PASS;
            return this.tryUseShears(player, hand, stack) ? InteractionResult.SUCCESS : InteractionResult.PASS;
        }

        // 喂食逻辑：利用配置对象的业务方法简化判定
        boolean isLiked = config.food().isLiked(stack);
        boolean isFavorite = config.food().isFavorite(stack);
        
        InteractionResult result = super.mobInteract(player, hand);
        if (!this.level().isClientSide && result.consumesAction() && (isLiked || isFavorite)) {
            if (this.isBaby()) {
                // 最爱食物使其实际瞬间成年（或根据配置步进）
                int growth = isFavorite ? -this.getAge() : config.food().likedFoodGrowthStepTicks();
                this.ageUp(growth);
            }
            if (this.silencedByShears) this.silencedByShears = false; // 喂食可解除禁言
            SpeciesSoundFacade.playCue(this, isFavorite ? SoundCue.EAT_FAVORITE : SoundCue.EAT, 1.0F, 1.0F);
        }
        return result;
    }

    private boolean tryUseShears(Player player, InteractionHand hand, ItemStack stack) {
        if (this.silencedByShears) return false;
        this.spawnAtLocation(Items.WHITE_WOOL); // 掉落战利品
        this.level().playSound(null, this, SoundEvents.SHEEP_SHEAR, SoundSource.PLAYERS, 1.0F, 1.0F);
        SpeciesSoundFacade.playCue(this, SoundCue.SHEAR_REACT, 1.0F, 1.0F);
        this.silencedByShears = true;
        stack.hurtAndBreak(1, player, getSlotForHandCustom(hand));
        return true;
    }

    private static EquipmentSlot getSlotForHandCustom(InteractionHand hand) {
        return hand == InteractionHand.MAIN_HAND ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND;
    }

    @Override
    public void die(DamageSource damageSource) {
        super.die(damageSource);
        if (this.level().isClientSide || this.isBaby()) return;
        
        SpeciesConfig.Loot loot = this.getSpeciesConfig().loot();
        if (loot != null && loot.enabled()) {
            // 这里后期可以接入更复杂的 Loot 注入逻辑
        }
    }

    // --- 音效接口实现 (SoundParticipant) ---
    @Override @Nullable protected SoundEvent getAmbientSound() {
        return SpeciesSoundFacade.resolveVanillaCue(this, SoundCue.AMBIENT);
    }
    @Override @Nullable protected SoundEvent getHurtSound(DamageSource source) {
        return SpeciesSoundFacade.resolveVanillaCue(this, SoundCue.HURT);
    }
    @Override @Nullable protected SoundEvent getDeathSound() {
        return SpeciesSoundFacade.resolveVanillaCue(this, SoundCue.DEATH);
    }
    @Override protected void playStepSound(BlockPos pos, BlockState state) {}
    
    @Override public String soundSpeciesId() { return this.getSpeciesConfig().speciesId(); }
    @Override public SpeciesConfig.Voice soundVoiceConfig() { return this.getSpeciesConfig().voice(); }
    @Override public SpeciesSoundCatalog soundCatalog() { 
        // 彻底收口：利用配置对象自带的路由功能，消除子类 if-else 判断
        return this.getSpeciesConfig().getSoundCatalog(); 
    }
    @Override public Set<String> playedSoundCues() { return this.playedCues; }
    @Override public boolean isSoundSilenced(SoundCue cue) { 
        return this.silencedByShears && !this.soundVoiceConfig().allowsCueWhileSilenced(cue); 
    }

    // --- 产卵块核心逻辑 (EggLayingSpecies) ---
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

    protected void updateEggBlockAttractedPlayer() {
        if (this.getSpeciesConfig().breeding().usesEggBlock() && this.hasCarriedEggBlock() && this.eggBlockPlayerUuid != null) {
            Player player = this.level().getPlayerByUUID(this.eggBlockPlayerUuid);
            if (player == null || !player.isAlive() || player.isSpectator()) this.eggBlockPlayerUuid = null;
        }
    }

    protected boolean shouldDisplayCarriedEggBlockParticles() {
        SpeciesConfig.Breeding breeding = this.getSpeciesConfig().breeding();
        return breeding.usesEggBlock() && this.hasCarriedEggBlock() && !this.isBaby() && breeding.showCarriedParticles();
    }

    protected void spawnCarriedEggBlockParticles() {
        if (this.tickCount % 10 != 0) return;
        this.level().addParticle(ParticleTypes.HEART, this.getRandomX(0.6D), this.getRandomY() + 0.5D, this.getRandomZ(0.6D), 0, 0.02D, 0);
    }

    private void updateGrowthVoice() {
        boolean babyNow = this.isBaby();
        if (this.wasBabyLastTick && !babyNow) SpeciesSoundFacade.playCue(this, SoundCue.GROW_UP, 1.0F, 1.0F);
        this.wasBabyLastTick = babyNow;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag nbt) {
        super.addAdditionalSaveData(nbt);
        nbt.putBoolean(TAG_SILENCED_BY_SHEARS, this.silencedByShears);
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
        this.silencedByShears = nbt.getBoolean(TAG_SILENCED_BY_SHEARS);
        this.setHasCarriedEggBlock(nbt.getBoolean(TAG_HAS_CARRIED_EGG_BLOCK));
        if (nbt.contains(TAG_EGG_BLOCK_TARGET_X)) {
            this.eggBlockTargetPos = new BlockPos(nbt.getInt(TAG_EGG_BLOCK_TARGET_X), nbt.getInt(TAG_EGG_BLOCK_TARGET_Y), nbt.getInt(TAG_EGG_BLOCK_TARGET_Z));
        }
        this.eggBlockPlacingCounter = nbt.getInt(TAG_EGG_BLOCK_PLACING_COUNTER);
        if (nbt.hasUUID(TAG_EGG_BLOCK_PLAYER_UUID)) this.eggBlockPlayerUuid = nbt.getUUID(TAG_EGG_BLOCK_PLAYER_UUID);
    }

    // --- AI Goals (核心逻辑类) ---
    protected final class MateForEggBlockGoal extends BreedGoal {
        private final AbstractTiansuluoEntity tiansuluo;
        public MateForEggBlockGoal(AbstractTiansuluoEntity tiansuluo, double speed) {
            super(tiansuluo, speed);
            this.tiansuluo = tiansuluo;
        }
        @Override public boolean canUse() { 
            return this.tiansuluo.getSpeciesConfig().breeding().usesEggBlock() && !this.tiansuluo.hasCarriedEggBlock() && super.canUse(); 
        }
        @Override protected void breed() {
            ServerPlayer player = this.animal.getLoveCause();
            if (player == null && this.partner.getLoveCause() != null) player = this.partner.getLoveCause();
            this.tiansuluo.setHasCarriedEggBlock(true);
            this.tiansuluo.setEggBlockAttractedPlayer(player);
            if (player != null) player.displayClientMessage(Component.translatable(this.tiansuluo.getSpeciesConfig().breeding().carriedMessageKey()), true);
            SpeciesSoundFacade.playCue(this.tiansuluo, SoundCue.BREED_SUCCESS, 1.0F, 1.0F);
            this.animal.setAge(6000);
            this.partner.setAge(6000);
            this.animal.resetLove();
            this.partner.resetLove();
            this.animal.level().broadcastEntityEvent(this.animal, (byte)18);
            if (this.animal.level().getGameRules().getBoolean(net.minecraft.world.level.GameRules.RULE_DOMOBLOOT)) {
                this.animal.level().addFreshEntity(new ExperienceOrb(this.animal.level(), this.animal.getX(), this.animal.getY(), this.animal.getZ(), this.animal.getRandom().nextInt(7) + 1));
            }
        }
    }

    protected final class LayEggBlockGoal extends Goal {
        @Override public boolean canUse() { 
            return AbstractTiansuluoEntity.this.getSpeciesConfig().breeding().usesEggBlock() && AbstractTiansuluoEntity.this.hasCarriedEggBlock() && !AbstractTiansuluoEntity.this.isBaby(); 
        }
        @Override public void tick() {
            BlockPos target = AbstractTiansuluoEntity.this.getCarriedEggBlockTargetPos();
            if (target == null) {
                target = AbstractTiansuluoEntity.this.blockPosition().below();
                AbstractTiansuluoEntity.this.setCarriedEggBlockTargetPos(target);
            }
            if (AbstractTiansuluoEntity.this.distanceToSqr(Vec3.atCenterOf(target)) > 4.0D) {
                AbstractTiansuluoEntity.this.getNavigation().moveTo(target.getX(), target.getY(), target.getZ(), 1.0D);
            } else {
                AbstractTiansuluoEntity.this.eggBlockPlacingCounter++;
                if (AbstractTiansuluoEntity.this.eggBlockPlacingCounter > AbstractTiansuluoEntity.this.getSpeciesConfig().breeding().hatchStageTicks()) {
                    // 放置逻辑由外部 Block 系统协同处理，这里仅负责状态清除
                    AbstractTiansuluoEntity.this.setHasCarriedEggBlock(false);
                }
            }
        }
    }

    @Override
    public @Nullable AgeableMob getBreedOffspring(ServerLevel level, AgeableMob other) {
        return null;
    }
}
