package com.ohyeah.ohyeahmod.sound.queue;

import com.ohyeah.ohyeahmod.sound.definition.SoundChannel;
import com.ohyeah.ohyeahmod.sound.definition.SoundCue;
import com.ohyeah.ohyeahmod.sound.definition.SoundPriority;

public record ActiveSoundSlot(SoundCue cue, SoundChannel channel, SoundPriority priority, long expectedEndTick) {
}
