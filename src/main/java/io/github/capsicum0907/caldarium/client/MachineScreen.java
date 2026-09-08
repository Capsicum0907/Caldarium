package io.github.capsicum0907.caldarium.client;

import io.github.capsicum0907.caldarium.Caldarium;
import io.github.capsicum0907.caldarium.MachineMenu;
import io.github.capsicum0907.caldarium.data.Skins;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

/**
 * The one screen. It draws a bar for what is stored and, when the machine burns, a
 * flame for what is left of the fuel — a furnace, in other words, because that is
 * the screen every player already knows how to read.
 *
 * <p>Every coordinate comes from {@link Skins}, which is also what painted the
 * picture underneath. Nothing here is measured against the image by eye.
 */
public class MachineScreen extends AbstractContainerScreen<MachineMenu> {
    private static final ResourceLocation SHEET = ResourceLocation
            .fromNamespaceAndPath(Caldarium.MODID, "textures/gui/" + Skins.GUI + ".png");

    /**
     * The furnace's own flame, borrowed rather than copied.
     *
     * <p>Naming vanilla's sprite ships none of vanilla's art with this mod, and it
     * follows a resource pack: somebody who has restyled the furnace has restyled
     * this too, without knowing this mod exists.
     */
    private static final ResourceLocation FLAME =
            ResourceLocation.withDefaultNamespace("container/furnace/lit_progress");



    @Override
    protected void init() {
        super.init();
        if (!menu.pours()) {
            return;
        }
        int step = Skins.POUR_W + Skins.POUR_GAP;
        int left = leftPos + (imageWidth - (step * 3 - Skins.POUR_GAP)) / 2;
        int y = topPos + Skins.POUR_Y;
        pour(left, y, 0, "gui.caldarium.pour.one");
        pour(left + step, y, 1, "gui.caldarium.pour.ten");
        pour(left + step * 2, y, 2, "gui.caldarium.pour.all");
    }

    private void pour(int x, int y, int id, String key) {
        addRenderableWidget(net.minecraft.client.gui.components.Button
                .builder(Component.translatable(key), pressed -> {
                    if (minecraft != null && minecraft.gameMode != null) {
                        minecraft.gameMode.handleInventoryButtonClick(menu.containerId, id);
                    }
                })
                .bounds(x, y, Skins.POUR_W, Skins.POUR_H)
                .build());
    }

