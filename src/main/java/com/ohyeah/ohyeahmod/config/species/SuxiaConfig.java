package com.ohyeah.ohyeahmod.config.species;

import com.ohyeah.ohyeahmod.config.ModConfig;
import com.ohyeah.ohyeahmod.config.SpeciesConfig;
import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * 酥虾独立配置 Provider。
 * 作为水生生物，其配置侧重于环境生成与基础生存属性。
 */
public final class SuxiaConfig {
    public static ModConfigSpec.IntValue SPAWN_WEIGHT;

    /**
     * 定义 TOML 路径。
     */
    public static void init(ModConfigSpec.Builder builder) {
        builder.comment("酥虾配置").push("suxia");
        SPAWN_WEIGHT = builder.defineInRange("spawnWeight", 8, 0, 100);
        builder.pop();
    }

    /**
     * 映射为静态 Record。
     * 由于其行为相对简单，大量 AI 字段使用了安全的默认值。
     */
    public static SpeciesConfig toRecord() {
        boolean loaded = ModConfig.COMMON_SPEC.isLoaded();

        return new SpeciesConfig(
                "suxia",
                "textures/entity/suxia.png",
                new SpeciesConfig.Attributes(10.0, 0.25, 16.0, 0.0),
                SpeciesConfig.Behavior.passive(),
                new SpeciesConfig.Spawn(
                        true,
                        loaded ? SPAWN_WEIGHT.get() : 8,
                        1, 2,
                        List.of("#minecraft:is_ocean", "#minecraft:is_river")
                ),
                SpeciesConfig.Breeding.none(),
                new SpeciesConfig.Food(
                        List.of("minecraft:seagrass", "minecraft:kelp"), 
                        List.of(), // 无最爱食物
                        6000 // growth ticks
                ),
                new SpeciesConfig.Loot(true, List.of("minecraft:cod")),
                SpeciesConfig.Voice.silent()
        );
    }
}
