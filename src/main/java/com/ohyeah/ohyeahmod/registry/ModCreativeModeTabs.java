package com.ohyeah.ohyeahmod.registry;

import com.ohyeah.ohyeahmod.OhYeah;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.stream.Stream;

/**
 * 模组创造模式标签页注册中心。
 */
public final class ModCreativeModeTabs {
    private static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, OhYeah.MODID);
    
    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> MAIN = TABS.register(
            "main",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.ohyeah.main"))
                    .icon(() -> new ItemStack(ModItems.TIANSULUO_PINK_SCARF_EGG.get()))
                    .displayItems((parameters, output) -> {
                        // 统一流式接受本模组所有的物品
                        Stream.of(
                                ModItems.TIANSULUO_PINK_SCARF_EGG,
                                ModItems.TIANSULUO_BATTLE_FACE_EGG,
                                ModItems.SUXIA_EGG,
                                ModItems.TIANSULUO_PINK_SCARF_LUANLUAN_BLOCK,
                                ModItems.TIANSULUO_BATTLE_FACE_LUANLUAN_BLOCK,
                                ModItems.XIAMI_HUHU,
                                ModItems.CHIPS
                        ).forEach(item -> output.accept(item.get()));
                    })
                    .build()
    );

    private ModCreativeModeTabs() {
    }

    public static void register(IEventBus modEventBus) {
        TABS.register(modEventBus);
    }
}
