package com.ohyeah.ohyeahmod.registry;

import com.ohyeah.ohyeahmod.OhYeah;
import com.ohyeah.ohyeahmod.block.LuanluanEggBlock;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModBlocks {
    private static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(OhYeah.MODID);

    public static final DeferredBlock<LuanluanEggBlock> TIANSULUO_PINK_SCARF_LUANLUAN_BLOCK = BLOCKS.register(
            "tiansuluo_pink_scarf_luanluan_block",
            () -> new LuanluanEggBlock("tiansuluo_pink_scarf")
    );
    public static final DeferredBlock<LuanluanEggBlock> TIANSULUO_BATTLE_FACE_LUANLUAN_BLOCK = BLOCKS.register(
            "tiansuluo_battle_face_luanluan_block",
            () -> new LuanluanEggBlock("tiansuluo_battle_face")
    );

    private ModBlocks() {
    }

    public static void register(IEventBus modEventBus) {
        BLOCKS.register(modEventBus);
    }
}
