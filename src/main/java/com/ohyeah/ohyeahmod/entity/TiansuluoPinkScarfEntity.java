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
 * 濠㈠灈鏅濈粈宀勬晸?(缂侇喖顦ú鍧楁晸? 閻庡湱鍋樼紞瀣晸?
 * <p>
 * 鐎瑰憡褰冪花鏌ユ晸?"闁告劕鎳橀崕?Procedure 濞寸媴绲块幃? 闁哄鍩栭悗顖炴煂瀹ュ棛鈧垶鏁嶇仦鐣屾闁圭鍋撻柡鍫濐槸缁ㄨ姤寰勮濞堟垹鎮扮仦鑹扮闂侇偅妲掔欢顐﹀箯閸℃鐎诲☉鎾规鐎氼厾绮╃€ｎ剚鐣?handleXxx 闁哄倽顫夌涵鍫曟晸?
 * 缂備綀鍕棡闁?TamableAnimal闁挎稑鐭侀獮蹇擃嚗濡ゅ嫷鍚囧☉鎾寸湽閳ь兛娴囩粣锟犳⒕韫囧鍋撴担鍛婄稄濞戞挸顑囬悺鎴犫偓鍦Х婢у潡鎮х憴鍕ㄥ亾瑜濋幏?
 */
public class TiansuluoPinkScarfEntity extends TamableAnimal implements EggLayingSpecies {

    // ====================================================================================
    // [闁轰焦婢橀埀顒€鍚嬬敮鍫曞礆鐠哄搫閰盷 缂備胶鍠嶇粩瀵哥不閿涘嫭鍊為柟纰樺亾闁哄牆顦抽、鎴炵▔鏉炴壆鐟㈤柟瀛樕戦弸鐔煎矗閸屾稒娈堕柨娑樺閹便劑寮ㄧ憴鍕靛妰闁告牕鎼悡娆撳础閸愭彃璁查悹瀣暞閺嗭絿鈧湱鍋樼紞瀣嵁鐎圭姰鈧偓闁?
    // ====================================================================================

    // --- 1. 闁哄秶顭堢缓鍓ф暜閹间礁娅ゅ☉鎾虫捣婢у潡鎮堕崱妤€妫橀柨?---
    public static final String SPECIES_ID = "tiansuluo_pink_scarf";
    // 闁瑰瓨鍔曢崟鐐媴閹鹃娼鹃柟鍓у仧椤斿牏鈧妫勭€硅櫕绋夋惔銊у蒋閹艰揪璁ｉ幏?
    public static final float WIDTH = 0.7F;
    public static final float HEIGHT = 0.7F;
    // 妤犵偟鍘ч崟鐐媴閹捐埖绁查弶鍫濆暕缁剟骞嬮幇顒€瀣€濞达絾鎸惧▓鎴犵磽閳哄倹鏉规慨锝嗘煣缁躲儵鏁?
    public static final float BABY_SCALE_FACTOR = 0.5F;

    // --- 2. 闁糕晞娅ｉ、鍛沪閻戝洦瀚?(Base Attributes) ---
    // 闁哄牃鍋撳鍫嗗懏鏅搁柛娑滄閳ь剛銆嬮幏?0.0D 闁烩晝顭堢紞瀣鎼达絽璐熼悗鐟板濞?10 濡増顨呯缓楣冩晸?
    public static final double BASE_MAX_HEALTH = 20.0D;
    // 缂佸顕ф慨鈺呮焻閻斿嘲顔婇柨?.25 閻忕偟鍋樼花顒佺▔椤撶姷鎼奸柛瀣箲閸欏啴鏁?
    public static final double BASE_MOVEMENT_SPEED = 0.25D;
    // 缂佷究鍨洪弲顐ｆ交閸婄喖鍤嬮悹鐑樼箘椤洭鏁?
    public static final double BASE_FOLLOW_RANGE = 16.0D;
    // 闁糕晞娅ｉ、鍛交閹寸偛鐏涢柡鈧拠鎻掓瘖闁?(闁烩晩鍠栨晶鐘崇瑹濠靛娴嗙€殿喚鎳撶粻閿嬪緞瀹ュ嫮鐭忛柨娑樼焷缁诲酣骞嬪Ο鍦▕濞戞捇缂氳棢闁?闁?
    public static final double BASE_ATTACK_DAMAGE = 3.0D;
    // 閻炴凹鍋呭〒鍫曟偉闁稁妫ㄩ柣妞绘櫅閹稑顕ｉ弴鐔割槯闁汇劌瀚簺闂侇偆鍠嶇粻濠氬极鐢喗瀚?.15 闁稿﹤绉朵簺闂侇偆鍣﹂幏?
    public static final double TEMPT_SPEED_MODIFIER = 1.15D;

    // --- 3. 闁瑰瓨蓱閺嬬喐绋夋惔銈庡殧闂傚﹤纾柈瀵哥磼閻旇绮撻柛鏂诲姂閳ь剚妲掔欢?(闁哄倽顫夐、?闁挎稒纰嶅﹢鍥礉閿涘嫷浼傜紒顔煎⒔閳ユ鎷犻懡銈庢健闁硅矇鍐ㄧ厬) ---
    // 濞寸姴娲︽禒铏规媼閺夎法绠撻柡鍐ㄧ埣閺嗛亶鏁嶅顒€缍€闁告垼顕ч幃妤冩媼妫颁胶绉跺ù鐘叉处娴犳椽鐛捄铏规閻犲洦娲栧浠嬪礄閼姐倖鐣遍柟顒傜帛濡炲倿姊绘潏鐐?20 ticks = 6缂佸甯囬埀顒€鍊界粔鎾礄閻戞﹩鍔冮柡鍐ㄧ埣濡潡寮甸鍕殮闁瑰瓨鍔曞浠嬪礄鐠囨彃鐏熼柡鈧幆褏纾鹃柨?
    public static final int RETALIATION_MEMORY_TICKS = 120;
    // 閺夌儐鍓濋棅鈺呮焻閻斿嘲顔婇柨娑欎亢閹碱偊宕濆☉娆愬焸闂傚倸鐡ㄩ鏉戭潰閼姐倖鍒涘ù锝呯箲閺侀箖宕欓弰蹇婂亾閸涱垱鐣遍弶鐑嗗墲闂娾晠鎮橀崹顐ｆ珱閹艰揪璁ｉ幏?0.0F 閻忕偟鍋樼花顒佹綇閸愩劍褰ラ柨?
    public static final float RETALIATION_TURN_SPEED = 20.0F;

    // 闁靛棙鍔曢崣褔鏌ㄩ琛″亾閹存繍鍚呴柟瀛橆焾椤曘垽妫呴搹顐ｎ槯闂傗偓閸栵紕绀勯柦鍐ㄥ婵繘宕滃鍡樺暁闁挎稑顧€缁?
    // 40 ticks = 2.0缂佸甯囬埀顒€鍊歌ぐ鍫ュ礄缂佹ɑ顦ч悷娆欑畱瑜板倻鎷犻銏㈠従妤犵偠娉涚槐鎴炴叏鐎ｎ亖鍋撻幒鎾存闁挎稑鑻埀顒佸笚閺嗙喕銇愰幒妤佺ォ闁活剦鍓熷Λ鍨嚕閳ь剟鎮橀顒傜濞ｅ洦绻嗛惁澶愬灳濠婂棭鍤旈梻濠呮珪閹歌京鈧懓鐬奸悵娑㈠礆鐠囨彃鍐€闁告垹鍎甸埀顒佺箘濞堟垹鈧懓鐬肩欢銊╁触鐏炵虎鍔勯柨?
    public static final int ATTACK_DECLARE_TICKS = 40;

    // 鐎殿喚鎳撶粻椋庝焊閸曨偄姣婇柛娆忓€归弳鐔兼晸?
    // 濞戞挴鍋撴繛鍡忊偓鍐插唨闁告垼顕цぐ鍌滀焊閸曨厽鐣辩€殿喚鎳撻惃鐘绘偋閳哄倹娈堕柨?闁轰緤绲介懘?闁?
    public static final int BURST_SHOTS = 3;
    // 闁告娲栬ぐ鍌氼嚕閻熸壆娈搁柣妞绘櫇濞堟垿宕洪搹璇℃敤濞寸鍊曢濠囨晸?
    public static final double PROJECTILE_DAMAGE = 5.0D;
    // 鐎殿喚鎳撻惃鐘绘偋閳哄拋妫ч悶娑樼焸閳ь剛鍠庣€规娊濡撮崒姘€ら梺顐㈠€搁埀顒傘€嬬槐?.5F~4.0F闁?
    public static final float PROJECTILE_SPEED = 3.0F;
    // 鐎殿喚鎳撻惃鐘绘偋閳哄倹娈犻悘蹇撳缁ㄥ潡宕欓崱妤€顔婇柛瀣箳浜涢柕鍡楀€搁埀顒冨缁夌儤寰勮缁夋椽寮敐蹇斿?.2F 濞戞捇缂氭禍銈咁嚗椤旇姤娈犻悘蹇撳閹?
    public static final float PROJECTILE_DIVERGENCE = 1.2F;

    // --- 4. 闁汇垻鍠庨幊锟犲川閵婏附鍩傚☉鎾抽叄閳瑰寮靛鍛緭闁?(Taming & LifeCycle) ---
    // 妤犵偟鍘ч崟楣冨嫉閻旂硶鍋撶紒妯活槯闂傗偓閸栵紕绀凾ick闁挎稑顧€閹?4000 ticks = 20 闁告帒妫濋幐鎾绘晬閸︻摨necraft 闁汇劌瀚粩瀛樺緞閳哄绀嗛柨?
    public static final int BABY_GROWTH_AGE = 24000;
    // 闁哥姴鍊块ˉ銈夊疾椤曗偓閳ь剚鑹鹃弸鈺呮偉闁稁妫ㄩ柣妞绘櫆濡炲倿鏁嶅畝鍐劜閺夆晛娲ㄥ▓鎴﹀箣閹扮増姣愰柡鍐ㄧ埣閺嗛亶鏁?000 ticks = 5 闁告帒妫濋幐鎾绘晸?
    public static final int FOOD_GROWTH_STEP = 6000;
    // 濞存籂鍐ㄧゲ闁秆勵殔椤掝噣宕犻弽銊ヮ暡闂傚洠鍋撻柣銊ュ濞撳墎浜?Tick 闁轰浇鍩囬埀?
    public static final int HATCH_STAGE_TICKS = 200;
    // 濞存籂鍐ㄧゲ闁秆勵殕閻?Tick 闂傚懎绻戝┃鈧悗娑㈡涧鐎垫煡鎯冮崟顒夋搐闁绘粌娲ら埀顒佸笚閺嗙喖鏁嶇仦瑙ｅ亾閼规壆楔閻忓繐绻愰顕€宕犻弽顒傂ㄩ煫鍥跺亖閳?00 閻炴稏鍔庨妵姘剁嵁閸愬弶缍?500 tick 濠⒀呭仜婵偞绋夐埀顒€鈻庨垾鎰佸姉闁告牗鐗炵换妯绘償閿旇　鍋?
    public static final int HATCH_CHANCE_INV = 500;
    // 濡炶鍨卞﹢鍥箣閹邦剙顫犻柣婊冩川濞堟垿宕氶崱妯兼Г (1/3 闁汇劌瀚々褔鎮抽崶銊ョ亣闁?闁?
    public static final int TAME_CHANCE_DENOMINATOR = 3;
    // 濡炶鍨卞﹢鍥触鎼搭垳绀夐柛鐘插€块ˉ銈夊嫉閳ь剟鎮ラ柆宥庢（闁绘せ鏅滄晶宥夊炊閻愬樊妲婚柣銊ュ閺佹捇宕ㄩ挊澶嗗亾绾攱瀚?.0F = 2 濡増顨呯缓楣冩晸?
    public static final float FOOD_HEAL_AMOUNT = 4.0F;

    // --- 5. 闁哥姵绮岄妶浠嬫煀瀹ュ洨鏋傚☉鎾虫捣閺佹捇骞嬮幇鐗堫€欓柨?---
    // 闁哄拋鍣ｉ埀顒佽壘閺嬧晠鎮ラ柆宥庢（闁绘せ鏅槐浼存偨閵娿倗鑹鹃柛姘鳖焾缁扁晠濡存担绋夸壕闁绘梻鍠嗛埀顑挎濮橈箓鏌婂蹇斿?
    public static final List<String> FOOD_LIKED = List.of("minecraft:wheat", "minecraft:carrot", "minecraft:beetroot", "minecraft:potato");
    // 闁哄绀佺€规娊宕板鍛厓濡炲鍠撴晶鍧楁晬濮樿鲸鏆忓ù婊冮叄閳瑰寮靛鍐ｅ亾娴ｅ憡绀€閻炴稈鍋撻柕鍡曡兌閻忔盯姊婚弶鎴濅壕闁绘梻鍠愰崹姘剁嵁鏉堢偓瀚?
    public static final List<String> FOOD_FAVORITE = List.of("minecraft:cake", "ohyeah:chips");
    // 闁汇垻鍠愰崹姘跺级閸愵喖娅㈠☉鎾虫捣閸忋垻鍖栨繝姘€欓柛鎺曨啇閹?
    public static final int SPAWN_WEIGHT = 10;
    public static final int SPAWN_MIN_GROUP = 1;
    public static final int SPAWN_MAX_GROUP = 3;
    public static final List<String> SPAWN_BIOMES = List.of("minecraft:plains", "minecraft:meadow");

    // --- 6. 閻犲浂鍙冮悡鍓佸寲閼姐倗鍩犻悽顖涙倐閸?---
    // 闂傚倽灏禍?Ambient)闂傚﹨娅曢弲銉╂儍閸曨啩鏇㈠矗閹达附锛熼柨?Tick)闁?000 ticks = 5 闁告帒妫濋幐鎾朵焊濠靛﹦妲搁悷娆欑畱瑜板倹绋夐埀顒€鈻庨埥鍛?
    public static final int AMBIENT_INTERVAL = 6000;
    // 闁绘顫夐悾鈺呮閾忣偅娅忛柣銊ュ琚濋柛娆愬灥閸犲酣宕＄壕瀣垫船闁烩晜鐗撻埀顒€鍊垮Σ璇差潰閵忥絿绠剧紓渚囧幖瑜板牓鏁?闂傚倽灏禍鐗堫殺閼测晜娈旈柨?
    public static final Map<String, Integer> VOICE_OVERRIDES = Map.of("ambient", 60, "hurt", 20);

    // ====================================================================================
    // [闁告劕鎳橀崕鎾偐閼哥鍋撴担鍝ユ憻婵炲牆鍚€缁楀矂寮悧鍫濈ウ闁告艾鏈鐎?
    // ====================================================================================

    // --- 闁告艾鏈鐐哄极閻楀牆绁﹂悗瑙勭煯缁?(Client-Server Synchronization) ---
    private static final EntityDataAccessor<Boolean> IS_ATTACKING = SynchedEntityData.defineId(TiansuluoPinkScarfEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> HAS_CARRIED_EGG_BLOCK = SynchedEntityData.defineId(TiansuluoPinkScarfEntity.class, EntityDataSerializers.BOOLEAN);

    // --- NBT 閻庢稒锚閸嬪秹鏌ㄩ鍡樺?---
    private static final String TAG_SILENCED_BY_SHEARS = "SilencedByShears";
    private static final String TAG_HAS_CARRIED_EGG_BLOCK = "HasLuanluanBlock";
    private static final String TAG_EGG_BLOCK_TARGET_X = "LuanluanBlockTargetX";
    private static final String TAG_EGG_BLOCK_TARGET_Y = "LuanluanBlockTargetY";
    private static final String TAG_EGG_BLOCK_TARGET_Z = "LuanluanBlockTargetZ";
    private static final String TAG_EGG_BLOCK_PLACING_COUNTER = "LuanluanBlockPlacingCounter";
    private static final String TAG_EGG_BLOCK_PLAYER_UUID = "LuanluanBlockPlayerUuid";

    // --- 閻庡湱鍋樼紞瀣礃閸涙潙鍔ラ柣妯款啇閹?(Entity Local State) ---
    private final Set<String> playedCues = new HashSet<>();
    private boolean silencedByShears;
    private @Nullable BlockPos eggBlockTargetPos;
    private int eggBlockPlacingCounter;
    private @Nullable UUID eggBlockPlayerUuid;
    private boolean wasBabyLastTick;

    // 閺夆晛鈧喖鍤嬮柡浣稿濮瑰鎯冮崟顐⑩挅濞达絾鐟﹀鍌炴⒐閸喒鍋撻幒鎾存闁靛棗鍊搁妵鍥晸?闁哄啯鍎奸妴鍐矆閻戞﹩鍔€濠㈣泛瀚花顒勫箛閵堝繑瀚?缂佷究鍨洪弲顐︽偐閼哥鍋撴笟濠冨?
    private int retaliationTicksRemaining;
    // 閻庣櫢绲鹃崹顒傛嫚椤撱垻鍙?闁藉啫瀚慨蹇涘磹閹烘挻娈堕柕鍡楀€歌ぐ鍫ュ礄缂佹ɑ顦ч悶姘煎亯缁佹挳鏁?ATTACK_DECLARE_TICKS闁挎稑鑻紞瀣礂鐠鸿　鍋撻幒鎾存闁?0 闁汇劌瀚悘娑㈡⒒鏉堝墽绀夐悷娆欑畱瑜板倸顕ｉ埀顒勬倶椤愶腹鍋撻弰蹇曞竼闁?
    private int retaliationDeclareTicksRemaining;

    // ====================================================================================
    // [闁汇垻鍠庨幊锟犲川閵婏附鍩傞柨?AI] 闁告帗绻傞～鎰板礌閺嶏妇鐟㈤柣妯垮煐閳ь兛绀侀悾楣冩晸?
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
        this.goalSelector.addGoal(1, new SitWhenOrderedToGoal(this)); // [閻庡湱濮锋晶鍧楁偋鐟欏嫧鍋撻—?闁告凹鍏涚划鐘诲川閹存帗濮㈤柛褎鍔掔粭?
        this.goalSelector.addGoal(2, new MateForEggBlockGoal<>(this, 1.1D, "message.ohyeah.tiansuluo_pink_scarf.luanluan_block_carried", com.ohyeah.ohyeahmod.registry.ModSoundEvents.TIANSULUO_PS_BREED_SUCCESS.get()));
        this.goalSelector.addGoal(3, new TemptGoal(this, TEMPT_SPEED_MODIFIER, stack -> this.isLikedFood(stack) || this.isFavoriteFood(stack), false));
        // [閻庡湱濮锋晶鍧楁偋鐟欏嫧鍋撻—?閻犺櫣鍠栧▓銏＄▔鐠佽櫕鐪介柕鍡楀€讳簺闁告柣鍔戦埀顒傚枎鐎硅櫕绋婂Ο缁樻1.1闁挎稑鐭佺粣娑氱矉鐠佸啿鐦滈柨?0闁哄秶鍘х槐鎴炴叏鐎ｎ厾顎€闂傚懎楠忕槐婵堟崉濠靛牜鐎?闁哄秶鍘ф禒鐘差潰閵忥絿顎€闂傚懎楠忛幏?
        this.goalSelector.addGoal(4, new FollowOwnerGoal(this, 1.1D, 10.0F, 2.0F));
        this.goalSelector.addGoal(5, new FollowParentGoal(this, 1.1D));
        this.goalSelector.addGoal(6, new LayEggBlockGoal<>(this, HATCH_STAGE_TICKS));
        this.goalSelector.addGoal(7, new WaterAvoidingRandomStrollGoal(this, 1.0D));
        this.goalSelector.addGoal(8, new LookAtPlayerGoal(this, Player.class, 6.0F));
        this.goalSelector.addGoal(9, new RandomLookAroundGoal(this));

        // [閻庡湱濮锋晶鍧楁偋鐟欏嫧鍋撻—?闁烩晩鍠楅悥锝夋煥娴ｅ摜鏆伴梺顐ｆ缁额偊宕￠崶鈺呯崜闁?
        // 濠碘€冲€归悘澶嬬▔鐠佽櫕鐪介悶姘煎亝閺侀箖宕欐导娆戠闁告帗鐟╅弨锝団偓瑙勭閺侀箖宕欓弰蹇婂亾閸滃啰骞㈠┑鈥冲€归悘澶嬬▔鐠佽櫕鐪介柡鈧拠鎻掓瘖濞存粌妫欓悡鍥儎椤旂晫鍨奸柨娑樿嫰閸垶鏌ㄦ担鍝ユ毎閻犲洢鍎冲ú浼村冀閸ラ攱瀚?
        this.targetSelector.addGoal(1, new OwnerHurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new OwnerHurtTargetGoal(this));
        // 闁稿繑绮岀花鎶芥焻閺勫繒甯嗛柨娑欎亢椤箓骞嶉幘鑼晩閺夆晜蓱濡插憡瀵煎宕囩闁归潧顑戦幏?
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
    // [闁哄秶顭堢缓鐐鐎ｂ晜顐介柛蹇嬪劚瑜版矑 閻忓繐妫楅ˇ鏌ュ级閸岀偐鍋撻弰蹇曞竼濠殿喗姊规晶顓犵磼濞嗗繐鏁堕梺顔哄妿濞?Procedure 濞寸媴绲块幃濠囧棘鐟欏嫮銆?
    // ====================================================================================

    @Override
    public void aiStep() {
        super.aiStep();
        if (this.level().isClientSide) {
            this.handleClientParticlesTick(); // [濞寸媴绲块幃濂?濠㈣泛瀚幃濠勨偓骞垮灪閸╂稓绮╅婊呯厛閻庢稒鍔橀妴鍐晸?
            return;
        }

        // 闁哄牆绉存慨鐔虹博椤栫偐鍋撻弰蹇曞竼
        this.handleVoiceSystemTick();         // [濞寸媴绲块幃濂?闁哄洤鐡ㄩ弻濠勬嫚椤撱垻鍙剧紒顖濆吹缁儤绋夋惔銏犵亣闂傗偓閸喚鐣柨?
        this.handleEggBlockLogicTick();       // [濞寸媴绲块幃濂?濠㈣泛瀚幃濠囧箹椤掑啰绠ラ柛妤呮涧濞硷紕鎹勯悢鍏碱吂闁绘壕鏅涢宥夋儍閸曨垪鍋撻弰蹇曞竼
        this.handleRetaliationTick();         // [濞寸媴绲块幃濂?濠㈣泛瀚幃濠冨緞瀹ュ嫮鐭忛柛濠冨笚閺嗙喐绋夋惔锛勭；闁诲浚鍋嗘慨鎼佸箑娴ｈ绨?
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        // 1. 閻忓繑绻嗛惁顖滄喆閿曗偓瑜板倸顔忛妷銉ュ緮濞存粍甯掓慨?(濠碘€冲亰缁变即宕滈鍕€紒鍌欐祰閳?
        InteractionResult toolResult = this.handleShearInteraction(player, hand, stack);
        if (toolResult.consumesAction()) return toolResult;

        // 2. 閻忓繑绻嗛惁顖滄喆閿曗偓瑜板倿宕伴崒鐑嗘（/濡炶鍨卞﹢?闁秆勫姃缁楀懘鏌呴弰蹇曞竼
        InteractionResult foodResult = this.handleTamingAndFeeding(player, hand, stack);
        if (foodResult.consumesAction()) return foodResult;

        // 3. 闁稿繑绮岀花铏緞閸曨厽鍊?(闁告鍠撴晶?TamableAnimal 濠㈣泛瀚幃濠勭矚閻戞ê顤侀柛娆愬▕閺侇參宕搁幇顏嗙憮/缂佹梹鐟ㄩ幑?
        return super.mobInteract(player, hand);
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        boolean actuallyHurt = super.hurt(source, amount);
        this.handleHurtRetaliationTrigger(actuallyHurt, source); // [濞寸媴绲块幃濂?濠㈣泛瀚幃濠囧矗濡も偓閸ゎ噣宕ｅ鍛瘖闁告帇鍊曢悾?
        return actuallyHurt;
    }

    // ====================================================================================
    // [闁告劕鎳橀崕?Procedure 闂侇偅妲掔欢顐も偓鍦仧楠炲洭宕犵弧?闁稿繗娓圭紞瀣儍閸曨亞鐟归柛鏃撶節閳ь剚妲掔欢顐﹀礂閵娾晛鍔ラ柛鎺戞椤洭宕烽妸锔诲妰闁挎稑鑻悿鍕偝娴煎褰柛鎰嚀娴犳盯寮伴幘鑸垫▕闁?
    // ====================================================================================

    /**
     * Procedure: 濠㈣泛瀚幃濠勨偓骞垮灪閸╂稓绮╅姘卞鐟滆埇鍨婚惌鎴犫偓娑欏姧缁辨瑩鎮介妸銈囪壘閻忕偞娲滈妵姘跺箹椤掑啯宕抽柛妤呮涧濞硷繝鎯冮崟顓炐﹂柟顑跨筏缁辨岸鏁?
     */
    private void handleClientParticlesTick() {
        if (this.hasCarriedEggBlock() && !this.isBaby() && this.tickCount % 10 == 0) {
            this.level().addParticle(ParticleTypes.HEART, this.getRandomX(0.6D), this.getRandomY() + 0.5D, this.getRandomZ(0.6D), 0, 0.02D, 0);
        }
    }

    /**
     * Procedure: 濡炵懓宕慨鈺呭嫉瀹ュ懎顫ょ紒鏃戝灣濞堟垿鎮抽姘兼殧闂傚﹨娅曢弲銉ッ规担瑙勫瘻缂佹儳灏呯槐婵嬬嵁鐠虹儤韬鐐烘？缂嶅姊归崹顔轰海闁哄啳娉涚粻宥夊箻椤撶喎鐏囬梻鈧懗顖ｅ殧闂傚﹦鍋ㄩ埀?
     */
    private void handleVoiceSystemTick() {
        boolean babyNow = this.isBaby();
        if (this.wasBabyLastTick && !babyNow) {
            this.playSound(com.ohyeah.ohyeahmod.registry.ModSoundEvents.TIANSULUO_PS_GROW_UP.get(), 1.0F, 1.0F);
        }
        this.wasBabyLastTick = babyNow;
    }

    /**
     * Procedure: 濡ょ姴鐭侀惁澶愬础闂堟稒鍋ラ柛姘鳖焾缁扁晠鎯冮崟顓炶礋閻庣鍩栧Σ鎼佸触閿旇法鐭濋柡鍫濐槹閺呫儵鏁嶉崼婵堟憼婵炶尪顔婄粭鏍閻愬瓨鈷戦悷娆忓€硅啯鐎殿喖楠忕槐姘舵晸?
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
     * Procedure: 闁瑰瓨蓱閺嬬喖鎮╅懜纰樺亾娴ｈ绨氶柡宥囶焾缁洪箖濡撮崒婵堫槹閻犳劧绲炬晶鎼佸礄韫囨稒鏁氶悗瑙勭煯缁劙骞侀妸銉㈠亾閹烘洦鍚€闁哄啳顔愮槐婵嬪箥瑜戦、鎴炴姜椤掆偓閹粓鏁嶇仦鍊熷珯闁革负鍔岄娲箣濡　鍋撻幒鏇ㄥ悁闁哄啫澧庣划銊╁级閻斿憡顦х€殿喒鍋撻柣蹇ｅ亾閹?
     */
    private void handleRetaliationTick() {
        if (this.retaliationTicksRemaining > 0) {
            this.retaliationTicksRemaining--;
            LivingEntity target = this.getTarget();
            if (target != null && target.isAlive()) {
                this.getLookControl().setLookAt(target, RETALIATION_TURN_SPEED, RETALIATION_TURN_SPEED);
                if (this.retaliationDeclareTicksRemaining > 0) {
                    this.retaliationDeclareTicksRemaining--;
                    this.setAttacking(true); // 闁告艾鏈鐐碘偓骞垮灪閸╂稓绮╅銈囩獥濠㈣泛瀚花顒勬嫅閸曨偄顫旈柣妯款啇閹?
                    if (this.retaliationDeclareTicksRemaining == 0) {
                        this.performRetaliationAttack(target); // 闁哄啫鐖煎Λ鍧楀礆鐢喚绀夐柛娆愬灥閻ㄧ姴顕ｉ悷鎵啂
                    }
                }
            } else {
                // 闁烩晩鍠楅悥锝嗙▔閵忕姰浜奸柟瀛樼墬椤掑瓨绂嶉埥鍛濞戞搩鍘介弻鍥ㄥ緞瀹ュ嫮鐭忛柣妯款啇閹?
                this.retaliationTicksRemaining = 0;
                this.setAttacking(false);
            }
        } else {
            this.setAttacking(false);
        }
    }

    /**
     * Procedure: 闁瑰嚖闄勯崺鍛存偝閳轰緡鍟€濞达綀娉曢弫銈夊礈椤忓嫬鐎ù婊勫笒婵晠鎯冮崟顖楀亾閺勫繒甯嗛柨娑樼墢椤╋妇鎳涢埀顒勫箳婢跺妯呴柨娑橆檧閹?
     */
    private InteractionResult handleShearInteraction(Player player, InteractionHand hand, ItemStack stack) {
        if (stack.is(Items.SHEARS)) {
            if (this.level().isClientSide)
                return !this.silencedByShears ? InteractionResult.CONSUME : InteractionResult.PASS;
            if (!this.silencedByShears) {
                this.spawnAtLocation(Items.WHITE_WOOL);
                this.level().playSound(null, this, SoundEvents.SHEEP_SHEAR, SoundSource.PLAYERS, 1.0F, 1.0F);
                this.playSound(com.ohyeah.ohyeahmod.registry.ModSoundEvents.TIANSULUO_PS_SHEAR_REACT.get(), 1.0F, 1.0F);
                this.silencedByShears = true; // 鐎殿喒鍋撻柛姘煎灣椤╋妇鎳涢埀顒勬偐鐠佽瀚?
                stack.hurtAndBreak(1, player, hand == InteractionHand.MAIN_HAND ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND);
                return InteractionResult.SUCCESS;
            }
            return InteractionResult.PASS; // 鐎规瓕灏～锕傚礈椤忓洨绠栭柨娑樿嫰閹风兘鎮鹃妷銈嗗攭闁?
        }
        return InteractionResult.PASS;
    }

    /**
     * Procedure: 濠㈣泛瀚幃濠囧窗閸岀儐妫ㄩ梺顐ｆ缁额偊鏁嶉崼婵嗕壕闁绘梻鍠嗛埀顑挎祰琚欓梻鍕╁€楅々锔炬嚊閳ь剟鏁嶆径澶岀憿濡炶鍨卞﹢鍥焻閺勫繒甯嗛柨?
     */
    private InteractionResult handleTamingAndFeeding(Player player, InteractionHand hand, ItemStack stack) {
        boolean isLiked = this.isLikedFood(stack);
        boolean isFavorite = this.isFavoriteFood(stack);

        if (!isLiked && !isFavorite) {
            return InteractionResult.PASS;
        }

        // 閻庡箍鍨洪崺娑氱博椤栨粍绾柟鎭掑劥缁绘垿宕堕悙鏉戠亣闁告梻鍠嶆禍鎺楀触鐏炵虎鍔勯柛鏂诲妺缂嶆棃鏁嶇仦鎯ь暡闁哄牆顦悿鍕⒔閸涱喗娈堕柟璇″枦椤撳摜绮诲Δ鈧﹢顏堝嫉瀹ュ懎顫ょ紒鏃戝灡婢х晫鎮板畝瀣?
        if (this.level().isClientSide) {
            return InteractionResult.CONSUME;
        }

        // --- 濡炶鍨卞﹢鍥焻閺勫繒甯?(Taming Logic) ---
        // 闁告瑯浜濆﹢?"闁哄牃鍋撻柨? 濡炲鍠撴晶鍧楀箥瀹ュ懎寰旈柡鍫濐樀閳瑰寮靛鍕憿闁搞儳鍋犻、鍛存嚄閽樺顫旈柨?
        if (isFavorite && !this.isTame()) {
            this.usePlayerItem(player, hand, stack); // 婵炴垵鐗愰埀顒侇殘婢у潡鏁?
            if (this.random.nextInt(TAME_CHANCE_DENOMINATOR) == 0 && !net.neoforged.neoforge.event.EventHooks.onAnimalTame(this, player)) {
                this.tame(player); // 閻犱降鍊撶€?
                this.navigation.stop();
                this.setTarget(null);
                this.level().broadcastEntityEvent(this, (byte) 7); // 闁圭虎鍘介弬浣界疀閸愩劏鍩岀紒顔藉笒閻?(闁瑰瓨鍔曟慨?
            } else {
                this.level().broadcastEntityEvent(this, (byte) 6); // 闁圭虎鍘介弬浣诡渶閹寸姴鍔旂紒顔藉笒閻?(濠㈡儼绮剧憴?
            }
            return InteractionResult.SUCCESS;
        }

        // --- 闁哥姴鍊块ˉ銈夊炊閻愰鏀?(Healing Logic) ---
        // 閻炴凹鍋婇埞瀣嫉瀹ュ懏鍊甸柨娑樿嫰閺夌儤顦伴悢鍛婁粯闁绘牠浜堕ˉ銈夋偋閳轰礁璁插ù鐘劚濞叉牜鎮伴埀顒勬晸?
        if (this.isTame() && this.isOwnedBy(player) && isFavorite && this.getHealth() < this.getMaxHealth()) {
            this.usePlayerItem(player, hand, stack); // 婵炴垵鐗愰埀顒侇殘婢у潡鏁?
            this.heal(FOOD_HEAL_AMOUNT);
            this.playSound(com.ohyeah.ohyeahmod.registry.ModSoundEvents.TIANSULUO_PS_EAT_FAVORITE.get(), 1.0F, 1.0F);
            return InteractionResult.SUCCESS;
        }

        // --- 闁稿矈鍓涢崯娑欑▔鎼淬伆鎺楁⒔閵堝浂娲ｉ悷灏佸亾 (Growth & Shear Remedy) ---
        // 闂侇偄鍊婚弫銈嗙鎼淬垹顣查柡鍫濐槸閺嬧晠鎮ラ柆宥庢（闁绘せ鏂傞埀顒€鍊瑰﹢顓㈠箣閹邦剙瀣€闁告瑯鍨伴崑鎾绘偄閻曞倻绀夐悶姘煎亜婢光偓閺夆晛娲﹂惁娲矗椤栨稐鍒掑璺虹Т瑜板倿鎮介悢璇插幋闁告梹鍐婚幏?
        boolean didSomething = false;
        if (this.isBaby()) {
            int growth = isFavorite ? -this.getAge() : FOOD_GROWTH_STEP; // 闁哄牃鍋撻柣鏍憾椤ャ倝鎮ч埡鍐粶闂傚倸鐡ㄩ崹姘剁嵁鏉堝墽绀夐柛姘剧畱閸垶宕濋悩鐑╁亾閻旂鐏囬柨?
            this.ageUp(growth);
            didSomething = true;
        }
        if (this.silencedByShears) {
            this.silencedByShears = false;
            didSomething = true;
        }

        if (didSomething || (isLiked || isFavorite)) { // 濠碘€冲€归悘澶嬵槹閻斿搫鈷栭柡鍫濐槹閺呫儵鏁嶇仦钘夌ギ濞达絾瀵у褏鎮伴埀顒勫箣閹邦剙瀣€濞戞梻鍠嶇槐鏉库槈閸絺鍋撳Δ鈧懟鐔煎箻椤撶喐鏉归柛姘暕缁椼垻鎲茬捄銊︾暠濠㈤€涘嵆閻?
            this.usePlayerItem(player, hand, stack); // 婵炴垵鐗愰埀顒侇殘婢у潡鏁?
            this.playSound(isFavorite ? com.ohyeah.ohyeahmod.registry.ModSoundEvents.TIANSULUO_PS_EAT_FAVORITE.get() : com.ohyeah.ohyeahmod.registry.ModSoundEvents.TIANSULUO_PS_EAT.get(), 1.0F, 1.0F);
            return InteractionResult.SUCCESS;
        }

        return InteractionResult.PASS;
    }

    /**
     * Procedure: 缂侇喖澧介垾姗€骞戦弴鈥崇闁告瑦顨呴崵顕€鎯夐鍕紵闁挎稑鏈晶鐣屾偘鐏炵硶鍋撳鈧粭澶愬矗椤栨艾鏂ч悹瀣噳閳ь剚绻傞崹鐣屸偓瑙勭啲缁辨繈鐛崼鏇炴缂傚喚鍠栧浠嬪礄閼姐倕笑闁诡兛鐒﹀┃鈧柣銊ュ閳ь剚甯熼鎼佸籍鐠佽瀚?
     */
    private void handleHurtRetaliationTrigger(boolean actuallyHurt, DamageSource source) {
        if (!actuallyHurt || this.level().isClientSide) return;

        Entity attackerEntity = source.getEntity();
        if (!(attackerEntity instanceof LivingEntity attackerLiving)) return;

        // [闂傚啳灏銈嗗椤﹀啴鏁嶅顒夋搐闁哄绮岄悾鐘诲及椤栨瑧顏遍柛娆樹海椤附銇欓娑欑疀闁汇劌瀚悿鍥偋閳哄绀夌紓浣圭箓椤曨喗绋夊鍫濆幋閻庣數鎳撻悾鐘绘儍閸曨亜鐦滃ù婊冩惈瀵粙宕欐导娆愬?
        if (this.isTame() && attackerLiving == this.getOwner()) return;

        // 閻犱礁澧介悿鍡樻交閸婄喖鍤嬮柡浣稿濮瑰鎯冮崟顒侇槯闂傚倽鎻幏?
        this.retaliationTicksRemaining = RETALIATION_MEMORY_TICKS;
        // 閻犱礁澧介悿鍡楊嚕閳ь剟鎮橀銏狀枀闁汇劌瀚铏规嚊閳ь剛鎷犻銏㈠従闁圭虎鍘介弬渚€宕愰幒鏇ㄥ悁闁哄啳顔愰幏?
        this.retaliationDeclareTicksRemaining = ATTACK_DECLARE_TICKS;

        // 闁圭虎鍘介弬渚€鎷戦崟顐㈩潝/閻庣櫢缍€閳诲牏鎷犻銏㈠従闁靛棗鍊哥紞瀣磹閹烘洦鍚€闁哄啫澧庣划銊╁级閻斿憡顦ч柨娑樿嫰閻ㄣ垽宕ｉ幋婵堟鐎殿喚鎳撶粻鐑芥晸?
        this.playSound(com.ohyeah.ohyeahmod.registry.ModSoundEvents.TIANSULUO_PS_ATTACK_DECLARE.get(), 1.0F, 1.0F);
    }

    /**
     * Procedure: 閻庡湱鍋ゅ顖炲箥瑜戦、鎴炵▔婢跺海绠鹃柛娆愬灥閼村﹦浜搁崟顓炩挅闁汇劌瀚晶鍧楁偠閸℃绲洪悘蹇撳閳ь剚妲掔欢顐ｇ▔鎼淬劎鍙鹃柡浣哥墳閹?
     */
    private void performRetaliationAttack(LivingEntity target) {
        for (int i = 0; i < BURST_SHOTS; i++) {
            TiansuluoPinkScarfProjectileEntity projectile = new TiansuluoPinkScarfProjectileEntity(this.level(), this);
            projectile.setDamage((float) PROJECTILE_DAMAGE);
            Vec3 muzzlePos = this.getRetaliationMuzzlePosition();
            projectile.setPos(muzzlePos.x, muzzlePos.y, muzzlePos.z);

            // 濡澘瀚粊鏉戭嚕瑜版帊澹曢柨娑欐皑閻庮垶宕欓崱娆愮獥闁哄秴娲ㄥ▓鎴︾叕椤愶紕绉奸柛瀣箣缁楀倹鎷呭鍥╂瀭闁?
            double d0 = target.getX() - muzzlePos.x;
            double d1 = target.getY(0.33D) - muzzlePos.y;
            double d2 = target.getZ() - muzzlePos.z;
            double d3 = Math.sqrt(d0 * d0 + d2 * d2);

            projectile.shoot(d0, d1 + d3 * 0.2D, d2, PROJECTILE_SPEED, PROJECTILE_DIVERGENCE);
            this.level().addFreshEntity(projectile);
        }
        this.playSound(com.ohyeah.ohyeahmod.registry.ModSoundEvents.TIANSULUO_PS_ATTACK_SHOT.get(), 1.0F, 1.0F);
    }

    /**
     * 閺夊牆鎳庢慨顏嗘媼閿涘嫮鏆€殿喚鎳撻惃鐘绘偋閳轰礁绲洪悘蹇撳閸嬶綁鎯冮崟顓涙晞闂傚倹娼欓幃婊堟煂韫囧海绉寸紓鍐惧櫙閹?
     */
    private Vec3 getRetaliationMuzzlePosition() {
        Vec3 forward = this.getViewVector(1.0F);
        double horizontalOffset = this.getBbWidth() * 0.5D + 0.65D; // 闁告瑦鍨甸惃鐘绘倷閻熺増韬棅顒夊亜婢х娀鎮鹃妷銉ょ俺闁稿绻掍簺
        return new Vec3(this.getX() + forward.x * horizontalOffset, this.getY() + this.getBbHeight() * 0.15D, this.getZ() + forward.z * horizontalOffset);
    }

    // ====================================================================================
    // [闁糕晞娅ｉ、鍛綇閸涱厼袠闁告帇鍊曢悾楣冨棘鐟欏嫮銆奭
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
    // [闁规亽鍎辫ぐ娑氣偓鍦仧楠炲槼 濞存籂鍐ㄧゲ闁秆勵殕閻楀疇绠涢崘顔瑰亾閺勫繒甯?(EggLayingSpecies)
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
    // [闁规亽鍎辫ぐ娑氣偓鍦仧楠炲槼 闂傚﹨娅曢弲銉у寲閼姐倗鍩?(SoundParticipant)
    // ====================================================================================
    @Override
    @Nullable
    protected SoundEvent getAmbientSound() {
        return this.silencedByShears ? null : com.ohyeah.ohyeahmod.registry.ModSoundEvents.TIANSULUO_PS_AMBIENT.get();
    }

    @Override
    @Nullable
    protected SoundEvent getHurtSound(DamageSource source) {
        return com.ohyeah.ohyeahmod.registry.ModSoundEvents.TIANSULUO_PS_HURT.get();
    }

    @Override
    @Nullable
    protected SoundEvent getDeathSound() {
        return com.ohyeah.ohyeahmod.registry.ModSoundEvents.TIANSULUO_PS_DEATH.get();
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState state) {
    }

    

    // ====================================================================================
    // [闁轰胶澧楀畵浣规償韫囨挸鐏欓柛鏍ㄦ够
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
        // [閻庡湱濮锋晶鍧楁偋鐟欏嫧鍋撻—?缂佽娴囬崑娑㈠触鎼存繂鏁╅柨娑樼灱閺?Minecraft 闁告鍠撴晶妤呭箳瑜嶉崺妤呮晸?
        // 闁搞儳濮崇拹鐔稿緞閳哄啰顦辩紓鍐╊殕濡叉悂鎮介妸銉ョゲ闁秆勵殘缁犳帒鈻撻弽顐ｇ暠闁挎稑鏈晶宥嗙閵夈劎绠查柨?null 闂傚啰绮娑㈠储閻斿搫顣兼鐐靛帶閸炲洨鎹㈡担鍦毇闁哄牆鎼崺妤呮晸?
        return null;
    }
}
