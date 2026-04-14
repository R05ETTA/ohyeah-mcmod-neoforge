package com.ohyeah.ohyeahmod.sound.bridge;

import com.ohyeah.ohyeahmod.sound.definition.SoundCue;
import com.ohyeah.ohyeahmod.sound.emission.SoundEmissionService;
import com.ohyeah.ohyeahmod.sound.policy.EntitySoundContext;
import com.ohyeah.ohyeahmod.sound.policy.SoundPolicyRegistry;
import com.ohyeah.ohyeahmod.sound.queue.EntitySoundQueue;
import com.ohyeah.ohyeahmod.sound.queue.EntitySoundQueueRepository;
import com.ohyeah.ohyeahmod.sound.queue.SoundQueueDecision;
import com.ohyeah.ohyeahmod.sound.request.SoundDecision;
import com.ohyeah.ohyeahmod.sound.request.SoundRequest;
import com.ohyeah.ohyeahmod.sound.request.SoundRequestFactory;
import net.minecraft.sounds.SoundEvent;
import org.jetbrains.annotations.Nullable;

/**
 * 音效网桥门面类。
 * 负责协调实体、策略与队列，并最终执行播放动作。
 */
public final class SpeciesSoundFacade {
    private SpeciesSoundFacade() {
    }

    public static @Nullable SoundEvent resolveVanillaCue(SoundParticipant participant, SoundCue cue) {
        if (!participant.isVoiceEnabled()) return null;

        if (cue == SoundCue.AMBIENT) {
            int chance = participant.rareAmbientChance();
            if (chance > 0 && participant.soundEntity().getRandom().nextInt(100) < chance) {
                cue = SoundCue.RARE_CALL;
            }
        }
        
        SoundRequest request = SoundRequestFactory.create(participant, cue, 1.0F, 1.0F);
        SoundDecision decision = SoundPolicyRegistry.resolve(participant.soundSpeciesId()).decide(new EntitySoundContext(participant, request));
        if (!decision.allowed() || decision.definition() == null) {
            return null;
        }
        
        EntitySoundQueue queue = EntitySoundQueueRepository.get(participant.soundEntity());
        SoundQueueDecision queueDecision = queue.tryAccept(participant, decision, cue, request.worldTime());
        if (!queueDecision.accepted()) {
            return null;
        }
        
        if (decision.usePipeline()) {
            SoundEmissionService.emit(participant, decision, request);
            return null;
        }
        
        return decision.useVanilla() ? decision.definition().event() : null;
    }

    public static boolean playCue(SoundParticipant participant, SoundCue cue, float volume, float pitch) {
        if (!participant.isVoiceEnabled()) return false;

        if (cue == SoundCue.AMBIENT) {
            int chance = participant.rareAmbientChance();
            if (chance > 0 && participant.soundEntity().getRandom().nextInt(100) < chance) {
                cue = SoundCue.RARE_CALL;
            }
        }
        
        SoundRequest request = SoundRequestFactory.create(participant, cue, volume, pitch);
        SoundDecision decision = SoundPolicyRegistry.resolve(participant.soundSpeciesId()).decide(new EntitySoundContext(participant, request));
        if (!decision.allowed() || decision.definition() == null) {
            return false;
        }
        
        EntitySoundQueue queue = EntitySoundQueueRepository.get(participant.soundEntity());
        SoundQueueDecision queueDecision = queue.tryAccept(participant, decision, cue, request.worldTime());
        if (!queueDecision.accepted()) {
            return false;
        }
        
        if (decision.usePipeline()) {
            SoundEmissionService.emit(participant, decision, request);
            return true;
        }
        
        participant.soundEntity().playSound(decision.definition().event(), volume, pitch);
        return true;
    }

    public static void tick(SoundParticipant participant) {
        EntitySoundQueueRepository.get(participant.soundEntity())
                .clearFinished(participant.soundEntity().level().getGameTime());
    }
}
