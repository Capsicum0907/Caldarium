package io.github.capsicum0907.caldarium;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

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
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.energy.IEnergyStorage;

/**
 * A {@link Kind} that is laid in lines rather than stood somewhere: a cable, and the
 * two doors at the ends of one.
 *
 * <p><b>It grows an arm towards every neighbour energy can cross to, and a drill where
 * that neighbour is outside the line.</b> That is the whole of what this class adds,
 * and it is not decoration. These blocks have no window to open, no wrench and nothing
 * to configure, and the rule they follow refuses some neighbours on purpose — a cable
 * will not offer to another mod's machine, which is what lets one be laid past it.
 * Without the arm there is nothing to tell that refusal from a line that has not
 * filled yet; without the drill there is nothing to say which face of a door is the
 * one doing the importing. ⭐ Both make a rule into something you look at.
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

    /** What each face is joined to. The names are the vanilla ones for the six sides. */
    public static final Map<Direction, EnumProperty<Joint>> JOINTS = joints();

    /**
     * Every shape a carrier of a given build can have, worked out once and shared by
     * every tier of it: what a block is shaped like depends on its middle and on
     * whether it draws drills, and not at all on which rung it stands on.
     */
    private static final Map<Integer, VoxelShape[]> SHAPES = new ConcurrentHashMap<>();

    private final VoxelShape[] shapes;

    public CarrierBlock(Kind kind, Tier tier, Properties properties) {
        super(kind, tier, properties);
        BlockState bare = getStateDefinition().any();
        for (Direction side : Direction.values()) {
            bare = bare.setValue(JOINTS.get(side), Joint.NONE);
        }
        registerDefaultState(bare);
        this.shapes = SHAPES.computeIfAbsent(kind.core() * 2 + (kind.door() ? 1 : 0),
                build -> shapes(build / 2, build % 2 == 1));
    }

    @Override
    protected MapCodec<? extends KindBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        JOINTS.values().forEach(builder::add);
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
            state = state.setValue(JOINTS.get(side),
                    joint(context.getLevel(), context.getClickedPos(), side));
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
                ? state.setValue(JOINTS.get(side), joint(whole, pos, side))
                : state;
    }

    /**
     * What this face is joined to. The question is asked of the capability rather than
     * of the block, so a machine from any mod answers it the same way this mod's own
     * blocks do — and a block with no energy in it at all answers nothing, which is
     * the same as no.
     */
    private Joint joint(Level level, BlockPos pos, Direction side) {
        IEnergyStorage neighbour = level.getCapability(Capabilities.EnergyStorage.BLOCK,
                pos.relative(side), side.getOpposite());
        if (neighbour == null || !kind().touches(neighbour)) {
            return Joint.NONE;
        }
        return Neighbours.inLine(neighbour) ? Joint.LINE : Joint.OUTSIDE;
    }

    /** The six faces, read as one number: the index of the shape they add up to. */
    private static int joined(BlockState state) {
        int index = 0;
        for (Direction side : Direction.values()) {
            index = index * Joint.values().length + state.getValue(JOINTS.get(side)).ordinal();
        }
        return index;
    }

    /**
     * The middle, and the middle with every combination of arms and drills on it.
     * Worked out once at registration: there are seven hundred and twenty-nine of them
     * and the answer is asked for on every block the player walks into.
     */
    private static VoxelShape[] shapes(int core, boolean drills) {
        VoxelShape middle = box(Skins.middleBox(core));
        VoxelShape[] along = new VoxelShape[Direction.values().length];
        VoxelShape[] outward = new VoxelShape[Direction.values().length];
        for (Direction side : Direction.values()) {
            along[side.ordinal()] = box(Skins.armBox(side));
            outward[side.ordinal()] = drills ? drill(side) : along[side.ordinal()];
        }

        Joint[] joints = Joint.values();
        int count = 1;
        for (int side = 0; side < Direction.values().length; side++) {
            count *= joints.length;
        }
        VoxelShape[] all = new VoxelShape[count];
        for (int index = 0; index < count; index++) {
            VoxelShape shape = middle;
            int rest = index;
            // ⚠ Unpicked in the reverse order of joined(), which builds the number by
            // multiplying up through the sides in order.
            for (int at = Direction.values().length - 1; at >= 0; at--) {
                Joint joint = joints[rest % joints.length];
                rest /= joints.length;
                if (joint == Joint.LINE) {
                    shape = Shapes.or(shape, along[at]);
                } else if (joint == Joint.OUTSIDE) {
                    shape = Shapes.or(shape, outward[at]);
                }
            }
            all[index] = shape.optimize();
        }
        return all;
    }

    /** Square steps widening towards the face of the block. */
    private static VoxelShape drill(Direction side) {
        VoxelShape shape = Shapes.empty();
        for (int step = 0; step < Skins.DRILL_STEPS; step++) {
            shape = Shapes.or(shape, box(Skins.drillBox(side, step)));
        }
        return shape;
    }

    private static VoxelShape box(int[] corners) {
        return Block.box(corners[0], corners[1], corners[2],
                corners[3], corners[4], corners[5]);
    }

    private static Map<Direction, EnumProperty<Joint>> joints() {
        Map<Direction, EnumProperty<Joint>> sides = new EnumMap<>(Direction.class);
        for (Direction side : Direction.values()) {
            sides.put(side, EnumProperty.create(side.getSerializedName(), Joint.class));
        }
        return Collections.unmodifiableMap(sides);
    }
}
