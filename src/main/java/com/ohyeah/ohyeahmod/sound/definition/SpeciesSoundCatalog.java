package com.ohyeah.ohyeahmod.sound.definition;

import com.ohyeah.ohyeahmod.registry.ModSoundEvents;
import java.util.EnumMap;
import java.util.Map;
import org.jetbrains.annotations.Nullable;

public final class SpeciesSoundCatalog {
    private final Map<SoundCue, SoundDefinition> definitions;

    public static final SpeciesSoundCatalog TIANSULUO = createTiansuluo();
    public static final SpeciesSoundCatalog PINK_SCARF = createPinkScarf();
    public static final SpeciesSoundCatalog SUXIA = createSuxia();

    private SpeciesSoundCatalog(Map<SoundCue, SoundDefinition> definitions) {
        this.definitions = Map.copyOf(definitions);
    }

    public @Nullable SoundDefinition resolve(SoundCue cue) {
        return this.definitions.get(cue);
    }

    private static SpeciesSoundCatalog createTiansuluo() {
        Builder builder = builder();
        String sid = "tiansuluo";
        builder.define(SoundCue.AMBIENT, simple(new SoundKey(sid, "ambient"), ModSoundEvents.TIANSULUO_AMBIENT.get()));
        builder.define(SoundCue.HURT, simple(new SoundKey(sid, "hurt"), ModSoundEvents.TIANSULUO_HURT.get()));
        builder.define(SoundCue.DEATH, simple(new SoundKey(sid, "death"), ModSoundEvents.TIANSULUO_DEATH.get()));
        return builder.build();
    }

    private static SpeciesSoundCatalog createPinkScarf() {
        Builder builder = builder();
        String sid = "tiansuluo_pink_scarf";
        builder.define(SoundCue.AMBIENT, simple(new SoundKey(sid, "ambient"), ModSoundEvents.TIANSULUO_PS_AMBIENT.get()));
        builder.define(SoundCue.HURT, simple(new SoundKey(sid, "hurt"), ModSoundEvents.TIANSULUO_PS_HURT.get()));
        builder.define(SoundCue.DEATH, simple(new SoundKey(sid, "death"), ModSoundEvents.TIANSULUO_PS_DEATH.get()));
        return builder.build();
    }

    private static SpeciesSoundCatalog createSuxia() {
        Builder builder = builder();
        String sid = "suxia";
        builder.define(SoundCue.AMBIENT, simple(new SoundKey(sid, "ambient"), ModSoundEvents.SUXIA_AMBIENT.get()));
        builder.define(SoundCue.HURT, simple(new SoundKey(sid, "hurt"), ModSoundEvents.SUXIA_HURT.get()));
        builder.define(SoundCue.DEATH, simple(new SoundKey(sid, "death"), ModSoundEvents.SUXIA_DEATH.get()));
        return builder.build();
    }

    private static SoundDefinition simple(SoundKey key, net.minecraft.sounds.SoundEvent event) {
        return new SoundDefinition(
                key, event, SoundChannel.VOICE, SoundPriority.NORMAL, 20, false, 
                SoundBudgetClass.CREATURE_AMBIENT, SoundSelectionMode.DIRECT, SoundInterruptMode.NEVER_INTERRUPT);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private final EnumMap<SoundCue, SoundDefinition> map = new EnumMap<>(SoundCue.class);

        public Builder define(SoundCue cue, SoundDefinition definition) {
            this.map.put(cue, definition);
            return this;
        }

        public SpeciesSoundCatalog build() {
            return new SpeciesSoundCatalog(this.map);
        }
    }
}
