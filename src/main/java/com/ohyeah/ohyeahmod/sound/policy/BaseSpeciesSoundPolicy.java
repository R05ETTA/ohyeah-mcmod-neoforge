package com.ohyeah.ohyeahmod.sound.policy;

import com.ohyeah.ohyeahmod.sound.bridge.SoundParticipant;
import com.ohyeah.ohyeahmod.sound.definition.SoundCue;
import com.ohyeah.ohyeahmod.sound.definition.SoundDefinition;
import com.ohyeah.ohyeahmod.sound.request.SoundDecision;
import com.ohyeah.ohyeahmod.sound.request.SoundRequest;
import org.jetbrains.annotations.Nullable;

/**
 * 基础物种音效策略。
 * 根据 SoundParticipant 提供的逻辑参数决定是否播放、如何播放。
 */
public class BaseSpeciesSoundPolicy implements SpeciesSoundPolicy {
    @Override
    public SoundDecision decide(EntitySoundContext context) {
        SoundRequest request = context.request();
        SoundParticipant participant = context.participant();
        SoundCue cue = request.cue();

        // 1. 基础开关检查
        if (!participant.isVoiceEnabled() || participant.isCueDisabled(cue)) {
            return SoundDecision.reject();
        }

        // 2. 禁言逻辑检查
        if (participant.isSoundSilenced(cue) && !participant.allowsCueWhileSilenced(cue)) {
            return SoundDecision.reject();
        }

        // 3. 解析音效定义
        SoundCue resolvedCue = participant.resolveCue(cue, participant.soundIsBaby());
        @Nullable SoundDefinition definition = participant.soundCatalog().resolve(resolvedCue);
        if (definition == null) {
            return SoundDecision.reject();
        }

        // 4. 构建决策
        boolean usePipeline = participant.enablePipeline();
        boolean useVanilla = !usePipeline && participant.enableVanilla() && participant.vanillaCues().contains(cue.key());

        return new SoundDecision(
                true,
                useVanilla,
                usePipeline,
                usePipeline && participant.enableLimiter(),
                participant.intervalTicks(cue),
                participant.durationTicks(cue, definition.defaultDurationTicks()),
                participant.overrideDefinition(definition, cue)
        );
    }
}
