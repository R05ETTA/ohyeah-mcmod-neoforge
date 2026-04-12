package com.ohyeah.ohyeahmod.client.sound;

import com.ohyeah.ohyeahmod.sound.definition.SoundBudgetClass;
import com.ohyeah.ohyeahmod.sound.network.EntitySoundPayload;
import java.util.Collection;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.Entity;

public final class ClientSoundBudgetManager {
    public boolean canPlay(EntitySoundPayload payload, Entity entity, LocalPlayer player, Collection<ActiveClientSound> activeSounds) {
        SoundBudgetClass budgetClass = SoundBudgetClass.fromName(payload.budgetClass());
        if (budgetClass == SoundBudgetClass.NONE || payload.listenerBudget() <= 0) {
            return true;
        }
        long activeCount = activeSounds.stream()
                .filter(sound -> sound.budgetClass() == budgetClass)
                .count();
        if (activeCount < payload.listenerBudget()) {
            return true;
        }
        return entity.distanceToSqr(player) < 64.0D;
    }
}
