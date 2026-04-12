package com.ohyeah.ohyeahmod.sound.bridge;

import com.ohyeah.ohyeahmod.config.SpeciesConfig;
import com.ohyeah.ohyeahmod.entity.tiansuluo.TiansuluoCoreComponent;
import com.ohyeah.ohyeahmod.sound.definition.SoundCue;
import com.ohyeah.ohyeahmod.sound.definition.SpeciesSoundCatalog;
import java.util.Set;
import java.util.Collections;
import net.minecraft.world.entity.Entity;

public interface SoundParticipant {
    default Entity soundEntity() {
        return (Entity) this;
    }

    default String soundSpeciesId() {
        return this instanceof TiansuluoCoreComponent.TiansuluoEntityInterface ti ? ti.getSpeciesConfig().speciesId() : "";
    }

    default SpeciesConfig.Voice soundVoiceConfig() {
        return this instanceof TiansuluoCoreComponent.TiansuluoEntityInterface ti ? ti.getSpeciesConfig().voice() : null;
    }

    SpeciesSoundCatalog soundCatalog();

    default Set<String> playedSoundCues() {
        return this instanceof TiansuluoCoreComponent.TiansuluoEntityInterface ti ? ti.getCore().getSoundSupport().playedSoundCues() : Collections.emptySet();
    }

    default boolean soundIsBaby() {
        return this instanceof net.minecraft.world.entity.LivingEntity le ? le.isBaby() : false;
    }

    default boolean isSoundSilenced(SoundCue cue) {
        return this instanceof TiansuluoCoreComponent.TiansuluoEntityInterface ti ? ti.getCore().isSoundSilenced() : false;
    }
}
