package com.ohyeah.ohyeahmod.client.sound;

import com.ohyeah.ohyeahmod.sound.definition.SoundSelectionMode;
import com.ohyeah.ohyeahmod.sound.network.EntitySoundPayload;
import java.util.Collection;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.Entity;

public final class SpeciesAmbientLimiter {
    public boolean canPlay(EntitySoundPayload payload, Entity entity, LocalPlayer player, Collection<ActiveClientSound> activeSounds) {
        if (SoundSelectionMode.fromName(payload.selectionMode()) != SoundSelectionMode.SPECIES_LIMITED_AMBIENT || payload.speciesAmbientCap() <= 0) {
            return true;
        }
        double windowSqr = (double) payload.ambientWindowRadius() * payload.ambientWindowRadius();
        long activeInWindow = activeSounds.stream()
                .filter(sound -> sound.speciesId().equals(payload.speciesId()))
                .filter(sound -> sound.cueName().equals(payload.cueName()))
                .filter(sound -> sound.selectionMode() == SoundSelectionMode.SPECIES_LIMITED_AMBIENT)
                .count();

        boolean alreadyPlaying = activeSounds.stream()
                .anyMatch(sound -> sound.entityId() == payload.entityId() && 
                                   sound.cueName().equals(payload.cueName()) &&
                                   sound.selectionMode() == SoundSelectionMode.SPECIES_LIMITED_AMBIENT);

        if (!alreadyPlaying && activeInWindow < payload.speciesAmbientCap()) {
            return entity.distanceToSqr(player) <= windowSqr;
        }
        return false;
    }
}
