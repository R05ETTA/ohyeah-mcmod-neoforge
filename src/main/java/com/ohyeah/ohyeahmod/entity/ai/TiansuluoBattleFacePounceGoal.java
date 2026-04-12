package com.ohyeah.ohyeahmod.entity.ai;

import com.ohyeah.ohyeahmod.entity.TiansuluoBattleFaceEntity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

/**
 * 战斗脸天素罗的扑击 AI 逻辑。
 */
public final class TiansuluoBattleFacePounceGoal extends Goal {
    private static final int MAX_FLIGHT_TICKS = 12;
    private static final double HITBOX_PADDING = 0.2D;

    private final TiansuluoBattleFaceEntity entity;
    private LivingEntity committedTarget;
    private int flightTicksRemaining;
    private boolean launched;
    private boolean resolved;

    public TiansuluoBattleFacePounceGoal(TiansuluoBattleFaceEntity entity) {
        this.entity = entity;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK, Goal.Flag.JUMP));
    }

    @Override
    public boolean canUse() {
        // 这里需要适配 TiansuluoBattleFaceEntity 中的状态检查
        return entity.isAttackCooldownReady() && entity.onGround();
    }

    @Override
    public boolean canContinueToUse() {
        return !resolved && launched;
    }

    @Override
    public void start() {
        this.committedTarget = entity.getTarget();
        this.flightTicksRemaining = MAX_FLIGHT_TICKS;
        this.launched = false;
        this.resolved = false;
    }

    @Override
    public void stop() {
        this.committedTarget = null;
        this.flightTicksRemaining = 0;
        this.launched = false;
    }

    @Override
    public void tick() {
        LivingEntity target = entity.getTarget();
        if (target == null || target != this.committedTarget) {
            this.resolved = true;
            return;
        }

        if (!this.launched) {
            this.launchAt(target);
            return;
        }

        if (this.tryResolveHit(target)) {
            return;
        }

        this.flightTicksRemaining--;
        if (this.entity.onGround() || this.flightTicksRemaining <= 0) {
            this.resolved = true;
            this.entity.setAttackCooldown(100);
        }
    }

    private void launchAt(LivingEntity target) {
        Vec3 aimPoint = target.getBoundingBox().getCenter();
        Vec3 origin = this.entity.getBoundingBox().getCenter();
        Vec3 delta = aimPoint.subtract(origin);
        Vec3 horizontal = new Vec3(delta.x, 0.0D, delta.z);
        Vec3 horizontalDirection = horizontal.lengthSqr() > 1.0E-7D ? horizontal.normalize() : Vec3.ZERO;

        double horizontalSpeed = 1.15D; // 简化为固定值，或者从配置读取
        double verticalSpeed = 0.42D;

        this.entity.setDeltaMovement(horizontalDirection.x * horizontalSpeed, verticalSpeed, horizontalDirection.z * horizontalSpeed);
        this.entity.hasImpulse = true;
        this.launched = true;
    }

    private boolean tryResolveHit(LivingEntity target) {
        AABB hitBox = this.entity.getBoundingBox().inflate(HITBOX_PADDING);
        if (!hitBox.intersects(target.getBoundingBox().inflate(HITBOX_PADDING))) {
            return false;
        }

        this.resolved = true;
        if (this.entity.doHurtTarget(target)) {
            this.entity.setAttackCooldown(100);
        }
        return true;
    }
}
