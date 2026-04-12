package com.ohyeah.ohyeahmod.sound.bridge;

import com.ohyeah.ohyeahmod.sound.definition.SoundCue;
import net.minecraft.sounds.SoundEvent;
import org.jetbrains.annotations.Nullable;

public final class VanillaSoundBridge {
    private VanillaSoundBridge() {
    }

    public static @Nullable SoundEvent resolveCue(SoundParticipant participant, SoundCue cue) {
        return SpeciesSoundFacade.resolveVanillaCue(participant, cue);
    }
}
