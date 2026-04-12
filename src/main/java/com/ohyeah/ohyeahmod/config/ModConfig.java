package com.ohyeah.ohyeahmod.config;

import com.ohyeah.ohyeahmod.config.species.BattleFaceConfig;
import com.ohyeah.ohyeahmod.config.species.PinkScarfConfig;
import com.ohyeah.ohyeahmod.config.species.SuxiaConfig;
import net.neoforged.neoforge.common.ModConfigSpec;
import java.util.List;

public final class ModConfig {
    public static final ModConfigSpec COMMON_SPEC;
    
    // --- 全局游戏配置 (非物种特定) ---
    public static final ModConfigSpec.BooleanValue SLEEP_WAKE_ENABLED;
    public static final ModConfigSpec.IntValue SLEEP_WAKE_RADIUS;
    public static final ModConfigSpec.ConfigValue<List<? extends String>> SLEEP_WAKE_SPECIES;
    public static final ModConfigSpec.ConfigValue<List<? extends String>> SHEAR_MUTE_SPECIES;

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();

        builder.comment("全局游戏玩法设置").push("gameplay");
        SLEEP_WAKE_ENABLED = builder.comment("是否启用睡觉/唤醒联动").define("sleepWakeEnabled", true);
        SLEEP_WAKE_RADIUS = builder.comment("唤醒检测半径").defineInRange("sleepWakeRadius", 6, 1, 32);
        SLEEP_WAKE_SPECIES = builder.comment("受睡觉联动影响的物种ID列表").defineList("sleepWakeSpecies", List.of("tiansuluo_pink_scarf"), o -> o instanceof String);
        SHEAR_MUTE_SPECIES = builder.comment("剪毛后会静音的物种列表").defineList("shearMuteSpecies", List.of("tiansuluo_pink_scarf", "tiansuluo_battle_face"), o -> o instanceof String);
        builder.pop();

        // --- 调用分控配置初始化 ---
        PinkScarfConfig.init(builder);
        BattleFaceConfig.init(builder);
        SuxiaConfig.init(builder);

        COMMON_SPEC = builder.build();
    }
}
