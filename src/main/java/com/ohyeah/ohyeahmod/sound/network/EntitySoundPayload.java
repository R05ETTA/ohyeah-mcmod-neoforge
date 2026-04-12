package com.ohyeah.ohyeahmod.sound.network;

import com.ohyeah.ohyeahmod.OhYeah;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record EntitySoundPayload(
        int entityId,
        String speciesId,
        String cueName,
        String soundId,
        int soundSourceOrdinal,
        String channel,
        int priority,
        String budgetClass,
        String selectionMode,
        String interruptMode,
        float volume,
        float pitch,
        long seed,
        int durationTicks,
        boolean limiterEnabled,
        int listenerBudget,
        int speciesAmbientCap,
        int ambientWindowRadius
) implements CustomPacketPayload {
    public static final Type<EntitySoundPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(OhYeah.MODID, "entity_sound"));
    public static final StreamCodec<ByteBuf, EntitySoundPayload> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public EntitySoundPayload decode(ByteBuf buffer) {
            return new EntitySoundPayload(
                    ByteBufCodecs.INT.decode(buffer),
                    ByteBufCodecs.STRING_UTF8.decode(buffer),
                    ByteBufCodecs.STRING_UTF8.decode(buffer),
                    ByteBufCodecs.STRING_UTF8.decode(buffer),
                    ByteBufCodecs.INT.decode(buffer),
                    ByteBufCodecs.STRING_UTF8.decode(buffer),
                    ByteBufCodecs.INT.decode(buffer),
                    ByteBufCodecs.STRING_UTF8.decode(buffer),
                    ByteBufCodecs.STRING_UTF8.decode(buffer),
                    ByteBufCodecs.STRING_UTF8.decode(buffer),
                    ByteBufCodecs.FLOAT.decode(buffer),
                    ByteBufCodecs.FLOAT.decode(buffer),
                    ByteBufCodecs.VAR_LONG.decode(buffer),
                    ByteBufCodecs.INT.decode(buffer),
                    ByteBufCodecs.BOOL.decode(buffer),
                    ByteBufCodecs.INT.decode(buffer),
                    ByteBufCodecs.INT.decode(buffer),
                    ByteBufCodecs.INT.decode(buffer)
            );
        }

        @Override
        public void encode(ByteBuf buffer, EntitySoundPayload value) {
            ByteBufCodecs.INT.encode(buffer, value.entityId());
            ByteBufCodecs.STRING_UTF8.encode(buffer, value.speciesId());
            ByteBufCodecs.STRING_UTF8.encode(buffer, value.cueName());
            ByteBufCodecs.STRING_UTF8.encode(buffer, value.soundId());
            ByteBufCodecs.INT.encode(buffer, value.soundSourceOrdinal());
            ByteBufCodecs.STRING_UTF8.encode(buffer, value.channel());
            ByteBufCodecs.INT.encode(buffer, value.priority());
            ByteBufCodecs.STRING_UTF8.encode(buffer, value.budgetClass());
            ByteBufCodecs.STRING_UTF8.encode(buffer, value.selectionMode());
            ByteBufCodecs.STRING_UTF8.encode(buffer, value.interruptMode());
            ByteBufCodecs.FLOAT.encode(buffer, value.volume());
            ByteBufCodecs.FLOAT.encode(buffer, value.pitch());
            ByteBufCodecs.VAR_LONG.encode(buffer, value.seed());
            ByteBufCodecs.INT.encode(buffer, value.durationTicks());
            ByteBufCodecs.BOOL.encode(buffer, value.limiterEnabled());
            ByteBufCodecs.INT.encode(buffer, value.listenerBudget());
            ByteBufCodecs.INT.encode(buffer, value.speciesAmbientCap());
            ByteBufCodecs.INT.encode(buffer, value.ambientWindowRadius());
        }
    };

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
