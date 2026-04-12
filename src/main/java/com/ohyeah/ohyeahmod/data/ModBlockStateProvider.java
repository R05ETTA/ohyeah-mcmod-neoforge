package com.ohyeah.ohyeahmod.data;

import com.ohyeah.ohyeahmod.OhYeah;
import com.ohyeah.ohyeahmod.block.LuanluanEggBlock;
import com.ohyeah.ohyeahmod.registry.ModBlocks;
import net.minecraft.data.PackOutput;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.client.model.generators.BlockStateProvider;
import net.neoforged.neoforge.client.model.generators.ConfiguredModel;
import net.neoforged.neoforge.client.model.generators.ModelFile;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.registries.DeferredBlock;

/**
 * 方块状态生成器 - 使用正确的 ConfiguredModel 类
 */
public class ModBlockStateProvider extends BlockStateProvider {

    public ModBlockStateProvider(PackOutput output, ExistingFileHelper exFileHelper) {
        super(output, OhYeah.MODID, exFileHelper);
    }

    @Override
    protected void registerStatesAndModels() {
        generateLuanluanEggBlock(ModBlocks.TIANSULUO_PINK_SCARF_LUANLUAN_BLOCK);
        generateLuanluanEggBlock(ModBlocks.TIANSULUO_BATTLE_FACE_LUANLUAN_BLOCK);
    }

    private void generateLuanluanEggBlock(DeferredBlock<LuanluanEggBlock> deferredBlock) {
        Block block = deferredBlock.get();
        String name = deferredBlock.getId().getPath();

        ModelFile stage0 = models().cubeAll(name + "_stage0", modLoc("block/" + name + "_stage0"));
        ModelFile stage1 = models().cubeAll(name + "_stage1", modLoc("block/" + name + "_stage1"));
        ModelFile stage2 = models().cubeAll(name + "_stage2", modLoc("block/" + name + "_stage2"));

        getVariantBuilder(block).forAllStates(state -> {
            int hatch = state.getValue(LuanluanEggBlock.HATCH);
            ModelFile model = switch (hatch) {
                case 1 -> stage1;
                case 2 -> stage2;
                default -> stage0;
            };
            return ConfiguredModel.builder()
                    .modelFile(model)
                    .build();
        });
    }
}
