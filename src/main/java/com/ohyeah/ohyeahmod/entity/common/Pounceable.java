package com.ohyeah.ohyeahmod.entity.common;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

/**
 * 可扑击生物接口。
 * 实现了此接口的实体，即可配合 PounceAttackGoal 施展基于物理抛物线的飞扑攻击。
 */
public interface Pounceable {
    
    // --- 状态判定 ---
    boolean isReadyToPounce();
    boolean isAttackCooldownReady();
    boolean isWithinPounceWindow(LivingEntity target);
    
    // --- 动作执行 ---
    void beginCharge();
    void faceRetaliationTarget(LivingEntity target);
    Vec3 getPounceAimPoint(LivingEntity target);
    void startCooldown();
    void finishSuccessfulRetaliation();

    // --- 物理参数配置 ---
    double getPounceHorizontalSpeed();
    double getPounceVerticalSpeed();
    int getPounceMaxFlightTicks();
    double getPounceHitboxPadding();
}
