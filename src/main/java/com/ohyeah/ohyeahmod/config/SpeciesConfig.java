package com.ohyeah.ohyeahmod.config;

import com.ohyeah.ohyeahmod.sound.definition.SoundCue;
import com.ohyeah.ohyeahmod.sound.definition.SoundDefinition;
import com.ohyeah.ohyeahmod.sound.definition.SpeciesSoundCatalog;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 物种核心配置模型 (Logic-Aware Config)。
 * 采用 Record 保证数据的不可变性和线程安全。
 * 本类不仅是数据容器，还内置了基础的业务判定逻辑（如进食、音效路由）。
 *
 * 【架构设计说明】：
 * 本配置采用“组件化 (Component-Based)”设计。SpeciesConfig 作为顶级容器，由多个子模块（如 Behavior, Voice）组成。
 * 为了简化配置：
 * 1. 每个子模块都提供了静态工厂方法（如 Behavior.passive(), Voice.silent()），用于快速创建默认配置。
 * 2. 简单的物种（如酥虾）可以仅配置其特有的属性，其他部分直接使用默认工厂，从而极大地减少冗余代码。
 * 3. 这种设计在保持 Record 强类型检查的同时，实现了“缺省配置”的灵活性。
 *
 * @param speciesId 物种唯一标识符（如 "tiansuluo_pink_scarf"）
 * @param texturePath 纹理资源路径，相对于 assets/ohyeah/ 目录
 * @param attributes 基础战斗与生命属性
 * @param behavior AI 行为与特殊能力参数
 * @param spawn 自然生成规则配置
 * @param breeding 繁殖方式与产卵块玩法逻辑
 * @param food 进食偏好与物品映射
 * @param loot 死亡掉落物配置
 * @param voice 语音系统（受管音效）参数
 */
