package com.ohyeah.ohyeahmod.sound.queue;

public record SoundQueueDecision(boolean accepted, boolean replacedCurrent) {
    public static SoundQueueDecision reject() {
        return new SoundQueueDecision(false, false);
    }
}
