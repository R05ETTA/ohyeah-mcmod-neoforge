package com.ohyeah.ohyeahmod.data;

import com.ohyeah.ohyeahmod.OhYeah;
import com.ohyeah.ohyeahmod.registry.ModBlocks;
import com.ohyeah.ohyeahmod.registry.ModEntityTypes;
import com.ohyeah.ohyeahmod.registry.ModItems;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.LanguageProvider;

/**
 * 语言文件生成器 - 自动化翻译系统
 */
public abstract class ModLangProvider extends LanguageProvider {

    public ModLangProvider(PackOutput output, String locale) {
        super(output, OhYeah.MODID, locale);
    }

    /**
     * 英文翻译
     */
    public static class English extends ModLangProvider {
        public English(PackOutput output) {
            super(output, "en_us");
        }

        @Override
        protected void addTranslations() {
            // 实名
            addEntityType(ModEntityTypes.TIANSULUO_PINK_SCARF, "Pink Scarf Luanluan");
            addEntityType(ModEntityTypes.TIANSULUO_BATTLE_FACE, "Battle Face Luanluan");
            addEntityType(ModEntityTypes.SUXIA, "Suxia");

            // 物品
            addItem(ModItems.TIANSULUO_PINK_SCARF_EGG, "Pink Scarf Luanluan");
            addItem(ModItems.TIANSULUO_BATTLE_FACE_EGG, "Battle Face Luanluan");
            addItem(ModItems.SUXIA_EGG, "Suxia Luanluan");
            addItem(ModItems.XIAMI_HUHU, "Xiami Huhu");
            addItem(ModItems.CHIPS, "Chips");

            // 方块
            addBlock(ModBlocks.TIANSULUO_PINK_SCARF_LUANLUAN_BLOCK, "Pink Scarf Luanluan Block");
            addBlock(ModBlocks.TIANSULUO_BATTLE_FACE_LUANLUAN_BLOCK, "Battle Face Luanluan Block");

            // 创造栏
            add("itemGroup.ohyeah.main", "Oh Yeah");

            // 消息提示
            add("message.ohyeah.tiansuluo_pink_scarf.luanluan_block_carried", "Tiansuluo is carrying a Luanluan Block and will place it near you");
            add("message.ohyeah.tiansuluo_pink_scarf.luanluan_block_placed", "Tiansuluo placed Luanluan Blocks. Block count: %s");
            add("message.ohyeah.tiansuluo_pink_scarf.luanluan_block_hatch_progress", "Luanluan Block hatch progress: stage %s / %s");
            add("message.ohyeah.tiansuluo_pink_scarf.luanluan_block_hatched", "Luanluan Blocks hatched. Babies spawned: %s");
            add("message.ohyeah.tiansuluo_pink_scarf.luanluan_block_broken", "You crushed a Luanluan Block");

            // 字幕
            add("subtitles.ohyeah.tiansuluo.ambient", "Tiansuluo murmurs softly");
            add("subtitles.ohyeah.tiansuluo.rare_call", "Tiansuluo makes a rare call");
            add("subtitles.ohyeah.tiansuluo.hurt", "Tiansuluo cries out in pain");
            add("subtitles.ohyeah.tiansuluo.death", "Tiansuluo collapses");
            add("subtitles.ohyeah.tiansuluo.eat", "Tiansuluo eating");
            add("subtitles.ohyeah.tiansuluo.eat_favorite", "Tiansuluo happily eating favorite food");
            add("subtitles.ohyeah.tiansuluo.tempted", "Tiansuluo is tempted");
            add("subtitles.ohyeah.tiansuluo.notice_player", "Tiansuluo notices you");
            add("subtitles.ohyeah.tiansuluo.spawn", "Tiansuluo appears");
            add("subtitles.ohyeah.tiansuluo.breed_success", "Tiansuluo breed success");
            add("subtitles.ohyeah.tiansuluo.carry_egg", "Tiansuluo starts carrying an egg");
            add("subtitles.ohyeah.tiansuluo.attack_shot", "Tiansuluo fires an attack");
            add("subtitles.ohyeah.tiansuluo.attack_end", "Tiansuluo finishes attack");
            add("subtitles.ohyeah.tiansuluo.attack_declare", "Tiansuluo prepares for battle");
            add("subtitles.ohyeah.tiansuluo.grow_up", "Tiansuluo grows up");
            add("subtitles.ohyeah.tiansuluo.shear_react", "Tiansuluo reacts to shears");

            add("subtitles.ohyeah.suxia.ambient", "Suxia chirps");
            add("subtitles.ohyeah.suxia.hurt", "Suxia squeaks");
            add("subtitles.ohyeah.suxia.death", "Suxia dies");
        }
    }

