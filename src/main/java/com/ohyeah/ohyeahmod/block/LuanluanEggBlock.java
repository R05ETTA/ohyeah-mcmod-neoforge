package com.ohyeah.ohyeahmod.block;

import com.ohyeah.ohyeahmod.config.ModSpeciesConfigs;
import com.ohyeah.ohyeahmod.config.SpeciesConfig;
import com.ohyeah.ohyeahmod.entity.SuxiaEntity;
import com.ohyeah.ohyeahmod.entity.TiansuluoBattleFaceEntity;
import com.ohyeah.ohyeahmod.entity.TiansuluoPinkScarfEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class LuanluanEggBlock extends Block {
    public static final IntegerProperty HATCH = BlockStateProperties.HATCH;
    public static final IntegerProperty EGGS = BlockStateProperties.EGGS;
    private static final VoxelShape ONE_EGG_SHAPE = Block.box(3.0D, 0.0D, 3.0D, 12.0D, 7.0D, 12.0D);
    private static final VoxelShape MULTI_EGG_SHAPE = Block.box(1.0D, 0.0D, 1.0D, 15.0D, 7.0D, 15.0D);
    private final String speciesId;

    public LuanluanEggBlock(String speciesId) {
        super(BlockBehaviour.Properties.of().strength(0.5F).randomTicks().noOcclusion());
        this.speciesId = speciesId;
        this.registerDefaultState(this.stateDefinition.any().setValue(HATCH, 0).setValue(EGGS, 1));
    }

    @Override
    public void stepOn(Level level, BlockPos pos, BlockState state, Entity entity) {
        this.destroyEgg(level, state, pos, entity, 100);
        super.stepOn(level, pos, state, entity);
    }

    @Override
    public void fallOn(Level level, BlockState state, BlockPos pos, Entity entity, float fallDistance) {
        if (!(entity instanceof Zombie)) {
            this.destroyEgg(level, state, pos, entity, 3);
        }
        super.fallOn(level, state, pos, entity, fallDistance);
    }

    private void destroyEgg(Level level, BlockState state, BlockPos pos, Entity entity, int chance) {
        if (!this.canDestroyEgg(level, entity)) {
            return;
        }
        if (!level.isClientSide && level.random.nextInt(chance) == 0 && state.is(this)) {
            this.decreaseEggs(level, pos, state);
        }
    }

    private void decreaseEggs(Level level, BlockPos pos, BlockState state) {
        int eggs = state.getValue(EGGS);
        if (eggs > 1) {
            level.setBlock(pos, state.setValue(EGGS, eggs - 1), 2);
        } else {
            level.removeBlock(pos, false);
        }
    }

    @Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (this.shouldHatch(level) && this.onSand(level, pos)) {
            int hatch = state.getValue(HATCH);
            if (hatch < 2) {
                level.setBlock(pos, state.setValue(HATCH, hatch + 1), 2);
            } else {
                for (int i = 0; i < state.getValue(EGGS); ++i) {
                    this.spawnHatchedEntity(level, pos);
                }
                level.removeBlock(pos, false);
            }
        }
    }

    private boolean shouldHatch(Level level) {
        return level.random.nextInt(500) == 0;
    }

    private boolean onSand(BlockGetter level, BlockPos pos) {
        return level.getBlockState(pos.below()).is(Blocks.SAND) || level.getBlockState(pos.below()).is(Blocks.RED_SAND);
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean isMoving) {
        super.onPlace(state, level, pos, oldState, isMoving);
    }

    private void spawnHatchedEntity(ServerLevel level, BlockPos pos) {
        EntityType<?> type = this.getEntityType();
        if (type != null) {
            Entity entity = type.create(level);
            if (entity instanceof AgeableMob ageableMob) {
                ageableMob.setBaby(true);
                ageableMob.moveTo(pos.getX() + 0.5D, pos.getY(), pos.getZ() + 0.5D, 0.0F, 0.0F);
                level.addFreshEntity(ageableMob);
            }
        }
    }

    private EntityType<?> getEntityType() {
        if ("tiansuluo_pink_scarf".equals(this.speciesId)) return EntityType.byString("ohyeah:tiansuluo_pink_scarf").orElse(null);
        if ("tiansuluo_battle_face".equals(this.speciesId)) return EntityType.byString("ohyeah:tiansuluo_battle_face").orElse(null);
        return null;
    }

    private boolean canDestroyEgg(Level level, Entity entity) {
        if (entity instanceof TiansuluoPinkScarfEntity || entity instanceof TiansuluoBattleFaceEntity || entity instanceof SuxiaEntity) {
            return false;
        }
        return entity instanceof LivingEntity;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(HATCH, EGGS);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return state.getValue(EGGS) > 1 ? MULTI_EGG_SHAPE : ONE_EGG_SHAPE;
    }

    private SpeciesConfig.Breeding breeding() {
        return ModSpeciesConfigs.get(this.speciesId).breeding();
    }
}
