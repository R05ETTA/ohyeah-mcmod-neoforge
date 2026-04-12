package com.ohyeah.ohyeahmod.entity.logic;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;

public interface SleepWakeSpeciesHandler {
    String speciesId();

    boolean shouldQueueSpawn(ServerPlayer player);

    boolean canSpawnAt(ServerPlayer player, BlockPos bedPos);

    void trySpawn(ServerPlayer player, BlockPos bedPos);
}
