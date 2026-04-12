package com.ohyeah.ohyeahmod.sound.policy;

import com.ohyeah.ohyeahmod.sound.bridge.SoundParticipant;
import com.ohyeah.ohyeahmod.sound.request.SoundRequest;

public record EntitySoundContext(SoundParticipant participant, SoundRequest request) {
}
