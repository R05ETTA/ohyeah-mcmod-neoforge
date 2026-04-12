package com.ohyeah.ohyeahmod.sound.bridge;

import com.ohyeah.ohyeahmod.sound.definition.SoundCue;
import net.minecraft.sounds.SoundEvent;
import org.jetbrains.annotations.Nullable;

public final class SuxiaSoundBridge {
    private SuxiaSoundBridge() {
    }

    public static @Nullable SoundEvent resolveCue(SoundParticipant participant, SoundCue cue) {
        return VanillaSoundBridge.resolveCue(participant, cue);
    }
}
