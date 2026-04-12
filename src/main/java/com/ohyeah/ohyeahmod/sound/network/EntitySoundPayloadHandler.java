package com.ohyeah.ohyeahmod.sound.network;

import com.ohyeah.ohyeahmod.client.sound.ClientSoundPlaybackService;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public final class EntitySoundPayloadHandler {
    private EntitySoundPayloadHandler() {
    }

    public static void handle(EntitySoundPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> ClientSoundPlaybackService.play(payload));
    }
}
