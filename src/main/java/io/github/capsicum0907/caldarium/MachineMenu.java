package io.github.capsicum0907.caldarium;

import io.github.capsicum0907.caldarium.data.Skins;

import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.level.Level;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.items.SlotItemHandler;

/**
 * One menu for every machine in the mod. What differs between them is whether there
 * is a slot to put something in, and that is a question the block entity answers.
 *
 * <p>A menu per block would be the same class registered once per set of numbers —
 * the same reason there is one block entity type for every generator.
 */
public class MachineMenu extends AbstractContainerMenu {
    /** Where the machine is, so the screen can name it and the game can close it. */
    private final ContainerLevelAccess access;
    private final ContainerData data;
    private final boolean burns;

    /** The client's side: the slot count arrives in the buffer, the numbers follow. */
    public MachineMenu(int id, Inventory inventory, RegistryFriendlyByteBuf buffer) {
        this(id, inventory, ContainerLevelAccess.NULL, buffer.readBoolean(),
                new ItemStackHandler(1), new SimpleContainerData(MachineData.SIZE));
    }

    public MachineMenu(int id, Inventory inventory, ContainerLevelAccess access, boolean burns,
            IItemHandler fuel, ContainerData data) {
        super(CaldariumRegistry.MACHINE_MENU.get(), id);
        this.access = access;
        this.data = data;
        this.burns = burns;

        if (burns) {
            addSlot(new SlotItemHandler(fuel, 0, Skins.FUEL_SLOT_X, Skins.FUEL_SLOT_Y));
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

    public int energy() {
        return MachineData.whole(data, MachineData.ENERGY);
    }

    public int capacity() {
        return MachineData.whole(data, MachineData.CAPACITY);
    }

    /** How much of the burning piece of fuel is left, as a fraction. */
    public float burned() {
        int length = MachineData.whole(data, MachineData.BURN_LENGTH);
        return length <= 0 ? 0.0F : MachineData.whole(data, MachineData.BURNING) / (float) length;
    }

    public float charged() {
        int capacity = capacity();
        return capacity <= 0 ? 0.0F : Math.min(1.0F, energy() / (float) capacity);
    }

    /**
     * Shift-clicking. The machine's own slot is offered first when something comes
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
        int machineSlots = burns ? 1 : 0;

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
                (level, pos) -> isMachine(level.getBlockEntity(pos))
                        && player.distanceToSqr(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5) <= 64.0,
                true);
    }

    private static boolean isMachine(BlockEntity entity) {
        return entity instanceof GeneratorBlockEntity || entity instanceof BatteryBlockEntity;
    }

    /**
     * Opening one. Both blocks do exactly this, so it is written once here rather
     * than twice in two blocks that would then be free to drift apart.
     *
     * <p>Whether there is a slot travels in the buffer: the client builds its own
     * copy of the menu before it has seen the block entity, so it has to be told.
     */
    public static InteractionResult open(Level level, BlockPos pos, Player player, boolean burns) {
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        if (level.getBlockEntity(pos) instanceof MenuProvider provider) {
            player.openMenu(provider, buffer -> buffer.writeBoolean(burns));
        }
        return InteractionResult.CONSUME;
    }

    /** Where the block is, for the screen's title. Absent on the client's copy. */
    public BlockPos where() {
        return access.evaluate((level, pos) -> pos, BlockPos.ZERO);
    }
}
