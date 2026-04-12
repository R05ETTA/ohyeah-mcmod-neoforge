package com.ohyeah.ohyeahmod.config.species;

import com.ohyeah.ohyeahmod.config.ModConfig;
import com.ohyeah.ohyeahmod.config.SpeciesConfig;
import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 天素罗 (粉围巾) 独立配置 Provider。
 * 负责定义 TOML 结构并将动态配置值组装为 SpeciesConfig Record。
 */
public final class PinkScarfConfig {
    // --- 基础属性配置 ---
    public static ModConfigSpec.DoubleValue MAX_HEALTH;
    public static ModConfigSpec.DoubleValue MOVEMENT_SPEED;
    public static ModConfigSpec.DoubleValue ATTACK_DAMAGE;
    public static ModConfigSpec.DoubleValue TEMPT_SPEED;
    
    // --- 行为逻辑配置 ---
    public static ModConfigSpec.IntValue RETALIATION_MEMORY;
    
    // --- 生成规则配置 ---
    public static ModConfigSpec.IntValue SPAWN_WEIGHT;
    public static ModConfigSpec.ConfigValue<List<? extends String>> SPAWN_BIOMES;
    
    // --- 进食偏好配置 ---
    public static ModConfigSpec.ConfigValue<List<? extends String>> FOOD_LIKED;
    public static ModConfigSpec.ConfigValue<List<? extends String>> FOOD_FAVORITE;
    
    // --- 音效系统配置 ---
    public static ModConfigSpec.IntValue AMBIENT_INTERVAL;
    public static ModConfigSpec.ConfigValue<List<? extends String>> VOICE_OVERRIDES;

    /**
     * 初始化 TOML 路径与默认值。
     */
    public static void init(ModConfigSpec.Builder builder) {
        builder.comment("天素罗 (粉围巾) 配置").push("pink_scarf");
        
        MAX_HEALTH = builder.defineInRange("maxHealth", 20.0, 1.0, 1024.0);
        MOVEMENT_SPEED = builder.defineInRange("movementSpeed", 0.25, 0.0, 1.0);
        ATTACK_DAMAGE = builder.defineInRange("attackDamage", 3.0, 0.0, 100.0);
        TEMPT_SPEED = builder.defineInRange("temptSpeed", 1.15, 0.5, 2.0);
        RETALIATION_MEMORY = builder.defineInRange("retaliationMemoryTicks", 120, 0, 12000);
        SPAWN_WEIGHT = builder.defineInRange("spawnWeight", 10, 0, 100);
        
        SPAWN_BIOMES = builder.defineList("spawnBiomes", 
                List.of("minecraft:plains", "minecraft:meadow"), 
                o -> o instanceof String);
        
        FOOD_LIKED = builder.defineList("foodLiked", 
                List.of("minecraft:wheat", "minecraft:carrot", "minecraft:beetroot", "minecraft:potato"), 
                o -> o instanceof String);
        
        FOOD_FAVORITE = builder.defineList("foodFavorite", 
                List.of("minecraft:cake", "ohyeah:chips"), 
                o -> o instanceof String);
        
        AMBIENT_INTERVAL = builder.comment("环境音效触发基础间隔(Ticks)")
                .defineInRange("ambientInterval", 6000, 20, 100000);
        
        VOICE_OVERRIDES = builder.comment("语音时长覆盖映射 (格式: 'cue_name:duration_ticks')")
                .defineList("voiceOverrides", 
                        List.of("ambient:60", "hurt:20"), 
                        o -> o instanceof String);
        
        builder.pop();
    }

