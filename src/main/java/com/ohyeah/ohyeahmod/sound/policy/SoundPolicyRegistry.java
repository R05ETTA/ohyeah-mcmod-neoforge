package com.ohyeah.ohyeahmod.sound.policy;

public final class SoundPolicyRegistry {
    private SoundPolicyRegistry() {
    }

    public static SpeciesSoundPolicy resolve(String speciesId) {
        if (speciesId.startsWith("tiansuluo")) {
            return TiansuluoSoundPolicy.INSTANCE;
        }
        if (speciesId.startsWith("suxia")) {
            return SuxiaSoundPolicy.INSTANCE;
        }
        return new BaseSpeciesSoundPolicy();
    }
}
