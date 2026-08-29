package io.github.capsicum0907.caldarium.client;

import io.github.capsicum0907.caldarium.Caldarium;
import io.github.capsicum0907.caldarium.MachineMenu;
import io.github.capsicum0907.caldarium.data.Skins;

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
        if (menu.burns()) {
            graphics.blit(SHEET, leftPos + Skins.FLAME_X, topPos + Skins.FLAME_Y,
                    Skins.FLAME_WELL_U, Skins.FLAME_WELL_V, Skins.FLAME_W, Skins.FLAME_H);

            int flame = Math.round(Skins.FLAME_H * menu.burned());
            if (flame > 0) {
                graphics.blit(SHEET, leftPos + Skins.FLAME_X,
                        topPos + Skins.FLAME_Y + Skins.FLAME_H - flame,
                        Skins.FLAME_U, Skins.FLAME_V + Skins.FLAME_H - flame, Skins.FLAME_W, flame);
            }
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        if (over(mouseX, mouseY, Skins.BAR_X, Skins.BAR_Y, Skins.BAR_W, Skins.BAR_H)) {
            // The exact numbers, because the bar only ever says roughly.
            graphics.renderTooltip(font, Component.translatable("gui.caldarium.stored",
                    count(menu.energy()), count(menu.capacity())), mouseX, mouseY);
            return;
        }
        renderTooltip(graphics, mouseX, mouseY);
    }

    private boolean over(int mouseX, int mouseY, int x, int y, int width, int height) {
        return mouseX >= leftPos + x && mouseX < leftPos + x + width
                && mouseY >= topPos + y && mouseY < topPos + y + height;
    }

    /** Grouped in threes: six figures of Forge Energy are unreadable otherwise. */
    private static String count(int amount) {
        return String.format("%,d", amount);
    }
}
