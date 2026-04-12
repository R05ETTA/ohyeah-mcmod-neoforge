package com.ohyeah.ohyeahmod.entity.logic;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;

public interface SleepWakeSpeciesHandler {
    String speciesId();

    boolean canSpawnAt(ServerPlayer player, BlockPos origin);

    void trySpawn(ServerPlayer player, BlockPos origin);
}
