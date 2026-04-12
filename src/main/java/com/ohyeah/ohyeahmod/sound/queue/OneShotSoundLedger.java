package com.ohyeah.ohyeahmod.sound.queue;

import com.ohyeah.ohyeahmod.sound.definition.SoundCue;
import java.util.Set;

public final class OneShotSoundLedger {
    private final Set<String> cues;

    public OneShotSoundLedger(Set<String> cues) {
        this.cues = cues;
    }

    public boolean hasPlayed(SoundCue cue) {
        return this.cues.contains(cue.name());
    }

    public void markPlayed(SoundCue cue) {
        this.cues.add(cue.name());
    }
}
