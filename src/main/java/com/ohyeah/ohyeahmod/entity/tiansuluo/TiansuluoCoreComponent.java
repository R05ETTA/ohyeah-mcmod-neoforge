package com.ohyeah.ohyeahmod.entity.tiansuluo;

import com.ohyeah.ohyeahmod.config.SpeciesConfig;
import com.ohyeah.ohyeahmod.entity.common.EggLayingSpecies;
import com.ohyeah.ohyeahmod.sound.bridge.SoundParticipant;
import com.ohyeah.ohyeahmod.sound.bridge.SpeciesSoundFacade;
import com.ohyeah.ohyeahmod.sound.definition.SoundCue;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.ai.goal.BreedGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;

/**
 * 天素罗生物的核心逻辑组件 - 组合优于继承的实践
 * 集中管理声音、产卵、状态观察及通用交互
 */
public final class TiansuluoCoreComponent {
    private static final String TAG_SILENCED = "SilencedByShears";
    private static final String TAG_HAS_EGG_BLOCK = "HasLuanluanBlock";

    private final SoundSupport soundSupport = new SoundSupport();
    private final EggLayingSupport eggLayingSupport = new EggLayingSupport();
    private final ObservationState observationState = new ObservationState();
    private final InteractionSupport interactionSupport = new InteractionSupport();

    private boolean silencedByShears;
    private final EntityDataAccessor<Boolean> eggBlockDataAccessor;

    public TiansuluoCoreComponent(EntityDataAccessor<Boolean> eggBlockDataAccessor) {
        this.eggBlockDataAccessor = eggBlockDataAccessor;
    }

