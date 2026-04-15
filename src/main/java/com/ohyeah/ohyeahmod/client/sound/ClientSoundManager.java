package com.ohyeah.ohyeahmod.client.sound;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.EntityBoundSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import java.util.HashMap;
import java.util.Map;

/**
 * 客户端声音管理器 (主动注册版)。
 * 负责环境音的全局并发控制、静默冷却、以及动作音的打断。
 */
@OnlyIn(Dist.CLIENT)
public final class ClientSoundManager {
    // 跟踪每个实体当前活跃的声音实例
    private static final Map<Integer, SoundInstance> ACTIVE_SOUNDS = new HashMap<>();
    // 跟踪每个实体的个人发声计时器
    private static final Map<Integer, Integer> ENTITY_TIMERS = new HashMap<>();
    
    private static long lastAmbientEndTime = 0;
    private static boolean isAmbientOccupied = false;
    private static final long GLOBAL_COOLDOWN_MS = 2000; // 说话后的 2 秒绝对静默

    /**
     * 每一帧更新管理器状态。
     */
    public static void update() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;

        var iterator = ACTIVE_SOUNDS.entrySet().iterator();
        while (iterator.hasNext()) {
            var entry = iterator.next();
            SoundInstance instance = entry.getValue();
            
            // 如果声音已播放结束或实例无效
            if (!mc.getSoundManager().isActive(instance)) {
                if (instance.getSource() == SoundSource.AMBIENT) {
                    releaseAmbient();
                }
                iterator.remove();
            }
        }
    }

    /**
     * 实体的 Tick 钩子，由实体类主动调用。
     */
    public static void tick(LivingEntity entity, SoundEvent ambientSound) {
        if (ambientSound == null || !entity.isAlive()) return;

        int entityId = entity.getId();
        int timer = ENTITY_TIMERS.getOrDefault(entityId, 40); // 初始给 2 秒缓冲

        if (--timer <= 0) {
            // 尝试申请发言权
            if (canSpeak(entity)) {
                playAmbient(entity, ambientSound);
                // 成功后：设置长冷却 (10-20秒)
                ENTITY_TIMERS.put(entityId, 200 + entity.getRandom().nextInt(200));
            } else {
                // 失败后：设置短重试 (1秒)
                ENTITY_TIMERS.put(entityId, 20);
            }
        } else {
            ENTITY_TIMERS.put(entityId, timer);
        }
    }

    private static boolean canSpeak(LivingEntity entity) {
        // 1. 是否有人正在说背景音？
        if (isAmbientOccupied) return false;
        
        // 2. 是否处于全局静默冷却期？
        if (System.currentTimeMillis() < lastAmbientEndTime + GLOBAL_COOLDOWN_MS) return false;

        // 3. 距离校验 (24格)
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || entity.distanceToSqr(mc.player) > 576.0D) return false;

        return true;
    }

    private static void playAmbient(LivingEntity entity, SoundEvent sound) {
        stopSound(entity); // 确保同一个实体不重叠
        
        EntityBoundSoundInstance instance = new EntityBoundSoundInstance(
                sound, SoundSource.AMBIENT, 1.0F, 1.0F, entity, entity.level().getRandom().nextLong()
        );
        
        isAmbientOccupied = true;
        ACTIVE_SOUNDS.put(entity.getId(), instance);
        Minecraft.getInstance().getSoundManager().play(instance);
    }

    /**
     * 播放一次性动作反馈音 (如受伤、吃东西)。
     * 优先级最高，无视限流，并打断背景音。
     */
    public static void playAction(LivingEntity entity, SoundEvent sound, SoundSource source) {
        if (sound == null) return;
        stopSound(entity);
        
        EntityBoundSoundInstance instance = new EntityBoundSoundInstance(
                sound, source, 1.0F, 1.0F, entity, entity.level().getRandom().nextLong()
        );
        
        ACTIVE_SOUNDS.put(entity.getId(), instance);
        Minecraft.getInstance().getSoundManager().play(instance);
    }

    public static void stopSound(LivingEntity entity) {
        SoundInstance instance = ACTIVE_SOUNDS.remove(entity.getId());
        if (instance != null) {
            Minecraft.getInstance().getSoundManager().stop(instance);
            if (instance.getSource() == SoundSource.AMBIENT) {
                releaseAmbient();
            }
        }
    }

    private static void releaseAmbient() {
        if (isAmbientOccupied) {
            isAmbientOccupied = false;
            lastAmbientEndTime = System.currentTimeMillis();
        }
    }

    private ClientSoundManager() {}
}
