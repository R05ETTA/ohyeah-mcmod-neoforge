package com.ohyeah.ohyeahmod.worldgen;

import com.ohyeah.ohyeahmod.config.ModSpeciesConfigs;
import com.ohyeah.ohyeahmod.config.SpeciesConfig;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 模组实体生物群系生成修正器。
 * 负责定义各物种在世界中的自然分布方案。
 */
public final class ModEntityBiomeModifiers {
    
    private ModEntityBiomeModifiers() {
    }

    /**
     * 获取当前生效的自然生成方案列表。
     * 采用动态方法获取配置，确保其值与 TOML 配置文件同步。
     */
    public static List<NaturalSpawnPlan> naturalSpawns() {
        return List.of(
                NaturalSpawnPlan.forSpecies("tiansuluo_pink_scarf_spawn", ModSpeciesConfigs.pinkScarf()),
                NaturalSpawnPlan.forSpecies("tiansuluo_battle_face_spawn", ModSpeciesConfigs.battleFace()),
                NaturalSpawnPlan.forSpecies("suxia_spawn", ModSpeciesConfigs.suxia())
        );
    }

    /**
     * 获取当前生成方案的简要摘要，用于日志输出。
     */
    public static String summary() {
        return naturalSpawns().stream()
                .map(NaturalSpawnPlan::toSummary)
                .collect(Collectors.joining("；"));
    }

    /**
     * 内部记录类：定义单个物种的生成计划。
     */
    public record NaturalSpawnPlan(String resourceName, String entityId, SpeciesConfig.Spawn spawnConfig) {
        public static NaturalSpawnPlan forSpecies(String resourceName, SpeciesConfig species) {
            return new NaturalSpawnPlan(resourceName, species.speciesId(), species.spawn());
        }

        private String toSummary() {
            return resourceName
                    + " -> "
                    + entityId
                    + "（权重 "
                    + spawnConfig.weight()
                    + "，群组 "
                    + spawnConfig.minGroup()
                    + "-"
                    + spawnConfig.maxGroup()
                    + "，生物群系 "
                    + String.join(", ", spawnConfig.biomes())
                    + "）";
        }
    }
}
