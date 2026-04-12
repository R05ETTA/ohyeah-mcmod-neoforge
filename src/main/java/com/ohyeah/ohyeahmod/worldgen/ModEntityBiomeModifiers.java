package com.ohyeah.ohyeahmod.worldgen;

import com.ohyeah.ohyeahmod.config.ModSpeciesConfigs;
import com.ohyeah.ohyeahmod.config.SpeciesConfig;
import java.util.List;
import java.util.stream.Collectors;

public final class ModEntityBiomeModifiers {
    
    private ModEntityBiomeModifiers() {
    }

    /**
     * 获取自然生成方案列表。
     * 使用方法调用而非静态常量，以防止在配置加载前触发类初始化。
     */
    public static List<NaturalSpawnPlan> naturalSpawns() {
        return List.of(
                NaturalSpawnPlan.forSpecies("tiansuluo_pink_scarf_spawn", ModSpeciesConfigs.getTiansuluoPinkScarf()),
                NaturalSpawnPlan.forSpecies("tiansuluo_battle_face_spawn", ModSpeciesConfigs.getTiansuluoBattleFace()),
                NaturalSpawnPlan.forSpecies("suxia_spawn", ModSpeciesConfigs.getSuxiaSpawn())
        );
    }

    public static String summary() {
        return naturalSpawns().stream()
                .map(NaturalSpawnPlan::toSummary)
                .collect(Collectors.joining("；"));
    }

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
