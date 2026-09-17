package io.github.capsicum0907.caldarium;

import java.util.List;

import io.github.capsicum0907.caldarium.data.Skins;

import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.items.SlotItemHandler;

/**
 * One menu for every machine in the mod. What differs between them is how many slots
 * there are and whether something is burning, and both are questions the block
 * entity answers through {@link Machine}.
 *
 * <p>A menu per block would be the same class registered once per set of numbers -
 * the same reason there is one block entity type for every kind.
 */
public class MachineMenu extends AbstractContainerMenu {
    private final ContainerLevelAccess access;
    private final ContainerData data;
    private final boolean burns;
    private final boolean pours;
    private final int machineSlots;

    /** The client's side: what the screen has to know arrives in the buffer. */
    public MachineMenu(int id, Inventory inventory, RegistryFriendlyByteBuf buffer) {
        this(id, inventory, ContainerLevelAccess.NULL, buffer.readBoolean(),
                new ItemStackHandler(buffer.readByte()), buffer.readBoolean(),
                new SimpleContainerData(MachineData.SIZE));
    }

    public MachineMenu(int id, Inventory inventory, ContainerLevelAccess access, boolean burns,
            IItemHandler machine, boolean pours, ContainerData data) {
        super(CaldariumRegistry.MACHINE_MENU.get(), id);
        this.access = access;
        this.data = data;
        this.burns = burns;
        this.pours = pours;
        this.machineSlots = machine.getSlots();

        for (int slot = 0; slot < machineSlots; slot++) {
            addSlot(new SlotItemHandler(machine, slot,
                    Skins.slotX(slot, machineSlots), Skins.slotY(slot, machineSlots)));
        }
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                addSlot(new Slot(inventory, column + row * 9 + 9,
                        Skins.INVENTORY_X + column * Skins.SLOT,
                        Skins.INVENTORY_Y + row * Skins.SLOT));
            }
        }
        for (int column = 0; column < 9; column++) {
            addSlot(new Slot(inventory, column,
                    Skins.INVENTORY_X + column * Skins.SLOT, Skins.HOTBAR_Y));
        }

        addDataSlots(data);
    }

    public boolean burns() {
        return burns;
    }

    public boolean pours() {
        return pours;
    }

    @Override
    public boolean clickMenuButton(Player player, int id) {
        int points = pouring(id);
        if (points == 0) {
            return false;
        }
        access.execute((level, pos) -> {
            if (level.getBlockEntity(pos) instanceof GeneratorBlockEntity generator) {
                generator.pour(player, points);
            }
        });
        return true;
    }

    public static int pourings() {
        return CaldariumConfig.pourSteps().size() + 1;
    }

    public static int pouring(int id) {
        List<Integer> steps = CaldariumConfig.pourSteps();
        if (id >= 0 && id < steps.size()) {
            return steps.get(id);
        }
        return id == steps.size() ? -1 : 0;
    }

    public int machineSlots() {
        return machineSlots;
    }

    public int energy() {
        return MachineData.whole(data, MachineData.ENERGY);
    }

    public int capacity() {
        return MachineData.whole(data, MachineData.CAPACITY);
    }

    /** Ticks of burning left in the piece of fuel that is alight. */
    public int burningTicks() {
        return MachineData.whole(data, MachineData.BURNING);
    }

    /** How much of the burning piece of fuel is left, as a fraction. */
    public float burned() {
        int length = MachineData.whole(data, MachineData.BURN_LENGTH);
        return length <= 0 ? 0.0F : MachineData.whole(data, MachineData.BURNING) / (float) length;
    }

    public int fluid() {
        return MachineData.whole(data, MachineData.FLUID);
    }

    public int fluidCapacity() {
        return MachineData.whole(data, MachineData.FLUID_CAPACITY);
    }

    public float filled() {
        int capacity = fluidCapacity();
        return capacity <= 0 ? 0.0F : Math.min(1.0F, fluid() / (float) capacity);
    }

    public float charged() {
        int capacity = capacity();
        return capacity <= 0 ? 0.0F : Math.min(1.0F, energy() / (float) capacity);
    }

    /**
     * Shift-clicking. The machine's own slots are offered first when something comes
     * from the inventory, and the inventory when something comes out of the machine.
     */
    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = slots.get(index);
        if (!slot.hasItem()) {
            return ItemStack.EMPTY;
        }
        ItemStack inSlot = slot.getItem();
        ItemStack before = inSlot.copy();

        if (index < machineSlots) {
            if (!moveItemStackTo(inSlot, machineSlots, slots.size(), true)) {
                return ItemStack.EMPTY;
            }
        } else if (machineSlots > 0 && moveItemStackTo(inSlot, 0, machineSlots, false)) {
            // Went into the machine.
        } else {
            // Between the inventory and the hotbar, the way every other screen does it.
            int inventoryEnd = machineSlots + 27;
            boolean fromInventory = index < inventoryEnd;
            int from = fromInventory ? inventoryEnd : machineSlots;
            int to = fromInventory ? slots.size() : inventoryEnd;
            if (!moveItemStackTo(inSlot, from, to, false)) {
                return ItemStack.EMPTY;
            }
        }

        if (inSlot.isEmpty()) {
            slot.setByPlayer(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }
        return before;
    }

    @Override
    public boolean stillValid(Player player) {
        return access.evaluate(
                (level, pos) -> level.getBlockEntity(pos) instanceof Machine
                        && player.distanceToSqr(pos.getX() + 0.5, pos.getY() + 0.5,
                                pos.getZ() + 0.5) <= 64.0,
                true);
    }

    /**
     * Opening one. Every block does exactly this, so it is written once here rather
     * than once per block in files that would then be free to drift apart.
     */
    public static InteractionResult open(Level level, BlockPos pos, Player player) {
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        if (level.getBlockEntity(pos) instanceof MenuProvider provider
                && provider instanceof Machine machine) {
            player.openMenu(provider, buffer -> {
                buffer.writeBoolean(machine.burns());
                buffer.writeByte(machine.machineSlots().getSlots());
                buffer.writeBoolean(machine.pours());
            });
        }
        return InteractionResult.CONSUME;
    }

    /** Where the block is, for anything that has to know. Absent on the client. */
    public BlockPos where() {
        return access.evaluate((level, pos) -> pos, BlockPos.ZERO);
    }
}
