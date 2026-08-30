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
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.neoforge.fluids.FluidUtil;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import io.github.capsicum0907.caldarium.data.Skins;

/** One row of {@link Generator}, as a block. The row is what tells it what to burn. */
public class GeneratorBlock extends BaseEntityBlock {
    /** Vanilla's own property, so the lit texture and the light level come free. */
    public static final BooleanProperty LIT = BlockStateProperties.LIT;

    public static final MapCodec<GeneratorBlock> CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                    Generator.CODEC.fieldOf("generator").forGetter(GeneratorBlock::row),
                    propertiesCodec())
                    .apply(instance, GeneratorBlock::new));

    /** A panel is only as tall as it needs to be to hold a face at the sky. */
    private static final VoxelShape PANEL = box(0.0, 0.0, 0.0, 16.0, Skins.PANEL_HEIGHT, 16.0);

    private final Generator.Made row;

    public GeneratorBlock(Generator.Made row, Properties properties) {
        super(properties);
        this.row = row;
        registerDefaultState(stateDefinition.any().setValue(LIT, Boolean.FALSE));
    }

    public Generator.Made row() {
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

    /**
     * A bucket in the hand fills or empties the tank; anything else falls through to
     * opening the screen. The game already knows how to do this for any fluid
     * container from any mod, so the whole of it is asking.
     */
    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level,
            BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (FluidUtil.getFluidHandler(stack).isPresent()
                && FluidUtil.interactWithFluidHandler(player, hand, level, pos, hit.getDirection())) {
            return ItemInteractionResult.sidedSuccess(level.isClientSide());
        }
        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new GeneratorBlockEntity(pos, state);
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos,
            CollisionContext context) {
        return row.source().flat() ? PANEL : super.getShape(state, level, pos, context);
    }

    /**
     * ⚠ Nothing here lets daylight past, panel or not.
     *
     * <p>A thin block would otherwise be taken for a see-through one: the default
     * answer is worked out from the shape, and three pixels is not a full block. It
     * is opaque material all the same, so what stands under a panel is in its shade
     * and a panel under a panel makes nothing.
     */
    @Override
    protected boolean propagatesSkylightDown(BlockState state, BlockGetter level, BlockPos pos) {
        return false;
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
