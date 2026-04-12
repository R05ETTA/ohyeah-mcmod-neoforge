package com.ohyeah.ohyeahmod.config.species;

import com.ohyeah.ohyeahmod.config.ModConfig;
import com.ohyeah.ohyeahmod.config.SpeciesConfig;
import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.List;

/**
 * 天素罗 (战斗脸) 独立配置 Provider。
 * 该物种倾向于近战突袭，拥有独特的游戏平衡参数。
 */
public final class BattleFaceConfig {
    public static ModConfigSpec.DoubleValue MAX_HEALTH;
    public static ModConfigSpec.DoubleValue MOVEMENT_SPEED;
    public static ModConfigSpec.DoubleValue ATTACK_DAMAGE;
    public static ModConfigSpec.DoubleValue TEMPT_SPEED;
    public static ModConfigSpec.IntValue RETALIATION_MEMORY;
    public static ModConfigSpec.IntValue SPAWN_WEIGHT;
    public static ModConfigSpec.ConfigValue<List<? extends String>> SPAWN_BIOMES;

    /**
     * 定义配置结构。
     */
    public static void init(ModConfigSpec.Builder builder) {
        builder.comment("天素罗 (战斗脸) 配置").push("battle_face");
        
        MAX_HEALTH = builder.defineInRange("maxHealth", 24.0, 1.0, 1024.0);
        MOVEMENT_SPEED = builder.defineInRange("movementSpeed", 0.28, 0.0, 1.0);
        ATTACK_DAMAGE = builder.defineInRange("attackDamage", 4.0, 0.0, 100.0);
        TEMPT_SPEED = builder.defineInRange("temptSpeed", 1.1, 0.5, 2.0);
        RETALIATION_MEMORY = builder.defineInRange("retaliationMemoryTicks", 100, 0, 12000);
        SPAWN_WEIGHT = builder.defineInRange("spawnWeight", 1, 0, 100);
        
        SPAWN_BIOMES = builder.defineList("spawnBiomes", 
                List.of("minecraft:plains", "minecraft:meadow"), 
                o -> o instanceof String);
        
        builder.pop();
    }

    /**
     * 映射为静态 Record。
     * 复用了粉围巾的部分通用逻辑（如进食、音效映射），体现了逻辑继承性。
     */
    public static SpeciesConfig toRecord() {
        boolean loaded = ModConfig.COMMON_SPEC.isLoaded();
        SpeciesConfig ps = PinkScarfConfig.toRecord();

        // 组装 Attributes
        SpeciesConfig.Attributes attributes = new SpeciesConfig.Attributes(
                loaded ? MAX_HEALTH.get() : 24.0D,
                loaded ? MOVEMENT_SPEED.get() : 0.28D,
                16.0D, // Follow Range
                loaded ? ATTACK_DAMAGE.get() : 4.0D
        );

        // 组装 Behavior (侧重于扑击 Pounce)
        SpeciesConfig.Behavior behavior = new SpeciesConfig.Behavior(
                loaded ? TEMPT_SPEED.get() : 1.1D,
                1.0D, // miscGoalSpeed
                1.1D, // mateGoalSpeed
                1.0D, // followParentSpeed
                1.0D, // wanderSpeed
                1.1D, // waterAvoidingWanderSpeed
                1.1D, // projectileAttackGoalSpeed (虽然是近战，但预留基础速度)
                loaded ? RETALIATION_MEMORY.get() : 100,
                1,    // retaliationBurstShots
                0,    // retaliationBurstIntervalTicks
                100,  // retaliationBurstCooldownTicks
                10.0D, // retaliationRange
                0.0F,  // retaliationProjectileSpeed
                0.0F,  // retaliationProjectileDivergence
                0.0D,  // retaliationProjectileDamage
                0.0D,  // retaliationTargetEyeOffset
                40,    // attackDeclareDurationTicks
                22.0D, // retaliationFaceTargetTurnSpeed
                3.0D,  // pounceLeapHorizontalSpeed (扑击冲力)
                1.15D, // pounceLeapVerticalSpeed (起跳高度)
                0, 5, 10, // hungerDamage (简单/普通/困难 - 困难模式扣10点)
                0.0D, 0.0D
        );

        // 组装 Spawn
        SpeciesConfig.Spawn spawn = new SpeciesConfig.Spawn(
                true,
                loaded ? SPAWN_WEIGHT.get() : 1,
                1, 1,
                loaded ? (List<String>) SPAWN_BIOMES.get() : List.of("minecraft:plains")
        );

        // 组装 Breeding
        SpeciesConfig.Breeding breeding = new SpeciesConfig.Breeding(
                true,
                "ohyeah:tiansuluo_battle_face_luanluan_block",
                "ohyeah:tiansuluo_battle_face",
                "ohyeah:tiansuluo_battle_face_egg",
                true,
                "message.ohyeah.tiansuluo_battle_face.luanluan_block_carried",
                "message.ohyeah.tiansuluo_battle_face.luanluan_block_hatch_progress",
                "message.ohyeah.tiansuluo_battle_face.luanluan_block_hatched",
                "message.ohyeah.tiansuluo_battle_face.luanluan_block_broken",
                "message.ohyeah.tiansuluo_battle_face.luanluan_block_placed",
                200, 24000, 8, 12, 1
        );

        return new SpeciesConfig(
                "tiansuluo_battle_face",
                "textures/entity/tiansuluo_battle_face.png",
                attributes, behavior, spawn, breeding, ps.food(),
                new SpeciesConfig.Loot(false, List.of()),
                ps.voice()
        );
    }
}
