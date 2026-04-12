package com.ohyeah.ohyeahmod.sound.definition;

import com.ohyeah.ohyeahmod.registry.ModSoundEvents;
import java.util.EnumMap;
import java.util.Map;
import org.jetbrains.annotations.Nullable;

/**
 * 声音目录管理类。
 * 采用懒加载模式，并补全所有关键语音映射。
 */
public final class SpeciesSoundCatalog {
    private final Map<SoundCue, SoundDefinition> definitions;

    private static SpeciesSoundCatalog tiansuluo;
    private static SpeciesSoundCatalog pinkScarf;
    private static SpeciesSoundCatalog suxia;

    public static SpeciesSoundCatalog tiansuluo() {
        if (tiansuluo == null) tiansuluo = createTiansuluo();
        return tiansuluo;
    }

    public static SpeciesSoundCatalog pinkScarf() {
        if (pinkScarf == null) pinkScarf = createPinkScarf();
        return pinkScarf;
    }

    public static SpeciesSoundCatalog suxia() {
        if (suxia == null) suxia = createSuxia();
        return suxia;
    }

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
        builder.define(SoundCue.RARE_CALL, simple(new SoundKey(sid, "rare_call"), ModSoundEvents.TIANSULUO_RARE_CALL.get()));
        builder.define(SoundCue.HURT, simple(new SoundKey(sid, "hurt"), ModSoundEvents.TIANSULUO_HURT.get()));
        builder.define(SoundCue.DEATH, simple(new SoundKey(sid, "death"), ModSoundEvents.TIANSULUO_DEATH.get()));
        builder.define(SoundCue.EAT, simple(new SoundKey(sid, "eat"), ModSoundEvents.TIANSULUO_EAT.get()));
        builder.define(SoundCue.EAT_FAVORITE, simple(new SoundKey(sid, "eat_favorite"), ModSoundEvents.TIANSULUO_EAT_FAVORITE.get()));
        builder.define(SoundCue.TEMPTED, simple(new SoundKey(sid, "tempted"), ModSoundEvents.TIANSULUO_TEMPTED.get()));
        builder.define(SoundCue.NOTICE_PLAYER, simple(new SoundKey(sid, "notice_player"), ModSoundEvents.TIANSULUO_NOTICE_PLAYER.get()));
        builder.define(SoundCue.BREED_SUCCESS, simple(new SoundKey(sid, "breed_success"), ModSoundEvents.TIANSULUO_BREED_SUCCESS.get()));
        builder.define(SoundCue.CARRY_EGG, simple(new SoundKey(sid, "carry_egg"), ModSoundEvents.TIANSULUO_CARRY_EGG.get()));
        builder.define(SoundCue.ATTACK_DECLARE, simple(new SoundKey(sid, "attack_declare"), ModSoundEvents.TIANSULUO_ATTACK_DECLARE.get()));
        builder.define(SoundCue.ATTACK_SHOT, simple(new SoundKey(sid, "attack_shot"), ModSoundEvents.TIANSULUO_ATTACK_SHOT.get()));
        builder.define(SoundCue.ATTACK_END, simple(new SoundKey(sid, "attack_end"), ModSoundEvents.TIANSULUO_ATTACK_END.get()));
        builder.define(SoundCue.GROW_UP, simple(new SoundKey(sid, "grow_up"), ModSoundEvents.TIANSULUO_GROW_UP.get()));
        builder.define(SoundCue.SHEAR_REACT, simple(new SoundKey(sid, "shear_react"), ModSoundEvents.TIANSULUO_SHEAR_REACT.get()));
        return builder.build();
    }

    private static SpeciesSoundCatalog createPinkScarf() {
        Builder builder = builder();
        String sid = "tiansuluo_pink_scarf";
        builder.define(SoundCue.AMBIENT, simple(new SoundKey(sid, "ambient"), ModSoundEvents.TIANSULUO_PS_AMBIENT.get()));
        builder.define(SoundCue.RARE_CALL, simple(new SoundKey(sid, "rare_call"), ModSoundEvents.TIANSULUO_PS_RARE_CALL.get()));
        builder.define(SoundCue.HURT, simple(new SoundKey(sid, "hurt"), ModSoundEvents.TIANSULUO_PS_HURT.get()));
        builder.define(SoundCue.DEATH, simple(new SoundKey(sid, "death"), ModSoundEvents.TIANSULUO_PS_DEATH.get()));
        builder.define(SoundCue.EAT, simple(new SoundKey(sid, "eat"), ModSoundEvents.TIANSULUO_PS_EAT.get()));
        builder.define(SoundCue.EAT_FAVORITE, simple(new SoundKey(sid, "eat_favorite"), ModSoundEvents.TIANSULUO_PS_EAT_FAVORITE.get()));
        builder.define(SoundCue.TEMPTED, simple(new SoundKey(sid, "tempted"), ModSoundEvents.TIANSULUO_PS_TEMPTED.get()));
        builder.define(SoundCue.NOTICE_PLAYER, simple(new SoundKey(sid, "notice_player"), ModSoundEvents.TIANSULUO_PS_NOTICE_PLAYER.get()));
        builder.define(SoundCue.BREED_SUCCESS, simple(new SoundKey(sid, "breed_success"), ModSoundEvents.TIANSULUO_PS_BREED_SUCCESS.get()));
        builder.define(SoundCue.CARRY_EGG, simple(new SoundKey(sid, "carry_egg"), ModSoundEvents.TIANSULUO_PS_CARRY_EGG.get()));
        builder.define(SoundCue.ATTACK_DECLARE, simple(new SoundKey(sid, "attack_declare"), ModSoundEvents.TIANSULUO_PS_ATTACK_DECLARE.get()));
        builder.define(SoundCue.ATTACK_SHOT, simple(new SoundKey(sid, "attack_shot"), ModSoundEvents.TIANSULUO_PS_ATTACK_SHOT.get()));
        builder.define(SoundCue.ATTACK_END, simple(new SoundKey(sid, "attack_end"), ModSoundEvents.TIANSULUO_PS_ATTACK_END.get()));
        builder.define(SoundCue.GROW_UP, simple(new SoundKey(sid, "grow_up"), ModSoundEvents.TIANSULUO_PS_GROW_UP.get()));
        builder.define(SoundCue.SHEAR_REACT, simple(new SoundKey(sid, "shear_react"), ModSoundEvents.TIANSULUO_PS_SHEAR_REACT.get()));
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
                key, event, SoundChannel.VOICE, SoundPriority.NORMAL, 40, false, 
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
