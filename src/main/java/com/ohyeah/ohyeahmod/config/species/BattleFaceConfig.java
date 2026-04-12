package com.ohyeah.ohyeahmod.config.species;

import net.neoforged.neoforge.common.ModConfigSpec;
import java.util.List;

/**
 * 天素罗 (战斗脸) 独立配置
 */
public final class BattleFaceConfig {
    public static ModConfigSpec.DoubleValue MAX_HEALTH;
    public static ModConfigSpec.DoubleValue MOVEMENT_SPEED;
    public static ModConfigSpec.DoubleValue ATTACK_DAMAGE;
    public static ModConfigSpec.DoubleValue TEMPT_SPEED;
    public static ModConfigSpec.IntValue RETALIATION_MEMORY;
    public static ModConfigSpec.IntValue SPAWN_WEIGHT;
    public static ModConfigSpec.ConfigValue<List<? extends String>> SPAWN_BIOMES;

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
}