public record SpeciesConfig(
        String speciesId,
        String texturePath,
        Attributes attributes,
        Behavior behavior,
        Spawn spawn,
        Breeding breeding,
        Food food,
        @Nullable Loot loot,
        Voice voice
) {
    /**
     * 获取该物种关联的音效目录。
     * 自动根据 speciesId 路由到 SpeciesSoundCatalog 中的静态工厂。
     */
    public SpeciesSoundCatalog getSoundCatalog() {
        return switch (speciesId) {
            case "tiansuluo_pink_scarf" -> SpeciesSoundCatalog.pinkScarf();
            case "suxia" -> SpeciesSoundCatalog.suxia();
            default -> SpeciesSoundCatalog.tiansuluo();
        };
    }

    /**
     * 基础属性配置块。
     */
    public record Attributes(
            /** 【最大生命值】 实体的血量上限。建议值：10.0 - 40.0。 */
            double maxHealth,
            /** 【移动速度】 基础游荡速度。数值越大，移动越快。通常在 0.2 - 0.3 之间。 */
            double movementSpeed,
            /** 【追踪范围】 实体能察觉到目标（如玩家、食物）的最大直线距离。 */
            double followRange,
            /** 【基础攻击伤害】 近战或弹射物反击时的基本伤害值。 */
            double attackDamage
    ) {
        /**
         * 创建一个标准的基础属性配置（无攻击性）。
         */
        public static Attributes defaultAttributes() {
            return new Attributes(20.0, 0.25, 16.0, 0.0);
        }
    }

    /**
     * AI 行为与玩法参数配置块。
     */
    public record Behavior(
            /** 【诱惑速度】 玩家持有实体喜爱的食物时，实体的跟随速度倍率。建议值：1.1 - 1.3。 */
            double temptSpeed,
            /** 【通用 AI 速度】 执行寻路等基础任务时的速度倍率。 */
            double miscGoalSpeed,
            /** 【繁殖寻找速度】 寻找配偶时的移动速度倍率。 */
            double mateGoalSpeed,
            /** 【跟随父母速度】 幼年实体跟在成年实体身后的速度倍率。 */
            double followParentSpeed,
            /** 【常规散步速度】 无目标随机游荡时的速度倍率。 */
            double wanderSpeed,
            /** 【陆地避水游荡速度】 实地在陆地上且试图避开水时的移动速度。 */
            double waterAvoidingWanderSpeed,
            /** 【远程攻击 AI 速度】 执行 RangedAttackGoal 时的移动/定位速度。 */
            double projectileAttackGoalSpeed,
            /** 【反击记忆时长 (Ticks)】 受到攻击后，在多长时间内会持续攻击该目标。 */
            int retaliationMemoryTicks,
            /** 【反击连发数】 远程反击模式下，单次爆发射击发射的弹射物数量。 */
            int retaliationBurstShots,
            /** 【爆发射击子弹间隔 (Ticks)】 连发过程中，每发子弹之间的微小延迟。 */
            int retaliationBurstIntervalTicks,
            /** 【爆发射击大冷却 (Ticks)】 两次连发循环之间的强制冷却时间。 */
            int retaliationBurstCooldownTicks,
            /** 【远程反击最大范围】 触发远程射击逻辑的距离阈值。 */
            double retaliationRange,
            /** 【弹射物初速度】 弹射物飞行的快慢。 */
            float retaliationProjectileSpeed,
            /** 【弹射物散射度】 精度参数，数值越大射得越歪。0.0 为绝对精准。 */
            float retaliationProjectileDivergence,
            /** 【远程反击基础伤害】 远程模式下弹射物的额外伤害加成。 */
            double retaliationProjectileDamage,
            /** 【目标眼部偏移量】 修正射击落点，防止总是瞄准脚底。 */
            double retaliationTargetEyeOffset,
            /** 【攻击宣言时长 (Ticks)】 发射首枚子弹前，播放“攻击宣告”音效并锁定目标的等待时长。 */
            int attackDeclareDurationTicks,
            /** 【反击转向速度】 锁定目标进行射击时的身体旋转平滑度。 */
            double retaliationFaceTargetTurnSpeed,
            /** 【扑击水平速度】 战脸实体发动突袭时的水平冲锋速度。 */
            double pounceLeapHorizontalSpeed,
            /** 【扑击垂直速度】 战脸实体发动突袭时的起跳高度。 */
            double pounceLeapVerticalSpeed,
            /** 【饥饿惩罚 - 简单模式】 攻击玩家后扣除的饥饿值。 */
            int hungerDamageEasy,
            /** 【饥饿惩罚 - 普通模式】 攻击玩家后扣除的饥饿值。 */
            int hungerDamageNormal,
            /** 【饥饿惩罚 - 困难模式】 攻击玩家后扣除的饥饿值。 */
            int hungerDamageHard,
            /** 【弹射物发射口高度比例】 0.0 代表脚底，1.0 代表实体顶端。 */
            double projectileMuzzleHeightRatio,
            /** 【弹射物枪口前置距离】 发射点相对于实体几何中心的水平偏移（防止子弹在体内碰撞）。 */
            double projectileFrontOffset
    ) {
        /**
         * 创建一个“被动”行为配置，禁用所有反击、突袭和远程逻辑。
         * 适用于酥虾、以及不具备自卫能力的幼崽或小型天素罗变体。
         */
        public static Behavior passive() {
            return new Behavior(
                    1.2, 1.0, 1.1, 1.1, 1.0, 1.0, 1.0,
                    0, 0, 0, 0, 0.0, 0.0f, 0.0f, 0.0, 0.0,
                    0, 0.0, 0.0, 0.0, 0, 0, 0, 0.0, 0.0
            );
        }
    }

    /**
     * 环境生成配置块。
     */
    public record Spawn(
            /** 【启用自然生成】 如果关闭，则实体不会在世界中自动刷新。 */
            boolean enabled,
            /** 【生成权重】 与原生物种对比的生成频率。建议值：5 - 15。 */
            int weight,
            /** 【最小群组规模】 每次生成最少出现的数量。 */
            int minGroup,
            /** 【最大群组规模】 每次生成最多出现的数量。 */
            int maxGroup,
            /** 【生物群系 ID 列表】 支持特定的 ResourceLocation 或 # 前缀的 Tag（如 #minecraft:is_plains）。 */
            List<String> biomes
    ) {
        /**
         * 创建一个“不生成”配置。
         */
        public static Spawn none() {
            return new Spawn(false, 0, 0, 0, List.of());
        }
    }

    /**
     * 繁殖、产卵块与幼体发育逻辑配置块。
     */
    public record Breeding(
            /** 【使用产卵块逻辑】 如果为 true，则繁殖后会由实体放置一个特殊的卵方块，而非直接生成幼崽。 */
            boolean usesEggBlock,
            /** 【卵方块 ID】 注册名称，如 "ohyeah:luanluan_block"。 */
            String eggBlockId,
            /** 【关联实体 ID】 用于从卵中孵化出来的实体类型。 */
            String entityId,
            /** 【卵物品 ID】 采集卵方块后掉落的物品 ID。 */
            String eggItemId,
            /** 【显示携带粒子】 实体携带卵方块去寻找放置点时，是否显示心形粒子。 */
            boolean showCarriedParticles,
            /** 【交互提示 Key】 实体处于不同阶段时，发送给玩家的本地化翻译键。 */
            String carriedMessageKey,
            String hatchProgressMessageKey,
            String hatchedMessageKey,
            String brokenMessageKey,
            String placedMessageKey,
            /** 【孵化周期时长 (Ticks)】 卵方块每次状态改变所需的时长。 */
            int hatchStageTicks,
            /** 【幼体成长期 (Ticks)】 从幼崽长到成年所需的默认时长。默认 24000 (1个游戏日)。 */
            int babyGrowthAgeTicks,
            /** 【寻址范围】 携带卵方块时，寻找安全放置位置的搜索半径。 */
            int maxPlaceRange,
            /** 【产卵成功率】 放置成功的基础概率（0-100）。 */
            int spawnChance,
            /** 【产卵初始数量】 放置后卵方块内的默认卵数。 */
            int spawnCount
    ) {
        /**
         * 判定给定方块是否为本物种对应的产卵块。
         */
        public boolean isEggBlock(Block block) {
            return usesEggBlock && eggBlockId.equals(BuiltInRegistries.BLOCK.getKey(block).toString());
        }

        /**
         * 创建一个“无繁殖/无卵块”配置。
         */
        public static Breeding none() {
            return new Breeding(
                    false, "", "", "", false, 
                    "", "", "", "", "", 
                    0, 24000, 0, 0, 0
            );
        }
    }

    /**
     * 进食与诱惑判定逻辑块。
     */
    public record Food(
            /** 【喜爱物品列表】 用于诱惑（Tempt）和治疗实体的物品。 */
            List<String> likedItemIds,
            /** 【最爱物品列表】 用于触发繁殖模式（Love Mode）或使幼崽瞬间长大的物品（如蛋糕）。 */
            List<String> favoriteItemIds,
            /** 【喂食成长增量 (Ticks)】 每喂一次“喜爱物品”，缩短的成长期时长。 */
            int likedFoodGrowthStepTicks
    ) {
        public boolean isLiked(ItemStack stack) {
            return likedItemIds.contains(BuiltInRegistries.ITEM.getKey(stack.getItem()).toString());
        }
        public boolean isFavorite(ItemStack stack) {
            return favoriteItemIds.contains(BuiltInRegistries.ITEM.getKey(stack.getItem()).toString());
        }
        public boolean isAnyFood(ItemStack stack) {
            return isLiked(stack) || isFavorite(stack);
        }

        /**
         * 创建一个不吃任何东西的配置。
         */
        public static Food none() {
            return new Food(List.of(), List.of(), 0);
        }
    }

    /**
     * 战利品掉落配置块。
     */
    public record Loot(
            /** 【启用掉落】 */
            boolean enabled, 
            /** 【成年掉落物 ID 列表】 */
            List<String> adultDropItemIds
    ) {
        public static Loot none() {
            return new Loot(false, List.of());
        }
    }

    /**
     * 复杂的语音系统（音效网桥）参数块。
     */
    public record Voice(
            /** 【启用语音系统】 如果关闭，则实体将彻底失声。 */
            boolean enabled,
            /** 【受伤语音开关】 */
            boolean hurtEnabled,
            /** 【死亡语音开关】 */
            boolean deathEnabled,
            /** 【幼年语音开关】 */
            boolean babyEnabled,
            /** 【启用受管音效流水线】 如果为 true，音效将由模组的优先级队列管理，而非原生混音。 */
            boolean enablePipeline,
            /** 【原生音效回退】 是否允许在流水线之外播放 Minecraft 原生音效。 */
            boolean enableVanilla,
            /** 【密度限制器】 是否启用基于距离和数量的音效并发限制。 */
            boolean enableLimiter,
            /** 【闲置语音基础间隔 (Ticks)】 两次闲置叫声之间的最小等待时间。建议 > 6000。 */
            int ambientInterval,
            /** 【闲置语音随机偏移】 增加叫声的不确定性。 */
            int ambientRandomness,
            /** 【稀有闲置语音概率 (0-100)】 触发罕见叫声的几率。 */
            int rareAmbientChance,
            /** 【听众预算】 玩家周围允许同时存在的最大同类声音数。 */
            int listenerBudget,
            /** 【物种上限】 单个物种在全局音效池中的最大配额。 */
            int speciesAmbientCap,
            /** 【环境音拾取半径】 限制玩家能听到声音的最大距离。 */
            int ambientWindowRadius,
            /** 【语音时长覆盖】 特定 Cue 的强制持续时长映射（cue_key -> ticks）。 */
            Map<String, Integer> voiceOverrides,
            /** 【禁言交互规则】 定义哪些操作会导致实体被禁言（如 "shears" 表示剪刀）。 */
            Set<String> silenceRules,
            Map<String, Float> pitchOverrides,
            Map<String, Float> volumeOverrides,
            /** 【禁言通过名单】 即使在被禁言状态下也强制允许播放的声音（如：受伤）。 */
            Set<String> allowWhileSilenced,
            Map<String, String> variantTextureMap,
            Map<String, String> variantVoiceMap,
            /** 【主动同步名单】 哪些音效触发后必须立即告知服务端逻辑。 */
            Set<String> activeReportingCues,
            Map<String, Integer> cuePriorityMap,
            /** 【成人音效集】 允许播放的声音 key 集合。 */
            Set<String> allowedCues,
            /** 【幼体音效集】 幼崽允许播放的声音集合。 */
            Set<String> allowedBabyCues,
            /** 【原生音效名单】 哪些 Key 应该走原生的 playSound 逻辑。 */
            Set<String> vanillaCues,
            /** 【单实体最大并发声数】 防止单个实体叠buff式地发声。 */
            int maxSimultaneousVoices,
            /** 【重要度阈值】 只有高于此优先级的声音才会被播放。 */
            int importanceThreshold,
            /** 【遮挡半径】 计算声音衰减时的参考半径。 */
            int occlusionRadius,
            Map<String, String> cueToChannelMap,
            Map<String, String> cueToPolicyMap
    ) {
        /**
         * 判定在被禁言（如剪刀处理后）的情况下，是否允许播放指定音效。
         */
        public boolean allowsCueWhileSilenced(SoundCue cue) {
            return allowWhileSilenced.contains(cue.key()) || allowWhileSilenced.contains("allow:" + cue.key());
        }

        /**
         * 语音 Key 路由逻辑。
         * 目前暂时原样返回，预留给未来多变体/年龄段语音映射。
         */
        public SoundCue resolveCue(SoundCue cue, boolean isBaby) {
            return cue;
        }

        /**
         * 检查指定的音效是否在物种的允许播放名单中。
         */
        public boolean isCueDisabled(SoundCue cue) {
            return !enabled || (!allowedCues.contains(cue.key()) && !allowedBabyCues.contains(cue.key()));
        }

        public int intervalTicks(SoundCue cue) { return 20; }
        
        /**
         * 获取指定音效的播放时长（Ticks）。
         * 优先读取配置中的 voiceOverrides，未找到则回退至 SoundDefinition 的默认值。
         */
        public int durationTicks(SoundCue cue, int defaultTicks) { 
            return voiceOverrides.getOrDefault(cue.key(), defaultTicks); 
        }
        
        /**
         * 预留的 Definition 修改钩子。
         */
        public SoundDefinition overrideDefinition(SoundDefinition definition, SoundCue cue) { 
            return definition; 
        }

        /**
         * 创建一个“彻底失声”配置，彻底关闭语音系统的所有能力。
         */
        public static Voice silent() {
            return new Voice(
                    false, false, false, false, false, false, false,
                    6000, 0, 0, 0, 0, 0,
                    java.util.Collections.emptyMap(), java.util.Collections.emptySet(), 
                    java.util.Collections.emptyMap(), java.util.Collections.emptyMap(),
                    java.util.Collections.emptySet(), java.util.Collections.emptyMap(), java.util.Collections.emptyMap(),
                    java.util.Collections.emptySet(), java.util.Collections.emptyMap(), java.util.Collections.emptySet(), java.util.Collections.emptySet(),
                    java.util.Collections.emptySet(), 0, 0, 0, 
                    java.util.Collections.emptyMap(), java.util.Collections.emptyMap()
            );
        }
    }
}
