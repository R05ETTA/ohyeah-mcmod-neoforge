package com.ohyeah.ohyeahmod.data;

import com.ohyeah.ohyeahmod.config.ModSpeciesConfigs;
import com.ohyeah.ohyeahmod.config.SpeciesConfig;
import com.ohyeah.ohyeahmod.registry.ModBlocks;
import com.ohyeah.ohyeahmod.registry.ModEntityTypes;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.data.loot.EntityLootSubProvider;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

/**
 * 掉落表生成器 - 1.21.1 最终修正版
 */
public class ModLootTableProvider {

    public static LootTableProvider create(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        return new LootTableProvider(output, Set.of(), List.of(
                new LootTableProvider.SubProviderEntry(ModBlockLoot::new, LootContextParamSets.BLOCK),
                new LootTableProvider.SubProviderEntry(ModEntityLoot::new, LootContextParamSets.ENTITY)
        ), registries);
    }

    private static class ModBlockLoot extends BlockLootSubProvider {
        protected ModBlockLoot(HolderLookup.Provider provider) {
            super(Collections.emptySet(), FeatureFlags.REGISTRY.allFlags(), provider);
        }

        @Override
        protected void generate() {
            this.add(ModBlocks.TIANSULUO_PINK_SCARF_LUANLUAN_BLOCK.get(), LootTable.lootTable());
            this.add(ModBlocks.TIANSULUO_BATTLE_FACE_LUANLUAN_BLOCK.get(), LootTable.lootTable());
        }

        @Override
        protected Iterable<Block> getKnownBlocks() {
            // 显式强制转型为 Block 以解决 1.21.1 泛型推导问题
            return Stream.of(
                    ModBlocks.TIANSULUO_PINK_SCARF_LUANLUAN_BLOCK.get(),
                    ModBlocks.TIANSULUO_BATTLE_FACE_LUANLUAN_BLOCK.get()
            ).map(b -> (Block) b).toList();
        }
    }

    private static class ModEntityLoot extends EntityLootSubProvider {
        protected ModEntityLoot(HolderLookup.Provider provider) {
            super(FeatureFlags.REGISTRY.allFlags(), provider);
        }

        @Override
        public void generate() {
            generateForSpecies("tiansuluo_pink_scarf", ModEntityTypes.TIANSULUO_PINK_SCARF.get());
            generateForSpecies("tiansuluo_battle_face", ModEntityTypes.TIANSULUO_BATTLE_FACE.get());
            generateForSpecies("suxia", ModEntityTypes.SUXIA.get());
        }

        private void generateForSpecies(String speciesId, EntityType<?> type) {
            SpeciesConfig.Loot config = ModSpeciesConfigs.get(speciesId).loot();
            
            // 鲁棒性检查：如果 Loot 配置不存在，或者显式禁用，或者掉落列表为空，则生成空掉落表
            if (config == null || !config.enabled() || config.adultDropItemIds().isEmpty()) {
                this.add(type, LootTable.lootTable());
                return;
            }

            LootTable.Builder builder = LootTable.lootTable();
            LootPool.Builder pool = LootPool.lootPool().setRolls(ConstantValue.exactly(1.0F));
            
            for (String itemId : config.adultDropItemIds()) {
                pool.add(LootItem.lootTableItem(BuiltInRegistries.ITEM.get(ResourceLocation.parse(itemId))));
            }
            
            this.add(type, builder.withPool(pool));
        }

        @Override
        protected Stream<EntityType<?>> getKnownEntityTypes() {
            return Stream.of(
                    ModEntityTypes.TIANSULUO_PINK_SCARF.get(),
                    ModEntityTypes.TIANSULUO_BATTLE_FACE.get(),
                    ModEntityTypes.SUXIA.get()
            );
        }
    }
}
