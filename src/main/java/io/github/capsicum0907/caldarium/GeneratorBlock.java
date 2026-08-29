package io.github.capsicum0907.caldarium;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;

/** One row of {@link Generator}, as a block. The row is what tells it what to burn. */
public class GeneratorBlock extends BaseEntityBlock {
    /** Vanilla's own property, so the lit texture and the light level come free. */
    public static final BooleanProperty LIT = BlockStateProperties.LIT;

    public static final MapCodec<GeneratorBlock> CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                    Generator.CODEC.fieldOf("generator").forGetter(GeneratorBlock::row),
                    propertiesCodec())
                    .apply(instance, GeneratorBlock::new));

    private final Generator row;

    public GeneratorBlock(Generator row, Properties properties) {
        super(properties);
        this.row = row;
        registerDefaultState(stateDefinition.any().setValue(LIT, Boolean.FALSE));
    }

    public Generator row() {
        return row;
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(LIT);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos,
            Player player, BlockHitResult hit) {
        return MachineMenu.open(level, pos, player);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new GeneratorBlockEntity(pos, state);
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    /** Server side only: burning and pushing are world state, not something drawn. */
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state,
            BlockEntityType<T> type) {
        return level.isClientSide() ? null
                : createTickerHelper(type, CaldariumRegistry.GENERATOR_ENTITY.get(),
                        GeneratorBlockEntity::serverTick);
    }
}
