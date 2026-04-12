package com.ohyeah.ohyeahmod.sound.policy;

import com.ohyeah.ohyeahmod.sound.request.SoundDecision;

public interface SpeciesSoundPolicy {
    SoundDecision decide(EntitySoundContext context);
}
