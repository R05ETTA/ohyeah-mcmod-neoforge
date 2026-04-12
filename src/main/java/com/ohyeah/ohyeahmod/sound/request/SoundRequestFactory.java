package com.ohyeah.ohyeahmod.sound.request;

import com.ohyeah.ohyeahmod.sound.bridge.SoundParticipant;
import com.ohyeah.ohyeahmod.sound.definition.SoundCue;

public final class SoundRequestFactory {
    private SoundRequestFactory() {
    }

    public static SoundRequest create(SoundParticipant participant, SoundCue cue, float volume, float pitch) {
        return new SoundRequest(
                participant.soundEntity(),
                participant.soundSpeciesId(),
                cue,
                volume,
                pitch,
                participant.soundEntity().level().getGameTime()
        );
    }
}
