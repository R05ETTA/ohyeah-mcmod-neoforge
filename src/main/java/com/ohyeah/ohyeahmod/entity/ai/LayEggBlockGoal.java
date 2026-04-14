package com.ohyeah.ohyeahmod.entity.ai;

import com.ohyeah.ohyeahmod.entity.common.EggLayingSpecies;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.Vec3;

public class LayEggBlockGoal<T extends PathfinderMob & EggLayingSpecies> extends Goal {
    private final T tiansuluo;
    private final int hatchStageTicks;

    public LayEggBlockGoal(T tiansuluo, int hatchStageTicks) {
        this.tiansuluo = tiansuluo;
        this.hatchStageTicks = hatchStageTicks;
    }

    @Override
    public boolean canUse() {
        return this.tiansuluo.hasCarriedEggBlock() && !this.tiansuluo.isBaby();
    }

    @Override
    public void tick() {
        BlockPos target = this.tiansuluo.getCarriedEggBlockTargetPos();
        if (target == null) {
            target = this.tiansuluo.blockPosition().below();
            this.tiansuluo.setCarriedEggBlockTargetPos(target);
        }
        if (this.tiansuluo.distanceToSqr(Vec3.atCenterOf(target)) > 4.0D) {
            this.tiansuluo.getNavigation().moveTo(target.getX(), target.getY(), target.getZ(), 1.0D);
        } else {
            this.tiansuluo.setEggBlockPlacingCounter(this.tiansuluo.getEggBlockPlacingCounter() + 1);
            if (this.tiansuluo.getEggBlockPlacingCounter() > this.hatchStageTicks) {
                this.tiansuluo.setHasCarriedEggBlock(false);
            }
        }
    }
}