    public void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(eggBlockDataAccessor, false);
    }

    public void addAdditionalSaveData(CompoundTag tag, Animal entity) {
        tag.putBoolean(TAG_SILENCED, this.silencedByShears);
        tag.putBoolean(TAG_HAS_EGG_BLOCK, this.hasCarriedEggBlock(entity));
        this.eggLayingSupport.addAdditionalSaveData(tag);
        this.soundSupport.addAdditionalSaveData(tag);
    }

    public void readAdditionalSaveData(CompoundTag tag, Animal entity) {
        this.silencedByShears = tag.getBoolean(TAG_SILENCED);
        this.setHasCarriedEggBlock(entity, tag.getBoolean(TAG_HAS_EGG_BLOCK));
        this.eggLayingSupport.readAdditionalSaveData(tag);
        if (!this.hasCarriedEggBlock(entity)) {
            this.eggLayingSupport.clear();
        }
        this.soundSupport.readAdditionalSaveData(tag);
    }

    public void tick(Animal entity) {
        if (entity.level().isClientSide) {
            this.updateClientVoices(entity);
            return;
        }
        SpeciesSoundFacade.tick((SoundParticipant) entity);
        this.updateCarryEggVoice(entity);
    }

    private void updateClientVoices(Animal entity) {
        if (this.observationState.shouldPlayGrowUp(entity.isBaby())) {
            this.playVoice(entity, SoundCue.GROW_UP);
        }
        if (this.observationState.shouldPlayTempted(this.interactionSupport.isTemptedByNearbyPlayer(entity))) {
            this.playVoice(entity, SoundCue.TEMPTED);
        }
        if (this.observationState.shouldPlayNoticePlayer(this.interactionSupport.isNoticingNearbyPlayer(entity))) {
            this.playVoice(entity, SoundCue.NOTICE_PLAYER);
        }
    }

    private void updateCarryEggVoice(Animal entity) {
        SpeciesConfig config = ((TiansuluoEntityInterface)entity).getSpeciesConfig();
        if (!config.breeding().usesEggBlock() || !this.hasCarriedEggBlock(entity) || entity.isBaby()) return;
        this.playVoice(entity, SoundCue.CARRY_EGG);
    }

    public InteractionResult handleInteract(Animal entity, Player player, InteractionHand hand, SpeciesConfig config) {
        ItemStack stack = player.getItemInHand(hand);
        InteractionResult shearResult = this.interactionSupport.tryHandleShears(entity, player, hand, stack, config, 
                this.silencedByShears, 
                () -> {
                    this.silencedByShears = true;
                    this.playVoice(entity, SoundCue.SHEAR_REACT);
                });
        if (shearResult != InteractionResult.PASS) return shearResult;

        return this.interactionSupport.handleFeeding(entity, player, hand, stack, config, 
                () -> this.silencedByShears = false, 
                cue -> this.playVoice(entity, cue));
    }

    public boolean playVoice(Animal entity, SoundCue cue) {
        return this.soundSupport.playVoice((SoundParticipant) entity, cue);
    }

    public boolean tryPlayVoice(Animal entity, SoundCue cue) {
        return this.soundSupport.tryPlayVoice((SoundParticipant) entity, cue);
    }

    public boolean isSoundSilenced() {
        return this.silencedByShears;
    }

    public boolean hasCarriedEggBlock(@Nullable Animal entity) {
        return entity != null && entity.getEntityData().get(eggBlockDataAccessor);
    }

    public void setHasCarriedEggBlock(Animal entity, boolean value) {
        entity.getEntityData().set(eggBlockDataAccessor, value);
        if (!value) this.eggLayingSupport.clear();
    }

    public SoundSupport getSoundSupport() { return soundSupport; }
    public EggLayingSupport getEggLayingSupport() { return eggLayingSupport; }
    public InteractionSupport getInteractionSupport() { return interactionSupport; }

    public interface TiansuluoEntityInterface {
        SpeciesConfig getSpeciesConfig();
        TiansuluoCoreComponent getCore();
    }

    // --- 内部支持类 ---

    public static final class SoundSupport {
        private static final String TAG_PLAYED_CUES = "PlayedSoundCues";
        private final Set<String> playedSoundCues = new HashSet<>();

        void addAdditionalSaveData(CompoundTag tag) {
            ListTag list = new ListTag();
            for (String cue : playedSoundCues) list.add(StringTag.valueOf(cue));
            tag.put(TAG_PLAYED_CUES, list);
        }

        void readAdditionalSaveData(CompoundTag tag) {
            playedSoundCues.clear();
            if (tag.contains(TAG_PLAYED_CUES, Tag.TAG_LIST)) {
                ListTag list = tag.getList(TAG_PLAYED_CUES, Tag.TAG_STRING);
                for (int i = 0; i < list.size(); i++) playedSoundCues.add(list.getString(i));
            }
        }

        public boolean playVoice(SoundParticipant participant, SoundCue cue) {
            if (participant.soundVoiceConfig().oneShotCues().contains(cue.key())) {
                playedSoundCues.add(cue.name());
            }
            return SpeciesSoundFacade.playCue(participant, cue, 1.0F, 1.0F);
        }

        public boolean tryPlayVoice(SoundParticipant participant, SoundCue cue) {
            if (participant.soundVoiceConfig().oneShotCues().contains(cue.key()) && playedSoundCues.contains(cue.name())) {
                return false;
            }
            return playVoice(participant, cue);
        }

        public Set<String> playedSoundCues() {
            return playedSoundCues;
        }
    }

    public static final class EggLayingSupport {
        private @Nullable BlockPos eggBlockTargetPos;
        private int eggBlockPlacingCounter;
        private @Nullable UUID eggBlockPlayerUuid;

        void addAdditionalSaveData(CompoundTag tag) {
            if (eggBlockTargetPos != null) {
                tag.putInt("TargetX", eggBlockTargetPos.getX());
                tag.putInt("TargetY", eggBlockTargetPos.getY());
                tag.putInt("TargetZ", eggBlockTargetPos.getZ());
            }
            tag.putInt("PlacingCounter", eggBlockPlacingCounter);
            if (eggBlockPlayerUuid != null) tag.putUUID("PlayerUuid", eggBlockPlayerUuid);
        }

        void readAdditionalSaveData(CompoundTag tag) {
            if (tag.contains("TargetX")) this.eggBlockTargetPos = new BlockPos(tag.getInt("TargetX"), tag.getInt("TargetY"), tag.getInt("TargetZ"));
            this.eggBlockPlacingCounter = tag.getInt("PlacingCounter");
            if (tag.hasUUID("PlayerUuid")) this.eggBlockPlayerUuid = tag.getUUID("PlayerUuid");
        }

        void clear() {
            this.eggBlockTargetPos = null;
            this.eggBlockPlacingCounter = 0;
            this.eggBlockPlayerUuid = null;
        }

        public @Nullable BlockPos getEggBlockTargetPos() { return eggBlockTargetPos; }
        public void setEggBlockTargetPos(@Nullable BlockPos pos) { this.eggBlockTargetPos = pos; }
        public int getEggBlockPlacingCounter() { return eggBlockPlacingCounter; }
        public void setEggBlockPlacingCounter(int counter) { this.eggBlockPlacingCounter = counter; }
        public @Nullable UUID getEggBlockPlayerUuid() { return eggBlockPlayerUuid; }
        public void setEggBlockPlayerUuid(@Nullable UUID uuid) { this.eggBlockPlayerUuid = uuid; }
    }

    private static final class ObservationState {
        private boolean wasBabyLastTick;
        private boolean wasTemptedByPlayer;
        private boolean wasNoticingPlayer;

        boolean shouldPlayGrowUp(boolean isCurrentlyBaby) {
            boolean shouldPlay = wasBabyLastTick && !isCurrentlyBaby;
            this.wasBabyLastTick = isCurrentlyBaby;
            return shouldPlay;
        }

        boolean shouldPlayTempted(boolean isCurrentlyTempted) {
            boolean shouldPlay = isCurrentlyTempted && !wasTemptedByPlayer;
            this.wasTemptedByPlayer = isCurrentlyTempted;
            return shouldPlay;
        }

        boolean shouldPlayNoticePlayer(boolean isCurrentlyNoticing) {
            boolean shouldPlay = isCurrentlyNoticing && !wasNoticingPlayer;
            this.wasNoticingPlayer = isCurrentlyNoticing;
            return shouldPlay;
        }
    }

    public static final class InteractionSupport {
        public boolean isTemptedByNearbyPlayer(Animal entity) {
            Player player = entity.level().getNearestPlayer(entity, 10.0D);
            if (player == null || !player.isAlive()) return false;
            return isBreedingItem(entity, player.getMainHandItem()) || isBreedingItem(entity, player.getOffhandItem());
        }

        public boolean isNoticingNearbyPlayer(Animal entity) {
            Player player = entity.level().getNearestPlayer(entity, 8.0D);
            return player != null && player.isAlive() && entity.hasLineOfSight(player);
        }

        public boolean isBreedingItem(Animal entity, ItemStack stack) {
            if (!(entity instanceof TiansuluoEntityInterface ti)) return false;
            SpeciesConfig config = ti.getSpeciesConfig();
            String itemId = BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();
            return config.food().favoriteItemIds().contains(itemId) || config.food().likedItemIds().contains(itemId);
        }

        public InteractionResult tryHandleShears(Animal entity, Player player, InteractionHand hand, ItemStack stack, 
                                               SpeciesConfig config, boolean currentlySilenced, Runnable onShearSuccess) {
            if (!stack.is(Items.SHEARS) || currentlySilenced) return InteractionResult.PASS;
            if (!entity.level().isClientSide) {
                stack.hurtAndBreak(1, player, entity.getSlotForHand(hand));
                entity.spawnAtLocation(Items.RED_WOOL);
                entity.level().playSound(null, entity, SoundEvents.SHEEP_SHEAR, SoundSource.PLAYERS, 1.0F, 1.0F);
                onShearSuccess.run();
            }
            return InteractionResult.SUCCESS;
        }

        public InteractionResult handleFeeding(Animal entity, Player player, InteractionHand hand, ItemStack stack, 
                                             SpeciesConfig config, Runnable onCuredSilenced, Consumer<SoundCue> voicePlayer) {
            String itemId = BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();
            boolean isFavorite = config.food().favoriteItemIds().contains(itemId);
            boolean isLiked = config.food().likedItemIds().contains(itemId);
            if (!isFavorite && !isLiked) return InteractionResult.PASS;

            if (!entity.level().isClientSide) {
                if (!player.getAbilities().instabuild) stack.shrink(1);
                onCuredSilenced.run();
                if (entity.isBaby()) {
                    if (isFavorite) entity.setAge(0);
                    else entity.ageUp(config.food().likedFoodGrowthStepTicks());
                } else entity.setInLove(player);
                voicePlayer.accept(isFavorite ? SoundCue.EAT_FAVORITE : SoundCue.EAT);
            }
            return InteractionResult.SUCCESS;
        }
    }

    // --- AI Goals ---

    public static final class MateForEggBlockGoal extends BreedGoal {
        private final Animal tiansuluo;

        public MateForEggBlockGoal(Animal animal, double speed) {
            super(animal, speed);
            this.tiansuluo = animal;
        }

        @Override
        public boolean canUse() {
            if (!(tiansuluo instanceof TiansuluoEntityInterface ti)) return false;
            return ti.getSpeciesConfig().breeding().usesEggBlock() && 
                   !ti.getCore().hasCarriedEggBlock(tiansuluo) && 
                   super.canUse();
        }

        @Override
        protected void breed() {
            ServerPlayer player = this.animal.getLoveCause();
            if (player == null && this.partner.getLoveCause() != null) player = this.partner.getLoveCause();
            
            if (player != null) {
                player.awardStat(Stats.ANIMALS_BRED);
                CriteriaTriggers.BRED_ANIMALS.trigger(player, this.animal, this.partner, null);
            }

            if (tiansuluo instanceof TiansuluoEntityInterface ti) {
                ti.getCore().setHasCarriedEggBlock(tiansuluo, true);
                ti.getCore().getEggLayingSupport().setEggBlockPlayerUuid(player != null ? player.getUUID() : null);
                // 暂时移除 message key，因为 SpeciesConfig 中未定义对应的字段
                // if (player != null) player.displayClientMessage(Component.translatable(ti.getSpeciesConfig().breeding().carriedMessageKey()), true);
                ti.getCore().tryPlayVoice(tiansuluo, SoundCue.BREED_SUCCESS);
            }

            this.animal.setAge(6000);
            this.partner.setAge(6000);
            this.animal.resetLove();
            this.partner.resetLove();
            
            Level level = this.animal.level();
            if (level.getGameRules().getBoolean(GameRules.RULE_DOMOBLOOT)) {
                level.addFreshEntity(new ExperienceOrb(level, this.animal.getX(), this.animal.getY(), this.animal.getZ(), this.animal.getRandom().nextInt(7) + 1));
            }
        }
    }

    public static final class LayEggBlockGoal extends Goal {
        private final Animal entity;
        private final TiansuluoEntityInterface ti;

        public LayEggBlockGoal(Animal entity) {
            this.entity = entity;
            this.ti = (TiansuluoEntityInterface) entity;
            this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            return ti.getSpeciesConfig().breeding().usesEggBlock() && 
                   ti.getCore().hasCarriedEggBlock(entity) && 
                   !entity.isBaby();
        }

        @Override
        public void tick() {
            EggLayingSupport support = ti.getCore().getEggLayingSupport();
            BlockPos targetPos = support.getEggBlockTargetPos();
            
            if (targetPos == null || !canPlaceEggBlockAt(targetPos)) {
                targetPos = findNearbyEggBlockTarget();
                support.setEggBlockTargetPos(targetPos);
            }

            if (targetPos == null) return;

            Vec3 targetCenter = Vec3.atBottomCenterOf(targetPos.above());
            if (entity.distanceToSqr(targetCenter) > 2.25D) {
                entity.getNavigation().moveTo(targetCenter.x, targetCenter.y, targetCenter.z, ti.getSpeciesConfig().behavior().mateGoalSpeed());
                support.setEggBlockPlacingCounter(0);
            } else {
                entity.getNavigation().stop();
                support.setEggBlockPlacingCounter(support.getEggBlockPlacingCounter() + 1);
                
                if (support.getEggBlockPlacingCounter() > 100) { // 假设 5 秒
                    placeEggBlock(targetPos);
                    ti.getCore().setHasCarriedEggBlock(entity, false);
                }
            }
        }

        private boolean canPlaceEggBlockAt(BlockPos pos) {
            return entity.level().getBlockState(pos).isFaceSturdy(entity.level(), pos, Direction.UP) && 
                   entity.level().getBlockState(pos.above()).isAir();
        }

        private void placeEggBlock(BlockPos pos) {
            String blockId = ti.getSpeciesConfig().breeding().eggBlockId();
            Block block = BuiltInRegistries.BLOCK.get(ResourceLocation.parse(blockId));
            entity.level().setBlockAndUpdate(pos.above(), block.defaultBlockState());
            entity.level().playSound(null, pos, SoundEvents.BAMBOO_PLACE, SoundSource.BLOCKS, 1.0F, 1.0F);
        }

        private @Nullable BlockPos findNearbyEggBlockTarget() {
            BlockPos center = entity.blockPosition();
            for (int i = 0; i < 32; i++) {
                BlockPos candidate = center.offset(entity.getRandom().nextInt(16) - 8, entity.getRandom().nextInt(4) - 2, entity.getRandom().nextInt(16) - 8);
                if (canPlaceEggBlockAt(candidate)) return candidate;
            }
            return null;
        }
    }
}
