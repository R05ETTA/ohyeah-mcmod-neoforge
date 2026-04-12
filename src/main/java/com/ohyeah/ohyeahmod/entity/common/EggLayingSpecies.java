package com.ohyeah.ohyeahmod.entity.common;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public interface EggLayingSpecies {
    boolean hasCarriedEggBlock();

    void setHasCarriedEggBlock(boolean hasCarriedEggBlock);

    @Nullable BlockPos getCarriedEggBlockTargetPos();

    void setCarriedEggBlockTargetPos(@Nullable BlockPos pos);

    int getEggBlockPlacingCounter();

    void setEggBlockPlacingCounter(int counter);

    @Nullable UUID getEggBlockAttractedPlayerUuid();

    void setEggBlockAttractedPlayerUuid(@Nullable UUID uuid);

    default void setEggBlockAttractedPlayer(@Nullable Player player) {
        this.setEggBlockAttractedPlayerUuid(player == null ? null : player.getUUID());
    }
}
