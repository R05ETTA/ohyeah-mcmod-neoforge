package com.ohyeah.ohyeahmod.entity;

import com.ohyeah.ohyeahmod.entity.ai.LayEggBlockGoal;
import com.ohyeah.ohyeahmod.entity.ai.MateForEggBlockGoal;
import com.ohyeah.ohyeahmod.entity.ai.PounceAttackGoal;
import com.ohyeah.ohyeahmod.entity.common.Pounceable;
import com.ohyeah.ohyeahmod.entity.common.EggLayingSpecies;
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
 * 澶╃礌缃?(鎴樻枟鑴? 瀹炰綋銆?
 * <p>
 * 宸插簲鐢?"鍐呴儴 Procedure 浠ｇ悊" 鏋舵瀯閲嶆瀯锛屽皢搴炲ぇ鐨勮涓洪€昏緫鎷嗗垎涓虹嫭绔嬬殑 handleXxx 鏂规硶銆?
 * 缁ф壙鑷?TamableAnimal锛岃幏寰楄涓汇€佽窡闅忋€佸潗涓嬬瓑瀹犵墿鐗规€э紝鍚屾椂淇濈暀鍏剁嫭鐗圭殑鈥滆搫鍔涢鎵戔€濊繎鎴樻満鍒躲€?
 */
public class TiansuluoBattleFaceEntity extends TamableAnimal implements EggLayingSpecies, SoundParticipant, Pounceable {
    
    // ====================================================================================
    // [鏁板€兼帶鍒跺彴] 缁熶竴绠＄悊鎵€鏈夎涓轰笌鎴樻枟鍙傛暟锛屼慨鏀规鍖哄煙鍗冲彲璋冩暣瀹炰綋骞宠　鎬?
    // ====================================================================================

    // --- 1. 鏍稿績甯搁噺涓庣墿鐞嗗弬鏁?---
    public static final String SPECIES_ID = "tiansuluo_battle_face";
    // 鎴愬勾浣撶鎾炵瀹藉害涓庨珮搴︺€?
    public static final float TARGET_ADULT_WIDTH = 0.8F;
    public static final float TARGET_ADULT_HEIGHT = 0.8F;
    // 骞煎勾浣撶浉杈冧簬鎴愬勾浣撶殑缂╂斁姣斾緥銆?
    public static final float BABY_SCALE_FACTOR = 0.5F;

    // --- 2. 鍩虹灞炴€?(Base Attributes) ---
    // 鏈€澶х敓鍛藉€笺€?4.0D 鐩稿綋浜庣帺瀹剁殑 12 棰楀績锛屾瘮绮夊洿宸炬洿鑲夈€?
    public static final double BASE_MAX_HEALTH = 24.0D;
    // 绉诲姩閫熷害銆?.28 灞炰簬涓瓑鍋忓揩銆?
    public static final double BASE_MOVEMENT_SPEED = 0.28D;
    // 绱㈡晫杩借釜璺濈銆?
    public static final double BASE_FOLLOW_RANGE = 16.0D;
    // 鍩虹杩戞垬鏀诲嚮鍔涖€?.0D = 2 棰楀績銆?
    public static final double BASE_ATTACK_DAMAGE = 4.0D;
    // 琚渶鐖遍鐗╁惛寮曟椂鐨勭Щ閫熶箻鏁般€?.1 鍊嶇Щ閫熴€?
    public static final double TEMPT_SPEED_MODIFIER = 1.1D;

    // --- 3. 鎴樻枟涓庤闊崇郴缁熻仈鍔ㄩ€昏緫 (鏂规2锛氭湇鍔＄绮剧‘璇荤鎺у埗) ---
    // 浠囨仺璁板繂鏃堕暱锛氬彈鍑诲悗璁颁綇浠囨仺骞跺皾璇曞弽鍑荤殑鎬绘椂闂淬€?00 ticks = 5 绉掋€傚悎閫傝寖鍥达細100~200銆?
    public static final int RETALIATION_MEMORY_TICKS = 100;
    // 鎵戝嚮绱㈡晫鍒ゅ畾鑼冨洿锛氱洰鏍囧湪姝よ寖鍥村唴鎵嶅厑璁歌Е鍙戦鎵?AI銆傚悎閫傝寖鍥达細8.0~12.0銆?
    public static final double RETALIATION_RANGE = 10.0D;
    // 杞韩閫熷害锛氳搫鍔涙湡闂寸洴浣忕洰鏍囩殑鐏垫晱搴︺€?2.0F 灞炰簬鏋佸揩锛岀鍚堟垬鏂楄劯鐨勬晱鎹疯瀹氥€?
    public static final float RETALIATION_TURN_SPEED = 22.0F;
    
    // 銆愬叧閿€戝鎴樿闊虫椂闀匡紙钃勫姏鍓嶆憞锛夛細
    // 鏂规2鏍稿績鏈哄埗銆傝鍊煎繀椤讳笌璧勬簮鍖呬腑 ATTACK_DECLARE (ogg鏂囦欢) 鐨勭墿鐞嗘椂闀夸弗鏍煎榻愶紒
    // 40 ticks = 2.0绉掋€傚彈鍑绘椂瑙﹀彂璇煶骞跺紑濮嬪€掓暟锛屽€掓暟鏈熼棿 entityData 浼氳鏍囪涓?attacking 鐘舵€侊紝鐢ㄤ簬椹卞姩瀹㈡埛绔挱鏀捐搫鍔涘姩鐢汇€?
    public static final int ATTACK_DECLARE_TICKS = 40;
    
    // 鎵戝嚮(Pounce)鐗╃悊鍙傛暟锛?
    // 椋炴墤姘村钩鍒濋€熷害鎺ㄥ姏銆?.0D 鏋佸叿鐖嗗彂鍔涖€?
    public static final double POUNCE_HORIZ_SPEED = 3.0D;
    // 椋炴墤鍨傜洿璧疯烦鍒濋€熷害銆?.15D 鑳借秺杩囧ぇ绾?鏍奸珮鐨勯殰纰嶇墿銆?
    public static final double POUNCE_VERT_SPEED = 1.15D;
    // 椋炴墤婊炵┖瀹归敊鏃堕棿銆傝秴杩囨 Tick 鏁拌嫢鏈惤鍦版垨鏈懡涓紝寮哄埗缁撴潫椋炴墤鐘舵€侀槻姝㈠崱姝汇€?
    public static final int POUNCE_MAX_FLIGHT = 12;
    // 鍛戒腑纰版挒绠辩殑琛ユ鑶ㄨ儉鍊笺€?.2D 鑳借椋炴墤鏇村鏄撴摝涓帺瀹躲€?
    public static final double POUNCE_PADDING = 0.2D;

    // --- 4. 鐢熷懡鍛ㄦ湡涓庨┋鏈嶅杺椋?(Taming & LifeCycle) ---
    // 骞煎勾鏈熸€绘椂闀匡紙Tick锛夈€?4000 ticks = 20 鍒嗛挓锛圡inecraft 鐨勪竴澶╋級銆?
    public static final int BABY_GROWTH_AGE = 24000;
    // 鍠傞鏅€氬枩鐖遍鐗╂椂锛岃烦杩囩殑鎴愰暱鏃堕暱銆?000 ticks = 5 鍒嗛挓銆?
    public static final int FOOD_GROWTH_STEP = 6000;
    // 浜у嵉鍧楀鍖栨墍闇€鐨勬渶灏?Tick 鏁般€?
    public static final int HATCH_STAGE_TICKS = 200;
    // 浜у嵉鍧楁瘡 Tick 闅忔満瀛靛寲鐨勬鐜囧€掓暟銆?00 琛ㄧず骞冲潎 500 tick 澧炲姞涓€娆″鍖栬繘搴︺€?
    public static final int HATCH_CHANCE_INV = 500;
    // 椹湇鎴愬姛鐜囩殑鍒嗘瘝 (1/3 鐨勬鐜囨垚鍔?銆?
    public static final int TAME_CHANCE_DENOMINATOR = 3;
    // 椹湇鍚庯紝鍠傞鏈€鐖遍鐗╂墍鍥炲鐨勭敓鍛藉€笺€?.0F = 2 棰楀績銆?
    public static final float FOOD_HEAL_AMOUNT = 4.0F;

    // --- 5. 鍠滃ソ閰嶇疆涓庣敓鎴愰檺鍒?---
    // 鏅€氬枩鐖遍鐗╋細鐢ㄤ簬鍚稿紩銆佸偓鐔熴€佷氦閰嶃€?
    public static final List<String> FOOD_LIKED = List.of("minecraft:wheat", "minecraft:carrot", "minecraft:beetroot", "minecraft:potato");
    // 鏋佸害鍠滅埍椋熺墿锛氱敤浜庨┋鏈嶃€佸洖琛€銆佺灛闂村偓鐔熸垚骞淬€?
    public static final List<String> FOOD_FAVORITE = List.of("minecraft:cake", "ohyeah:chips");
    // 鐢熸垚鏉冮噸涓庣兢绯婚檺鍒躲€?
    public static final int SPAWN_WEIGHT = 1;
    public static final int SPAWN_MIN_GROUP = 1;
    public static final int SPAWN_MAX_GROUP = 1;
    public static final List<String> SPAWN_BIOMES = List.of("minecraft:plains", "minecraft:meadow");

    // --- 6. 璇煶绯荤粺甯搁噺 ---
    // 闂茶亰(Ambient)闊虫晥鐨勮Е鍙戦棿闅?Tick)銆?000 ticks = 5 鍒嗛挓灏濊瘯瑙﹀彂涓€娆°€?
    public static final int AMBIENT_INTERVAL = 6000;
    // 鐗规畩闊虫晥鐨勮Е鍙戝喎鍗磋鐩栥€傞槻姝㈣繛缁彈鍑?闂茶亰楝肩暅銆?
    public static final Map<String, Integer> VOICE_OVERRIDES = Map.of("ambient", 60, "hurt", 20);

    // ====================================================================================
    // [鍐呴儴鐘舵€佸瓧娈典笌鏁版嵁鍚屾]
    // ====================================================================================
    
    // --- 鍚屾鏁版嵁瀹氫箟 (Client-Server Synchronization) ---
    private static final EntityDataAccessor<Boolean> IS_ATTACKING = SynchedEntityData.defineId(TiansuluoBattleFaceEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> HAS_CARRIED_EGG_BLOCK = SynchedEntityData.defineId(TiansuluoBattleFaceEntity.class, EntityDataSerializers.BOOLEAN);

    // --- NBT 瀛樺偍閿€?---
    private static final String TAG_SILENCED_BY_SHEARS = "SilencedByShears";
    private static final String TAG_HAS_CARRIED_EGG_BLOCK = "HasLuanluanBlock";
    private static final String TAG_EGG_BLOCK_TARGET_X = "LuanluanBlockTargetX";
    private static final String TAG_EGG_BLOCK_TARGET_Y = "LuanluanBlockTargetY";
    private static final String TAG_EGG_BLOCK_TARGET_Z = "LuanluanBlockTargetZ";
    private static final String TAG_EGG_BLOCK_PLACING_COUNTER = "LuanluanBlockPlacingCounter";
    private static final String TAG_EGG_BLOCK_PLAYER_UUID = "LuanluanBlockPlayerUuid";

    // --- 瀹炰綋鍐呴儴鐘舵€?(Entity Local State) ---
    private final Set<String> playedCues = new HashSet<>();
    private boolean silencedByShears;
    private @Nullable BlockPos eggBlockTargetPos;
    private int eggBlockPlacingCounter;
    private @Nullable UUID eggBlockPlayerUuid;
    private boolean wasBabyLastTick;

    // 杩借釜鏁屼汉鐨勫墿浣欐椂闀垮€掓暟銆傚ぇ浜?鏃惰〃绀哄浜庢劋鎬掑噯澶囨墤鍑荤姸鎬併€?
    private int retaliationTicksRemaining;
    // 瀹ｆ垬璇煶/钃勫姏鍊掓暟銆傚€掓暟鏈熼棿浼氬湪瀹㈡埛绔悓姝?IS_ATTACKING = true锛屽父鐢ㄤ簬鎾斁钃勫姏鎶栧姩鍔ㄧ敾銆?
    private int retaliationDeclareTicksRemaining;
    // 鎵戝嚮鎶€鑳界殑鍐峰嵈鏃堕棿銆傞槻姝㈣繛缁鐣滈鎵戙€?
    private int pounceCooldownTicks;

    // ====================================================================================
    // [鐢熷懡鍛ㄦ湡涓?AI] 鍒濆鍖栦笌鐘舵€佸畾涔?
    // ====================================================================================

    public TiansuluoBattleFaceEntity(EntityType<? extends TiansuluoBattleFaceEntity> entityType, Level level) {
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
        this.goalSelector.addGoal(1, new SitWhenOrderedToGoal(this)); // [瀹犵墿鐗规€ 鍚粠鍛戒护鍧愪笅
        
        // 椋炴墤 AI 鐩爣锛屽叾浼氬湪 satisfies isReadyToPounce() (杩借釜鐘舵€? 鏃跺皾璇曞彂鍔ㄧ墿鐞嗛鎵?
        this.goalSelector.addGoal(2, new PounceAttackGoal<>(this));
        // 鍏滃簳杩戞垬鏀诲嚮锛屽湪椋炴墤鍐峰嵈鏃惰创韬倝鎼?
        this.goalSelector.addGoal(3, new MeleeAttackGoal(this, 1.2D, false));
        
        this.goalSelector.addGoal(4, new MateForEggBlockGoal<>(this, 1.1D, "message.ohyeah.tiansuluo_battle_face.luanluan_block_carried"));
        this.goalSelector.addGoal(5, new TemptGoal(this, TEMPT_SPEED_MODIFIER, stack -> this.isLikedFood(stack) || this.isFavoriteFood(stack), false));
        // [瀹犵墿鐗规€ 璺熼殢涓讳汉銆傜Щ鍔ㄩ€熷害涔樻暟1.1锛岃窛绂讳富浜?0鏍煎紑濮嬭窡闅忥紝璺濈2鏍煎仠姝㈣窡闅忋€?
        this.goalSelector.addGoal(6, new FollowOwnerGoal(this, 1.1D, 10.0F, 2.0F));
        this.goalSelector.addGoal(7, new FollowParentGoal(this, 1.1D));
        this.goalSelector.addGoal(8, new LayEggBlockGoal<>(this, HATCH_STAGE_TICKS));
        this.goalSelector.addGoal(9, new WaterAvoidingRandomStrollGoal(this, 1.0D));
        this.goalSelector.addGoal(10, new LookAtPlayerGoal(this, Player.class, 6.0F));
        this.goalSelector.addGoal(11, new RandomLookAroundGoal(this));
        
        // [瀹犵墿鐗规€ 鐩爣閿佸畾閫昏緫鍗囩骇銆?
        this.targetSelector.addGoal(1, new OwnerHurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new OwnerHurtTargetGoal(this));
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
    // [鏍稿績浜嬩欢鍏ュ彛] 灏嗗鏉傞€昏緫濮旀墭缁欏唴閮ㄧ殑 Procedure 浠ｇ悊鏂规硶
    // ====================================================================================

    @Override
    public void aiStep() {
        super.aiStep();
        if (this.level().isClientSide) {
            this.handleClientParticlesTick(); // [浠ｇ悊] 澶勭悊瀹㈡埛绔矑瀛愯〃鐜?
            return;
        }

        // 鏈嶅姟绔€昏緫
        this.handleVoiceSystemTick();         // [浠ｇ悊] 鏇存柊璇煶绯荤粺涓庢垚闀垮箍鎾?
        this.handleEggBlockLogicTick();       // [浠ｇ悊] 澶勭悊鎼繍鍗靛潡璺熼殢鐜╁鐨勯€昏緫
        this.handleRetaliationTick();         // [浠ｇ悊] 澶勭悊澶嶄粐鍊掓暟涓庨鎵戝喎鍗寸姸鎬佹満
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        
        // 1. 灏濊瘯瑙﹀彂宸ュ叿浜掑姩 (濡傦細鍓垁绂佽█)
        InteractionResult toolResult = this.handleShearInteraction(player, hand, stack);
        if (toolResult.consumesAction()) return toolResult;

        // 2. 灏濊瘯瑙﹀彂鍠傞/椹湇/鍧愪笅閫昏緫
        InteractionResult foodResult = this.handleTamingAndFeeding(player, hand, stack);
        if (foodResult.consumesAction()) return foodResult;

        // 3. 鍏滃簳澶勭悊 (鍘熺増 TamableAnimal 澶勭悊绌烘墜鍙抽敭鍧愪笅/绔欒捣)
        return super.mobInteract(player, hand);
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        boolean actuallyHurt = super.hurt(source, amount);
        this.handleHurtRetaliationTrigger(actuallyHurt, source); // [浠ｇ悊] 澶勭悊鍙楀嚮鍙嶅嚮鍒ゅ畾
        return actuallyHurt;
    }

    // ====================================================================================
    // [鍐呴儴 Procedure 閫昏緫瀹炵幇鍖篯 鍏蜂綋鐨勪笟鍔￠€昏緫鍏ㄩ儴鍒嗙鍦ㄦ锛屽疄鐜伴珮鍐呰仛鏄撶淮鎶?
    // ====================================================================================

    /**
     * Procedure: 澶勭悊瀹㈡埛绔績褰㈢矑瀛愶紙鐢ㄤ簬灞曠ず鎼捣鍗靛潡鐨勭姸鎬侊級銆?
     */
    private void handleClientParticlesTick() {
        if (this.hasCarriedEggBlock() && !this.isBaby() && this.tickCount % 10 == 0) {
            this.level().addParticle(ParticleTypes.HEART, this.getRandomX(0.6D), this.getRandomY() + 0.5D, this.getRandomZ(0.6D), 0, 0.02D, 0);
        }
    }

    /**
     * Procedure: 椹卞姩鏈嶅姟绔殑鐜闊虫晥娴佹按绾匡紝骞跺湪骞间綋闀垮ぇ鏃跺箍鎾垚闀胯闊炽€?
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
     * Procedure: 楠岃瘉鍗靛潡鍚稿紩鐨勭帺瀹舵槸鍚︿粛鏈夋晥锛堝瓨娲讳笖闈炴梺瑙傛ā寮忥級銆?
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
     * Procedure: 鎴樻枟鐘舵€佹満鏍稿績銆傝礋璐ｆ墸鍑忛攣瀹氫粐鎭ㄥ€掕鏃躲€侀鎵戝喎鍗达紝骞跺湪钃勫姏鏈熼棿闈㈠悜鏁屼汉銆?
     * 娉ㄦ剰锛氭垬鏂楄劯涓嶅湪姝ゅ鐩存帴鐗╃悊寮€鐏紝鑰屾槸灏嗙姸鎬佹毚闇茬粰 TiansuluoBattleFacePounceGoal銆?
     */
    private void handleRetaliationTick() {
        if (this.pounceCooldownTicks > 0) this.pounceCooldownTicks--;

        if (this.retaliationTicksRemaining > 0) {
            this.retaliationTicksRemaining--;
            LivingEntity target = this.getTarget();
            if (target != null && target.isAlive()) {
                this.faceRetaliationTarget(target);
                if (this.retaliationDeclareTicksRemaining > 0) {
                    this.retaliationDeclareTicksRemaining--;
                    this.setAttacking(true); // 钃勫姏闃舵锛屽悓姝ョ粰瀹㈡埛绔挱鏀炬姈鍔ㄥ姩鐢?
                }
                // 鍊掕鏃剁粨鏉熷悗锛孉I Goal (TiansuluoBattleFacePounceGoal) 浼氭帴绠″苟鎵ц瀹為檯鐨勭墿鐞嗚捣璺炽€?
            } else {
                // 鐩爣涓㈠け鎴栨浜★紝涓柇澶嶄粐鐘舵€?
                this.retaliationTicksRemaining = 0;
                this.setAttacking(false);
            }
        } else {
            this.setAttacking(false);
        }
    }

    /**
     * Procedure: 鎷︽埅鐜╁浣跨敤鍓垁浜掑姩鐨勯€昏緫锛堢瑷€鎺夋瘺锛夈€?
     */
    private InteractionResult handleShearInteraction(Player player, InteractionHand hand, ItemStack stack) {
        if (stack.is(Items.SHEARS)) {
            if (this.level().isClientSide) return !this.silencedByShears ? InteractionResult.CONSUME : InteractionResult.PASS;
            if (!this.silencedByShears) {
                this.spawnAtLocation(Items.WHITE_WOOL); 
                this.level().playSound(null, this, SoundEvents.SHEEP_SHEAR, SoundSource.PLAYERS, 1.0F, 1.0F);
                SpeciesSoundFacade.playCue(this, SoundCue.SHEAR_REACT, 1.0F, 1.0F);
                this.silencedByShears = true; // 寮€鍚瑷€鐘舵€?
                stack.hurtAndBreak(1, player, hand == InteractionHand.MAIN_HAND ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND);
                return InteractionResult.SUCCESS;
            }
            return InteractionResult.PASS; // 宸茶鍓繃锛屽拷鐣ヤ氦浜?
        }
        return InteractionResult.PASS;
    }

    /**
     * Procedure: 澶勭悊鍠傞閫昏緫锛堝偓鐔熴€佽В闄ょ瑷€锛変笌椹湇閫昏緫銆?
     */
    private InteractionResult handleTamingAndFeeding(Player player, InteractionHand hand, ItemStack stack) {
        boolean isLiked = this.isLikedFood(stack);
        boolean isFavorite = this.isFavoriteFood(stack);

        if (!isLiked && !isFavorite) {
            return InteractionResult.PASS;
        }

        // 瀹㈡埛绔洿鎺ヨ繑鍥炴垚鍔熶互鍚屾鍔ㄤ綔锛屾墍鏈夊疄闄呮暟鎹绠楀湪鏈嶅姟绔墽琛屻€?
        if (this.level().isClientSide) {
            return InteractionResult.CONSUME;
        }

        // --- 椹湇閫昏緫 (Taming Logic) ---
        if (isFavorite && !this.isTame()) {
            this.usePlayerItem(player, hand, stack); // 娑堣€楃墿鍝?
            if (this.random.nextInt(TAME_CHANCE_DENOMINATOR) == 0 && !net.neoforged.neoforge.event.EventHooks.onAnimalTame(this, player)) {
                this.tame(player); // 璁や富
                this.navigation.stop();
                this.setTarget(null);
                this.level().broadcastEntityEvent(this, (byte) 7); // 鎾斁蹇冨舰绮掑瓙 (鎴愬姛)
            } else {
                this.level().broadcastEntityEvent(this, (byte) 6); // 鎾斁榛戠儫绮掑瓙 (澶辫触)
            }
            return InteractionResult.SUCCESS;
        }

        // --- 鍠傞鍥炶 (Healing Logic) ---
        if (this.isTame() && this.isOwnedBy(player) && isFavorite && this.getHealth() < this.getMaxHealth()) {
            this.usePlayerItem(player, hand, stack); // 娑堣€楃墿鍝?
            this.heal(FOOD_HEAL_AMOUNT);
            SpeciesSoundFacade.playCue(this, SoundCue.EAT_FAVORITE, 1.0F, 1.0F);
            return InteractionResult.SUCCESS;
        }

        // --- 鍌啛涓庤В闄ょ瑷€ (Growth & Shear Remedy) ---
        boolean didSomething = false;
        if (this.isBaby()) {
            int growth = isFavorite ? -this.getAge() : FOOD_GROWTH_STEP; // 鏈€鐖遍鐗╃灛闂存垚骞达紝鍚﹀垯鍔犻€熸垚闀?
            this.ageUp(growth);
            didSomething = true;
        }
        if (this.silencedByShears) {
            this.silencedByShears = false; 
            didSomething = true;
        }
        
        if (didSomething || (isLiked || isFavorite)) { 
            this.usePlayerItem(player, hand, stack); // 娑堣€楃墿鍝?
            SpeciesSoundFacade.playCue(this, isFavorite ? SoundCue.EAT_FAVORITE : SoundCue.EAT, 1.0F, 1.0F);
            return InteractionResult.SUCCESS;
        }

        return InteractionResult.PASS;
    }

    /**
     * Procedure: 绮剧‘鎹曡幏鍙楀嚮鐬棿锛屾墽琛屸€滀笉鍙師璋呪€濆垽瀹氾紝骞堕噸缃弽鍑荤姸鎬佹満鐨勫€掕鏃躲€?
     */
    private void handleHurtRetaliationTrigger(boolean actuallyHurt, DamageSource source) {
        if (!actuallyHurt || this.level().isClientSide) return;
        
        Entity attackerEntity = source.getEntity();
        if (!(attackerEntity instanceof LivingEntity attackerLiving)) return;

        // [闃茶浼锛氬鏋滃畠鏄竴鍙椹湇鐨勫疇鐗╋紝缁濆涓嶈兘瀵瑰畠鐨勪富浜哄弽鍑汇€?
        if (this.isTame() && attackerLiving == this.getOwner()) return;

        // 璁剧疆杩借釜鏁屼汉鐨勬湁鏁堢獥鍙ｆ湡锛?00 tick = 5 绉掞級
        this.retaliationTicksRemaining = RETALIATION_MEMORY_TICKS;
        // 璁惧畾寮€鐏?鎵戝嚮鍓嶇殑瀹ｆ垬璇煶鎾斁鍊掕鏃讹紙榛樿 40 tick = 2 绉掞紝闇€涓庨煶棰戞枃浠堕暱搴﹀榻愶級
        this.retaliationDeclareTicksRemaining = ATTACK_DECLARE_TICKS;
        
        // 鎾斁钃勫姏/瀹ｈ█璇煶銆傝璇煶鎾斁瀹屾瘯鏃讹紝AI Goal (TiansuluoBattleFacePounceGoal) 浼氭伆濂藉厑璁歌繘鍏ヤ笅涓€闃舵鐨勭墿鐞嗛鎵戙€?
        SpeciesSoundFacade.playCue(this, SoundCue.ATTACK_DECLARE, 1.0F, 1.0F);
    }

    // ====================================================================================
    // [椋炴墤鎴樻枟涓撳睘鏀拺鏂规硶] (琚?TiansuluoBattleFacePounceGoal 璋冪敤鐨勬帴鍙?
    // ====================================================================================

    public boolean isReadyToPounce() { return this.retaliationTicksRemaining > 0; }
    public boolean isAttackCooldownReady() { return this.pounceCooldownTicks <= 0; }
    public void startCooldown() { this.pounceCooldownTicks = 40; }
    public void beginCharge() { SpeciesSoundFacade.playCue(this, SoundCue.ATTACK_DECLARE, 1.0F, 1.2F); }
    public void finishSuccessfulRetaliation() {
        this.retaliationTicksRemaining = 0;
        this.startCooldown();
        SpeciesSoundFacade.playCue(this, SoundCue.ATTACK_END, 1.0F, 1.0F);
    }

    public void faceRetaliationTarget(LivingEntity target) {
        this.getLookControl().setLookAt(target, RETALIATION_TURN_SPEED, RETALIATION_TURN_SPEED);
    }

    public boolean isWithinPounceWindow(LivingEntity target) {
        double distSq = this.distanceToSqr(target);
        return distSq >= 4.0D && distSq <= RETALIATION_RANGE * RETALIATION_RANGE;
    }

    public Vec3 getPounceAimPoint(LivingEntity target) {
        return target.position().add(0, target.getBbHeight() * 0.5D, 0);
    }

    // --- Pounceable 参数接口实现 ---
    @Override public double getPounceHorizontalSpeed() { return POUNCE_HORIZ_SPEED; }
    @Override public double getPounceVerticalSpeed() { return POUNCE_VERT_SPEED; }
    @Override public int getPounceMaxFlightTicks() { return POUNCE_MAX_FLIGHT; }
    @Override public double getPounceHitboxPadding() { return POUNCE_PADDING; }

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

    public void setAttacking(boolean attacking) { this.entityData.set(IS_ATTACKING, attacking); }
    public boolean isAttacking() { return this.entityData.get(IS_ATTACKING); }

    // ====================================================================================
    // [鎺ュ彛瀹炵幇] 浜у嵉鍧楁牳蹇冮€昏緫 (EggLayingSpecies)
    // ====================================================================================
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

    // ====================================================================================
    // [鎺ュ彛瀹炵幇] 闊虫晥绯荤粺 (SoundParticipant)
    // ====================================================================================
    @Override @Nullable protected SoundEvent getAmbientSound() { return SpeciesSoundFacade.resolveVanillaCue(this, SoundCue.AMBIENT); }
    @Override @Nullable protected SoundEvent getHurtSound(DamageSource source) { return SpeciesSoundFacade.resolveVanillaCue(this, SoundCue.HURT); }
    @Override @Nullable protected SoundEvent getDeathSound() { return SpeciesSoundFacade.resolveVanillaCue(this, SoundCue.DEATH); }
    @Override protected void playStepSound(BlockPos pos, BlockState state) {}
    
    @Override public String soundSpeciesId() { return SPECIES_ID; }
    @Override public SpeciesSoundCatalog soundCatalog() { return SpeciesSoundCatalog.tiansuluo(); }
    @Override public Set<String> playedSoundCues() { return this.playedCues; }
    @Override public boolean isSoundSilenced(SoundCue cue) { return this.silencedByShears && !this.allowsCueWhileSilenced(cue); }
    @Override public boolean isVoiceEnabled() { return true; }
    @Override public boolean isCueDisabled(SoundCue cue) { return false; }
    @Override public boolean allowsCueWhileSilenced(SoundCue cue) { return Set.of("eat", "eat_favorite", "shear_react").contains(cue.key()); }
    @Override public boolean enablePipeline() { return true; }
    @Override public boolean enableVanilla() { return false; }
    @Override public Set<String> vanillaCues() { return Collections.emptySet(); }
    @Override public boolean enableLimiter() { return true; }
    @Override public int ambientIntervalTicks() { return AMBIENT_INTERVAL; }
    @Override public int ambientRandomnessTicks() { return 0; }
    @Override public int rareAmbientChance() { return 5; }
    @Override public int intervalTicks(SoundCue cue) { return 20; }
    @Override public Map<String, Integer> voiceOverrides() { return VOICE_OVERRIDES; }

    // ====================================================================================
    // [鏁版嵁搴忓垪鍖朷
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
        nbt.putInt("PounceCooldown", this.pounceCooldownTicks);
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
        this.pounceCooldownTicks = nbt.getInt("PounceCooldown");
        if (nbt.hasUUID(TAG_EGG_BLOCK_PLAYER_UUID)) this.eggBlockPlayerUuid = nbt.getUUID(TAG_EGG_BLOCK_PLAYER_UUID);
    }

    @Override
    public @Nullable AgeableMob getBreedOffspring(ServerLevel level, AgeableMob other) {
        // [瀹犵墿鐗规€ 绻佽偛鍚庝唬锛岀敱 Minecraft 鍘熺増鎺у埗銆?
        // 鍥犱负澶╃礌缃楁槸鐢ㄥ嵉鍧楃箒娈栫殑锛屾墍浠ヨ繑鍥?null 闃绘鍘熺増骞煎唇绻佹畺鏈哄埗銆?
        return null;
    }
}