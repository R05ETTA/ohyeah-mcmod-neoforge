package com.ohyeah.ohyeahmod.sound.definition;

public enum SoundPriority {
    LOW(0),
    NORMAL(20),
    HIGH(60),
    CRITICAL(90),
    FORCE(120);

    private final int rank;

    SoundPriority(int rank) {
        this.rank = rank;
    }

    public int rank() {
        return this.rank;
    }

    public static SoundPriority fromRank(int rank) {
        SoundPriority selected = LOW;
        for (SoundPriority priority : values()) {
            if (rank >= priority.rank()) {
                selected = priority;
            }
        }
        return selected;
    }
}
