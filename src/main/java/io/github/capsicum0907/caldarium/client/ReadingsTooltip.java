package io.github.capsicum0907.caldarium.client;

import org.joml.Matrix4f;

import io.github.capsicum0907.caldarium.Tooltips;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.resources.language.I18n;

public record ReadingsTooltip(Tooltips.Readings readings) implements ClientTooltipComponent {
    private static final int LINE = 10;
    private static final int GAP = 6;
    private static final char POINT = '.';
    private static final int COLOUR = ChatFormatting.GRAY.getColor();

    private record Columns(int label, int whole, int fraction, int space, int unit) {
        int width() {
            return label + GAP + whole + fraction + space + unit;
        }
    }

    @Override
    public int getHeight() {
        return readings.rows().size() * LINE;
    }

    @Override
    public int getWidth(Font font) {
        return columns(font).width();
    }

    @Override
    public void renderText(Font font, int x, int y, Matrix4f matrix, MultiBufferSource.BufferSource buffer) {
        Columns columns = columns(font);
        int top = y;
        for (Tooltips.Row row : readings.rows()) {
            String whole = whole(row.amount());
            int point = x + columns.label() + GAP + columns.whole();
            draw(font, I18n.get(row.label()), x, top, matrix, buffer);
            draw(font, whole, point - font.width(whole), top, matrix, buffer);
            draw(font, fraction(row.amount()), point, top, matrix, buffer);
            draw(font, I18n.get(row.unit()), point + columns.fraction() + columns.space(), top, matrix, buffer);
            top += LINE;
        }
    }

    private Columns columns(Font font) {
        int label = 0;
        int whole = 0;
        int fraction = 0;
        int unit = 0;
        for (Tooltips.Row row : readings.rows()) {
            label = Math.max(label, font.width(I18n.get(row.label())));
            whole = Math.max(whole, font.width(whole(row.amount())));
            fraction = Math.max(fraction, font.width(fraction(row.amount())));
            unit = Math.max(unit, font.width(I18n.get(row.unit())));
        }
        return new Columns(label, whole, fraction, font.width(" "), unit);
    }

    private static String whole(String amount) {
        int at = amount.indexOf(POINT);
        return at < 0 ? amount : amount.substring(0, at);
    }

    private static String fraction(String amount) {
        int at = amount.indexOf(POINT);
        return at < 0 ? "" : amount.substring(at);
    }

    private static void draw(Font font, String text, int x, int y, Matrix4f matrix,
            MultiBufferSource.BufferSource buffer) {
        font.drawInBatch(text, x, y, COLOUR, true, matrix, buffer, Font.DisplayMode.NORMAL, 0,
                LightTexture.FULL_BRIGHT);
    }
}
