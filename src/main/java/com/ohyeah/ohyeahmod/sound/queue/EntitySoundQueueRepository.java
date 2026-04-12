package com.ohyeah.ohyeahmod.sound.queue;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.world.entity.Entity;

public final class EntitySoundQueueRepository {
    private static final Map<UUID, EntitySoundQueue> QUEUES = new ConcurrentHashMap<>();

    private EntitySoundQueueRepository() {
    }

    public static EntitySoundQueue get(Entity entity) {
        return QUEUES.computeIfAbsent(entity.getUUID(), ignored -> new EntitySoundQueue());
    }
}
