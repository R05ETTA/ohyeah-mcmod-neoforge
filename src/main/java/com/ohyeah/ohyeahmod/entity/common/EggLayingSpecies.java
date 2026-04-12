package com.ohyeah.ohyeahmod.entity.common;

import com.ohyeah.ohyeahmod.entity.tiansuluo.TiansuluoCoreComponent;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.animal.Animal;
import org.jetbrains.annotations.Nullable;
import java.util.UUID;

public interface EggLayingSpecies {
    default boolean hasCarriedEggBlock() {
        return this instanceof TiansuluoCoreComponent.TiansuluoEntityInterface ti ? ti.getCore().hasCarriedEggBlock((Animal)this) : false;
    }

    default void setHasCarriedEggBlock(boolean hasCarriedEggBlock) {
        if (this instanceof TiansuluoCoreComponent.TiansuluoEntityInterface ti) ti.getCore().setHasCarriedEggBlock((Animal)this, hasCarriedEggBlock);
    }

    default @Nullable BlockPos getCarriedEggBlockTargetPos() {
        return getEggLayingSupport().getEggBlockTargetPos();
    }

    default void setCarriedEggBlockTargetPos(@Nullable BlockPos pos) {
        getEggLayingSupport().setEggBlockTargetPos(pos);
    }

    default int getEggBlockPlacingCounter() {
        return getEggLayingSupport().getEggBlockPlacingCounter();
    }

    default void setEggBlockPlacingCounter(int counter) {
        getEggLayingSupport().setEggBlockPlacingCounter(counter);
    }

    default @Nullable UUID getEggBlockAttractedPlayerUuid() {
        return getEggLayingSupport().getEggBlockPlayerUuid();
    }

    default void setEggBlockAttractedPlayerUuid(@Nullable UUID uuid) {
        getEggLayingSupport().setEggBlockPlayerUuid(uuid);
    }

    TiansuluoCoreComponent.EggLayingSupport getEggLayingSupport();
}
