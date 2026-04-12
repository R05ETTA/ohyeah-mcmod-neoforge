package com.ohyeah.ohyeahmod.sound.request;

import com.ohyeah.ohyeahmod.sound.definition.SoundCue;
import net.minecraft.world.entity.Entity;

public record SoundRequest(
        Entity entity,
        String speciesId,
        SoundCue cue,
        float volume,
        float pitch,
        long worldTime
) {
}
