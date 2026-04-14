package com.ohyeah.ohyeahmod.entity;

import com.ohyeah.ohyeahmod.registry.ModEntityTypes;
import com.ohyeah.ohyeahmod.sound.definition.SoundCue;
import com.ohyeah.ohyeahmod.sound.definition.SpeciesSoundCatalog;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 酥虾实体 (Suxia)。
 * <p>
 * 水生/两栖环境生物。已应用 "内部 Procedure 代理" 与 "数值控制台" 架构对齐重构。
 */
public class SuxiaEntity extends Animal implements com.ohyeah.ohyeahmod.sound.bridge.SoundParticipant {

    // ====================================================================================
    // [数值控制台] 统一管理所有行为与基础参数
    // ====================================================================================

    // --- 1. 核心常量 ---
    public static final String SPECIES_ID = "suxia";

    // --- 2. 基础属性 (Base Attributes) ---
    // 最大生命值。10.0D 相当于玩家的 5 颗心，属于脆弱生物。
    public static final double BASE_MAX_HEALTH = 10.0D;
    // 移动速度。0.25D 属于中等偏慢。
    public static final double BASE_MOVEMENT_SPEED = 0.25D;
    // 索敌追踪距离。
    public static final double BASE_FOLLOW_RANGE = 16.0D;
    // 基础攻击力（被动生物无攻击行为，故为 0）。
    public static final double ATTACK_DAMAGE = 0.0D;

    // --- 3. 生命周期与食物 (LifeCycle & Feeding) ---
    // 喂食喜爱食物时，跳过的成长时长。6000 ticks = 5 分钟。
    public static final int FOOD_GROWTH_STEP = 6000;
    // 喜爱食物：海草、海带（用于吸引与催熟）。
    public static final List<String> FOOD_LIKED = List.of("minecraft:seagrass", "minecraft:kelp");
    // 最爱食物（酥虾目前无特殊最爱机制，留空备用）。
    public static final List<String> FOOD_FAVORITE = List.of();

    // --- 4. 生成配置 (Spawning) ---
    public static final int SPAWN_WEIGHT = 8;
    public static final int SPAWN_MIN_GROUP = 1;
    public static final int SPAWN_MAX_GROUP = 2;
    // 默认生成群系：河流与海洋。
    public static final List<String> DEFAULT_SPAWN_BIOMES = List.of("#minecraft:is_ocean", "#minecraft:is_river");

    // --- 5. 战利品 (Loot) ---
    // 预期掉落物（在 LootTable JSON 中实现，此处做记录）。
    public static final List<String> ADULT_LOOT_ITEMS = List.of("minecraft:cod");

    // --- 6. 语音系统常量 ---
    // 闲聊(Ambient)音效的触发间隔(Tick)。6000 ticks = 5 分钟。
    public static final int AMBIENT_INTERVAL = 6000;

    // ====================================================================================
    // [生命周期与 AI]
    // ====================================================================================

    public SuxiaEntity(EntityType<? extends SuxiaEntity> entityType, Level level) {
        super(entityType, level);
    }

    public static boolean canSpawn(EntityType<? extends Animal> type, LevelAccessor level, MobSpawnType spawnType, BlockPos pos, RandomSource random) {
        return Animal.checkAnimalSpawnRules(type, level, spawnType, pos, random);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, BASE_MAX_HEALTH)
                .add(Attributes.MOVEMENT_SPEED, BASE_MOVEMENT_SPEED)
                .add(Attributes.FOLLOW_RANGE, BASE_FOLLOW_RANGE);
    }

    @Override
    protected void registerGoals() {
        // 受到伤害时逃跑 (1.25倍移速)
        this.goalSelector.addGoal(0, new PanicGoal(this, 1.25D));
        // 允许繁殖
        this.goalSelector.addGoal(1, new BreedGoal(this, 1.0D));
        // 被喜爱食物吸引 (1.2倍移速)
        this.goalSelector.addGoal(2, new TemptGoal(this, 1.2D, stack -> this.isFood(stack), false));
        // 幼体跟随父母
        this.goalSelector.addGoal(3, new FollowParentGoal(this, 1.1D));
        // 漫无目的游荡
        this.goalSelector.addGoal(4, new RandomStrollGoal(this, 1.0D));
        // 盯着玩家看
        this.goalSelector.addGoal(5, new LookAtPlayerGoal(this, net.minecraft.world.entity.player.Player.class, 6.0F));
        // 随机四处张望
        this.goalSelector.addGoal(6, new RandomLookAroundGoal(this));
    }

    @Override
    public boolean isFood(ItemStack stack) {
        return FOOD_LIKED.stream().anyMatch(id -> BuiltInRegistries.ITEM.get(ResourceLocation.parse(id)) == stack.getItem());
    }

    @Override
    public @Nullable AgeableMob getBreedOffspring(ServerLevel level, AgeableMob other) {
        // 返回一只新的酥虾幼崽
        return ModEntityTypes.SUXIA.get().create(level);
    }

    // ====================================================================================
    // [接口实现] 音效系统 (SoundParticipant)
    // 酥虾目前是“哑巴生物”（isVoiceEnabled = false），此区为防御性实现。
    // ====================================================================================

    @Override public String soundSpeciesId() { return SPECIES_ID; }
    @Override public SpeciesSoundCatalog soundCatalog() { return SpeciesSoundCatalog.suxia(); }
    @Override public Set<String> playedSoundCues() { return Collections.emptySet(); }
    @Override public boolean isSoundSilenced(SoundCue cue) { return false; }
    
    @Override public boolean isVoiceEnabled() { return false; }
    @Override public boolean isCueDisabled(SoundCue cue) { return true; }
    @Override public boolean allowsCueWhileSilenced(SoundCue cue) { return false; }
    @Override public boolean enablePipeline() { return false; }
    @Override public boolean enableVanilla() { return false; }
    @Override public Set<String> vanillaCues() { return Collections.emptySet(); }
    @Override public boolean enableLimiter() { return false; }
    @Override public int ambientIntervalTicks() { return AMBIENT_INTERVAL; }
    @Override public int ambientRandomnessTicks() { return 0; }
    @Override public int rareAmbientChance() { return 0; }
    @Override public int intervalTicks(SoundCue cue) { return 20; }
    @Override public Map<String, Integer> voiceOverrides() { return Collections.emptyMap(); }
}
