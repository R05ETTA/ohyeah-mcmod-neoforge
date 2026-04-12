package com.ohyeah.ohyeahmod.client.sound;

import com.ohyeah.ohyeahmod.sound.definition.SoundBudgetClass;
import com.ohyeah.ohyeahmod.sound.definition.SoundInterruptMode;
import com.ohyeah.ohyeahmod.sound.definition.SoundSelectionMode;
import net.minecraft.client.resources.sounds.SoundInstance;

public record ActiveClientSound(
        int entityId,
        String speciesId,
        String cueName,
        String channel,
        int priorityRank,
        SoundBudgetClass budgetClass,
        SoundSelectionMode selectionMode,
        SoundInterruptMode interruptMode,
        SoundInstance instance
) {
}
