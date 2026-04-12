package com.ohyeah.ohyeahmod.sound.definition;

import net.minecraft.sounds.SoundEvent;

public record SoundDefinition(
        SoundKey key,
        SoundEvent event,
        SoundChannel channel,
        SoundPriority priority,
        int defaultDurationTicks,
        boolean oneShot,
        SoundBudgetClass budgetClass,
        SoundSelectionMode selectionMode,
        SoundInterruptMode interruptMode
) {
}
