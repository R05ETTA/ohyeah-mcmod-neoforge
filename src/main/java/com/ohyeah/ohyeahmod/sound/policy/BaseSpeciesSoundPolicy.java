package com.ohyeah.ohyeahmod.sound.policy;

import com.ohyeah.ohyeahmod.config.SpeciesConfig;
import com.ohyeah.ohyeahmod.sound.definition.SoundCue;
import com.ohyeah.ohyeahmod.sound.definition.SoundDefinition;
import com.ohyeah.ohyeahmod.sound.request.SoundDecision;
import com.ohyeah.ohyeahmod.sound.request.SoundRequest;
import org.jetbrains.annotations.Nullable;

public class BaseSpeciesSoundPolicy implements SpeciesSoundPolicy {
    @Override
    public SoundDecision decide(EntitySoundContext context) {
        SoundRequest request = context.request();
        SpeciesConfig.Voice config = context.participant().soundVoiceConfig();
        SoundCue cue = request.cue();
        @Nullable SoundDefinition definition = context.participant().soundCatalog().resolve(config.resolveCue(cue, context.participant().soundIsBaby()));
        if (definition == null || config.isCueDisabled(cue)) {
            return SoundDecision.reject();
        }
        if (context.participant().isSoundSilenced(cue) && !config.allowsCueWhileSilenced(cue)) {
            return SoundDecision.reject();
        }
        // Once a species enables pipeline, all of its resolved cues are managed by the pipeline.
        boolean usePipeline = config.enablePipeline();
        boolean useVanilla = !usePipeline && config.enableVanilla() && config.vanillaCues().contains(cue.key());
        return new SoundDecision(
                true,
                useVanilla,
                usePipeline,
                usePipeline && config.enableLimiter(),
                config.intervalTicks(cue),
                config.durationTicks(cue, definition.defaultDurationTicks()),
                config.overrideDefinition(definition, cue)
        );
    }
}
