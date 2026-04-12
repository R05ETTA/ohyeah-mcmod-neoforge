package com.ohyeah.ohyeahmod.data;

import com.ohyeah.ohyeahmod.OhYeah;
import com.ohyeah.ohyeahmod.registry.ModItems;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.client.model.generators.ItemModelProvider;
import net.neoforged.neoforge.client.model.generators.ModelFile;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.registries.DeferredItem;

/**
 * 物品模型生成器 - 1.21.1 稳健引用版
 */
public class ModItemModelProvider extends ItemModelProvider {

    public ModItemModelProvider(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, OhYeah.MODID, existingFileHelper);
    }

    @Override
    protected void registerModels() {
        // --- 手持物品 ---
        simpleItem(ModItems.TIANSULUO_PINK_SCARF_EGG);
        simpleItem(ModItems.TIANSULUO_BATTLE_FACE_EGG);
        simpleItem(ModItems.SUXIA_EGG);
        simpleItem(ModItems.XIAMI_HUHU);
        simpleItem(ModItems.CHIPS);

        // --- 方块物品 ---
        blockItem(ModItems.TIANSULUO_PINK_SCARF_LUANLUAN_BLOCK);
        blockItem(ModItems.TIANSULUO_BATTLE_FACE_LUANLUAN_BLOCK);
    }

    private void simpleItem(DeferredItem<? extends Item> item) {
        String name = item.getId().getPath();
        withExistingParent(name, mcLoc("item/generated"))
                .texture("layer0", modLoc("item/" + name));
    }

    private void blockItem(DeferredItem<? extends Item> item) {
        String name = item.getId().getPath();
        // 栾栾块是多阶段方块，默认 block/name 模型不存在（只有 stage0/1/2）
        // 手持和物品栏显示应指向基础阶段 stage0
        getBuilder(name).parent(new ModelFile.UncheckedModelFile(modLoc("block/" + name + "_stage0")));
    }
}
