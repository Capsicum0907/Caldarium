package io.github.capsicum0907.caldarium;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.items.ItemStackHandler;

/**
 * Every generator. What differs between them is what they draw on, and that is a
 * question the {@link Source} on the row answers.
 *
 * <p>The two that burn share the whole of their machinery and differ only in where a
 * piece of fuel comes from: a slot, or a tank. The one that does not burn keeps
 * nothing at all, because there is nothing to keep.
 */
public class GeneratorBlockEntity extends BlockEntity implements MenuProvider, Machine {
    /** A bucket, which is the unit a fluid fuel is measured in. */
    private static final int DRAUGHT = 1000;

    /** How often the sky is looked at. Twenty times a second is twenty times too many. */
    private static final int SUN_EVERY = 20;

    private final Generator.Made row;
    private final CaldariumConfig.Rates rates;
    private final Store store;
    private final Neighbours sides = new Neighbours();
    private final ItemStackHandler fuel;
    private final FuelTank tank;
    private final MachineData data;

    private int burning;
    private int burnLength;

    /** Measured now and then rather than every tick; see {@link #SUN_EVERY}. */
    private int lookAgain;
    private float reaching;

    private static final long FINE = 4_096L;

    private long spare;

    public GeneratorBlockEntity(BlockPos pos, BlockState state) {
        super(CaldariumRegistry.GENERATOR_ENTITY.get(), pos, state);
        this.row = ((GeneratorBlock) state.getBlock()).row();
        this.rates = CaldariumConfig.GENERATORS.get(row);
        this.store = new Store(Store.Role.SOURCE, Wiring.OPEN,
                () -> rates.capacity().get(), () -> rates.transfer().get(), this::setChanged);
        this.tank = row.source() == Source.FLUID
                ? new FuelTank(CaldariumConfig.tank(row), this::setChanged)
                : null;
        this.data = new MachineData(store::getEnergyStored, store::getMaxEnergyStored,
                () -> burning, () -> burnLength,
                () -> tank == null ? 0 : tank.getFluidAmount(),
                () -> tank == null ? 0 : tank.getCapacity());
        // Sized by the source: a generator with no slot offers no item handler at all,
        // rather than an empty one a hopper would still line itself up against.
        this.fuel = new ItemStackHandler(row.source() == Source.ITEM ? 1 : 0) {
            @Override
            public boolean isItemValid(int slot, ItemStack stack) {
                return Fuel.FURNACE.accepts(stack);
            }

            /**
             * Fuel goes in and does not come back out. Without this a hopper set under
             * the burner — the arrangement everybody builds under a furnace — pulls the
             * coal straight back out of it, and the generator never runs.
             *
             * <p>What is <em>not</em> fuel may still be taken, which is how the empty
             * bucket a lava bucket leaves behind gets collected.
             */
            @Override
            public ItemStack extractItem(int slot, int amount, boolean simulate) {
                return Fuel.FURNACE.accepts(getStackInSlot(slot))
                        ? ItemStack.EMPTY
                        : super.extractItem(slot, amount, simulate);
            }

            @Override
            protected void onContentsChanged(int slot) {
                setChanged();
            }
        };
    }

    public Store store() {
        return store;
    }

    /** Null when this generator has no slot, so no item handler is offered at all. */
    public ItemStackHandler fuel() {
        return fuel.getSlots() == 0 ? null : fuel;
    }

    /** Null when this generator has no tank, for the same reason. */
    public FuelTank tank() {
        return tank;
    }

    public FluidStack held() {
        return tank == null ? FluidStack.EMPTY : tank.getFluid();
    }

