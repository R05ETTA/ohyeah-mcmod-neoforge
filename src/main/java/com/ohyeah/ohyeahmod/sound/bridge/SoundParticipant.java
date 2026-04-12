package com.ohyeah.ohyeahmod.sound.bridge;

import com.ohyeah.ohyeahmod.config.SpeciesConfig;
import com.ohyeah.ohyeahmod.sound.definition.SoundCue;
import com.ohyeah.ohyeahmod.sound.definition.SpeciesSoundCatalog;
import java.util.Set;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

public interface SoundParticipant {
    default Entity soundEntity() {
        return (Entity) this;
    }

    String soundSpeciesId();

    SpeciesConfig.Voice soundVoiceConfig();

    SpeciesSoundCatalog soundCatalog();

    Set<String> playedSoundCues();

    default boolean soundIsBaby() {
        return this instanceof LivingEntity le && le.isBaby();
    }

    boolean isSoundSilenced(SoundCue cue);
}
