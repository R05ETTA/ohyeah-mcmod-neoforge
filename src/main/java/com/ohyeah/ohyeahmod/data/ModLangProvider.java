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
            // Entities
            addEntityType(ModEntityTypes.TIANSULUO_PINK_SCARF, "Pink Scarf Tiansuluo");
            addEntityType(ModEntityTypes.TIANSULUO_BATTLE_FACE, "Battle Face Tiansuluo");
            addEntityType(ModEntityTypes.SUXIA, "Suxia");

            // Items
            addItem(ModItems.TIANSULUO_PINK_SCARF_EGG, "Pink Scarf Luanluan");
            add(ModItems.TIANSULUO_PINK_SCARF_EGG.get().getDescriptionId() + ".desc", "A special Luanluan of Pink Scarf Tiansuluo.");
            add(ModItems.TIANSULUO_PINK_SCARF_EGG.get().getDescriptionId() + ".desc_2", "Dropped by adults. Right-click to spawn.");

            addItem(ModItems.TIANSULUO_BATTLE_FACE_EGG, "Battle Face Luanluan");
            add(ModItems.TIANSULUO_BATTLE_FACE_EGG.get().getDescriptionId() + ".desc", "A special Luanluan of Battle Face Tiansuluo.");
            add(ModItems.TIANSULUO_BATTLE_FACE_EGG.get().getDescriptionId() + ".desc_2", "Dropped by adults. Right-click to spawn.");

            addItem(ModItems.SUXIA_EGG, "Suxia Luanluan");
            add(ModItems.SUXIA_EGG.get().getDescriptionId() + ".desc", "A special Luanluan of Suxia.");
            add(ModItems.SUXIA_EGG.get().getDescriptionId() + ".desc_2", "Right-click to spawn a Suxia.");

            addItem(ModItems.XIAMI_HUHU, "Xiami Huhu");
            addItem(ModItems.CHIPS, "Chips");

            // Blocks
            addBlock(ModBlocks.TIANSULUO_PINK_SCARF_LUANLUAN_BLOCK, "Pink Scarf Luanluan Block");
            addBlock(ModBlocks.TIANSULUO_BATTLE_FACE_LUANLUAN_BLOCK, "Battle Face Luanluan Block");

            // Creative Tab
            add("itemGroup.ohyeah.main", "Oh Yeah!");

            // Messages - Pink Scarf
            add("message.ohyeah.tiansuluo_pink_scarf.luanluan_block_carried", "Tiansuluo is carrying a Luanluan Block and looking for a place to put it...");
            add("message.ohyeah.tiansuluo_pink_scarf.luanluan_block_placed", "Tiansuluo placed a Luanluan Block. Total blocks: %s");
            add("message.ohyeah.tiansuluo_pink_scarf.luanluan_block_hatch_progress", "Luanluan Block hatch progress: Stage %s / %s");
            add("message.ohyeah.tiansuluo_pink_scarf.luanluan_block_hatched", "Luanluan Block hatched! Spawned babies: %s");
            add("message.ohyeah.tiansuluo_pink_scarf.luanluan_block_broken", "You crushed a Pink Scarf Luanluan Block!");

            // Messages - Battle Face
            add("message.ohyeah.tiansuluo_battle_face.luanluan_block_carried", "Battle Face Tiansuluo is now carrying a Luanluan Block!");
            add("message.ohyeah.tiansuluo_battle_face.luanluan_block_placed", "Battle Face Tiansuluo placed a Luanluan Block. Total: %s");
            add("message.ohyeah.tiansuluo_battle_face.luanluan_block_hatch_progress", "Battle Face Luanluan hatch progress: Stage %s / %s");
            add("message.ohyeah.tiansuluo_battle_face.luanluan_block_hatched", "Battle Face Luanluan hatched! Spawned: %s");
            add("message.ohyeah.tiansuluo_battle_face.luanluan_block_broken", "You crushed a Battle Face Luanluan Block!");

            // Subtitles
            add("subtitles.ohyeah.tiansuluo.ambient", "Tiansuluo murmurs softly");
            add("subtitles.ohyeah.tiansuluo.rare_call", "Tiansuluo makes a rare call");
            add("subtitles.ohyeah.tiansuluo.hurt", "Tiansuluo cries out");
            add("subtitles.ohyeah.tiansuluo.death", "Tiansuluo collapses");
            add("subtitles.ohyeah.tiansuluo.eat", "Tiansuluo is chewing");
            add("subtitles.ohyeah.tiansuluo.eat_favorite", "Tiansuluo happily chews favorite food");
            add("subtitles.ohyeah.tiansuluo.tempted", "Tiansuluo is attracted");
            add("subtitles.ohyeah.tiansuluo.notice_player", "Tiansuluo notices someone");
            add("subtitles.ohyeah.tiansuluo.spawn", "Tiansuluo appears");
            add("subtitles.ohyeah.tiansuluo.breed_success", "Tiansuluo chirps happily");
            add("subtitles.ohyeah.tiansuluo.carry_egg", "Tiansuluo shows off its Luanluan");
            add("subtitles.ohyeah.tiansuluo.attack_shot", "Tiansuluo fires a Luanluan");
            add("subtitles.ohyeah.tiansuluo.attack_end", "Tiansuluo ends counterattack");
            add("subtitles.ohyeah.tiansuluo.attack_declare", "Tiansuluo prepares for battle");
            add("subtitles.ohyeah.tiansuluo.grow_up", "Little Tiansuluo grows up");
            add("subtitles.ohyeah.tiansuluo.shear_react", "Tiansuluo protests being sheared");

            add("subtitles.ohyeah.suxia.ambient", "Suxia chirps softly");
            add("subtitles.ohyeah.suxia.hurt", "Suxia is hurt");
            add("subtitles.ohyeah.suxia.death", "Suxia dies");
            add("subtitles.ohyeah.suxia.squirt", "Suxia squirts ink");
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
            addEntityType(ModEntityTypes.TIANSULUO_PINK_SCARF, "天素罗_粉围巾");
            addEntityType(ModEntityTypes.TIANSULUO_BATTLE_FACE, "天素罗_战斗脸");
            addEntityType(ModEntityTypes.SUXIA, "素虾");

            // 物品
            addItem(ModItems.TIANSULUO_PINK_SCARF_EGG, "栾栾_粉围巾");
            add(ModItems.TIANSULUO_PINK_SCARF_EGG.get().getDescriptionId() + ".desc", "天素罗_粉围巾的特殊栾栾");
            add(ModItems.TIANSULUO_PINK_SCARF_EGG.get().getDescriptionId() + ".desc_2", "成年天素罗_粉围巾掉落，右键生成天素罗_粉围巾");

            addItem(ModItems.TIANSULUO_BATTLE_FACE_EGG, "栾栾_战斗脸");
            add(ModItems.TIANSULUO_BATTLE_FACE_EGG.get().getDescriptionId() + ".desc", "天素罗_战斗脸的特殊栾栾");
            add(ModItems.TIANSULUO_BATTLE_FACE_EGG.get().getDescriptionId() + ".desc_2", "成年天素罗_战斗脸掉落，右键生成天素罗_战斗脸");

            addItem(ModItems.SUXIA_EGG, "栾栾_素虾");
            add(ModItems.SUXIA_EGG.get().getDescriptionId() + ".desc", "素虾的特殊栾栾");
            add(ModItems.SUXIA_EGG.get().getDescriptionId() + ".desc_2", "右键生成一只素虾");

            addItem(ModItems.XIAMI_HUHU, "虾米糊糊");
            addItem(ModItems.CHIPS, "薯片");

            // 方块
            addBlock(ModBlocks.TIANSULUO_PINK_SCARF_LUANLUAN_BLOCK, "天素罗_粉围巾栾栾块");
            addBlock(ModBlocks.TIANSULUO_BATTLE_FACE_LUANLUAN_BLOCK, "天素罗_战斗脸栾栾块");

            // 创造栏
            add("itemGroup.ohyeah.main", "Oh Yeah!");

            // 消息提示 - 粉围巾
            add("message.ohyeah.tiansuluo_pink_scarf.luanluan_block_carried", "天素罗_粉围巾已进入带蛋状态，会在你附近寻找位置放置栾栾块");
            add("message.ohyeah.tiansuluo_pink_scarf.luanluan_block_placed", "天素罗_粉围巾已放置栾栾块，当前块数：%s");
            add("message.ohyeah.tiansuluo_pink_scarf.luanluan_block_hatch_progress", "栾栾块孵化进度：第 %s / %s 阶段");
            add("message.ohyeah.tiansuluo_pink_scarf.luanluan_block_hatched", "栾栾块已孵化，诞生幼体数量：%s");
            add("message.ohyeah.tiansuluo_pink_scarf.luanluan_block_broken", "你踩碎了一枚天素罗_粉围巾栾栾块");

            // 消息提示 - 战斗脸
            add("message.ohyeah.tiansuluo_battle_face.luanluan_block_carried", "天素罗_战斗脸已进入带蛋状态，准备放下栾栾块！");
            add("message.ohyeah.tiansuluo_battle_face.luanluan_block_placed", "天素罗_战斗脸已放置栾栾块，当前块数：%s");
            add("message.ohyeah.tiansuluo_battle_face.luanluan_block_hatch_progress", "战斗脸栾栾块孵化进度：第 %s / %s 阶段");
            add("message.ohyeah.tiansuluo_battle_face.luanluan_block_hatched", "战斗脸栾栾块已孵化，诞生幼体数量：%s");
            add("message.ohyeah.tiansuluo_battle_face.luanluan_block_broken", "你踩碎了一枚天素罗_战斗脸栾栾块");

            // 字幕
            add("subtitles.ohyeah.tiansuluo.ambient", "天素罗轻声呢喃");
            add("subtitles.ohyeah.tiansuluo.rare_call", "天素罗发出稀有呼唤");
            add("subtitles.ohyeah.tiansuluo.hurt", "天素罗发出痛叫");
            add("subtitles.ohyeah.tiansuluo.death", "天素罗瘫软倒下");
            add("subtitles.ohyeah.tiansuluo.eat", "天素罗在咀嚼");
            add("subtitles.ohyeah.tiansuluo.eat_favorite", "天素罗开心地吃着最爱的食物");
            add("subtitles.ohyeah.tiansuluo.tempted", "天素罗被吸引了");
            add("subtitles.ohyeah.tiansuluo.notice_player", "天素罗注意到了谁");
            add("subtitles.ohyeah.tiansuluo.spawn", "天素罗出现了");
            add("subtitles.ohyeah.tiansuluo.breed_success", "天素罗发出欢快叫声");
            add("subtitles.ohyeah.tiansuluo.carry_egg", "天素罗炫耀地展示栾栾");
            add("subtitles.ohyeah.tiansuluo.attack_shot", "天素罗喷射栾栾");
            add("subtitles.ohyeah.tiansuluo.attack_end", "天素罗结束反击");
            add("subtitles.ohyeah.tiansuluo.attack_declare", "天素罗准备战斗");
            add("subtitles.ohyeah.tiansuluo.grow_up", "小天素罗长大了");
            add("subtitles.ohyeah.tiansuluo.shear_react", "天素罗抗议剪毛");

            add("subtitles.ohyeah.suxia.ambient", "素虾轻轻漫游");
            add("subtitles.ohyeah.suxia.hurt", "素虾受伤了");
            add("subtitles.ohyeah.suxia.death", "素虾倒下了");
            add("subtitles.ohyeah.suxia.squirt", "素虾喷出墨汁");
        }
    }
}
