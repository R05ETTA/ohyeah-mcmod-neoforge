package com.ohyeah.ohyeahmod.client.sound;

import net.minecraft.client.resources.sounds.EntityBoundSoundInstance;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;

public final class TrackedEntitySoundInstance extends EntityBoundSoundInstance {
    public TrackedEntitySoundInstance(SoundEvent soundEvent, SoundSource source, float volume, float pitch, Entity entity, long seed) {
        super(soundEvent, source, volume, pitch, entity, seed);
    }
}
