package com.ohyeah.ohyeahmod.entity.logic;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;

/**
 * 睡眠唤醒生成处理器的接口标准。
 */
public interface SleepWakeSpeciesHandler {
    /**
     * 该生成器对应的物种 ID。
     */
    String speciesId();

    /**
     * 该生成器是否处于启用状态。
     */
    boolean isEnabled();

    /**
     * 是否满足排队生成的初步条件。
     */
    boolean shouldQueueSpawn(ServerPlayer player);

    /**
     * 目标床位是否满足物理生成条件。
     */
    boolean canSpawnAt(ServerPlayer player, BlockPos bedPos);

    /**
     * 尝试在床边生成实体。
     */
    void trySpawn(ServerPlayer player, BlockPos bedPos);
}
