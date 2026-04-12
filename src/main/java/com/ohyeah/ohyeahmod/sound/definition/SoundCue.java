package com.ohyeah.ohyeahmod.sound.definition;

public enum SoundCue {
    AMBIENT,
    RARE_CALL,
    TEMPTED,
    EAT,
    EAT_FAVORITE,
    BREED_SUCCESS,
    CARRY_EGG,
    ATTACK_SHOT,
    ATTACK_END,
    HURT,
    ATTACK_DECLARE,
    DEATH,
    NOTICE_PLAYER,
    SPAWN,
    GROW_UP,
    SHEAR_REACT;

    public String key() {
        return this.name().toLowerCase();
    }

    public static SoundCue fromKey(String key) {
        for (SoundCue cue : values()) {
            if (cue.key().equalsIgnoreCase(key)) {
                return cue;
            }
        }
        throw new IllegalArgumentException("Unknown sound cue: " + key);
    }
}
