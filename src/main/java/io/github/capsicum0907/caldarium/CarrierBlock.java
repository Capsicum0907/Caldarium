package io.github.capsicum0907.caldarium;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import io.github.capsicum0907.caldarium.data.Skins;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.PipeBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.energy.IEnergyStorage;

/**
 * A {@link Kind} that is laid in lines rather than stood somewhere: a cable, and the
 * two doors at the ends of one.
 *
 * <p><b>It grows an arm towards every neighbour energy can cross to.</b> That is the
 * whole of what this class adds, and it is not decoration. These blocks have no
 * window to open, no wrench and nothing to configure, and the rule they follow
 * refuses some neighbours on purpose — a cable will not offer to another mod's
 * machine, which is what lets one be laid past it. Without an arm there is nothing to
 * tell that refusal from a line that simply has not filled yet. ⭐ The arm makes the
 * rule something you look at rather than something you deduce.
 *
 * <p>⚠ A class of its own rather than a flag on {@link KindBlock}, because the six
 * properties are added by {@code createBlockStateDefinition}, which the block
 * constructor calls before any subclass has had a chance to set a field. A block that
 * asked "am I a cable?" there would be asking a field that is still null.
 */
public class CarrierBlock extends KindBlock {
    public static final MapCodec<CarrierBlock> CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                    Kind.CODEC.fieldOf("kind").forGetter(KindBlock::kind),
                    Tier.CODEC.fieldOf("tier").forGetter(KindBlock::tier),
                    propertiesCodec())
                    .apply(instance, CarrierBlock::new));

    /** Every shape it can have, one per set of sides it is joined on. */
    private final VoxelShape[] shapes;

    public CarrierBlock(Kind kind, Tier tier, Properties properties) {
        super(kind, tier, properties);
        BlockState bare = getStateDefinition().any();
        for (Direction side : Direction.values()) {
            bare = bare.setValue(PipeBlock.PROPERTY_BY_DIRECTION.get(side), false);
        }
        registerDefaultState(bare);
        this.shapes = shapes(kind.core());
    }

    @Override
    protected MapCodec<? extends KindBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        PipeBlock.PROPERTY_BY_DIRECTION.values().forEach(builder::add);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos,
            CollisionContext context) {
        return shapes[joined(state)];
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockState state = defaultBlockState();
        for (Direction side : Direction.values()) {
            state = state.setValue(PipeBlock.PROPERTY_BY_DIRECTION.get(side),
                    connects(context.getLevel(), context.getClickedPos(), side));
        }
        return state;
    }

    /**
     * One face, when what is on the other side of it changes.
     *
     * <p>⚠ Only where there is a whole {@link Level} to ask. What arrives during world
     * generation is a {@code LevelAccessor} with no capabilities behind it, and a
     * block that asked anyway would bake in an answer of "nothing is there".
     */
    @Override
    protected BlockState updateShape(BlockState state, Direction side, BlockState neighbour,
            LevelAccessor level, BlockPos pos, BlockPos neighbourPos) {
        return level instanceof Level whole
                ? state.setValue(PipeBlock.PROPERTY_BY_DIRECTION.get(side),
                        connects(whole, pos, side))
                : state;
    }

    /**
     * Whether energy can cross this face. The question is asked of the capability
     * rather than of the block, so a machine from any mod answers it the same way this
     * mod's own blocks do — and a block with no energy in it at all answers nothing,
     * which is the same as no.
     */
    private boolean connects(Level level, BlockPos pos, Direction side) {
        IEnergyStorage neighbour = level.getCapability(Capabilities.EnergyStorage.BLOCK,
                pos.relative(side), side.getOpposite());
        return neighbour != null && kind().touches(Neighbours.ours(neighbour));
    }

    private static int joined(BlockState state) {
        int sides = 0;
        for (Direction side : Direction.values()) {
            if (state.getValue(PipeBlock.PROPERTY_BY_DIRECTION.get(side))) {
                sides |= 1 << side.ordinal();
            }
        }
        return sides;
    }

    /**
     * The middle, and the middle with each combination of arms on it. Worked out once
     * at registration: there are sixty-four of them and the answer is asked for on
     * every block the player walks into.
     */
    private static VoxelShape[] shapes(int core) {
        int in = (Skins.SIZE - core) / 2;
        VoxelShape middle = Block.box(in, in, in, core + in, core + in, core + in);
        VoxelShape[] arms = new VoxelShape[Direction.values().length];
        for (Direction side : Direction.values()) {
            arms[side.ordinal()] = arm(side);
        }
        VoxelShape[] all = new VoxelShape[1 << Direction.values().length];
        for (int sides = 0; sides < all.length; sides++) {
            VoxelShape shape = middle;
            for (Direction side : Direction.values()) {
                if ((sides & 1 << side.ordinal()) != 0) {
                    shape = Shapes.or(shape, arms[side.ordinal()]);
                }
            }
            all[sides] = shape;
        }
        return all;
    }

    /** One arm: a square post reaching in from the face of the block it points at. */
    private static VoxelShape arm(Direction side) {
        int near = (Skins.SIZE - Skins.ARM_ACROSS) / 2;
        int far = near + Skins.ARM_ACROSS;
        int deep = Skins.ARM_DEEP;
        int back = Skins.SIZE - deep;
        return switch (side) {
            case NORTH -> Block.box(near, near, 0, far, far, deep);
            case SOUTH -> Block.box(near, near, back, far, far, Skins.SIZE);
            case WEST -> Block.box(0, near, near, deep, far, far);
            case EAST -> Block.box(back, near, near, Skins.SIZE, far, far);
            case DOWN -> Block.box(near, 0, near, far, deep, far);
            case UP -> Block.box(near, back, near, far, Skins.SIZE, far);
        };
    }
}
