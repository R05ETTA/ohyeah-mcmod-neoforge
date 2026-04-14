package com.ohyeah.ohyeahmod.entity;

import com.ohyeah.ohyeahmod.entity.ai.LayEggBlockGoal;
import com.ohyeah.ohyeahmod.entity.ai.MateForEggBlockGoal;
import com.ohyeah.ohyeahmod.entity.ai.PounceAttackGoal;
import com.ohyeah.ohyeahmod.entity.common.Pounceable;
import com.ohyeah.ohyeahmod.entity.common.EggLayingSpecies;
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
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * 濠㈠灈鏅濈粈宀€绱?(闁瑰瓨蓱閺嬬喖鎳? 閻庡湱鍋樼紞瀣Υ?
 * <p>
 * 鐎瑰憡褰冪花鏌ユ偨?"闁告劕鎳橀崕?Procedure 濞寸媴绲块幃? 闁哄鍩栭悗顖炴煂瀹ュ棛鈧垶鏁嶇仦鐣屾閹煎鍋涢妵鍥儍閸曨噮鏀藉☉鎾存そ閳ь剚妲掔欢顐﹀箯閸℃鐎诲☉鎾规鐎氼厾绮╃€ｎ剚鐣?handleXxx 闁哄倽顫夌涵鍫曞Υ?
 * 缂備綀鍕棡闁?TamableAnimal闁挎稑鐭侀獮蹇擃嚗濡ゅ嫷鍚囧☉鎾寸湽閳ь兛娴囩粣锟犳⒕韫囧鍋撴担鍛婄稄濞戞挸顑囬悺鎴犫偓鍦Х婢у潡鎮х憴鍕ㄥ亾瑜濈槐婵嬪触鐏炵偓顦уǎ鍥ㄧ箘閺嗏偓闁稿繐澧庣€氼參鎮ч崷顓熺暠闁炽儲绮忛幖顐﹀礉濞戙埄妫ч柟鍨灍閳ь剚绻嗙换搴ㄥ箣濡粯绨氶柛鎺曠堪閳?
 */
public class TiansuluoBattleFaceEntity extends TamableAnimal implements EggLayingSpecies, Pounceable {
    
    // ====================================================================================
    // [闁轰焦婢橀埀顒€鍚嬬敮鍫曞礆鐠哄搫閰盷 缂備胶鍠嶇粩瀵哥不閿涘嫭鍊為柟纰樺亾闁哄牆顦抽、鎴炵▔鏉炴壆鐟㈤柟瀛樕戦弸鐔煎矗閸屾稒娈堕柨娑樺閹便劑寮ㄧ憴鍕靛妰闁告牕鎼悡娆撳础閸愭彃璁查悹瀣暞閺嗭絿鈧湱鍋樼紞瀣嵁鐎圭姰鈧偓闁?
    // ====================================================================================

    // --- 1. 闁哄秶顭堢缓鍓ф暜閹间礁娅ゅ☉鎾虫捣婢у潡鎮堕崱妤€妫橀柡?---
    public static final String SPECIES_ID = "tiansuluo_battle_face";
    // 闁瑰瓨鍔曢崟鐐媴閹鹃娼鹃柟鍓у仧椤斿牏鈧妫勭€硅櫕绋夋惔銊у蒋閹艰揪璐熼埀?
    public static final float TARGET_ADULT_WIDTH = 0.8F;
    public static final float TARGET_ADULT_HEIGHT = 0.8F;
    // 妤犵偟鍘ч崟鐐媴閹捐埖绁查弶鍫濆暕缁剟骞嬮幇顒€瀣€濞达絾鎸惧▓鎴犵磽閳哄倹鏉规慨锝嗘煣缁躲儵濡?
    public static final float BABY_SCALE_FACTOR = 0.5F;

    // --- 2. 闁糕晞娅ｉ、鍛沪閻愮补鍋?(Base Attributes) ---
    // 闁哄牃鍋撳鍫嗗懏鏅搁柛娑滄閳ь剛顑曢埀?4.0D 闁烩晝顭堢紞瀣鎼达絽璐熼悗鐟板濞?12 濡増顨呯缓楣冩晬鐏炲墽妲风紒顔碱槸濞插灝顔忛悙顒佺函闁兼彃顦埀?
    public static final double BASE_MAX_HEALTH = 24.0D;
    // 缂佸顕ф慨鈺呮焻閻斿嘲顔婇柕?.28 閻忕偟鍋樼花顒佺▔椤撶姷鎼奸柛瀣箰閹烩晠濡?
    public static final double BASE_MOVEMENT_SPEED = 0.28D;
    // 缂佷究鍨洪弲顐ｆ交閸婄喖鍤嬮悹鐑樼箘椤洭濡?
    public static final double BASE_FOLLOW_RANGE = 16.0D;
    // 闁糕晞娅ｉ、鍛交閹寸偛鐏涢柡鈧拠鎻掓瘖闁告梹绋忛埀?.0D = 2 濡増顨呯缓楣冨Υ?
    public static final double BASE_ATTACK_DAMAGE = 4.0D;
    // 閻炴凹鍋呭〒鍫曟偉闁稁妫ㄩ柣妞绘櫅閹稑顕ｉ弴鐔割槯闁汇劌瀚簺闂侇偆鍠嶇粻濠氬极閼割兘鍋?.1 闁稿﹤绉朵簺闂侇偆鍠嗛埀?
    public static final double TEMPT_SPEED_MODIFIER = 1.1D;

    // --- 3. 闁瑰瓨蓱閺嬬喐绋夋惔銈庡殧闂傚﹤纾柈瀵哥磼閻旇绮撻柛鏂诲姂閳ь剚妲掔欢?(闁哄倽顫夐、?闁挎稒纰嶅﹢鍥礉閿涘嫷浼傜紒顔煎⒔閳ユ鎷犻懡銈庢健闁硅矇鍐ㄧ厬) ---
    // 濞寸姴娲︽禒铏规媼閺夎法绠撻柡鍐ㄧ埣閺嗛亶鏁嶅顒€缍€闁告垼顕ч幃妤冩媼妫颁胶绉跺ù鐘叉处娴犳椽鐛捄铏规閻犲洦娲栧浠嬪礄閼姐倖鐣遍柟顒傜帛濡炲倿姊诲ǎ顑藉亾?00 ticks = 5 缂佸甯囬埀顒€鍊搁幃搴ㄦ焻閸屾繂鐦遍柛銉ㄦ彧缁?00~200闁?
    public static final int RETALIATION_MEMORY_TICKS = 100;
    // 闁瑰灚鍨甸崵顔炬閵忊剝娅曢柛鎺嬪€曢悾楣冩嚑閸愩劍绾柨娑欐皑濞蹭即寮介崶褎韬慨婵勫€涚€垫牠宕堕弶鎴濇暥闁归潧绉撮崢鎴犳媼濮濆睗鏇㈠矗閹达富妫ч柟?AI闁靛棗鍊搁幃搴ㄦ焻閸屾繂鐦遍柛銉ㄦ彧缁?.0~12.0闁?
    public static final double RETALIATION_RANGE = 10.0D;
    // 閺夌儐鍓濋棅鈺呮焻閻斿嘲顔婇柨娑欎亢閹碱偊宕濆☉娆愬焸闂傚倸顕ú瀛樻媴韫囨洘绐楅柡宥呮川濞堟垿鎮橀崹顐ｆ珱閹艰揪璐熼埀?2.0F 閻忕偟鍋樼花顒勫几娴ｅ憡褰ラ柨娑樼灱椤戜線宕ラ崼鐔风仜闁哄倹顨夐崝顖炴儍閸曨剚娅涢柟鍦焿椤旀洜鈧鍝庨埀?
    public static final float RETALIATION_TURN_SPEED = 22.0F;
    
    // 闁靛棙鍔曢崣褔鏌ㄩ琛″亾閹存繍鍚呴柟瀛橆焾椤曘垽妫呴搹顐ｎ槯闂傗偓閸栵紕绀勯柦鍐ㄥ婵繘宕滃鍡樺暁闁挎稑顧€缁?
    // 闁哄倽顫夐、?闁哄秶顭堢缓楣冨嫉閸濆嫬鐓戦柕鍡楀€介姘跺磹閻撳海绠戝銈堫唺缁楀瞼鎸ч崟顒傜埍闁告牕鎳嶉懙?ATTACK_DECLARE (ogg闁哄倸娲ｅ▎? 闁汇劌瀚晶鍧楁偠閸℃ɑ顦ч梻鈧径绋跨船闁哄秶鍘ч顔筋瀲閹板墎纾?
    // 40 ticks = 2.0缂佸甯囬埀顒€鍊歌ぐ鍫ュ礄缂佹ɑ顦ч悷娆欑畱瑜板倻鎷犻銏㈠従妤犵偠娉涚槐鎴炴叏鐎ｎ亖鍋撻幒鎾存闁挎稑鑻埀顒佸笚閺嗙喖寮甸悢鍏硷紵 entityData 濞村吋淇洪～锕傚冀閸ヮ亶鍞跺☉?attacking 闁绘鍩栭埀顑跨筏缁辨繈鎮介妸銈囪壘濡炵懓宕慨鈺冣偓骞垮灪閸╂稓绮╅娑欏啊闁衡偓閹规劖鎯堥柛鏃€绋戞慨鈺呮偨濮瑰洠鍋?
    public static final int ATTACK_DECLARE_TICKS = 40;
    
    // 闁瑰灚鍨甸崵?Pounce)闁绘せ鏅濋幃濠囧矗閸屾稒娈堕柨?
    // 濡炲鍋炴晶銈咁潩閺夋垿鎸柛鎺撶箞閳ь剛鍠庣€规娊骞掗妸銉ヮ潝闁?.0D 闁哄绀侀崣鍧楁偉閸℃绲洪柛鏃€绋忛埀?
    public static final double POUNCE_HORIZ_SPEED = 3.0D;
    // 濡炲鍋炴晶銈夊垂閸屾粍绾悹褏鏌夐悜锕傚礆濠靛鍋撻悢宄邦唺闁?.15D 闁煎啿鈧喓楔閺夆晛娲ら妵鍥╃棯?闁哄秴銈搁悵顕€鎯冮崟顖涱唶缁炬澘绉舵晶鍧楀Υ?
    public static final double POUNCE_VERT_SPEED = 1.15D;
    // 濡炲鍋炴晶銈咁煥閻愮鏁勯悗鐟扮秺閺佸﹪寮崼鏇燂紵闁靛棗鍊界粔瀛樻交閸ヮ煈鍔?Tick 闁轰焦濯界€氥垽寮甸鍥ㄥ劙闁革妇澧楅崹銊╁嫉椤忓嫭鍤掑☉鎿冨弿缁辨繂顕ｉ崫鍕厬缂備焦鎸诲顐ｎ槹閻愭潙鈪抽柣妯垮煐閳ь兛绶氬Σ璇差潰閵忕姴骞㈡慨婵囩湽閳?
    public static final int POUNCE_MAX_FLIGHT = 12;
    // 闁告稒鍨濋懙鎴犲枈閻楀牊瀵紒鐘轰含濞堟垹鎮伴妷锔诲妧闁艰翰鍔忛崕澶愬磹缁楄　鍋?.2D 闁煎啿鈧噥鍞ㄥ瀣仦婢с倝寮撮弶鎴晣闁哄嫭鎸婚幗婵囩▔椤撶姴璐熼悗纭呯堪閳?
    public static final double POUNCE_PADDING = 0.2D;

    // --- 4. 闁汇垻鍠庨幊锟犲川閵婏附鍩傚☉鎾抽叄閳瑰寮靛鍛緭濡?(Taming & LifeCycle) ---
    // 妤犵偟鍘ч崟楣冨嫉閻旂硶鍋撶紒妯活槯闂傗偓閸栵紕绀凾ick闁挎稑顦埀?4000 ticks = 20 闁告帒妫濋幐鎾绘晬閸︻摨necraft 闁汇劌瀚粩瀛樺緞閳哄绀嗛柕?
    public static final int BABY_GROWTH_AGE = 24000;
    // 闁哥姴鍊块ˉ銈夊疾椤曗偓閳ь剚鑹鹃弸鈺呮偉闁稁妫ㄩ柣妞绘櫆濡炲倿鏁嶅畝鍐劜閺夆晛娲ㄥ▓鎴﹀箣閹扮増姣愰柡鍐ㄧ埣閺嗛亶濡?000 ticks = 5 闁告帒妫濋幐鎾诲Υ?
    public static final int FOOD_GROWTH_STEP = 6000;
    // 濞存籂鍐ㄧゲ闁秆勵殔椤掝噣宕犻弽銊ヮ暡闂傚洠鍋撻柣銊ュ濞撳墎浜?Tick 闁轰浇鍩囬埀?
    public static final int HATCH_STAGE_TICKS = 200;
    // 濞存籂鍐ㄧゲ闁秆勵殕閻?Tick 闂傚懎绻戝┃鈧悗娑㈡涧鐎垫煡鎯冮崟顒夋搐闁绘粌娲ら埀顒佸笚閺嗙喖濡?00 閻炴稏鍔庨妵姘剁嵁閸愬弶缍?500 tick 濠⒀呭仜婵偞绋夐埀顒€鈻庨垾鎰佸姉闁告牗鐗炵换妯绘償閿旇　鍋?
    public static final int HATCH_CHANCE_INV = 500;
    // 濡炶鍨卞﹢鍥箣閹邦剙顫犻柣婊冩川濞堟垿宕氶崱妯兼Г (1/3 闁汇劌瀚々褔鎮抽崶銊ョ亣闁?闁?
    public static final int TAME_CHANCE_DENOMINATOR = 3;
    // 濡炶鍨卞﹢鍥触鎼搭垳绀夐柛鐘插€块ˉ銈夊嫉閳ь剟鎮ラ柆宥庢（闁绘せ鏅滄晶宥夊炊閻愬樊妲婚柣銊ュ閺佹捇宕ㄩ挊澶嗗亾缁楄　鍋?.0F = 2 濡増顨呯缓楣冨Υ?
    public static final float FOOD_HEAL_AMOUNT = 4.0F;

    // --- 5. 闁哥姵绮岄妶浠嬫煀瀹ュ洨鏋傚☉鎾虫捣閺佹捇骞嬮幇鐗堫€欓柛?---
    // 闁哄拋鍣ｉ埀顒佽壘閺嬧晠鎮ラ柆宥庢（闁绘せ鏅槐浼存偨閵娿倗鑹鹃柛姘鳖焾缁扁晠濡存担绋夸壕闁绘梻鍠嗛埀顑挎濮橈箓鏌婂鍐ｅ亾?
    public static final List<String> FOOD_LIKED = List.of("minecraft:wheat", "minecraft:carrot", "minecraft:beetroot", "minecraft:potato");
    // 闁哄绀佺€规娊宕板鍛厓濡炲鍠撴晶鍧楁晬濮樿鲸鏆忓ù婊冮叄閳瑰寮靛鍐ｅ亾娴ｅ憡绀€閻炴稈鍋撻柕鍡曡兌閻忔盯姊婚弶鎴濅壕闁绘梻鍠愰崹姘剁嵁濞ｎ兘鍋?
    public static final List<String> FOOD_FAVORITE = List.of("minecraft:cake", "ohyeah:chips");
    // 闁汇垻鍠愰崹姘跺级閸愵喖娅㈠☉鎾虫捣閸忋垻鍖栨繝姘€欓柛鎺曠堪閳?
    public static final int SPAWN_WEIGHT = 1;
    public static final int SPAWN_MIN_GROUP = 1;
    public static final int SPAWN_MAX_GROUP = 1;
    public static final List<String> SPAWN_BIOMES = List.of("minecraft:plains", "minecraft:meadow");

    // --- 6. 閻犲浂鍙冮悡鍓佸寲閼姐倗鍩犻悽顖涙倐閸?---
    // 闂傚倽灏禍?Ambient)闂傚﹨娅曢弲銉╂儍閸曨啩鏇㈠矗閹达附锛熼梻?Tick)闁?000 ticks = 5 闁告帒妫濋幐鎾朵焊濠靛﹦妲搁悷娆欑畱瑜板倹绋夐埀顒€鈻庢幊閳?
    public static final int AMBIENT_INTERVAL = 6000;
    // 闁绘顫夐悾鈺呮閾忣偅娅忛柣銊ュ琚濋柛娆愬灥閸犲酣宕＄壕瀣垫船闁烩晜鐗撻埀顒€鍊垮Σ璇差潰閵忥絿绠剧紓渚囧幖瑜板牓宕?闂傚倽灏禍鐗堫殺閼测晜娈旈柕?
    public static final Map<String, Integer> VOICE_OVERRIDES = Map.of("ambient", 60, "hurt", 20);

    // ====================================================================================
    // [闁告劕鎳橀崕鎾偐閼哥鍋撴担鍝ユ憻婵炲牆鍚€缁楀矂寮悧鍫濈ウ闁告艾鏈鐎?
    // ====================================================================================
    
    // --- 闁告艾鏈鐐哄极閻楀牆绁﹂悗瑙勭煯缁?(Client-Server Synchronization) ---
    private static final EntityDataAccessor<Boolean> IS_ATTACKING = SynchedEntityData.defineId(TiansuluoBattleFaceEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> HAS_CARRIED_EGG_BLOCK = SynchedEntityData.defineId(TiansuluoBattleFaceEntity.class, EntityDataSerializers.BOOLEAN);

    // --- NBT 閻庢稒锚閸嬪秹鏌ㄩ灏栧亾?---
    private static final String TAG_SILENCED_BY_SHEARS = "SilencedByShears";
    private static final String TAG_HAS_CARRIED_EGG_BLOCK = "HasLuanluanBlock";
    private static final String TAG_EGG_BLOCK_TARGET_X = "LuanluanBlockTargetX";
    private static final String TAG_EGG_BLOCK_TARGET_Y = "LuanluanBlockTargetY";
    private static final String TAG_EGG_BLOCK_TARGET_Z = "LuanluanBlockTargetZ";
    private static final String TAG_EGG_BLOCK_PLACING_COUNTER = "LuanluanBlockPlacingCounter";
    private static final String TAG_EGG_BLOCK_PLAYER_UUID = "LuanluanBlockPlayerUuid";

    // --- 閻庡湱鍋樼紞瀣礃閸涙潙鍔ラ柣妯垮煐閳?(Entity Local State) ---
    private final Set<String> playedCues = new HashSet<>();
    private boolean silencedByShears;
    private @Nullable BlockPos eggBlockTargetPos;
    private int eggBlockPlacingCounter;
    private @Nullable UUID eggBlockPlayerUuid;
    private boolean wasBabyLastTick;

    // 閺夆晛鈧喖鍤嬮柡浣稿濮瑰鎯冮崟顐⑩挅濞达絾鐟﹀鍌炴⒐閸喒鍋撻幒鎾存闁靛棗鍊搁妵鍥ㄧ?闁哄啯鍎奸妴鍐矆閸濆嫷妲卞ù婊冨閸斿骞€閹烘垵娅欏璺烘处婢с倝宕欓懡銈呅﹂柟顑块檷閳?
    private int retaliationTicksRemaining;
    // 閻庣櫢绲鹃崹顒傛嫚椤撱垻鍙?闁藉啫瀚慨蹇涘磹閹烘挻娈堕柕鍡楀€搁埀顒佸笚閺嗙喖寮甸悢鍏硷紵濞村吋鑹惧﹢顏嗏偓骞垮灪閸╂稓绮╅姘€辨慨?IS_ATTACKING = true闁挎稑鑻悥鍫曟偨閵娿倗鑹鹃柟缁㈠幗閺備線鎷戦崟顐㈩潝闁硅埖鐗曟慨鈺呭礉閵娧勬毎闁?
    private int retaliationDeclareTicksRemaining;
    // 闁瑰灚鍨甸崵顕€骞庨埀顒勬嚄閻ｅ本鐣遍柛鎰槻瀹撳牓寮崼鏇燂紵闁靛棗鍊垮Σ璇差潰閵忥絿绠剧紓渚囧弮椤戞鎮惧鍫＇闁瑰灚鍨埀?
    private int pounceCooldownTicks;

    // ====================================================================================
    // [闁汇垻鍠庨幊锟犲川閵婏附鍩傚☉?AI] 闁告帗绻傞～鎰板礌閺嶏妇鐟㈤柣妯垮煐閳ь兛绀侀悾鐐▕?
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
        this.goalSelector.addGoal(1, new SitWhenOrderedToGoal(this)); // [閻庡湱濮锋晶鍧楁偋鐟欏嫧鍋撻—?闁告凹鍏涚划鐘诲川閹存帗濮㈤柛褎鍔掔粭?
        
        // 濡炲鍋炴晶?AI 闁烩晩鍠楅悥锝夋晬鐏炶棄寰撳ù鍏艰壘濠€?satisfies isReadyToPounce() (閺夆晛鈧喖鍤嬮柣妯垮煐閳? 闁哄啳娉涢惃鍓ф嫚閺囩偛绲洪柛鏂诲妿婢у潡鎮堕崱娑辨＇闁?
        this.goalSelector.addGoal(2, new PounceAttackGoal<>(this));
        // 闁稿繑绮岀花铏交閹寸偛鐏涢柡鈧拠鎻掓瘖闁挎稑鑻﹢顏咁槹閻愭潙鈪抽柛鎰槻瀹撳牓寮幆鏉垮灡闂婎剦鍋夐崐婵嬪箹?
        this.goalSelector.addGoal(3, new MeleeAttackGoal(this, 1.2D, false));
        
        this.goalSelector.addGoal(4, new MateForEggBlockGoal<>(this, 1.1D, "message.ohyeah.tiansuluo_battle_face.luanluan_block_carried", com.ohyeah.ohyeahmod.registry.ModSoundEvents.TIANSULUO_BREED_SUCCESS.get()));
        this.goalSelector.addGoal(5, new TemptGoal(this, TEMPT_SPEED_MODIFIER, stack -> this.isLikedFood(stack) || this.isFavoriteFood(stack), false));
        // [閻庡湱濮锋晶鍧楁偋鐟欏嫧鍋撻—?閻犺櫣鍠栧▓銏＄▔鐠佽櫕鐪介柕鍡楀€讳簺闁告柣鍔戦埀顒傚枎鐎硅櫕绋婂Ο缁樻1.1闁挎稑鐭佺粣娑氱矉鐠佸啿鐦滃ù?0闁哄秶鍘х槐鎴炴叏鐎ｎ厾顎€闂傚懎楠忕槐婵堟崉濠靛牜鐎?闁哄秶鍘ф禒鐘差潰閵忥絿顎€闂傚懎绻堥埀?
        this.goalSelector.addGoal(6, new FollowOwnerGoal(this, 1.1D, 10.0F, 2.0F));
        this.goalSelector.addGoal(7, new FollowParentGoal(this, 1.1D));
        this.goalSelector.addGoal(8, new LayEggBlockGoal<>(this, HATCH_STAGE_TICKS));
        this.goalSelector.addGoal(9, new WaterAvoidingRandomStrollGoal(this, 1.0D));
        this.goalSelector.addGoal(10, new LookAtPlayerGoal(this, Player.class, 6.0F));
        this.goalSelector.addGoal(11, new RandomLookAroundGoal(this));
        
        // [閻庡湱濮锋晶鍧楁偋鐟欏嫧鍋撻—?闁烩晩鍠楅悥锝夋煥娴ｅ摜鏆伴梺顐ｆ缁额偊宕￠崶鈺呯崜闁?
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
    // [闁哄秶顭堢缓鐐鐎ｂ晜顐介柛蹇嬪劚瑜版矑 閻忓繐妫楅ˇ鏌ュ级閸岀偐鍋撻弰蹇曞竼濠殿喗姊规晶顓犵磼濞嗗繐鏁堕梺顔哄妿濞?Procedure 濞寸媴绲块幃濠囧棘鐟欏嫮銆?
    // ====================================================================================

    @Override
    public void aiStep() {
        super.aiStep();
        if (this.level().isClientSide) {
            this.handleClientParticlesTick(); // [濞寸媴绲块幃濂?濠㈣泛瀚幃濠勨偓骞垮灪閸╂稓绮╅婊呯厛閻庢稒鍔橀妴鍐偝?
            return;
        }

        // 闁哄牆绉存慨鐔虹博椤栫偐鍋撻弰蹇曞竼
        this.handleVoiceSystemTick();         // [濞寸媴绲块幃濂?闁哄洤鐡ㄩ弻濠勬嫚椤撱垻鍙剧紒顖濆吹缁儤绋夋惔銏犵亣闂傗偓閸喚鐣柟?
        this.handleEggBlockLogicTick();       // [濞寸媴绲块幃濂?濠㈣泛瀚幃濠囧箹椤掑啰绠ラ柛妤呮涧濞硷紕鎹勯悢鍏碱吂闁绘壕鏅涢宥夋儍閸曨垪鍋撻弰蹇曞竼
        this.handleRetaliationTick();         // [濞寸媴绲块幃濂?濠㈣泛瀚幃濠冨緞瀹ュ嫮鐭忛柛濠冨笚閺嗙喐绋夋惔顭戞＇闁瑰灚鍨甸崰搴ㄥ础鐎电笑闁诡兛鐒﹀┃鈧?
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
     * Procedure: 濠㈣泛瀚幃濠勨偓骞垮灪閸╂稓绮╅姘卞鐟滆埇鍨婚惌鎴犫偓娑欏姧缁辨瑩鎮介妸銈囪壘閻忕偞娲滈妵姘跺箹椤掑啯宕抽柛妤呮涧濞硷繝鎯冮崟顓炐﹂柟顑跨筏缁辨岸濡?
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
        if (true) {
            
        }

        boolean babyNow = this.isBaby();
        if (this.wasBabyLastTick && !babyNow) {
            this.playSound(com.ohyeah.ohyeahmod.registry.ModSoundEvents.TIANSULUO_GROW_UP.get(), 1.0F, 1.0F);
        }
        this.wasBabyLastTick = babyNow;
    }

    /**
     * Procedure: 濡ょ姴鐭侀惁澶愬础闂堟稒鍋ラ柛姘鳖焾缁扁晠鎯冮崟顓炶礋閻庣鍩栧Σ鎼佸触閿旇法鐭濋柡鍫濐槹閺呫儵鏁嶉崼婵堟憼婵炶尪顔婄粭鏍閻愬瓨鈷戦悷娆忓€硅啯鐎殿喖楠忕槐姘跺Υ?
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
     * Procedure: 闁瑰瓨蓱閺嬬喖鎮╅懜纰樺亾娴ｈ绨氶柡宥囶焾缁洪箖濡撮崒婵堫槹閻犳劧绲炬晶鎼佸礄韫囨稒鏁氶悗瑙勭煯缁劙骞侀妸銉㈠亾閹烘洦鍚€闁哄啳缈伴埀顑跨窔椤ワ綁骞嶉幋婵嗘瀻闁告鎻槐婵嬬嵁鐠虹儤韬柦鍐ㄥ婵繘寮甸悢鍏硷紵闂傚牄鍨归幃婊堝极鐏炲吋鐪介柕?
     * 婵炲鍔嶉崜浼存晬濮橆厼鐏涢柡鍌涱殙閸旑垱绋夊鍛含婵縿鍊曢ˇ鈺呮儎鐎涙ê澶嶉柣妞绘櫇閹﹤顕ｉ埀顒勬倶椤愵剛绀夐柤鏉挎湰濡插摜浜搁崱娆徯﹂柟顑跨劍濮ｆ岸妫侀懠顒傝埗 TiansuluoBattleFacePounceGoal闁?
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
                    this.setAttacking(true); // 闁藉啫瀚慨蹇涙⒓閼告鍞介柨娑樿嫰閹挸顫㈤妷褏鑸堕悗骞垮灪閸╂稓绮╅娑欏啊闁衡偓閻愵剙顫嶉柛鏂诲妼婵晠鎮?
                }
                // 闁稿﹥甯熼鎼佸籍閸撲胶娉㈤柡澶屽枎閹鏁嶇€涘 Goal (TiansuluoBattleFacePounceGoal) 濞村吋纰嶇敮瀵哥不閳ュ疇瀚欓柟绗涘棭鏀介悗鍦仱濡绢垶鎯冮崟顓炩挅闁荤偛妫滈幑锝囨崉閻愯В鍋?
            } else {
                // 闁烩晩鍠楅悥锝嗙▔閵忕姰浜奸柟瀛樼墬椤掑瓨绂嶉埥鍛濞戞搩鍘介弻鍥ㄥ緞瀹ュ嫮鐭忛柣妯垮煐閳?
                this.retaliationTicksRemaining = 0;
                this.setAttacking(false);
            }
        } else {
            this.setAttacking(false);
        }
    }

    /**
     * Procedure: 闁瑰嚖闄勯崺鍛存偝閳轰緡鍟€濞达綀娉曢弫銈夊礈椤忓嫬鐎ù婊勫笒婵晠鎯冮崟顖楀亾閺勫繒甯嗛柨娑樼墢椤╋妇鎳涢埀顒勫箳婢跺妯呴柨娑橆槶閳?
     */
    private InteractionResult handleShearInteraction(Player player, InteractionHand hand, ItemStack stack) {
        if (stack.is(Items.SHEARS)) {
            if (this.level().isClientSide) return !this.silencedByShears ? InteractionResult.CONSUME : InteractionResult.PASS;
            if (!this.silencedByShears) {
                this.spawnAtLocation(Items.WHITE_WOOL); 
                this.level().playSound(null, this, SoundEvents.SHEEP_SHEAR, SoundSource.PLAYERS, 1.0F, 1.0F);
                this.playSound(com.ohyeah.ohyeahmod.registry.ModSoundEvents.TIANSULUO_SHEAR_REACT.get(), 1.0F, 1.0F);
                this.silencedByShears = true; // 鐎殿喒鍋撻柛姘煎灣椤╋妇鎳涢埀顒勬偐閼哥鍋?
                stack.hurtAndBreak(1, player, hand == InteractionHand.MAIN_HAND ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND);
                return InteractionResult.SUCCESS;
            }
            return InteractionResult.PASS; // 鐎规瓕灏～锕傚礈椤忓洨绠栭柨娑樿嫰閹风兘鎮鹃妷銈嗗攭濞?
        }
        return InteractionResult.PASS;
    }

    /**
     * Procedure: 濠㈣泛瀚幃濠囧窗閸岀儐妫ㄩ梺顐ｆ缁额偊鏁嶉崼婵嗕壕闁绘梻鍠嗛埀顑挎祰琚欓梻鍕╁€楅々锔炬嚊閳ь剟鏁嶆径澶岀憿濡炶鍨卞﹢鍥焻閺勫繒甯嗛柕?
     */
    private InteractionResult handleTamingAndFeeding(Player player, InteractionHand hand, ItemStack stack) {
        boolean isLiked = this.isLikedFood(stack);
        boolean isFavorite = this.isFavoriteFood(stack);

        if (!isLiked && !isFavorite) {
            return InteractionResult.PASS;
        }

        // 閻庡箍鍨洪崺娑氱博椤栨粍绾柟鎭掑劥缁绘垿宕堕悙鏉戠亣闁告梻鍠嶆禍鎺楀触鐏炵虎鍔勯柛鏂诲妺缂嶆棃鏁嶇仦鎯ь暡闁哄牆顦悿鍕⒔閸涱喗娈堕柟璇″枦椤撳摜绮诲Δ鈧﹢顏堝嫉瀹ュ懎顫ょ紒鏃戝灡婢х晫鎮扮仦鐑╁亾?
        if (this.level().isClientSide) {
            return InteractionResult.CONSUME;
        }

        // --- 濡炶鍨卞﹢鍥焻閺勫繒甯?(Taming Logic) ---
        if (isFavorite && !this.isTame()) {
            this.usePlayerItem(player, hand, stack); // 婵炴垵鐗愰埀顒侇殘婢у潡宕?
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
        if (this.isTame() && this.isOwnedBy(player) && isFavorite && this.getHealth() < this.getMaxHealth()) {
            this.usePlayerItem(player, hand, stack); // 婵炴垵鐗愰埀顒侇殘婢у潡宕?
            this.heal(FOOD_HEAL_AMOUNT);
            this.playSound(com.ohyeah.ohyeahmod.registry.ModSoundEvents.TIANSULUO_EAT_FAVORITE.get(), 1.0F, 1.0F);
            return InteractionResult.SUCCESS;
        }

        // --- 闁稿矈鍓涢崯娑欑▔鎼淬伆鎺楁⒔閵堝浂娲ｉ悷灏佸亾 (Growth & Shear Remedy) ---
        boolean didSomething = false;
        if (this.isBaby()) {
            int growth = isFavorite ? -this.getAge() : FOOD_GROWTH_STEP; // 闁哄牃鍋撻柣鏍憾椤ャ倝鎮ч埡鍐粶闂傚倸鐡ㄩ崹姘剁嵁鏉堝墽绀夐柛姘剧畱閸垶宕濋悩鐑╁亾閻旂鐏囬梻鈧?
            this.ageUp(growth);
            didSomething = true;
        }
        if (this.silencedByShears) {
            this.silencedByShears = false; 
            didSomething = true;
        }
        
        if (didSomething || (isLiked || isFavorite)) { 
            this.usePlayerItem(player, hand, stack);
            this.playSound(isFavorite ? com.ohyeah.ohyeahmod.registry.ModSoundEvents.TIANSULUO_EAT_FAVORITE.get() : com.ohyeah.ohyeahmod.registry.ModSoundEvents.TIANSULUO_EAT.get(), 1.0F, 1.0F);
            return InteractionResult.SUCCESS;
        }

        return InteractionResult.PASS;
        }

    /**
     * Procedure: 缂侇喖澧介垾姗€骞戦弴鈥崇闁告瑦顨呴崵顕€鎯夐鍕紵闁挎稑鏈晶鐣屾偘鐏炵硶鍋撳鈧粭澶愬矗椤栨艾鏂ч悹瀣噳閳ь剚绻傞崹鐣屸偓瑙勭啲缁辨繈鐛崼鏇炴缂傚喚鍠栧浠嬪礄閼姐倕笑闁诡兛鐒﹀┃鈧柣銊ュ閳ь剚甯熼鎼佸籍闊祴鍋?
     */
    private void handleHurtRetaliationTrigger(boolean actuallyHurt, DamageSource source) {
        if (!actuallyHurt || this.level().isClientSide) return;
        
        Entity attackerEntity = source.getEntity();
        if (!(attackerEntity instanceof LivingEntity attackerLiving)) return;

        // [闂傚啳灏銈嗗椤﹀啴鏁嶅顒夋搐闁哄绮岄悾鐘诲及椤栨瑧顏遍柛娆樹海椤附銇欓娑欑疀闁汇劌瀚悿鍥偋閳哄绀夌紓浣圭箓椤曨喗绋夊鍫濆幋閻庣數鎳撻悾鐘绘儍閸曨亜鐦滃ù婊冩惈瀵粙宕欏Ч鍥ｅ亾?
        if (this.isTame() && attackerLiving == this.getOwner()) return;

        // 閻犱礁澧介悿鍡樻交閸婄喖鍤嬮柡浣稿濮瑰鎯冮崟顒佺畳闁轰礁鐗忛悰銉╁矗閿濆棙鍩傞柨?00 tick = 5 缂佸甯槐?
        this.retaliationTicksRemaining = RETALIATION_MEMORY_TICKS;
        // 閻犱焦鍎抽悾鎯ь嚕閳ь剟鎮?闁瑰灚鍨甸崵顕€宕滃鍥ㄧ暠閻庣櫢绲鹃崹顒傛嫚椤撱垻鍙鹃柟缁㈠幗閺備線宕愰幒鏇ㄥ悁闁哄啳顔愮槐娆愵渶濡鍚?40 tick = 2 缂佸甯槐婵嬫閳ь剚绋夋惔銊у従濡増鍨堕弸鍐╃閸洘姣愰幖杈剧畱椤曨喗顬囬幇鍓佺
        this.retaliationDeclareTicksRemaining = ATTACK_DECLARE_TICKS;
        
        // 闁圭虎鍘介弬渚€鎷戦崟顐㈩潝/閻庣櫢缍€閳诲牏鎷犻銏㈠従闁靛棗鍊介姘辨嫚椤撱垻鍙鹃柟缁㈠幗閺備胶鈧懓鏈惁顖炲籍鐠佸湱绀堿I Goal (TiansuluoBattleFacePounceGoal) 濞村吋纰嶆导鍡樼附閽樺甯掗悹浣圭摃缁绘﹢宕楅妷銈囩憮濞戞挴鍋撻梻鍐煐椤斿矂鎯冮崟顓炩挅闁荤偛妫濋ˉ锝夊箥閹存瑢鍋?
        this.playSound(com.ohyeah.ohyeahmod.registry.ModSoundEvents.TIANSULUO_ATTACK_DECLARE.get(), 1.0F, 1.0F);
    }

    // ====================================================================================
    // [濡炲鍋炴晶銈夊箣濡粯鐏嶅☉鎾存尭閻﹢寮ㄩ娑欏闁哄倽顫夌涵绂?(閻?TiansuluoBattleFacePounceGoal 閻犲鍟伴弫銈夋儍閸曨剙澶嶉柛?
    // ====================================================================================

    public boolean isReadyToPounce() { return this.retaliationTicksRemaining > 0; }
    public boolean isAttackCooldownReady() { return this.pounceCooldownTicks <= 0; }
    public void startCooldown() { this.pounceCooldownTicks = 40; }
    public void beginCharge() { this.playSound(com.ohyeah.ohyeahmod.registry.ModSoundEvents.TIANSULUO_ATTACK_DECLARE.get(), 1.0F, 1.2F); }
    public void finishSuccessfulRetaliation() {
        this.retaliationTicksRemaining = 0;
        this.startCooldown();
        this.playSound(com.ohyeah.ohyeahmod.registry.ModSoundEvents.TIANSULUO_ATTACK_END.get(), 1.0F, 1.0F);
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

    // --- Pounceable 閸欏倹鏆熼幒銉ュ經鐎圭偟骞?---
    @Override public double getPounceHorizontalSpeed() { return POUNCE_HORIZ_SPEED; }
    @Override public double getPounceVerticalSpeed() { return POUNCE_VERT_SPEED; }
    @Override public int getPounceMaxFlightTicks() { return POUNCE_MAX_FLIGHT; }
    @Override public double getPounceHitboxPadding() { return POUNCE_PADDING; }

    // ====================================================================================
    // [閸╄櫣顢呮潏鍛И閸掋倕鐣鹃弬瑙勭《]
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
    // [闁规亽鍎辫ぐ娑氣偓鍦仧楠炲槼 濞存籂鍐ㄧゲ闁秆勵殕閻楀疇绠涢崘顔瑰亾閺勫繒甯?(EggLayingSpecies)
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
    // [闁规亽鍎辫ぐ娑氣偓鍦仧楠炲槼 闂傚﹨娅曢弲銉у寲閼姐倗鍩?(SoundParticipant)
    // ====================================================================================
    @Override
    @Nullable
    protected SoundEvent getAmbientSound() {
        return this.silencedByShears ? null : com.ohyeah.ohyeahmod.registry.ModSoundEvents.TIANSULUO_AMBIENT.get();
    }

    @Override
    @Nullable
    protected SoundEvent getHurtSound(DamageSource source) {
        return com.ohyeah.ohyeahmod.registry.ModSoundEvents.TIANSULUO_HURT.get();
    }

    @Override
    @Nullable
    protected SoundEvent getDeathSound() {
        return com.ohyeah.ohyeahmod.registry.ModSoundEvents.TIANSULUO_DEATH.get();
    }

    @Override
    public int getAmbientSoundInterval() {
        return AMBIENT_INTERVAL;
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
        // [閻庡湱濮锋晶鍧楁偋鐟欏嫧鍋撻—?缂佽娴囬崑娑㈠触鎼存繂鏁╅柨娑樼灱閺?Minecraft 闁告鍠撴晶妤呭箳瑜嶉崺妤呭Υ?
        // 闁搞儳濮崇拹鐔稿緞閳哄啰顦辩紓鍐╊殕濡叉悂鎮介妸銉ョゲ闁秆勵殘缁犳帒鈻撻弽顐ｇ暠闁挎稑鏈晶宥嗙閵夈劎绠查柛?null 闂傚啰绮娑㈠储閻斿搫顣兼鐐靛帶閸炲洨鎹㈡担鍦毇闁哄牆鎼崺妤呭Υ?
        return null;
    }
}