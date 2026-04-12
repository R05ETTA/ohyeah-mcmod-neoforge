package com.ohyeah.ohyeahmod.sound.emission;

import com.ohyeah.ohyeahmod.sound.bridge.SoundParticipant;
import com.ohyeah.ohyeahmod.sound.network.EntitySoundPayload;
import com.ohyeah.ohyeahmod.sound.request.SoundDecision;
import com.ohyeah.ohyeahmod.sound.request.SoundRequest;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.neoforge.network.PacketDistributor;

public final class SoundEmissionService {
    private SoundEmissionService() {
    }

    public static void emit(SoundParticipant participant, SoundDecision decision, SoundRequest request) {
        if (!(participant.soundEntity().level() instanceof ServerLevel serverLevel) || decision.definition() == null) {
            return;
        }
        ResourceLocation soundId = BuiltInRegistries.SOUND_EVENT.getKey(decision.definition().event());
        if (soundId == null) {
            return;
        }
        EntitySoundPayload payload = new EntitySoundPayload(
                participant.soundEntity().getId(),
                participant.soundSpeciesId(),
                request.cue().name(),
                soundId.toString(),
                participant.soundEntity().getSoundSource().ordinal(),
                decision.definition().channel().name(),
                decision.definition().priority().rank(),
                decision.definition().budgetClass().name(),
                decision.definition().selectionMode().name(),
                decision.definition().interruptMode().name(),
                request.volume(),
                request.pitch(),
                participant.soundEntity().getRandom().nextLong(),
                decision.durationTicks(),
                decision.useLimiter(),
                participant.soundVoiceConfig().listenerBudget(),
                participant.soundVoiceConfig().speciesAmbientCap(),
                participant.soundVoiceConfig().ambientWindowRadius()
        );
        PacketDistributor.sendToPlayersTrackingChunk(serverLevel, participant.soundEntity().chunkPosition(), payload);
    }
}
