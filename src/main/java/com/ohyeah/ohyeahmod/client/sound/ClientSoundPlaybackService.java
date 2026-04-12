package com.ohyeah.ohyeahmod.client.sound;

import com.ohyeah.ohyeahmod.sound.definition.SoundBudgetClass;
import com.ohyeah.ohyeahmod.sound.definition.SoundInterruptMode;
import com.ohyeah.ohyeahmod.sound.definition.SoundSelectionMode;
import com.ohyeah.ohyeahmod.sound.network.EntitySoundPayload;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;

public final class ClientSoundPlaybackService {
    private static final Map<String, ActiveClientSound> ACTIVE_SOUNDS = new ConcurrentHashMap<>();
    private static final ClientSoundBudgetManager BUDGET_MANAGER = new ClientSoundBudgetManager();
    private static final SpeciesAmbientLimiter AMBIENT_LIMITER = new SpeciesAmbientLimiter();

    private ClientSoundPlaybackService() {
    }

    public static void play(EntitySoundPayload payload) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null || minecraft.player == null) {
            return;
        }
        Entity entity = minecraft.level.getEntity(payload.entityId());
        if (entity == null || entity.isRemoved()) {
            return;
        }
        if (payload.limiterEnabled()) {
            if (!BUDGET_MANAGER.canPlay(payload, entity, minecraft.player, ACTIVE_SOUNDS.values())) {
                return;
            }
            if (!AMBIENT_LIMITER.canPlay(payload, entity, minecraft.player, ACTIVE_SOUNDS.values())) {
                return;
            }
        }
        SoundEvent soundEvent = BuiltInRegistries.SOUND_EVENT.get(ResourceLocation.parse(payload.soundId()));
        if (soundEvent == null) {
            return;
        }
        SoundSource source = resolveSource(payload.soundSourceOrdinal());
        SoundManager manager = minecraft.getSoundManager();
        String key = payload.entityId() + ":" + payload.channel();
        ActiveClientSound previous = ACTIVE_SOUNDS.get(key);
        if (previous != null) {
            SoundInterruptMode mode = SoundInterruptMode.fromName(payload.interruptMode());
            if (mode == SoundInterruptMode.FORCE_REPLACE) {
                manager.stop(previous.instance());
                ACTIVE_SOUNDS.remove(key);
            } else if (mode == SoundInterruptMode.INTERRUPT_LOWER) {
                if (payload.priority() > previous.priorityRank()) {
                    manager.stop(previous.instance());
                    ACTIVE_SOUNDS.remove(key);
                } else {
                    return;
                }
            } else {
                return;
            }
        }
        SoundInstance instance = new TrackedEntitySoundInstance(soundEvent, source, payload.volume(), payload.pitch(), entity, payload.seed());
        manager.play(instance);
        ACTIVE_SOUNDS.put(key, new ActiveClientSound(
                payload.entityId(),
                payload.speciesId(),
                payload.cueName(),
                payload.channel(),
                payload.priority(),
                SoundBudgetClass.fromName(payload.budgetClass()),
                SoundSelectionMode.fromName(payload.selectionMode()),
                SoundInterruptMode.fromName(payload.interruptMode()),
                instance
        ));
    }

    public static void tick() {
        Minecraft minecraft = Minecraft.getInstance();
        SoundManager manager = minecraft.getSoundManager();
        ACTIVE_SOUNDS.entrySet().removeIf(entry -> shouldRemove(entry.getValue(), minecraft, manager));
    }

    private static boolean shouldRemove(ActiveClientSound sound, Minecraft minecraft, SoundManager manager) {
        if (minecraft.level == null || minecraft.level.getEntity(sound.entityId()) == null) {
            manager.stop(sound.instance());
            return true;
        }
        return !manager.isActive(sound.instance());
    }

    private static SoundSource resolveSource(int ordinal) {
        SoundSource[] values = SoundSource.values();
        if (ordinal < 0 || ordinal >= values.length) {
            return SoundSource.NEUTRAL;
        }
        return values[ordinal];
    }
}
