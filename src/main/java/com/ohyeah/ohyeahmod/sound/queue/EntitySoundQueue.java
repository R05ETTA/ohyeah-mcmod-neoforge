package com.ohyeah.ohyeahmod.sound.queue;

import com.ohyeah.ohyeahmod.sound.bridge.SoundParticipant;
import com.ohyeah.ohyeahmod.sound.definition.SoundCue;
import com.ohyeah.ohyeahmod.sound.request.SoundDecision;
import java.util.EnumMap;
import java.util.Map;

public final class EntitySoundQueue {
    private final Map<SoundCue, Long> lastEndByCue = new EnumMap<>(SoundCue.class);
    private final Map<com.ohyeah.ohyeahmod.sound.definition.SoundChannel, ActiveSoundSlot> activeByChannel = new EnumMap<>(com.ohyeah.ohyeahmod.sound.definition.SoundChannel.class);

    public SoundQueueDecision tryAccept(SoundParticipant participant, SoundDecision decision, SoundCue cue, long worldTime) {
        this.clearFinished(worldTime);
        OneShotSoundLedger ledger = new OneShotSoundLedger(participant.playedSoundCues());
        if (decision.definition() == null) {
            return SoundQueueDecision.reject();
        }
        if (decision.definition().oneShot() && ledger.hasPlayed(cue)) {
            return SoundQueueDecision.reject();
        }
        long lastEnd = this.lastEndByCue.getOrDefault(cue, Long.MIN_VALUE / 4);
        if (worldTime - lastEnd < decision.intervalTicks()) {
            return SoundQueueDecision.reject();
        }
        ActiveSoundSlot current = this.activeByChannel.get(decision.definition().channel());
        boolean replaced = false;
        if (current != null && current.expectedEndTick() > worldTime) {
            switch (decision.definition().interruptMode()) {
                case NEVER_INTERRUPT -> {
                    return SoundQueueDecision.reject();
                }
                case INTERRUPT_LOWER -> {
                    if (current.priority().rank() >= decision.definition().priority().rank()) {
                        return SoundQueueDecision.reject();
                    }
                    replaced = true;
                }
                case FORCE_REPLACE -> replaced = true;
            }
        }
        long expectedEndTick = worldTime + Math.max(0, decision.durationTicks());
        this.activeByChannel.put(decision.definition().channel(), new ActiveSoundSlot(cue, decision.definition().channel(), decision.definition().priority(), expectedEndTick));
        this.lastEndByCue.put(cue, expectedEndTick);
        if (decision.definition().oneShot()) {
            ledger.markPlayed(cue);
        }
        return new SoundQueueDecision(true, replaced);
    }

    public boolean isCueActive(SoundCue cue, long worldTime) {
        this.clearFinished(worldTime);
        return this.activeByChannel.values().stream().anyMatch(slot -> slot.cue() == cue);
    }

    public void clearFinished(long worldTime) {
        this.activeByChannel.entrySet().removeIf(entry -> entry.getValue().expectedEndTick() <= worldTime);
    }
}
