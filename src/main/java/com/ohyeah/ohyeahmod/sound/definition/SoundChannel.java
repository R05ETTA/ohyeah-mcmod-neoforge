package com.ohyeah.ohyeahmod.sound.definition;

public enum SoundChannel {
    AMBIENT,
    VOICE,
    ACTION,
    REACTION;

    public static SoundChannel fromName(String name) {
        for (SoundChannel channel : values()) {
            if (channel.name().equalsIgnoreCase(name)) {
                return channel;
            }
        }
        return VOICE;
    }
}
