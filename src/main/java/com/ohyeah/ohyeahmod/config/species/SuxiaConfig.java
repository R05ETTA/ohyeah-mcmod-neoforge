package com.ohyeah.ohyeahmod.config.species;

import net.neoforged.neoforge.common.ModConfigSpec;

/**
 * 苏夏 (Suxia) 独立配置
 */
public final class SuxiaConfig {
    public static ModConfigSpec.IntValue SPAWN_WEIGHT;

    public static void init(ModConfigSpec.Builder builder) {
        builder.comment("苏夏 (Suxia) 配置").push("suxia");
        
        SPAWN_WEIGHT = builder.defineInRange("spawnWeight", 8, 0, 100);
        
        builder.pop();
    }
}
