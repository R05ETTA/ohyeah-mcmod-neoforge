package com.ohyeah.ohyeahmod.data;

import com.ohyeah.ohyeahmod.OhYeah;
import com.ohyeah.ohyeahmod.registry.ModItems;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.ItemTagsProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

/**
 * 物品标签数据生成器 - 自动归类模组物品
 */
public class ModItemTagsProvider extends ItemTagsProvider {

    public static final TagKey<Item> EGGS = TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath(OhYeah.MODID, "eggs"));

    public ModItemTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> provider, CompletableFuture<TagLookup<Block>> blockTags, @Nullable ExistingFileHelper existingFileHelper) {
        super(output, provider, blockTags, OhYeah.MODID, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        // 将所有刷怪蛋归类
        tag(EGGS).add(
                ModItems.TIANSULUO_PINK_SCARF_EGG.get(),
                ModItems.TIANSULUO_BATTLE_FACE_EGG.get(),
                ModItems.SUXIA_EGG.get()
        );

        // 示例：可以根据物种配置自动将 Liked Food 归类 (如果需要)
    }
}
