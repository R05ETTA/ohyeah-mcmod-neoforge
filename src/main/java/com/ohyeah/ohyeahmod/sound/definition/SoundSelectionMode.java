package com.ohyeah.ohyeahmod.sound.definition;

public enum SoundSelectionMode {
    DIRECT,
    SPECIES_LIMITED_AMBIENT;

    public static SoundSelectionMode fromName(String name) {
        for (SoundSelectionMode mode : values()) {
            if (mode.name().equalsIgnoreCase(name)) {
                return mode;
            }
        }
        return DIRECT;
    }
}
