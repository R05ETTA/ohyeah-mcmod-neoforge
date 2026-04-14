package com.ohyeah.ohyeahmod.sound.bridge;

import com.ohyeah.ohyeahmod.sound.definition.SoundCue;
import com.ohyeah.ohyeahmod.sound.definition.SoundDefinition;
import com.ohyeah.ohyeahmod.sound.definition.SpeciesSoundCatalog;
import java.util.Set;
import java.util.Map;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

/**
 * 参与音效系统的实体接口。
 * 定义了音效路由、禁言逻辑及音效流水线参数。
 */
public interface SoundParticipant {
    default Entity soundEntity() {
        return (Entity) this;
    }

    String soundSpeciesId();

    SpeciesSoundCatalog soundCatalog();

    Set<String> playedSoundCues();

    default boolean soundIsBaby() {
        return this instanceof LivingEntity le && le.isBaby();
    }

    boolean isSoundSilenced(SoundCue cue);

    // --- 语音配置方法 ---

    boolean isVoiceEnabled();

    boolean isCueDisabled(SoundCue cue);

    boolean allowsCueWhileSilenced(SoundCue cue);

    boolean enablePipeline();

    boolean enableVanilla();

    Set<String> vanillaCues();

    boolean enableLimiter();

    int ambientIntervalTicks();

    int ambientRandomnessTicks();

    int rareAmbientChance();

    int intervalTicks(SoundCue cue);

    default int durationTicks(SoundCue cue, int defaultTicks) {
        return voiceOverrides().getOrDefault(cue.key(), defaultTicks);
    }

    Map<String, Integer> voiceOverrides();

    default SoundDefinition overrideDefinition(SoundDefinition definition, SoundCue cue) {
        return definition;
    }

    default SoundCue resolveCue(SoundCue cue, boolean isBaby) {
        return cue;
    }
}
