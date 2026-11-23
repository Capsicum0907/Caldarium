package io.github.capsicum0907.caldarium.client;

import java.util.List;
import java.util.Optional;

import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
//? if >=1.20 {
import net.minecraft.client.gui.GuiGraphics;
//?} else {
/*import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.screens.Screen;
*///?}

final class Canvas {
    private static final int SHEET_SIZE = 256;

    //? if >=1.20 {
    private final GuiGraphics graphics;

    Canvas(GuiGraphics graphics) {
        this.graphics = graphics;
    }
    //?} else {
    /*private final PoseStack pose;
    private final Screen screen;

    Canvas(PoseStack pose, Screen screen) {
        this.pose = pose;
        this.screen = screen;
    }
    *///?}

    void blit(ResourceLocation sheet, int x, int y, int u, int v, int width, int height) {
        //? if >=1.20 {
        graphics.blit(sheet, x, y, u, v, width, height);
        //?} else {
        /*RenderSystem.setShaderTexture(0, sheet);
        GuiComponent.blit(pose, x, y, u, v, width, height, SHEET_SIZE, SHEET_SIZE);
        *///?}
    }

    void text(Font font, Component text, int x, int y, int colour) {
        //? if >=1.20 {
        graphics.drawString(font, text, x, y, colour, false);
        //?} else
        //font.draw(pose, text, x, y, colour);
    }

    void tooltip(Font font, Component line, int x, int y) {
        //? if >=1.20 {
        graphics.renderTooltip(font, line, x, y);
        //?} else
        //screen.renderTooltip(pose, line, x, y);
    }

    void tooltip(Font font, List<Component> lines, Optional<TooltipComponent> image, int x, int y) {
        //? if >=1.20 {
        graphics.renderTooltip(font, lines, image, x, y);
        //?} else
        //screen.renderTooltip(pose, lines, image, x, y);
    }
}
