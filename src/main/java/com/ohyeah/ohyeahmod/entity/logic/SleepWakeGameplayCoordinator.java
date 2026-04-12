package com.ohyeah.ohyeahmod.entity.logic;

import com.ohyeah.ohyeahmod.config.ModConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class SleepWakeGameplayCoordinator {
    private static final Map<String, SleepWakeSpeciesHandler> HANDLERS = new LinkedHashMap<>();

    static {
        register(com.ohyeah.ohyeahmod.entity.tiansuluo.spawn.TiansuluoPinkScarfBedWakeSpawner.INSTANCE);
    }

    private SleepWakeGameplayCoordinator() {
    }

    public static void register(SleepWakeSpeciesHandler handler) {
        HANDLERS.put(handler.speciesId(), handler);
    }

    public static boolean shouldQueueSpawn(ServerPlayer player) {
        if (player == null || !ModConfig.SLEEP_WAKE_ENABLED.get()) {
            return false;
        }
        for (SleepWakeSpeciesHandler handler : getConfiguredHandlers()) {
            if (handler.shouldQueueSpawn(player)) {
                return true;
            }
        }
        return false;
    }

    public static void trySpawnAfterWake(ServerPlayer player, BlockPos bedPos) {
        if (player == null || bedPos == null || !ModConfig.SLEEP_WAKE_ENABLED.get()) {
            return;
        }

        List<SleepWakeSpeciesHandler> candidates = new ArrayList<>();
        for (SleepWakeSpeciesHandler handler : getConfiguredHandlers()) {
            if (handler.canSpawnAt(player, bedPos)) {
                candidates.add(handler);
            }
        }
        if (candidates.isEmpty()) {
            return;
        }

        SleepWakeSpeciesHandler selected = candidates.get(player.getRandom().nextInt(candidates.size()));
        selected.trySpawn(player, bedPos);
    }

    private static List<SleepWakeSpeciesHandler> getConfiguredHandlers() {
        List<SleepWakeSpeciesHandler> handlers = new ArrayList<>();
        List<? extends String> enabledSpecies = ModConfig.SLEEP_WAKE_SPECIES.get();
        for (String speciesId : enabledSpecies) {
            SleepWakeSpeciesHandler handler = HANDLERS.get(speciesId);
            if (handler != null) {
                handlers.add(handler);
            }
        }
        return handlers;
    }
}
