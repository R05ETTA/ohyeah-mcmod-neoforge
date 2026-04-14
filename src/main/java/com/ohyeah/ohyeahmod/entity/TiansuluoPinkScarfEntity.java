package com.ohyeah.ohyeahmod.entity;

import com.ohyeah.ohyeahmod.entity.ai.LayEggBlockGoal;
import com.ohyeah.ohyeahmod.entity.ai.MateForEggBlockGoal;
import com.ohyeah.ohyeahmod.entity.common.EggLayingSpecies;
import com.ohyeah.ohyeahmod.entity.projectile.TiansuluoPinkScarfProjectileEntity;
import com.ohyeah.ohyeahmod.sound.bridge.SoundParticipant;
import com.ohyeah.ohyeahmod.sound.bridge.SpeciesSoundFacade;
import com.ohyeah.ohyeahmod.sound.definition.SoundCue;
import com.ohyeah.ohyeahmod.sound.definition.SpeciesSoundCatalog;
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

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * 天素�?(粉围�? 实体�?
 * <p>
 * 已应�?"内部 Procedure 代理" 架构重构，将所有庞大的行为逻辑拆分为独立的 handleXxx 方法�?
 * 继承�?TamableAnimal，获得认主、跟随、坐下等宠物特性�?
 */
public class TiansuluoPinkScarfEntity extends TamableAnimal implements EggLayingSpecies, SoundParticipant {

    // ====================================================================================
    // [数值控制台] 统一管理所有行为与战斗参数，修改此区域即可调整实体平衡�?
    // ====================================================================================

    // --- 1. 核心常量与物理参�?---
    public static final String SPECIES_ID = "tiansuluo_pink_scarf";
    // 成年体碰撞箱宽度与高度�?
    public static final float WIDTH = 0.7F;
    public static final float HEIGHT = 0.7F;
    // 幼年体相较于成年体的缩放比例�?
    public static final float BABY_SCALE_FACTOR = 0.5F;

    // --- 2. 基础属�?(Base Attributes) ---
    // 最大生命值�?0.0D 相当于玩家的 10 颗心�?
    public static final double BASE_MAX_HEALTH = 20.0D;
    // 移动速度�?.25 属于中等偏慢�?
    public static final double BASE_MOVEMENT_SPEED = 0.25D;
    // 索敌追踪距离�?
    public static final double BASE_FOLLOW_RANGE = 16.0D;
    // 基础近战攻击�?(目前依靠弹幕复仇，近战作为补�?�?
    public static final double BASE_ATTACK_DAMAGE = 3.0D;
    // 被最爱食物吸引时的移速乘数�?.15 倍移速�?
    public static final double TEMPT_SPEED_MODIFIER = 1.15D;

    // --- 3. 战斗与语音系统联动逻辑 (方案2：服务端精确读秒控制) ---
    // 仇恨记忆时长：受击后记住仇恨并尝试反击的总时间�?20 ticks = 6秒。超出此时间未完成反击则放弃�?
    public static final int RETALIATION_MEMORY_TICKS = 120;
    // 转身速度：蓄力期间死死盯住攻击者的转身灵敏度�?0.0F 属于较快�?
    public static final float RETALIATION_TURN_SPEED = 20.0F;

    // 【关键】宣战语音时长（蓄力前摇）：
    // 40 ticks = 2.0秒。受击时触发语音并开始倒数，倒数归零瞬间开火，保证“语音播完立刻反击”的完美同步�?
    public static final int ATTACK_DECLARE_TICKS = 40;

    // 弹幕射击参数�?
    // 一次反击发射的弹射物数�?散弹)�?
    public static final int BURST_SHOTS = 3;
    // 单发弹射物的基础伤害�?
    public static final double PROJECTILE_DAMAGE = 5.0D;
    // 弹射物飞行速度。合适值：1.5F~4.0F�?
    public static final float PROJECTILE_SPEED = 3.0F;
    // 弹射物散射精准度偏移。值越大越散�?.2F 为轻微散射�?
    public static final float PROJECTILE_DIVERGENCE = 1.2F;

    // --- 4. 生命周期与驯服喂�?(Taming & LifeCycle) ---
    // 幼年期总时长（Tick）�?4000 ticks = 20 分钟（Minecraft 的一天）�?
    public static final int BABY_GROWTH_AGE = 24000;
    // 喂食普通喜爱食物时，跳过的成长时长�?000 ticks = 5 分钟�?
    public static final int FOOD_GROWTH_STEP = 6000;
    // 产卵块孵化所需的最小 Tick 数。
    public static final int HATCH_STAGE_TICKS = 200;
    // 产卵块每 Tick 随机孵化的概率倒数，值越小孵化越快。500 表示平均 500 tick 增加一次孵化进度。
    public static final int HATCH_CHANCE_INV = 500;
    // 驯服成功率的分母 (1/3 的概率成功)。
    public static final int TAME_CHANCE_DENOMINATOR = 3;
    // 驯服后，喂食最爱食物所回复的生命值�?.0F = 2 颗心�?
    public static final float FOOD_HEAL_AMOUNT = 4.0F;

    // --- 5. 喜好配置与生成限�?---
    // 普通喜爱食物：用于吸引、催熟、交配�?
    public static final List<String> FOOD_LIKED = List.of("minecraft:wheat", "minecraft:carrot", "minecraft:beetroot", "minecraft:potato");
    // 极度喜爱食物：用于驯服、回血、瞬间催熟成年�?
    public static final List<String> FOOD_FAVORITE = List.of("minecraft:cake", "ohyeah:chips");
    // 生成权重与群系限制�?
    public static final int SPAWN_WEIGHT = 10;
    public static final int SPAWN_MIN_GROUP = 1;
    public static final int SPAWN_MAX_GROUP = 3;
    public static final List<String> SPAWN_BIOMES = List.of("minecraft:plains", "minecraft:meadow");

    // --- 6. 语音系统常量 ---
    // 闲聊(Ambient)音效的触发间�?Tick)�?000 ticks = 5 分钟尝试触发一次�?
    public static final int AMBIENT_INTERVAL = 6000;
    // 特殊音效的触发冷却覆盖。防止连续受�?闲聊鬼畜�?
    public static final Map<String, Integer> VOICE_OVERRIDES = Map.of("ambient", 60, "hurt", 20);

    // ====================================================================================
    // [内部状态字段与数据同步]
    // ====================================================================================

    // --- 同步数据定义 (Client-Server Synchronization) ---
    private static final EntityDataAccessor<Boolean> IS_ATTACKING = SynchedEntityData.defineId(TiansuluoPinkScarfEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> HAS_CARRIED_EGG_BLOCK = SynchedEntityData.defineId(TiansuluoPinkScarfEntity.class, EntityDataSerializers.BOOLEAN);

    // --- NBT 存储键�?---
    private static final String TAG_SILENCED_BY_SHEARS = "SilencedByShears";
    private static final String TAG_HAS_CARRIED_EGG_BLOCK = "HasLuanluanBlock";
    private static final String TAG_EGG_BLOCK_TARGET_X = "LuanluanBlockTargetX";
    private static final String TAG_EGG_BLOCK_TARGET_Y = "LuanluanBlockTargetY";
    private static final String TAG_EGG_BLOCK_TARGET_Z = "LuanluanBlockTargetZ";
    private static final String TAG_EGG_BLOCK_PLACING_COUNTER = "LuanluanBlockPlacingCounter";
    private static final String TAG_EGG_BLOCK_PLAYER_UUID = "LuanluanBlockPlayerUuid";

    // --- 实体内部状�?(Entity Local State) ---
    private final Set<String> playedCues = new HashSet<>();
    private boolean silencedByShears;
    private @Nullable BlockPos eggBlockTargetPos;
    private int eggBlockPlacingCounter;
    private @Nullable UUID eggBlockPlayerUuid;
    private boolean wasBabyLastTick;

    // 追踪敌人的剩余时长倒数。大�?时表示正处于愤�?索敌状态�?
    private int retaliationTicksRemaining;
    // 宣战语音/蓄力倒数。受击时被赋�?ATTACK_DECLARE_TICKS，当其倒数�?0 的瞬间，触发开火逻辑�?
    private int retaliationDeclareTicksRemaining;

    // ====================================================================================
    // [生命周期�?AI] 初始化与状态定�?
    // ====================================================================================

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
        this.goalSelector.addGoal(1, new SitWhenOrderedToGoal(this)); // [宠物特性] 听从命令坐下
        this.goalSelector.addGoal(2, new MateForEggBlockGoal<>(this, 1.1D, "message.ohyeah.tiansuluo_pink_scarf.luanluan_block_carried"));
        this.goalSelector.addGoal(3, new TemptGoal(this, TEMPT_SPEED_MODIFIER, stack -> this.isLikedFood(stack) || this.isFavoriteFood(stack), false));
        // [宠物特性] 跟随主人。移动速度乘数1.1，距离主�?0格开始跟随，距离2格停止跟随�?
        this.goalSelector.addGoal(4, new FollowOwnerGoal(this, 1.1D, 10.0F, 2.0F));
        this.goalSelector.addGoal(5, new FollowParentGoal(this, 1.1D));
        this.goalSelector.addGoal(6, new LayEggBlockGoal<>(this, HATCH_STAGE_TICKS));
        this.goalSelector.addGoal(7, new WaterAvoidingRandomStrollGoal(this, 1.0D));
        this.goalSelector.addGoal(8, new LookAtPlayerGoal(this, Player.class, 6.0F));
        this.goalSelector.addGoal(9, new RandomLookAroundGoal(this));

        // [宠物特性] 目标锁定逻辑升级�?
        // 如果主人被攻击，则锁定攻击者；如果主人攻击了某目标，则锁定该目标�?
        this.targetSelector.addGoal(1, new OwnerHurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new OwnerHurtTargetGoal(this));
        // 兜底逻辑：被打了还是会还手�?
        this.targetSelector.addGoal(3, new HurtByTargetGoal(this));
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(IS_ATTACKING, false);
        builder.define(HAS_CARRIED_EGG_BLOCK, false);
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

    // ====================================================================================
    // [核心事件入口] 将复杂逻辑委托给内部的 Procedure 代理方法
    // ====================================================================================

    @Override
    public void aiStep() {
        super.aiStep();
        if (this.level().isClientSide) {
            this.handleClientParticlesTick(); // [代理] 处理客户端粒子表�?
            return;
        }

        // 服务端逻辑
        this.handleVoiceSystemTick();         // [代理] 更新语音系统与成长广�?
        this.handleEggBlockLogicTick();       // [代理] 处理搬运卵块跟随玩家的逻辑
        this.handleRetaliationTick();         // [代理] 处理复仇倒数与开火状态机
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        // 1. 尝试触发工具互动 (如：剪刀禁言)
        InteractionResult toolResult = this.handleShearInteraction(player, hand, stack);
        if (toolResult.consumesAction()) return toolResult;

        // 2. 尝试触发喂食/驯服/坐下逻辑
        InteractionResult foodResult = this.handleTamingAndFeeding(player, hand, stack);
        if (foodResult.consumesAction()) return foodResult;

        // 3. 兜底处理 (原版 TamableAnimal 处理空手右键坐下/站起)
        return super.mobInteract(player, hand);
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        boolean actuallyHurt = super.hurt(source, amount);
        this.handleHurtRetaliationTrigger(actuallyHurt, source); // [代理] 处理受击反击判定
        return actuallyHurt;
    }

    // ====================================================================================
    // [内部 Procedure 逻辑实现区] 具体的业务逻辑全部分离在此，实现高内聚易维�?
    // ====================================================================================

    /**
     * Procedure: 处理客户端心形粒子（用于展示搬起卵块的状态）�?
     */
    private void handleClientParticlesTick() {
        if (this.hasCarriedEggBlock() && !this.isBaby() && this.tickCount % 10 == 0) {
            this.level().addParticle(ParticleTypes.HEART, this.getRandomX(0.6D), this.getRandomY() + 0.5D, this.getRandomZ(0.6D), 0, 0.02D, 0);
        }
    }

    /**
     * Procedure: 驱动服务端的环境音效流水线，并在幼体长大时广播成长语音�?
     */
    private void handleVoiceSystemTick() {
        if (this.isVoiceEnabled()) {
            SpeciesSoundFacade.tick(this);
        }

        boolean babyNow = this.isBaby();
        if (this.wasBabyLastTick && !babyNow) {
            SpeciesSoundFacade.playCue(this, SoundCue.GROW_UP, 1.0F, 1.0F);
        }
        this.wasBabyLastTick = babyNow;
    }

    /**
     * Procedure: 验证卵块吸引的玩家是否仍有效（存活且非旁观模式）�?
     */
    private void handleEggBlockLogicTick() {
        if (this.hasCarriedEggBlock() && this.eggBlockPlayerUuid != null) {
            Player player = this.level().getPlayerByUUID(this.eggBlockPlayerUuid);
            if (player == null || !player.isAlive() || player.isSpectator()) {
                this.eggBlockPlayerUuid = null;
            }
        }
    }

    /**
     * Procedure: 战斗状态机核心。负责扣减锁定仇恨倒计时，执行转向，并在宣战倒计时结束时开火�?
     */
    private void handleRetaliationTick() {
        if (this.retaliationTicksRemaining > 0) {
            this.retaliationTicksRemaining--;
            LivingEntity target = this.getTarget();
            if (target != null && target.isAlive()) {
                this.getLookControl().setLookAt(target, RETALIATION_TURN_SPEED, RETALIATION_TURN_SPEED);
                if (this.retaliationDeclareTicksRemaining > 0) {
                    this.retaliationDeclareTicksRemaining--;
                    this.setAttacking(true); // 同步客户端：处于蓄力状�?
                    if (this.retaliationDeclareTicksRemaining == 0) {
                        this.performRetaliationAttack(target); // 时间到，发射弹幕
                    }
                }
            } else {
                // 目标丢失或死亡，中断复仇状�?
                this.retaliationTicksRemaining = 0;
                this.setAttacking(false);
            }
        } else {
            this.setAttacking(false);
        }
    }

    /**
     * Procedure: 拦截玩家使用剪刀互动的逻辑（禁言掉毛）�?
     */
    private InteractionResult handleShearInteraction(Player player, InteractionHand hand, ItemStack stack) {
        if (stack.is(Items.SHEARS)) {
            if (this.level().isClientSide)
                return !this.silencedByShears ? InteractionResult.CONSUME : InteractionResult.PASS;
            if (!this.silencedByShears) {
                this.spawnAtLocation(Items.WHITE_WOOL);
                this.level().playSound(null, this, SoundEvents.SHEEP_SHEAR, SoundSource.PLAYERS, 1.0F, 1.0F);
                SpeciesSoundFacade.playCue(this, SoundCue.SHEAR_REACT, 1.0F, 1.0F);
                this.silencedByShears = true; // 开启禁言状�?
                stack.hurtAndBreak(1, player, hand == InteractionHand.MAIN_HAND ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND);
                return InteractionResult.SUCCESS;
            }
            return InteractionResult.PASS; // 已被剪过，忽略交�?
        }
        return InteractionResult.PASS;
    }

    /**
     * Procedure: 处理喂食逻辑（催熟、解除禁言）与驯服逻辑�?
     */
    private InteractionResult handleTamingAndFeeding(Player player, InteractionHand hand, ItemStack stack) {
        boolean isLiked = this.isLikedFood(stack);
        boolean isFavorite = this.isFavoriteFood(stack);

        if (!isLiked && !isFavorite) {
            return InteractionResult.PASS;
        }

        // 客户端直接返回成功以同步动作，所有实际数据计算在服务端执行�?
        if (this.level().isClientSide) {
            return InteractionResult.CONSUME;
        }

        // --- 驯服逻辑 (Taming Logic) ---
        // 只有 "最�? 食物才具有驯服与回血能力�?
        if (isFavorite && !this.isTame()) {
            this.usePlayerItem(player, hand, stack); // 消耗物�?
            if (this.random.nextInt(TAME_CHANCE_DENOMINATOR) == 0 && !net.neoforged.neoforge.event.EventHooks.onAnimalTame(this, player)) {
                this.tame(player); // 认主
                this.navigation.stop();
                this.setTarget(null);
                this.level().broadcastEntityEvent(this, (byte) 7); // 播放心形粒子 (成功)
            } else {
                this.level().broadcastEntityEvent(this, (byte) 6); // 播放黑烟粒子 (失败)
            }
            return InteractionResult.SUCCESS;
        }

        // --- 喂食回血 (Healing Logic) ---
        // 被驯服后，喂食最爱食物可以回血�?
        if (this.isTame() && this.isOwnedBy(player) && isFavorite && this.getHealth() < this.getMaxHealth()) {
            this.usePlayerItem(player, hand, stack); // 消耗物�?
            this.heal(FOOD_HEAL_AMOUNT);
            SpeciesSoundFacade.playCue(this, SoundCue.EAT_FAVORITE, 1.0F, 1.0F);
            return InteractionResult.SUCCESS;
        }

        // --- 催熟与解除禁言 (Growth & Shear Remedy) ---
        // 适用于所有喜爱食物。未成年可催熟，被剪过毛可恢复发生能力�?
        boolean didSomething = false;
        if (this.isBaby()) {
            int growth = isFavorite ? -this.getAge() : FOOD_GROWTH_STEP; // 最爱食物瞬间成年，否则加速成�?
            this.ageUp(growth);
            didSomething = true;
        }
        if (this.silencedByShears) {
            this.silencedByShears = false;
            didSomething = true;
        }

        if (didSomething || (isLiked || isFavorite)) { // 如果食物有效，即使满血成年也会消耗并播放吃东西的声音
            this.usePlayerItem(player, hand, stack); // 消耗物�?
            SpeciesSoundFacade.playCue(this, isFavorite ? SoundCue.EAT_FAVORITE : SoundCue.EAT, 1.0F, 1.0F);
            return InteractionResult.SUCCESS;
        }

        return InteractionResult.PASS;
    }

    /**
     * Procedure: 精确捕获受击瞬间，执行“不可原谅”判定，并重置反击状态机的倒计时�?
     */
    private void handleHurtRetaliationTrigger(boolean actuallyHurt, DamageSource source) {
        if (!actuallyHurt || this.level().isClientSide) return;

        Entity attackerEntity = source.getEntity();
        if (!(attackerEntity instanceof LivingEntity attackerLiving)) return;

        // [防误伤]：如果它是一只被驯服的宠物，绝对不能对它的主人反击�?
        if (this.isTame() && attackerLiving == this.getOwner()) return;

        // 设置追踪敌人的时间�?
        this.retaliationTicksRemaining = RETALIATION_MEMORY_TICKS;
        // 设置开火前的宣言语音播放倒计时�?
        this.retaliationDeclareTicksRemaining = ATTACK_DECLARE_TICKS;

        // 播放蓄力/宣言语音。当倒计时结束时，将发射弹幕�?
        SpeciesSoundFacade.playCue(this, SoundCue.ATTACK_DECLARE, 1.0F, 1.0F);
    }

    /**
     * Procedure: 实际执行三连发弹射物的物理发射逻辑与音效�?
     */
    private void performRetaliationAttack(LivingEntity target) {
        for (int i = 0; i < BURST_SHOTS; i++) {
            TiansuluoPinkScarfProjectileEntity projectile = new TiansuluoPinkScarfProjectileEntity(this.level(), this);
            projectile.setDamage((float) PROJECTILE_DAMAGE);
            Vec3 muzzlePos = this.getRetaliationMuzzlePosition();
            projectile.setPos(muzzlePos.x, muzzlePos.y, muzzlePos.z);

            // 预测弹道：瞄准目标的身体偏上位置�?
            double d0 = target.getX() - muzzlePos.x;
            double d1 = target.getY(0.33D) - muzzlePos.y;
            double d2 = target.getZ() - muzzlePos.z;
            double d3 = Math.sqrt(d0 * d0 + d2 * d2);

            projectile.shoot(d0, d1 + d3 * 0.2D, d2, PROJECTILE_SPEED, PROJECTILE_DIVERGENCE);
            this.level().addFreshEntity(projectile);
        }
        SpeciesSoundFacade.playCue(this, SoundCue.ATTACK_SHOT, 1.0F, 1.0F);
    }

    /**
     * 辅助计算弹射物发射点的空间向量位置�?
     */
    private Vec3 getRetaliationMuzzlePosition() {
        Vec3 forward = this.getViewVector(1.0F);
        double horizontalOffset = this.getBbWidth() * 0.5D + 0.65D; // 发射点在身前略微偏移
        return new Vec3(this.getX() + forward.x * horizontalOffset, this.getY() + this.getBbHeight() * 0.15D, this.getZ() + forward.z * horizontalOffset);
    }

    // ====================================================================================
    // [基础辅助判定方法]
    // ====================================================================================

    private boolean isLikedFood(ItemStack stack) {
        return FOOD_LIKED.stream().anyMatch(id -> BuiltInRegistries.ITEM.get(ResourceLocation.parse(id)) == stack.getItem());
    }

    private boolean isFavoriteFood(ItemStack stack) {
        return FOOD_FAVORITE.stream().anyMatch(id -> BuiltInRegistries.ITEM.get(ResourceLocation.parse(id)) == stack.getItem());
    }

    @Override
    public boolean isFood(ItemStack stack) {
        return this.isLikedFood(stack) || this.isFavoriteFood(stack);
    }

    public void setAttacking(boolean attacking) {
        this.entityData.set(IS_ATTACKING, attacking);
    }

    public boolean isAttacking() {
        return this.entityData.get(IS_ATTACKING);
    }

    // ====================================================================================
    // [接口实现] 产卵块核心逻辑 (EggLayingSpecies)
    // ====================================================================================
    @Override
    public boolean hasCarriedEggBlock() {
        return this.entityData.get(HAS_CARRIED_EGG_BLOCK);
    }

    @Override
    public void setHasCarriedEggBlock(boolean has) {
        this.entityData.set(HAS_CARRIED_EGG_BLOCK, has);
        if (!has) {
            this.eggBlockTargetPos = null;
            this.eggBlockPlacingCounter = 0;
            this.eggBlockPlayerUuid = null;
        }
    }

    @Override
    public @Nullable BlockPos getCarriedEggBlockTargetPos() {
        return this.eggBlockTargetPos;
    }

    @Override
    public void setCarriedEggBlockTargetPos(@Nullable BlockPos pos) {
        this.eggBlockTargetPos = pos;
    }

    @Override
    public int getEggBlockPlacingCounter() {
        return this.eggBlockPlacingCounter;
    }

    @Override
    public void setEggBlockPlacingCounter(int counter) {
        this.eggBlockPlacingCounter = counter;
    }

    @Override
    public @Nullable UUID getEggBlockAttractedPlayerUuid() {
        return this.eggBlockPlayerUuid;
    }

    @Override
    public void setEggBlockAttractedPlayerUuid(@Nullable UUID uuid) {
        this.eggBlockPlayerUuid = uuid;
    }

    // ====================================================================================
    // [接口实现] 音效系统 (SoundParticipant)
    // ====================================================================================
    @Override
    @Nullable
    protected SoundEvent getAmbientSound() {
        return SpeciesSoundFacade.resolveVanillaCue(this, SoundCue.AMBIENT);
    }

    @Override
    @Nullable
    protected SoundEvent getHurtSound(DamageSource source) {
        return SpeciesSoundFacade.resolveVanillaCue(this, SoundCue.HURT);
    }

    @Override
    @Nullable
    protected SoundEvent getDeathSound() {
        return SpeciesSoundFacade.resolveVanillaCue(this, SoundCue.DEATH);
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState state) {
    }

    @Override
    public String soundSpeciesId() {
        return SPECIES_ID;
    }

    @Override
    public SpeciesSoundCatalog soundCatalog() {
        return SpeciesSoundCatalog.pinkScarf();
    }

    @Override
    public Set<String> playedSoundCues() {
        return this.playedCues;
    }

    @Override
    public boolean isSoundSilenced(SoundCue cue) {
        return this.silencedByShears && !this.allowsCueWhileSilenced(cue);
    }

    @Override
    public boolean isVoiceEnabled() {
        return true;
    }

    @Override
    public boolean isCueDisabled(SoundCue cue) {
        return false;
    }

    @Override
    public boolean allowsCueWhileSilenced(SoundCue cue) {
        return Set.of("eat", "eat_favorite", "shear_react").contains(cue.key());
    }

    @Override
    public boolean enablePipeline() {
        return true;
    }

    @Override
    public boolean enableVanilla() {
        return false;
    }

    @Override
    public Set<String> vanillaCues() {
        return Collections.emptySet();
    }

    @Override
    public boolean enableLimiter() {
        return true;
    }

    @Override
    public int ambientIntervalTicks() {
        return AMBIENT_INTERVAL;
    }

    @Override
    public int ambientRandomnessTicks() {
        return 0;
    }

    @Override
    public int rareAmbientChance() {
        return 5;
    }

    @Override
    public int intervalTicks(SoundCue cue) {
        return 20;
    }

    @Override
    public Map<String, Integer> voiceOverrides() {
        return VOICE_OVERRIDES;
    }

    // ====================================================================================
    // [数据序列化]
    // ====================================================================================
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

    @Override
    public @Nullable AgeableMob getBreedOffspring(ServerLevel level, AgeableMob other) {
        // [宠物特性] 繁育后代，由 Minecraft 原版控制�?
        // 因为天素罗是用卵块繁殖的，所以返�?null 阻止原版幼崽繁殖机制�?
        return null;
    }
}
