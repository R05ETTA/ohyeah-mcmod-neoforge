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
    // --- 全局音效系统常量 (硬编码操作) ---
    public static final int LISTENER_BUDGET = 16;
    public static final int SPECIES_AMBIENT_CAP = 3;
    public static final int AMBIENT_WINDOW_RADIUS = 16;

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
                LISTENER_BUDGET,
                SPECIES_AMBIENT_CAP,
                AMBIENT_WINDOW_RADIUS
        );
        PacketDistributor.sendToPlayersTrackingChunk(serverLevel, participant.soundEntity().chunkPosition(), payload);
    }
}
