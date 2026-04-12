package com.ohyeah.ohyeahmod.registry;

import com.ohyeah.ohyeahmod.OhYeah;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.food.Foods;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.DeferredSpawnEggItem;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModItems {
    private static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(OhYeah.MODID);

    public static final DeferredItem<Item> TIANSULUO_PINK_SCARF_EGG = ITEMS.register("tiansuluo_pink_scarf_egg", () -> new DeferredSpawnEggItem(ModEntityTypes.TIANSULUO_PINK_SCARF, 0xF3D7A6, 0x9D6E52, new Item.Properties()));
    public static final DeferredItem<Item> TIANSULUO_BATTLE_FACE_EGG = ITEMS.register("tiansuluo_battle_face_egg", () -> new DeferredSpawnEggItem(ModEntityTypes.TIANSULUO_BATTLE_FACE, 0xF3D7A6, 0x9D6E52, new Item.Properties()));
    public static final DeferredItem<Item> SUXIA_EGG = ITEMS.register("suxia_egg", () -> new DeferredSpawnEggItem(ModEntityTypes.SUXIA, 0x4E617B, 0xB7C9D9, new Item.Properties()));
    public static final DeferredItem<BlockItem> TIANSULUO_PINK_SCARF_LUANLUAN_BLOCK = ITEMS.registerSimpleBlockItem(ModBlocks.TIANSULUO_PINK_SCARF_LUANLUAN_BLOCK);
    public static final DeferredItem<BlockItem> TIANSULUO_BATTLE_FACE_LUANLUAN_BLOCK = ITEMS.registerSimpleBlockItem(ModBlocks.TIANSULUO_BATTLE_FACE_LUANLUAN_BLOCK);
    public static final DeferredItem<Item> XIAMI_HUHU = ITEMS.registerItem("xiami_huhu", Item::new, new Item.Properties().food(new FoodProperties.Builder().nutrition(6).saturationModifier(0.6F).build()));
    public static final DeferredItem<Item> CHIPS = ITEMS.registerItem("chips", Item::new, new Item.Properties().food(Foods.COOKIE));

    private ModItems() {
    }

    public static void register(IEventBus modEventBus) {
        ITEMS.register(modEventBus);
    }
}
