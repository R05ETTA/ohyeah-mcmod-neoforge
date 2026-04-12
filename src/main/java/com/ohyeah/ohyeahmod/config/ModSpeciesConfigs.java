package com.ohyeah.ohyeahmod.config;

import com.ohyeah.ohyeahmod.config.species.BattleFaceConfig;
import com.ohyeah.ohyeahmod.config.species.PinkScarfConfig;
import com.ohyeah.ohyeahmod.config.species.SuxiaConfig;

/**
 * 物种配置中心注册表。
 * 现已重构为动态获取模式，确保在 NeoForge 配置加载完成后读取到最新的 TOML 数值。
 */
public final class ModSpeciesConfigs {

    private ModSpeciesConfigs() {
    }

    /**
     * 【天素罗 - 粉围巾】动态配置获取
     */
    public static SpeciesConfig pinkScarf() {
        return PinkScarfConfig.toRecord();
    }
    
    /**
     * 【天素罗 - 战斗脸】动态配置获取
     */
    public static SpeciesConfig battleFace() {
        return BattleFaceConfig.toRecord();
    }
    
    /**
     * 【酥虾】动态配置获取
     */
    public static SpeciesConfig suxia() {
        return SuxiaConfig.toRecord();
    }

    /**
     * 根据 ID 路由到对应的物种配置。
     * 如果未找到，将输出错误日志并回退到粉围巾配置。
     */
    public static SpeciesConfig get(String speciesId) {
        return switch (speciesId) {
            case "tiansuluo_pink_scarf" -> pinkScarf();
            case "tiansuluo_battle_face" -> battleFace();
            case "suxia" -> suxia();
            default -> {
                com.mojang.logging.LogUtils.getLogger().error("试图获取未知的物种配置 ID: [{}]. 请检查拼写或注册逻辑！已自动回退至粉围巾配置。", speciesId);
                yield pinkScarf();
            }
        };
    }
}
