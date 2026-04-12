package com.ohyeah.ohyeahmod.sound.definition;

public enum SoundInterruptMode {
    NEVER_INTERRUPT,
    INTERRUPT_LOWER,
    FORCE_REPLACE;

    public static SoundInterruptMode fromName(String name) {
        for (SoundInterruptMode mode : values()) {
            if (mode.name().equalsIgnoreCase(name)) {
                return mode;
            }
        }
        return INTERRUPT_LOWER;
    }
}