    /**
     * What standing on it costs. Nothing is paid unless it died: a thing that shrugs
     * the blow off has lost no health, and a thing that cannot die never will.
     */
    public int reap(ServerLevel level, LivingEntity living) {
        if (row.source() != Source.LIFE) {
            return 0;
        }
        float had = living.getHealth();
        if (had <= 0.0F || living.isDeadOrDying() || store.isFull()
                || living.getMaxHealth() > CaldariumConfig.lethalHealth(row)) {
            return 0;
        }
        living.hurt(level.damageSources().generic(), Float.MAX_VALUE);
        if (!living.isDeadOrDying()) {
            return 0;
        }
        give(had);
        setChanged();
        return Math.round(had);
    }

    /**
     * What a blow was worth.
     *
     * <p>The blow is worked out the way the game works one out - the attack damage the
     * player has, scaled by how far the swing has recovered, which is
     * {@code 0.2 + scale * scale * 0.8} in {@code Player#attack}. ⚠ <b>Enchantments are
     * not in it</b>: the game asks {@code getEnchantedDamage} with the thing being hit,
     * and there is nothing being hit here. So this measures the arm and the weapon and
     * not the sharpness on it.
     *
     * <p>⚠ The swing is reset afterwards, or the same click held down would land at
     * full strength every time and the cooldown would be decoration.
     */
    public int hit(Player player) {
        if (row.source() != Source.BLOW) {
            return 0;
        }
        float damage = (float) player.getAttributeValue(Attributes.ATTACK_DAMAGE);
        float scale = player.getAttackStrengthScale(0.5F);
        player.resetAttackStrengthTicker();
        float blow = damage * (0.2F + scale * scale * 0.8F);
        if (blow <= 0.0F) {
            return 0;
        }
        long worth = (long) (blow * rates.makes().get());
        int given = (int) Math.min(Integer.MAX_VALUE, worth);
        store.fill(given);
        setChanged();
        return given;
    }

    /** What a strike is worth here. Whatever will not fit is lost, as it would be. */
    public int strike(boolean summoned) {
        if (row.source() != Source.STORM) {
            return 0;
        }
        int worth = summoned ? CaldariumConfig.stormSummoned() : CaldariumConfig.stormNatural();
        if (worth <= 0) {
            return 0;
        }
        store.fill(worth);
        setChanged();
        return worth;
    }

    @Override
    public boolean pours() {
        return row.source() == Source.EXPERIENCE;
    }

    /** Experience points off the player and into the fire. A negative amount means all of it. */
    public int pour(Player player, int points) {
        if (!pours()) {
            return 0;
        }
        int wanted = points < 0 ? Experience.points(player) : points;
        int each = Math.max(1, rates.makes().get());
        int taken = Experience.take(player, Math.min(wanted, store.room() / each));
        if (taken <= 0) {
            return 0;
        }
        store.fill((int) Math.min(Integer.MAX_VALUE, (long) taken * each));
        setChanged();
        return taken;
    }

    @Override
    public boolean burns() {
        return row.source().burns();
    }

    @Override
    public ItemStackHandler machineSlots() {
        return fuel;
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state,
            GeneratorBlockEntity generator) {
        if (!(level instanceof ServerLevel server)) {
            return;
        }
        boolean wasWorking = generator.working();

        switch (generator.row.source()) {
            case ITEM, FLUID -> generator.burn();
            case SUN, HEAT, LAMP -> generator.soak(server, pos);
            case EXPERIENCE, LIFE, STORM, BLOW -> { }
        }
        Pushing.push(generator.sides, server, pos, generator.store, null, false);

        boolean working = generator.working();
        if (wasWorking != working) {
            level.setBlock(pos, state.setValue(GeneratorBlock.LIT, working), Block.UPDATE_ALL);
        }
    }

    /** Ticks of fuel left. Read by the game tests, which share this package. */
    int burning() {
        return burning;
    }

    /** Whether it is doing its work, whatever that work is. */
    private boolean working() {
        return row.source().stored() ? burning > 0 : reaching > 0.0F;
    }