    public MachineScreen(MachineMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.imageWidth = Skins.GUI_WIDTH;
        this.imageHeight = Skins.GUI_HEIGHT;
        this.inventoryLabelY = Skins.INVENTORY_Y - 12;
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.blit(SHEET, leftPos, topPos, 0, 0, imageWidth, imageHeight);

        // Both overlays grow from the bottom, so the part shown is the bottom of the
        // strip rather than the whole of it squashed.
        int charged = Math.round(Skins.BAR_H * menu.charged());
        if (charged > 0) {
            graphics.blit(SHEET, leftPos + Skins.BAR_X, topPos + Skins.BAR_Y + Skins.BAR_H - charged,
                    Skins.BAR_U, Skins.BAR_V + Skins.BAR_H - charged, Skins.BAR_W, charged);
        }
        // How many slots there are is the machine's business, so the panel does not
        // carry them: it is shared, and a battery drawn with a burner's fuel slot was
        // showing a hollow that took nothing.
        int count = menu.machineSlots();
        for (int slot = 0; slot < count; slot++) {
            graphics.blit(SHEET, leftPos + Skins.slotX(slot, count) - 1,
                    topPos + Skins.slotY(slot, count) - 1,
                    Skins.FUEL_WELL_U, Skins.FUEL_WELL_V, Skins.SLOT, Skins.SLOT);
        }
        // Only a machine with a tank has one drawn; the rest of the panel is shared.
        if (menu.fluidCapacity() > 0) {
            graphics.blit(SHEET, leftPos + Skins.TANK_X - 1, topPos + Skins.TANK_Y - 1,
                    Skins.TANK_WELL_U, Skins.TANK_WELL_V, Skins.TANK_WELL_W, Skins.TANK_WELL_H);
            int filled = Math.round(Skins.BAR_H * menu.filled());
            if (filled > 0) {
                graphics.blit(SHEET, leftPos + Skins.TANK_X,
                        topPos + Skins.TANK_Y + Skins.BAR_H - filled,
                        Skins.TANK_U, Skins.TANK_V + Skins.BAR_H - filled, Skins.BAR_W, filled);
            }
        }
        if (menu.burns()) {
            // ⚠ Only the burning part, and nothing at all underneath it.
            //
            // The sprite is not a flame cut out of transparency: all fourteen by
            // fourteen pixels are opaque, and the ones that are not flame are the
            // panel colours - 0xC6C6C6 for the face and 0x8B8B8B for a recess, which
            // are the two this mod paints its own panel with. It is a tile meant to
            // be laid on a panel, and laying it on this one is seamless.
            //
            // So there is no silhouette to darken. Drawing the whole sprite dark put
            // a grey box on the panel, which is what it looked like. Vanilla shows an
            // unlit flame above the burning one because its background image has one
            // painted in; this one simply shows nothing until something is burning.
            int flame = Math.round(Skins.FLAME_H * menu.burned());
            if (flame > 0) {
                graphics.blitSprite(FLAME, Skins.FLAME_W, Skins.FLAME_H,
                        0, Skins.FLAME_H - flame,
                        leftPos + Skins.FLAME_X, topPos + Skins.FLAME_Y + Skins.FLAME_H - flame,
                        Skins.FLAME_W, flame);
            }
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        if (menu.pours()) {
            Component left = Component.translatable("gui.caldarium.burning",
                    menu.burningTicks() / 20);
            graphics.drawString(font, left,
                    leftPos + (imageWidth - font.width(left)) / 2,
                    topPos + Skins.POUR_Y + Skins.POUR_H + 4, 0x404040, false);
        }
        if (over(mouseX, mouseY, Skins.BAR_X, Skins.BAR_Y, Skins.BAR_W, Skins.BAR_H)) {
            // The exact numbers, because the bar only ever says roughly.
            graphics.renderTooltip(font, Component.translatable("gui.caldarium.stored",
                    count(menu.energy()), count(menu.capacity())), mouseX, mouseY);
            return;
        }
        if (menu.fluidCapacity() > 0
                && over(mouseX, mouseY, Skins.TANK_X, Skins.TANK_Y, Skins.BAR_W, Skins.BAR_H)) {
            graphics.renderTooltip(font, Component.translatable("gui.caldarium.held",
                    count(menu.fluid()), count(menu.fluidCapacity())), mouseX, mouseY);
            return;
        }
        // How long the fire has left, on the fire and on the slot the fuel goes in.
        if (menu.burns() && menu.burningTicks() > 0) {
            Component line = Component.translatable("gui.caldarium.burning",
                    left(menu.burningTicks()));
            if (over(mouseX, mouseY, Skins.FLAME_X, Skins.FLAME_Y, Skins.FLAME_W, Skins.FLAME_H)) {
                graphics.renderTooltip(font, line, mouseX, mouseY);
                return;
            }
            // ⚠ On the slot it is added to what the game was going to say rather than
            // put in place of it. Restricting it to an empty slot, as it was, made it
            // almost useless: a burner that is alight has fuel in that slot, so the
            // one moment the line is worth reading was the one it was hidden in.
            if (onMachineSlot()) {
                List<Component> lines =
                        new ArrayList<>(getTooltipFromContainerItem(hoveredSlot.getItem()));
                lines.add(line);
                graphics.renderTooltip(font, lines,
                        hoveredSlot.getItem().getTooltipImage(), mouseX, mouseY);
                return;
            }
        }
        renderTooltip(graphics, mouseX, mouseY);
    }

    /** Whether the cursor is on one of the machine's own slots. */
    private boolean onMachineSlot() {
        return hoveredSlot != null && menu.slots.indexOf(hoveredSlot) < menu.machineSlots();
    }

    private boolean over(int mouseX, int mouseY, int x, int y, int width, int height) {
        return mouseX >= leftPos + x && mouseX < leftPos + x + width
                && mouseY >= topPos + y && mouseY < topPos + y + height;
    }

    /** Grouped in threes: six figures of Forge Energy are unreadable otherwise. */
    private static String count(int amount) {
        return String.format("%,d", amount);
    }

    /**
     * Ticks, as a length of time a person can read. A bucket of lava is twenty
     * thousand of them, which is sixteen minutes and forty seconds and is not a
     * number anybody wants to be told in ticks.
     *
     * <p>Locale.ROOT for the decimal point: the separator moves with the language,
     * and a fire with 12,4 seconds left reads as a mistake to half its readers.
     */
    private static String left(int ticks) {
        int seconds = ticks / 20;
        return seconds >= 60
                ? String.format(Locale.ROOT, "%d:%02d", seconds / 60, seconds % 60)
                : String.format(Locale.ROOT, "%.1fs", ticks / 20.0F);
    }
}
