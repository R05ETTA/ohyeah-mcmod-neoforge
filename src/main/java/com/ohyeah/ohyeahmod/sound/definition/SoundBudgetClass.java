package com.ohyeah.ohyeahmod.sound.definition;

public enum SoundBudgetClass {
    NONE,
    CREATURE_AMBIENT,
    CREATURE_REACTION,
    CREATURE_ACTION;

    public static SoundBudgetClass fromName(String name) {
        for (SoundBudgetClass budgetClass : values()) {
            if (budgetClass.name().equalsIgnoreCase(name)) {
                return budgetClass;
            }
        }
        return NONE;
    }
}