    /**
     * 将当前的动态配置值映射为静态 Record 对象。
     * 在组装过程中引入局部变量以提高构造函数的可读性。
     */
    public static SpeciesConfig toRecord() {
        boolean loaded = ModConfig.COMMON_SPEC.isLoaded();

        // 组装 Attributes
        SpeciesConfig.Attributes attributes = new SpeciesConfig.Attributes(
                loaded ? MAX_HEALTH.get() : 20.0D,
                loaded ? MOVEMENT_SPEED.get() : 0.25D,
                16.0D, // Follow Range 默认 16
                loaded ? ATTACK_DAMAGE.get() : 3.0D
        );

        // 组装 Behavior
        SpeciesConfig.Behavior behavior = new SpeciesConfig.Behavior(
                loaded ? TEMPT_SPEED.get() : 1.15D,
                1.0D, // miscGoalSpeed
                1.1D, // mateGoalSpeed
                1.0D, // followParentSpeed
                1.0D, // wanderSpeed
                1.1D, // waterAvoidingWanderSpeed
                1.15D, // projectileAttackGoalSpeed
                loaded ? RETALIATION_MEMORY.get() : 120,
                3,    // retaliationBurstShots
                10,   // retaliationBurstIntervalTicks
                30,   // retaliationBurstCooldownTicks
                12.0D, // retaliationRange
                3.0F,  // retaliationProjectileSpeed
                1.2F,  // retaliationProjectileDivergence
                5.0D,  // retaliationProjectileDamage
                0.25D, // retaliationTargetEyeOffset
                40,    // attackDeclareDurationTicks
                20.0D, // retaliationFaceTargetTurnSpeed
                0.0D,  // pounceLeapHorizontalSpeed (远程单位为0)
                0.0D,  // pounceLeapVerticalSpeed (远程单位为0)
                0, 0, 0, // hungerDamage (简单/普通/困难)
                0.15D, // projectileMuzzleHeightRatio
                0.65D  // projectileFrontOffset
        );

        // 组装 Spawn
        SpeciesConfig.Spawn spawn = new SpeciesConfig.Spawn(
                true,
                loaded ? SPAWN_WEIGHT.get() : 10,
                1, 3,
                loaded ? (List<String>) SPAWN_BIOMES.get() : List.of("minecraft:plains", "minecraft:meadow")
        );

        // 组装 Breeding
        SpeciesConfig.Breeding breeding = new SpeciesConfig.Breeding(
                true,
                "ohyeah:tiansuluo_pink_scarf_luanluan_block",
                "ohyeah:tiansuluo_pink_scarf",
                "ohyeah:tiansuluo_pink_scarf_egg",
                true,
                "message.ohyeah.tiansuluo_pink_scarf.luanluan_block_carried",
                "message.ohyeah.tiansuluo_pink_scarf.luanluan_block_hatch_progress",
                "message.ohyeah.tiansuluo_pink_scarf.luanluan_block_hatched",
                "message.ohyeah.tiansuluo_pink_scarf.luanluan_block_broken",
                "message.ohyeah.tiansuluo_pink_scarf.luanluan_block_placed",
                200, 24000, 8, 12, 1
        );

        // 组装 Food
        SpeciesConfig.Food food = new SpeciesConfig.Food(
                loaded ? (List<String>) FOOD_LIKED.get() : List.of("minecraft:wheat"),
                loaded ? (List<String>) FOOD_FAVORITE.get() : List.of("minecraft:cake"),
                6000 // growth ticks
        );

        // 组装 Voice
        SpeciesConfig.Voice voice = new SpeciesConfig.Voice(
                true, true, true, true, true, false, true,
                loaded ? AMBIENT_INTERVAL.get() : 6000,
                0, 5, 6, 3, 16,
                loaded ? parseStringListToMap(VOICE_OVERRIDES.get()) : Collections.emptyMap(),
                Collections.emptySet(), Map.of(), Map.of(),
                Set.of("mute_when_silenced", "allow:eat", "allow:eat_favorite", "allow:shear_react"),
                Map.of(), Map.of(), Set.of("notice_player", "spawn", "grow_up"),
                Map.of(), 
                Set.of("ambient", "tempted", "eat", "eat_favorite", "breed_success", "carry_egg", "attack_shot", "attack_end", "attack_declare", "notice_player", "spawn", "grow_up", "shear_react"),
                Set.of("ambient", "tempted", "hurt", "death"),
                Collections.emptySet(), 6, 3, 16, Map.of(), Map.of()
        );

        return new SpeciesConfig(
                "tiansuluo_pink_scarf",
                "textures/entity/tiansuluo_pink_scarf.png",
                attributes, behavior, spawn, breeding, food,
                new SpeciesConfig.Loot(true, List.of("ohyeah:chips")),
                voice
        );
    }

    private static Map<String, Integer> parseStringListToMap(List<? extends String> list) {
        Map<String, Integer> map = new LinkedHashMap<>();
        for (String entry : list) {
            String[] parts = entry.split(":");
            if (parts.length == 2) {
                try {
                    map.put(parts[0].trim(), Integer.parseInt(parts[1].trim()));
                } catch (NumberFormatException ignored) {}
            }
        }
        return map;
    }
}
