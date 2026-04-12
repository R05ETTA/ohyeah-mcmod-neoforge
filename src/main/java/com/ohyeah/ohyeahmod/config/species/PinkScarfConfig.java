package com.ohyeah.ohyeahmod.config.species;

import net.neoforged.neoforge.common.ModConfigSpec;
import java.util.List;

/**
 * 天素罗 (粉围巾) 独立配置
 */
public final class PinkScarfConfig {
    public static ModConfigSpec.DoubleValue MAX_HEALTH;
    public static ModConfigSpec.DoubleValue MOVEMENT_SPEED;
    public static ModConfigSpec.DoubleValue ATTACK_DAMAGE;
    public static ModConfigSpec.DoubleValue TEMPT_SPEED;
    public static ModConfigSpec.IntValue RETALIATION_MEMORY;
    public static ModConfigSpec.IntValue SPAWN_WEIGHT;
    public static ModConfigSpec.ConfigValue<List<? extends String>> SPAWN_BIOMES;
    public static ModConfigSpec.ConfigValue<List<? extends String>> FOOD_LIKED;
    public static ModConfigSpec.ConfigValue<List<? extends String>> FOOD_FAVORITE;
    public static ModConfigSpec.IntValue AMBIENT_INTERVAL;
    public static ModConfigSpec.ConfigValue<List<? extends String>> VOICE_OVERRIDES;

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
        
        AMBIENT_INTERVAL = builder.comment("环境音效触发间隔(Ticks)")
                .defineInRange("ambientInterval", 6000, 20, 100000);
        
        VOICE_OVERRIDES = builder.comment("语音时长覆盖 (格式: 'cue_name:duration_ticks')")
                .defineList("voiceOverrides", 
                        List.of("ambient:60", "hurt:20"), 
                        o -> o instanceof String);
        
        builder.pop();
    }
}