    /**
     * 中文翻译
     */
    public static class Chinese extends ModLangProvider {
        public Chinese(PackOutput output) {
            super(output, "zh_cn");
        }

        @Override
        protected void addTranslations() {
            // 实名
            addEntityType(ModEntityTypes.TIANSULUO_PINK_SCARF, "天素罗粉围巾");
            addEntityType(ModEntityTypes.TIANSULUO_BATTLE_FACE, "天素罗战斗脸");
            addEntityType(ModEntityTypes.SUXIA, "素虾");

            // 物品
            addItem(ModItems.TIANSULUO_PINK_SCARF_EGG, "粉围巾栾栾");
            addItem(ModItems.TIANSULUO_BATTLE_FACE_EGG, "战斗脸栾栾");
            addItem(ModItems.SUXIA_EGG, "素虾栾栾");
            addItem(ModItems.XIAMI_HUHU, "虾米糊糊");
            addItem(ModItems.CHIPS, "薯片");

            // 方块
            addBlock(ModBlocks.TIANSULUO_PINK_SCARF_LUANLUAN_BLOCK, "天素罗粉围巾栾栾块");
            addBlock(ModBlocks.TIANSULUO_BATTLE_FACE_LUANLUAN_BLOCK, "天素罗战斗脸栾栾块");

            // 创造栏
            add("itemGroup.ohyeah.main", "Oh Yeah");

            // 消息提示
            add("message.ohyeah.tiansuluo_pink_scarf.luanluan_block_carried", "天素罗正抱着一个栾栾块并准备把它放在你附近");
            add("message.ohyeah.tiansuluo_pink_scarf.luanluan_block_placed", "天素罗已放置栾栾块，当前块数：%s");
            add("message.ohyeah.tiansuluo_pink_scarf.luanluan_block_hatch_progress", "栾栾块孵化进度：第 %s / %s 阶段");
            add("message.ohyeah.tiansuluo_pink_scarf.luanluan_block_hatched", "栾栾块已孵化，诞生幼体数量：%s");
            add("message.ohyeah.tiansuluo_pink_scarf.luanluan_block_broken", "你踩碎了一枚天素罗粉围巾栾栾块");

            // 字幕
            add("subtitles.ohyeah.tiansuluo.ambient", "天素罗轻声呢喃");
            add("subtitles.ohyeah.tiansuluo.rare_call", "天素罗发出稀有的鸣叫");
            add("subtitles.ohyeah.tiansuluo.hurt", "天素罗发出痛叫");
            add("subtitles.ohyeah.tiansuluo.death", "天素罗瘫软倒下");
            add("subtitles.ohyeah.tiansuluo.eat", "天素罗正在进食");
            add("subtitles.ohyeah.tiansuluo.eat_favorite", "天素罗开心地吃着最爱的食物");
            add("subtitles.ohyeah.tiansuluo.tempted", "天素罗被吸引了");
            add("subtitles.ohyeah.tiansuluo.notice_player", "天素罗注意到了你");
            add("subtitles.ohyeah.tiansuluo.spawn", "天素罗出现了");
            add("subtitles.ohyeah.tiansuluo.breed_success", "天素罗繁殖成功");
            add("subtitles.ohyeah.tiansuluo.carry_egg", "天素罗开始抱块");
            add("subtitles.ohyeah.tiansuluo.attack_shot", "天素罗发起了攻击");
            add("subtitles.ohyeah.tiansuluo.attack_end", "天素罗停止了攻击");
            add("subtitles.ohyeah.tiansuluo.attack_declare", "天素罗进入战斗状态");
            add("subtitles.ohyeah.tiansuluo.grow_up", "天素罗长大了");
            add("subtitles.ohyeah.tiansuluo.shear_react", "天素罗对剪刀有了反应");

            add("subtitles.ohyeah.suxia.ambient", "素虾鸣叫");
            add("subtitles.ohyeah.suxia.hurt", "素虾发出痛叫");
            add("subtitles.ohyeah.suxia.death", "素虾死亡");
        }
    }
}