    /**
     * Burning continues even once full, and the surplus is lost. The alternative is a
     * generator that stops mid-log and resumes, which means remembering a fraction of
     * a piece of fuel; a furnace does not do that either.
     */
    private void burn() {
        if (burning > 0) {
            burning--;
            give(1.0F);
        }
        if (burning <= 0 && !store.isFull()) {
            light();
        }
    }

    private void light() {
        int ticks = switch (row.source()) {
            case FLUID -> draw();
            case ITEM -> take();
            default -> 0;
        };
        if (ticks <= 0) {
            return;
        }
        burning = ticks;
        burnLength = ticks;
        setChanged();
    }

    /**
     * One piece of fuel out of the slot. The remainder is put back rather than thrown
     * away: a bucket of lava that burns leaves a bucket, and a generator that ate it
     * would be a worse machine than a furnace.
     */
    private int take() {
        ItemStack stack = fuel.getStackInSlot(0);
        int ticks = Fuel.FURNACE.burnTicks(stack);
        if (ticks <= 0) {
            return 0;
        }
        ItemStack remainder = stack.getCraftingRemainingItem();
        stack.shrink(1);
        if (stack.isEmpty() && !remainder.isEmpty()) {
            fuel.setStackInSlot(0, remainder);
        }
        return ticks;
    }

    /**
     * A bucket out of the tank, and nothing at all if there is less than that in it.
     * Burning half a draught for half as long would be defensible, but it puts a
     * fraction into the saved state to no visible end.
     */
    private int draw() {
        if (tank == null || tank.getFluidAmount() < DRAUGHT) {
            return 0;
        }
        int ticks = Fuel.burnTicks(tank.getFluid().getFluid());
        if (ticks <= 0) {
            return 0;
        }
        tank.spend(DRAUGHT);
        return ticks;
    }

    private void soak(ServerLevel level, BlockPos pos) {
        if (--lookAgain <= 0) {
            lookAgain = SUN_EVERY;
            reaching = switch (row.source()) {
                case SUN -> Sunlight.reaching(level, pos);
                case HEAT -> Heat.reaching(level, pos);
                case LAMP -> Lamplight.reaching(level, pos);
                default -> 0.0F;
            };
        }
        if (reaching > 0.0F) {
            give(reaching);
        }
    }

    /**
     * A share of one helping.
     *
     * <p>A row says it makes so much every so many ticks, which is how a rate below one
     * a tick gets written down at all while the amount stays a whole number.
     *
     * <p>Nothing is ever rounded up, and the one place anything is dropped is here, on
     * the way out. Truncating each tick instead would leave the panel that reads
     * lamplight making nothing at all below full light.
     */
    private void give(float share) {
        long rate = rates.makes().get();
        long every = Math.max(1, rates.every().get());
        spare += (long) (rate * (double) share * FINE / every);
        long whole = spare / FINE;
        if (whole > 0) {
            store.fill((int) Math.min(Integer.MAX_VALUE, whole));
            spare -= whole * FINE;
        }
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable(getBlockState().getBlock().getDescriptionId());
    }

    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
        return new MachineMenu(id, inventory,
                ContainerLevelAccess.create(level, worldPosition), burns(), fuel, pours(), data);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt("Energy", store.raw());
        tag.putInt("Burning", burning);
        tag.putInt("BurnLength", burnLength);
        if (fuel.getSlots() > 0) {
            tag.put("Fuel", fuel.serializeNBT(registries));
        }
        if (tank != null) {
            tag.put("Tank", tank.writeToNBT(registries, new CompoundTag()));
        }
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        store.restore(tag.getInt("Energy"));
        burning = tag.getInt("Burning");
        burnLength = tag.getInt("BurnLength");
        if (tag.contains("Fuel") && fuel.getSlots() > 0) {
            fuel.deserializeNBT(registries, tag.getCompound("Fuel"));
        }
        if (tag.contains("Tank") && tank != null) {
            tank.readFromNBT(registries, tag.getCompound("Tank"));
        }
    }
}
