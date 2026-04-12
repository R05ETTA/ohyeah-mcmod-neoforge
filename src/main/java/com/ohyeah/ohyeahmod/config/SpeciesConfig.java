package com.ohyeah.ohyeahmod.config;

import com.ohyeah.ohyeahmod.sound.definition.SoundCue;
import com.ohyeah.ohyeahmod.sound.definition.SoundDefinition;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 物种配置全集 - 采用内部 Record 结构实现高度聚合
 */
public record SpeciesConfig(
        String speciesId,
        String texturePath,
        Attributes attributes,
        Behavior behavior,
        Spawn spawn,
        Breeding breeding,
        Food food,
        Loot loot,
        Voice voice
) {
    public record Attributes(double maxHealth, double movementSpeed, double followRange, double attackDamage) {}

    public record Behavior(
            double temptSpeed, double wanderSpeed, double followParentSpeed, double breedSpeed,
            double mateGoalSpeed, double projectileAttackGoalSpeed, double retaliationRange,
            int retaliationMemoryTicks, int retaliationBurstShots, int retaliationBurstIntervalTicks,
            int retaliationBurstCooldownTicks, float retaliationProjectileDamage, float retaliationProjectileSpeed,
            float retaliationProjectileDivergence, double retaliationTargetEyeOffset, int attackDeclareDurationTicks,
            double retaliationFaceTargetTurnSpeed, double pounceRange, double pounceLeapHorizontalSpeed,
            double pounceLeapVerticalSpeed, int pounceCooldownTicks, int hungerDamageEasy,
            int hungerDamageNormal, int hungerDamageHard, double projectileFrontOffset,
            double projectileMuzzleHeightRatio
    ) {}

    public record Spawn(boolean enabled, int weight, int minGroup, int maxGroup, List<String> biomes) {}

    public record Breeding(
            boolean usesEggBlock, String eggBlockId, String hatchEntityTypeId, String eggItemId,
            boolean showCarriedParticles, String hatchProgressMessageKey, String hatchedMessageKey,
            String brokenMessageKey, String placedMessageKey, int hatchStageTicks, int babyGrowthAgeTicks,
            int eggBlockSearchRadius, int eggBlockBreakChanceStepOn, int eggBlockBreakChanceLanded
    ) {}

    public record Food(List<String> likedItemIds, List<String> favoriteItemIds, int likedFoodGrowthStepTicks) {}

    public record Loot(boolean enabled, List<String> adultDropItemIds) {}

    public record Voice(
            boolean enableVanilla, boolean enableStrategy, boolean enablePipeline, boolean enableLimiter,
            int ambientIntervalTicks, int rareAmbientChance, Map<String, Integer> durationOverrides,
            Set<String> disabledCues, Map<String, String> babyCueOverrides, Map<String, String> adultCueOverrides,
            Set<String> silenceRules, Map<String, Integer> priorityOverrides, Map<String, String> channelOverrides,
            Set<String> oneShotCues, Map<String, String> interruptRules, Set<String> pipelineCues,
            Set<String> vanillaCues, int listenerBudget, int speciesAmbientCap, int ambientWindowRadius,
            Map<String, String> selectionModeOverrides, Map<String, Integer> intervalOverrides
    ) {
        public int durationTicks(SoundCue cue, int defaultTicks) {
            return durationOverrides.getOrDefault(cue.key(), defaultTicks);
        }

        public SoundCue resolveCue(SoundCue cue, boolean isBaby) {
            String overrideKey = isBaby ? babyCueOverrides.get(cue.key()) : adultCueOverrides.get(cue.key());
            if (overrideKey != null) {
                try {
                    return SoundCue.fromKey(overrideKey);
                } catch (IllegalArgumentException ignored) {}
            }
            return cue;
        }

        public boolean isCueDisabled(SoundCue cue) {
            return disabledCues.contains(cue.key());
        }

        public boolean allowsCueWhileSilenced(SoundCue cue) {
            return silenceRules.contains("allow:" + cue.key());
        }

        public int intervalTicks(SoundCue cue) {
            if (cue == SoundCue.AMBIENT) return ambientIntervalTicks;
            return intervalOverrides.getOrDefault(cue.key(), 20);
        }

        public SoundDefinition overrideDefinition(SoundDefinition definition, SoundCue cue) {
            // 这里可以实现根据配置修改 Definition 的逻辑，暂时原样返回
            return definition;
        }
    }
}
