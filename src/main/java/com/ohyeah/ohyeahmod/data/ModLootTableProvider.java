package com.ohyeah.ohyeahmod.data;

import com.ohyeah.ohyeahmod.registry.ModBlocks;
import com.ohyeah.ohyeahmod.registry.ModEntityTypes;
import com.ohyeah.ohyeahmod.registry.ModItems;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.data.loot.EntityLootSubProvider;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
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
 * 战利品表数据生成器。
 */
public final class ModLootTableProvider {
    public static LootTableProvider create(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        return new LootTableProvider(output, Set.of(), List.of(
                new LootTableProvider.SubProviderEntry(ModBlockLoot::new, LootContextParamSets.BLOCK),
                new LootTableProvider.SubProviderEntry(ModEntityLoot::new, LootContextParamSets.ENTITY)
        ), registries);
    }

    public static class ModBlockLoot extends BlockLootSubProvider {
        protected ModBlockLoot(HolderLookup.Provider registries) {
            super(Collections.emptySet(), FeatureFlags.REGISTRY.allFlags(), registries);
        }

        @Override
        protected void generate() {
            dropSelf(ModBlocks.TIANSULUO_PINK_SCARF_LUANLUAN_BLOCK.get());
            dropSelf(ModBlocks.TIANSULUO_BATTLE_FACE_LUANLUAN_BLOCK.get());
        }

        @Override
        protected Iterable<Block> getKnownBlocks() {
            return Stream.of(
                    ModBlocks.TIANSULUO_PINK_SCARF_LUANLUAN_BLOCK,
                    ModBlocks.TIANSULUO_BATTLE_FACE_LUANLUAN_BLOCK
            ).map(supplier -> (Block) supplier.get())::iterator;
        }
    }

    public static class ModEntityLoot extends EntityLootSubProvider {
        protected ModEntityLoot(HolderLookup.Provider registries) {
            super(FeatureFlags.REGISTRY.allFlags(), registries);
        }

        @Override
        public void generate() {
            // 实体掉落配置（直接引用 Item 对象，而非硬编码字符串）
            generateForSpecies(ModEntityTypes.TIANSULUO_PINK_SCARF.get(), List.of(Items.CAKE, ModItems.CHIPS.get()));
            generateForSpecies(ModEntityTypes.TIANSULUO_BATTLE_FACE.get(), List.of(Items.CAKE, ModItems.CHIPS.get()));
            generateForSpecies(ModEntityTypes.SUXIA.get(), List.of(Items.COD));
        }

        private void generateForSpecies(EntityType<?> type, List<Item> lootItems) {
            LootTable.Builder builder = LootTable.lootTable();
            LootPool.Builder pool = LootPool.lootPool().setRolls(ConstantValue.exactly(1.0F));
            
            for (Item item : lootItems) {
                pool.add(LootItem.lootTableItem(item));
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
