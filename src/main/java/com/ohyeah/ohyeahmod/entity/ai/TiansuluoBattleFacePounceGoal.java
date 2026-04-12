package com.ohyeah.ohyeahmod.entity.ai;

import com.ohyeah.ohyeahmod.entity.TiansuluoBattleFaceEntity;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

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
        LivingEntity target = this.entity.getTarget();
        return this.entity.isReadyToPounce()
                && this.entity.isAttackCooldownReady()
                && this.entity.onGround()
                && target != null
                && this.entity.isWithinPounceWindow(target);
    }

    @Override
    public boolean canContinueToUse() {
        return !this.resolved
                && this.entity.isReadyToPounce()
                && this.committedTarget != null
                && this.entity.getTarget() == this.committedTarget
                && this.launched;
    }

    @Override
    public void start() {
        this.committedTarget = this.entity.getTarget();
        this.flightTicksRemaining = MAX_FLIGHT_TICKS;
        this.launched = false;
        this.resolved = false;
        this.entity.beginCharge();
    }

    @Override
    public void stop() {
        this.committedTarget = null;
        this.flightTicksRemaining = 0;
        this.launched = false;
    }

    @Override
    public void tick() {
        LivingEntity target = this.entity.getTarget();
        if (target == null || target != this.committedTarget) {
            this.resolved = true;
            return;
        }

        this.entity.faceRetaliationTarget(target);

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
            this.entity.startCooldown();
        }
    }

    private void launchAt(LivingEntity target) {
        Vec3 aimPoint = this.entity.getPounceAimPoint(target);
        Vec3 origin = this.entity.getBoundingBox().getCenter();
        Vec3 delta = aimPoint.subtract(origin);
        Vec3 horizontal = new Vec3(delta.x, 0.0D, delta.z);
        Vec3 horizontalDirection = horizontal.lengthSqr() > 1.0E-7D ? horizontal.normalize() : Vec3.ZERO;

        double horizontalSpeed = Math.max(
                Math.max(this.entity.getSpeciesConfig().behavior().pounceLeapHorizontalSpeed(), 0.9D),
                Math.min(1.3D, horizontal.length() * 0.4D)
        );
        double verticalBase = Math.max(this.entity.getSpeciesConfig().behavior().pounceLeapVerticalSpeed(), 0.42D);
        double verticalOffset = Mth.clamp(delta.y * 0.25D, -0.18D, 0.3D);
        double verticalSpeed = Mth.clamp(verticalBase + verticalOffset, 0.24D, 0.95D);

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
            this.entity.finishSuccessfulRetaliation();
        } else {
            this.entity.startCooldown();
        }
        return true;
    }
}
