package com.ohyeah.ohyeahmod.config;

import com.ohyeah.ohyeahmod.config.species.BattleFaceConfig;
import com.ohyeah.ohyeahmod.config.species.PinkScarfConfig;
import com.ohyeah.ohyeahmod.config.species.SuxiaConfig;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 物种配置工厂 - 负责将 ModConfig 中的动态值映射到 SpeciesConfig Record 中
 */
public final class ModSpeciesConfigs {

    public static SpeciesConfig TIANSULUO_PINK_SCARF = getTiansuluoPinkScarf();
    public static SpeciesConfig TIANSULUO_BATTLE_FACE = getTiansuluoBattleFace();
    public static SpeciesConfig SUXIA_SPAWN = getSuxiaSpawn();
    public static SpeciesConfig SUXIA_VOICE = SUXIA_SPAWN;

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

    public static SpeciesConfig getTiansuluoPinkScarf() {
        boolean loaded = ModConfig.COMMON_SPEC.isLoaded();

        return new SpeciesConfig(
                "tiansuluo_pink_scarf",
                "textures/entity/tiansuluo_pink_scarf.png",
                new SpeciesConfig.Attributes(
                        loaded ? PinkScarfConfig.MAX_HEALTH.get() : 20.0D,
                        loaded ? PinkScarfConfig.MOVEMENT_SPEED.get() : 0.25D,
                        16.0D,
                        loaded ? PinkScarfConfig.ATTACK_DAMAGE.get() : 3.0D
                ),
                new SpeciesConfig.Behavior(
                        loaded ? PinkScarfConfig.TEMPT_SPEED.get() : 1.15D, 1.0D, 1.1D, 1.0D,
                        1.0D, 1.1D, 12.0D,
                        loaded ? PinkScarfConfig.RETALIATION_MEMORY.get() : 120, 3, 10, 30,
                        3.0F, 1.2F, 5.0F,
                        0.25D, 40, 20.0D,
                        0.0D, 0.0D, 0.0D,
                        0, 0, 0, 0,
                        0.15D, 0.65D
                ),
                new SpeciesConfig.Spawn(
                        true,
                        loaded ? PinkScarfConfig.SPAWN_WEIGHT.get() : 10,
                        1, 3,
                        loaded ? (List<String>) PinkScarfConfig.SPAWN_BIOMES.get() : List.of("minecraft:plains", "minecraft:meadow")
                ),
                new SpeciesConfig.Breeding(
                        true,
                        "ohyeah:tiansuluo_pink_scarf_luanluan_block",
                        "ohyeah:tiansuluo_pink_scarf",
                        "ohyeah:tiansuluo_pink_scarf_egg",
                        true,
                        "message.ohyeah.tiansuluo_pink_scarf.luanluan_block_hatch_progress",
                        "message.ohyeah.tiansuluo_pink_scarf.luanluan_block_hatched",
                        "message.ohyeah.tiansuluo_pink_scarf.luanluan_block_broken",
                        "message.ohyeah.tiansuluo_pink_scarf.luanluan_block_placed",
                        200,
                        24000,
                        8,
                        12,
                        1
                ),
                new SpeciesConfig.Food(
                        loaded ? (List<String>) PinkScarfConfig.FOOD_LIKED.get() : List.of("minecraft:wheat"),
                        loaded ? (List<String>) PinkScarfConfig.FOOD_FAVORITE.get() : List.of("minecraft:cake"),
                        6000
                ),
                new SpeciesConfig.Loot(true, List.of("ohyeah:chips")),
                new SpeciesConfig.Voice(
                        true, true, true, true,
                        loaded ? PinkScarfConfig.AMBIENT_INTERVAL.get() : 6000,
                        0,
                        loaded ? parseStringListToMap(PinkScarfConfig.VOICE_OVERRIDES.get()) : Collections.emptyMap(),
                        Set.of("rare_call"),
                        Map.of(),
                        Map.of(),
                        Set.of("mute_when_silenced", "allow:eat", "allow:eat_favorite", "allow:shear_react"),
                        Map.of(),
                        Map.of(),
                        Set.of("notice_player", "spawn", "grow_up"),
                        Map.of(),
                        Set.of("ambient", "tempted", "eat", "eat_favorite", "breed_success", "carry_egg", "attack_shot", "attack_end", "attack_declare", "notice_player", "spawn", "grow_up", "shear_react"),
                        Set.of("ambient", "tempted", "hurt", "death"),
                        6, 3, 16, Map.of(), Map.of()
                )
        );
    }

