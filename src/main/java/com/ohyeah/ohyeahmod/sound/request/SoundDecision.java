package com.ohyeah.ohyeahmod.sound.request;

import com.ohyeah.ohyeahmod.sound.definition.SoundDefinition;
import org.jetbrains.annotations.Nullable;

public record SoundDecision(
        boolean allowed,
        boolean useVanilla,
        boolean usePipeline,
        boolean useLimiter,
        int intervalTicks,
        int durationTicks,
        @Nullable SoundDefinition definition
) {
    public static SoundDecision reject() {
        return new SoundDecision(false, false, false, false, 0, 0, null);
    }
}