    public static SpeciesConfig getTiansuluoBattleFace() {
        boolean loaded = ModConfig.COMMON_SPEC.isLoaded();
        SpeciesConfig pinkScarf = getTiansuluoPinkScarf();
        return new SpeciesConfig(
                "tiansuluo_battle_face",
                "textures/entity/tiansuluo_battle_face.png",
                new SpeciesConfig.Attributes(
                        loaded ? BattleFaceConfig.MAX_HEALTH.get() : 24.0D,
                        loaded ? BattleFaceConfig.MOVEMENT_SPEED.get() : 0.28D,
                        16.0D,
                        loaded ? BattleFaceConfig.ATTACK_DAMAGE.get() : 4.0D
                ),
                new SpeciesConfig.Behavior(
                        loaded ? BattleFaceConfig.TEMPT_SPEED.get() : 1.1D, 1.0D, 1.1D, 1.0D,
                        1.0D, 1.0D, 10.0D,
                        loaded ? BattleFaceConfig.RETALIATION_MEMORY.get() : 100, 1, 0, 100,
                        0.0F, 0.0F, 0.0F,
                        0.0D, 40, 22.0D,
                        3.0D, 1.15D, 0.55D,
                        100, 1, 3, 5,
                        0.0D, 0.0D
                ),
                new SpeciesConfig.Spawn(
                        true,
                        loaded ? BattleFaceConfig.SPAWN_WEIGHT.get() : 1,
                        1, 1,
                        loaded ? (List<String>) BattleFaceConfig.SPAWN_BIOMES.get() : List.of("minecraft:plains")
                ),
                new SpeciesConfig.Breeding(
                        true,
                        "ohyeah:tiansuluo_battle_face_luanluan_block",
                        "ohyeah:tiansuluo_battle_face",
                        "ohyeah:tiansuluo_battle_face_egg",
                        true,
                        "message.ohyeah.tiansuluo_battle_face.luanluan_block_hatch_progress",
                        "message.ohyeah.tiansuluo_battle_face.luanluan_block_hatched",
                        "message.ohyeah.tiansuluo_battle_face.luanluan_block_broken",
                        "message.ohyeah.tiansuluo_battle_face.luanluan_block_placed",
                        200,
                        24000,
                        8,
                        12,
                        1
                ),
                pinkScarf.food(),
                new SpeciesConfig.Loot(false, List.of()),
                pinkScarf.voice()
        );
    }

    public static SpeciesConfig getSuxiaSpawn() {
        boolean loaded = ModConfig.COMMON_SPEC.isLoaded();
        return new SpeciesConfig(
                "suxia",
                "textures/entity/suxia.png",
                new SpeciesConfig.Attributes(10.0, 0.25, 16.0, 0.0),
                new SpeciesConfig.Behavior(
                        1.15, 1.0, 1.1, 1.0, 1.0, 1.0, 0.0,
                        0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                        0, 0, 0, 0, 0, 0, 0, 0, 0
                ),
                new SpeciesConfig.Spawn(true, loaded ? SuxiaConfig.SPAWN_WEIGHT.get() : 8, 1, 2, List.of("#minecraft:is_ocean", "#minecraft:is_river")),
                new SpeciesConfig.Breeding(
                        false, "", "ohyeah:suxia", "ohyeah:suxia_egg",
                        false, "", "", "", "", 0, 24000, 0, 0, 0
                ),
                new SpeciesConfig.Food(List.of("minecraft:seagrass", "minecraft:kelp"), List.of(), 6000),
                new SpeciesConfig.Loot(true, List.of("minecraft:cod")),
                new SpeciesConfig.Voice(
                        false, false, false, false,
                        6000, 0, Map.of(), Set.of(), Map.of(), Map.of(), Set.of(), Map.of(), Map.of(), Set.of(), Map.of(), Set.of(), Set.of(), 0, 0, 0, Map.of(), Map.of()
                )
        );
    }

    private ModSpeciesConfigs() {
    }

    public static SpeciesConfig get(String speciesId) {
        if ("tiansuluo_pink_scarf".equals(speciesId)) return getTiansuluoPinkScarf();
        if ("tiansuluo_battle_face".equals(speciesId)) return getTiansuluoBattleFace();
        if ("suxia".equals(speciesId)) return getSuxiaSpawn();
        return getTiansuluoPinkScarf();
    }
}
